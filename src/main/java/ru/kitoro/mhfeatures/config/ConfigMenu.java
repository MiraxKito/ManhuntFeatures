package ru.kitoro.mhfeatures.config;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryDragEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import ru.kitoro.mhfeatures.MHFeaturesPlugin;

import java.util.Collections;

public final class ConfigMenu implements Listener {
    private final MHFeaturesPlugin plugin;

    public ConfigMenu(MHFeaturesPlugin plugin) { this.plugin = plugin; }

    public void open(Player player) {
        MenuHolder holder = new MenuHolder();
        Inventory inventory = Bukkit.createInventory(holder, 27, plugin.color(plugin.messageRaw("config-title")));
        holder.setInventory(inventory);
        set(inventory, 10, Material.COMPASS, "config-compass-glow", "compass.glow");
        set(inventory, 11, Material.ENDER_EYE, "config-compass-hunter-only", "compass.hunter-only");
        set(inventory, 12, Material.SKELETON_SKULL, "config-eliminated-spectator", "game.eliminated-runner-spectator");
        set(inventory, 14, Material.CHEST, "config-reset-inventory", "reset.clear-inventory");
        set(inventory, 15, Material.NAME_TAG, "config-tab-enabled", "tab.enabled");
        set(inventory, 16, Material.PAPER, "config-language", "language");
        player.openInventory(inventory);
    }

    @EventHandler
    public void onClick(InventoryClickEvent event) {
        if (!(event.getInventory().getHolder() instanceof MenuHolder)) return;
        event.setCancelled(true);
        if (!(event.getWhoClicked() instanceof Player)) return;
        Player player = (Player) event.getWhoClicked();
        if (plugin.getResetManager().isResetting() || plugin.getGameManager().isActive()) {
            player.closeInventory();
            player.sendMessage(plugin.message("config-locked"));
            return;
        }
        String path = pathForSlot(event.getRawSlot());
        if (path == null) return;
        if (path.equals("language")) {
            String current = plugin.getConfig().getString("language", "en");
            plugin.getConfig().set("language", current.equalsIgnoreCase("ru") ? "en" : "ru");
            plugin.saveConfig();
            plugin.loadMessages();
        } else {
            plugin.getConfig().set(path, !plugin.getConfig().getBoolean(path, true));
            plugin.saveConfig();
        }
        player.sendMessage(plugin.message("config-saved"));
        open(player);
    }

    @EventHandler
    public void onDrag(InventoryDragEvent event) {
        if (event.getInventory().getHolder() instanceof MenuHolder) event.setCancelled(true);
    }

    private String pathForSlot(int slot) {
        switch (slot) {
            case 10: return "compass.glow";
            case 11: return "compass.hunter-only";
            case 12: return "game.eliminated-runner-spectator";
            case 14: return "reset.clear-inventory";
            case 15: return "tab.enabled";
            case 16: return "language";
            default: return null;
        }
    }

    private void set(Inventory inventory, int slot, Material material, String key, String path) {
        ItemStack item = new ItemStack(material);
        ItemMeta meta = item.getItemMeta();
        meta.setDisplayName(plugin.color(plugin.messageRaw(key)));
        String value;
        if (path.equals("language")) value = plugin.getConfig().getString(path, "en").toUpperCase();
        else value = plugin.getConfig().getBoolean(path, true) ? plugin.messageRaw("config-on") : plugin.messageRaw("config-off");
        meta.setLore(Collections.singletonList(plugin.color(plugin.messageRaw("config-value") + value)));
        item.setItemMeta(meta);
        inventory.setItem(slot, item);
    }

    private static final class MenuHolder implements InventoryHolder {
        private Inventory inventory;

        @Override
        public Inventory getInventory() { return inventory; }

        private void setInventory(Inventory inventory) { this.inventory = inventory; }
    }
}
