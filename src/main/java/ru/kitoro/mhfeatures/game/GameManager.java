package ru.kitoro.mhfeatures.game;

import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.bukkit.scoreboard.Objective;
import org.bukkit.scoreboard.Scoreboard;
import org.bukkit.scoreboard.ScoreboardManager;
import ru.kitoro.mhfeatures.MHFeaturesPlugin;
import ru.kitoro.mhfeatures.role.Role;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

public final class GameManager {
    private final MHFeaturesPlugin plugin;
    private boolean active;
    private long startedAt;
    private Scoreboard scoreboard;
    private Objective timerObjective;
    private String timerEntry;
    private int taskId = -1;
    private final Set<UUID> eliminatedRunners = new HashSet<UUID>();
    private final Path stateFile;

    public GameManager(MHFeaturesPlugin plugin) {
        this.plugin = plugin;
        this.stateFile = plugin.getDataFolder().toPath().resolve("game.state");
    }

    public void restore() {
        if (plugin.getResetManager().isRecoveryActive() || !Files.exists(stateFile)) return;
        try {
            startedAt = Long.parseLong(new String(Files.readAllBytes(stateFile), StandardCharsets.UTF_8).trim());
            active = true;
            createTimer();
        } catch (Exception exception) {
            plugin.getLogger().warning("Could not restore Manhunt game state: " + exception.getMessage());
            clearState();
        }
    }

    public boolean isActive() { return active; }

    public long elapsedSeconds() {
        if (!active) return 0L;
        return Math.max(0L, (System.currentTimeMillis() - startedAt) / 1000L);
    }

    public boolean start() {
        if (active) return false;
        if (plugin.getRoleManager().count(Role.HUNTER) < 1 || plugin.getRoleManager().count(Role.RUNNER) < 1) return false;
        World overworld = plugin.getWorldManager().getOverworld();
        if (overworld == null || Bukkit.getWorld(plugin.getWorldManager().netherName()) == null
                || Bukkit.getWorld(plugin.getWorldManager().endName()) == null) return false;

        active = true;
        startedAt = System.currentTimeMillis();
        eliminatedRunners.clear();
        saveState();
        preparePlayers();
        createTimer();
        plugin.broadcast(plugin.messageRaw("game-started"));
        return true;
    }

    public boolean stop(String reason) {
        if (!active) return false;
        long duration = elapsedSeconds();
        active = false;
        cancelTimer();
        removeTimer();
        clearState();
        for (Player player : Bukkit.getOnlinePlayers()) {
            plugin.getCompassManager().removeAll(player);
            clearPlayer(player);
            Location spawn = Bukkit.getWorld(plugin.getWorldManager().originalName()) == null
                    ? null : Bukkit.getWorld(plugin.getWorldManager().originalName()).getSpawnLocation();
            if (spawn != null) player.teleport(spawn);
        }
        plugin.getRoleManager().clearAll();
        plugin.getTabManager().updateAll();
        String message = reason == null
                ? plugin.messageRaw("game-stopped")
                : reason;
        plugin.broadcast(message + " " + formatDuration(duration));
        return true;
    }

    public void handleRunnerDeath(Player runner) {
        if (!active || plugin.getRoleManager().get(runner.getUniqueId()) != Role.RUNNER
                || eliminatedRunners.contains(runner.getUniqueId())) return;
        eliminatedRunners.add(runner.getUniqueId());
        Bukkit.getScheduler().runTask(plugin, () -> {
            if (!active) return;
            if (plugin.getRoleManager().count(Role.RUNNER) <= 1) {
                plugin.getRoleManager().set(runner.getUniqueId(), Role.NONE);
                stop(plugin.messageRaw("hunters-won"));
                return;
            }
            plugin.getRoleManager().set(runner.getUniqueId(), Role.NONE);
            plugin.getTabManager().update(runner);
            if (plugin.getConfig().getBoolean("game.eliminated-runner-spectator", true)) {
                runner.setGameMode(GameMode.SPECTATOR);
            } else {
                plugin.getWorldManager().teleportToOriginalSpawn(runner);
            }
            plugin.broadcast(plugin.messageRaw("runner-eliminated"));
        });
    }

