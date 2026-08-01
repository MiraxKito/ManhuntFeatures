package ru.kitoro.mhfeatures.compass;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import ru.kitoro.mhfeatures.MHFeaturesPlugin;
import ru.kitoro.mhfeatures.role.Role;

public final class CompassTracker {
    private final MHFeaturesPlugin plugin;

    public CompassTracker(MHFeaturesPlugin plugin) { this.plugin = plugin; }

    public void activate(Player hunter) {
        Player nearest = null;
        double nearestDistance = Double.MAX_VALUE;
        for (Player candidate : Bukkit.getOnlinePlayers()) {
            if (candidate.equals(hunter) || candidate.getWorld() != hunter.getWorld()) continue;
            if (plugin.getRoleManager().get(candidate.getUniqueId()) != Role.RUNNER) continue;
            if (plugin.getConfig().getBoolean("compass.ignore-vanished", true)
                    && !hunter.canSee(candidate)) continue;
            double distance = hunter.getLocation().distanceSquared(candidate.getLocation());
            if (distance < nearestDistance) {
                nearest = candidate;
                nearestDistance = distance;
            }
        }
        if (nearest == null) {
            hunter.setCompassTarget(hunter.getWorld().getSpawnLocation());
            hunter.sendMessage(plugin.message("compass.message-no-target"));
            return;
        }
        Location target = nearest.getLocation().clone();
        hunter.setCompassTarget(target);
    }
}
