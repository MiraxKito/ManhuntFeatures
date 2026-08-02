package ru.kitoro.mhfeatures.compass;

import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.enchantments.Enchantment;
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
        meta.setDisplayName(plugin.color(plugin.messageRaw("compass-name")));
        if (plugin.getConfig().getBoolean("compass.glow", true)) {
            Enchantment glow = Enchantment.getByName("DURABILITY");
            if (glow == null) glow = Enchantment.getByName("UNBREAKING");
            if (glow != null) {
                meta.addEnchant(glow, 1, true);
                meta.addItemFlags(org.bukkit.inventory.ItemFlag.HIDE_ENCHANTS);
            }
        }
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
