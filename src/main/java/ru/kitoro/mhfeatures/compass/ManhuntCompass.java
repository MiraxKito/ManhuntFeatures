package ru.kitoro.mhfeatures.compass;

import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataType;
import ru.kitoro.mhfeatures.MHFeaturesPlugin;

public final class ManhuntCompass {
    private final MHFeaturesPlugin plugin;
    private final NamespacedKey key;

    public ManhuntCompass(MHFeaturesPlugin plugin) {
        this.plugin = plugin;
        key = new NamespacedKey(plugin, "manhunt_compass");
    }

    public ItemStack create() {
        ItemStack item = new ItemStack(Material.COMPASS);
        ItemMeta meta = item.getItemMeta();
        meta.setDisplayName(plugin.color(plugin.getConfig().getString("compass.name", "&6Manhunt Compass")));
        meta.getPersistentDataContainer().set(key, PersistentDataType.BYTE, (byte) 1);
        item.setItemMeta(meta);
        return item;
    }

    public boolean is(ItemStack item) {
        if (item == null || item.getType() != Material.COMPASS || !item.hasItemMeta()) return false;
        Byte marker = item.getItemMeta().getPersistentDataContainer().get(key, PersistentDataType.BYTE);
        return marker != null && marker == 1;
    }
}
