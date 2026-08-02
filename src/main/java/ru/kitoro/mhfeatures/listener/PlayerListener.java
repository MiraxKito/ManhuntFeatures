package ru.kitoro.mhfeatures.listener;

import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerRespawnEvent;
import org.bukkit.event.player.PlayerChangedWorldEvent;
import ru.kitoro.mhfeatures.MHFeaturesPlugin;

public final class PlayerListener implements Listener {
    private final MHFeaturesPlugin plugin;

    public PlayerListener(MHFeaturesPlugin plugin) { this.plugin = plugin; }

    @EventHandler
    public void onJoin(PlayerJoinEvent event) {
        plugin.getResetManager().recoverPlayer(event.getPlayer());
        plugin.getGameManager().recoverPlayer(event.getPlayer());
        plugin.getGameManager().applyScoreboard(event.getPlayer());
        plugin.getTabManager().update(event.getPlayer());
        ensureLater(event.getPlayer());
    }

    @EventHandler
    public void onDeath(PlayerDeathEvent event) {
        event.getDrops().removeIf(plugin.getCompassManager().item()::is);
        plugin.getCompassManager().removeAll(event.getEntity());
    }

    @EventHandler
    public void onRespawn(PlayerRespawnEvent event) {
        ensureLater(event.getPlayer());
    }

    @EventHandler
    public void onWorldChange(PlayerChangedWorldEvent event) {
        ensureLater(event.getPlayer());
    }

    private void ensureLater(org.bukkit.entity.Player player) {
        plugin.getServer().getScheduler().runTask(plugin, () -> plugin.getCompassManager().ensure(player));
    }
}
