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
            plugin.getLogger().info("Pending reset detected. Removing old Manhunt worlds before world loading.");
            worlds.deleteForPendingReset();
            Files.deleteIfExists(pendingFile);
            plugin.setStartupSeed(seed);
        } catch (Exception exception) {
            plugin.getLogger().severe("Could not complete pending reset: " + exception.getMessage());
        }
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
        plugin.broadcast("Начат сброс мэнхант миров. Игроки будут перенесены, затем сервер перезапустится для безопасной генерации.");
        try {
            Files.createDirectories(plugin.getDataFolder().toPath());
            Files.write(pendingFile, Long.toString(seed).getBytes(StandardCharsets.UTF_8));
            for (Player player : Bukkit.getOnlinePlayers()) {
                if (worlds.isManhuntWorld(player.getWorld())) worlds.teleportToOriginalSpawn(player);
                plugin.getCompassManager().removeAll(player);
                clearInventory(player);
            }
            plugin.broadcast("Заявка на сброс сохранена. Выполните обычный restart сервера, чтобы удалить и заново сгенерировать миры без зависания сервера.");
            resetting = false;
        } catch (IOException exception) {
            resetting = false;
            plugin.getLogger().severe("Could not schedule reset: " + exception.getMessage());
            plugin.broadcast("Сброс не запланирован. Подробности находятся в консоли сервера.");
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