    public void handleRunnerWin(Player player) {
        if (!active || plugin.getRoleManager().get(player.getUniqueId()) != Role.RUNNER) return;
        stop(plugin.messageRaw("runners-won"));
    }

    public void recoverPlayer(Player player) {
        if (!active || plugin.getResetManager().isRecoveryActive()) return;
        if (!plugin.getWorldManager().isManhuntWorld(player.getWorld())) return;
        Role role = plugin.getRoleManager().get(player.getUniqueId());
        if (role == Role.HUNTER) {
            player.teleport(plugin.getWorldManager().getOverworld().getSpawnLocation());
            player.setGameMode(GameMode.SURVIVAL);
            plugin.getCompassManager().ensure(player);
        } else if (role == Role.RUNNER) {
            player.setGameMode(GameMode.SURVIVAL);
        }
    }

    public void applyScoreboard(Player player) {
        if (active && scoreboard != null) player.setScoreboard(scoreboard);
    }

    private void preparePlayers() {
        World overworld = plugin.getWorldManager().getOverworld();
        for (Player player : Bukkit.getOnlinePlayers()) {
            player.setGameMode(GameMode.SURVIVAL);
            if (plugin.getRoleManager().get(player.getUniqueId()) == Role.HUNTER) {
                player.teleport(overworld.getSpawnLocation());
                player.setBedSpawnLocation(overworld.getSpawnLocation(), true);
                player.setCompassTarget(overworld.getSpawnLocation());
                plugin.getCompassManager().ensure(player);
            }
        }
    }

    private void createTimer() {
        ScoreboardManager manager = Bukkit.getScoreboardManager();
        if (manager == null) return;
        scoreboard = manager.getNewScoreboard();
        timerObjective = scoreboard.registerNewObjective("mh_timer", "dummy", plugin.color(
                plugin.messageRaw("timer-title")));
        taskId = Bukkit.getScheduler().scheduleSyncRepeatingTask(plugin, () -> {
            if (timerObjective == null) return;
            if (timerEntry != null) timerObjective.getScoreboard().resetScores(timerEntry);
            timerEntry = formatDuration(elapsedSeconds());
            timerObjective.getScore(timerEntry).setScore(0);
        }, 0L, 20L);
        for (Player player : Bukkit.getOnlinePlayers()) player.setScoreboard(scoreboard);
    }

    private void removeTimer() {
        for (Player player : Bukkit.getOnlinePlayers()) {
            ScoreboardManager manager = Bukkit.getScoreboardManager();
            if (manager != null) player.setScoreboard(manager.getMainScoreboard());
        }
        scoreboard = null;
        timerObjective = null;
        timerEntry = null;
    }

    private void cancelTimer() {
        if (taskId != -1) Bukkit.getScheduler().cancelTask(taskId);
        taskId = -1;
    }

    private void saveState() {
        try {
            Files.createDirectories(stateFile.getParent());
            Files.write(stateFile, Long.toString(startedAt).getBytes(StandardCharsets.UTF_8));
        } catch (IOException exception) {
            plugin.getLogger().warning("Could not save Manhunt game state: " + exception.getMessage());
        }
    }

    private void clearState() {
        try { Files.deleteIfExists(stateFile); }
        catch (IOException exception) { plugin.getLogger().warning("Could not clear Manhunt game state: " + exception.getMessage()); }
    }

    private void clearPlayer(Player player) {
        player.getInventory().clear();
        player.getInventory().setArmorContents(null);
        player.getInventory().setItemInOffHand(null);
        player.setGameMode(GameMode.SURVIVAL);
        player.updateInventory();
    }

    private String formatDuration(long seconds) {
        return String.format("%02d:%02d:%02d", seconds / 3600L, (seconds / 60L) % 60L, seconds % 60L);
    }
}
