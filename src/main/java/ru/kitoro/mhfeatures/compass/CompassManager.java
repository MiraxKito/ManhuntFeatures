package ru.kitoro.mhfeatures.compass;

import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import ru.kitoro.mhfeatures.MHFeaturesPlugin;
import ru.kitoro.mhfeatures.role.Role;

public final class CompassManager {
    private final MHFeaturesPlugin plugin;
    private final ManhuntCompass compass;

    public CompassManager(MHFeaturesPlugin plugin) {
        this.plugin = plugin;
        this.compass = new ManhuntCompass(plugin);
    }

    public ManhuntCompass item() { return compass; }

    public void ensure(Player player) {
        if (plugin.getRoleManager().get(player.getUniqueId()) != Role.HUNTER) return;
        if (contains(player)) return;
        ItemStack item = compass.create();
        int free = player.getInventory().firstEmpty();
        if (free >= 0) {
            player.getInventory().setItem(free, item);
        } else {
            ItemStack displaced = player.getInventory().getItem(0);
            if (displaced != null && displaced.getType().isItem()) {
                player.getWorld().dropItemNaturally(player.getLocation(), displaced);
            }
            player.getInventory().setItem(0, item);
        }
        player.updateInventory();
    }

    public boolean contains(Player player) {
        for (ItemStack item : player.getInventory().getContents()) {
            if (compass.is(item)) return true;
        }
        return false;
    }

    public void removeAll(Player player) {
        for (int slot = 0; slot < player.getInventory().getSize(); slot++) {
            if (compass.is(player.getInventory().getItem(slot))) player.getInventory().setItem(slot, null);
        }
    }
}
