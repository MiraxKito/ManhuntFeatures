package ru.kitoro.mhfeatures.tab;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import ru.kitoro.mhfeatures.MHFeaturesPlugin;
import ru.kitoro.mhfeatures.role.Role;

public final class TabManager {
    private final MHFeaturesPlugin plugin;

    public TabManager(MHFeaturesPlugin plugin) {
        this.plugin = plugin;
    }

    public void update(Player player) {
        if (!plugin.getConfig().getBoolean("tab.enabled", true)) {
            player.setPlayerListName(player.getName());
            return;
        }
        Role role = plugin.getRoleManager().get(player.getUniqueId());
        String prefix;
        switch (role) {
            case HUNTER:
                prefix = plugin.getConfig().getString("tab.hunter-prefix", "&c[HUNTER] &f");
                break;
            case RUNNER:
                prefix = plugin.getConfig().getString("tab.runner-prefix", "&a[RUNNER] &f");
                break;
            default:
                prefix = plugin.getConfig().getString("tab.none-prefix", "&7");
                break;
        }
        player.setPlayerListName(plugin.color(prefix) + player.getName());
    }

    public void updateAll() {
        for (Player player : Bukkit.getOnlinePlayers()) update(player);
    }
}
