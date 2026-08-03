package ru.kitoro.mhfeatures.compass;

import org.bukkit.Material;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;
import ru.kitoro.mhfeatures.MHFeaturesPlugin;
import ru.kitoro.mhfeatures.role.Role;

public final class CompassListener implements Listener {
    private final MHFeaturesPlugin plugin;

    public CompassListener(MHFeaturesPlugin plugin) { this.plugin = plugin; }

    @EventHandler
    public void onInteract(PlayerInteractEvent event) {
        ItemStack item = event.getItem();
        if (item == null || item.getType() != Material.COMPASS) return;
        if (!plugin.getCompassManager().item().is(item)) return;
        if (event.getAction() != org.bukkit.event.block.Action.RIGHT_CLICK_AIR
                && event.getAction() != org.bukkit.event.block.Action.RIGHT_CLICK_BLOCK) return;
        if (!event.getPlayer().hasPermission("mhfeatures.use.compass")) return;
        if (plugin.getResetManager().isResetPending()) return;
        if (plugin.getConfig().getBoolean("compass.hunter-only", true)
                && plugin.getRoleManager().get(event.getPlayer().getUniqueId()) != Role.HUNTER) return;

        event.setCancelled(true);
        plugin.getCompassTracker().activate(event.getPlayer());
    }
}
