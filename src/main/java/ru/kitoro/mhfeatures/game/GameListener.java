package ru.kitoro.mhfeatures.game;

import org.bukkit.entity.EnderDragon;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.event.entity.PlayerDeathEvent;
import ru.kitoro.mhfeatures.MHFeaturesPlugin;

public final class GameListener implements Listener {
    private final MHFeaturesPlugin plugin;

    public GameListener(MHFeaturesPlugin plugin) { this.plugin = plugin; }

    @EventHandler
    public void onPlayerDeath(PlayerDeathEvent event) {
        plugin.getGameManager().handleRunnerDeath(event.getEntity());
    }

    @EventHandler
    public void onDragonDeath(EntityDeathEvent event) {
        if (!(event.getEntity() instanceof EnderDragon)
                || !event.getEntity().getWorld().getName().equals(plugin.getWorldManager().endName())) return;
        for (org.bukkit.entity.Player player : org.bukkit.Bukkit.getOnlinePlayers()) {
            plugin.getGameManager().handleRunnerWin(player);
        }
    }
}
