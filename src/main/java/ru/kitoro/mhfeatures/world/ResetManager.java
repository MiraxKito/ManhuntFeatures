package ru.kitoro.mhfeatures.world;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import ru.kitoro.mhfeatures.MHFeaturesPlugin;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;

public final class ResetManager {
    private final MHFeaturesPlugin plugin;
    private final ManhuntWorldManager worlds;
    private boolean resetting;
    private long recoveryUntil;
    private final Path pendingFile;

    public ResetManager(MHFeaturesPlugin plugin, ManhuntWorldManager worlds) {
        this.plugin = plugin;
        this.worlds = worlds;
        this.pendingFile = plugin.getDataFolder().toPath().resolve("reset.pending");
    }

    public boolean isResetting() { return resetting; }

    public void preparePendingReset() {
        if (!Files.exists(pendingFile)) return;
        try {
            String value = new String(Files.readAllBytes(pendingFile), StandardCharsets.UTF_8).trim();
            Long seed = Long.parseLong(value);
            plugin.getLogger().info(plugin.messageRaw("reset-pending-log"));
            worlds.deleteForPendingReset();
            Files.deleteIfExists(pendingFile);
            Files.deleteIfExists(plugin.getDataFolder().toPath().resolve("game.state"));
            long recoverySeconds = Math.max(10L, plugin.getConfig().getLong("reset.recovery-seconds", 60L));
            recoveryUntil = System.currentTimeMillis() + recoverySeconds * 1000L;
            plugin.setStartupSeed(seed);
        } catch (Exception exception) {
            plugin.getLogger().severe("Could not complete pending reset: " + exception.getMessage());
        }
    }

    public boolean isRecoveryActive() {
        return System.currentTimeMillis() < recoveryUntil;
    }

    public void recoverPlayer(Player player) {
        if (!isRecoveryActive()) return;
        worlds.teleportToOriginalSpawn(player);
        plugin.getCompassManager().removeAll(player);
        clearInventory(player);
    }

    public Long readPendingSeed() {
        if (!Files.exists(pendingFile)) return null;
        try { return Long.parseLong(new String(Files.readAllBytes(pendingFile), StandardCharsets.UTF_8).trim()); }
        catch (Exception ignored) { return null; }
    }

    public void reset(Long suppliedSeed) {
        if (resetting) return;
        resetting = true;
        long seed = worlds.chooseSeed(suppliedSeed);
        plugin.broadcast(plugin.messageRaw("reset-started"));
        try {
            Files.createDirectories(plugin.getDataFolder().toPath());
            Files.write(pendingFile, Long.toString(seed).getBytes(StandardCharsets.UTF_8));
            for (Player player : Bukkit.getOnlinePlayers()) {
                if (worlds.isManhuntWorld(player.getWorld())) worlds.teleportToOriginalSpawn(player);
                plugin.getCompassManager().removeAll(player);
                clearInventory(player);
            }
            plugin.broadcast(plugin.messageRaw("reset-saved"));
            resetting = false;
        } catch (IOException exception) {
            resetting = false;
            plugin.getLogger().severe("Could not schedule reset: " + exception.getMessage());
            plugin.broadcast(plugin.messageRaw("reset-failed"));
        }
    }

    private void clearInventory(Player player) {
        if (plugin.getConfig().getBoolean("reset.clear-inventory", true)) player.getInventory().clear();
        if (plugin.getConfig().getBoolean("reset.clear-armor", true)) player.getInventory().setArmorContents(null);
        if (plugin.getConfig().getBoolean("reset.clear-offhand", true)) player.getInventory().setItemInOffHand(null);
        if (plugin.getConfig().getBoolean("manhunt.clear-ender-chest-on-reset", false)) player.getEnderChest().clear();
        player.updateInventory();
    }
}
