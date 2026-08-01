package ru.kitoro.mhfeatures.role;

import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import ru.kitoro.mhfeatures.MHFeaturesPlugin;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public final class RoleManager {
    private final MHFeaturesPlugin plugin;
    private final Map<UUID, Role> roles = new HashMap<>();
    private File file;
    private FileConfiguration data;

    public RoleManager(MHFeaturesPlugin plugin) {
        this.plugin = plugin;
        load();
    }

    public void load() {
        file = new File(plugin.getDataFolder(), "roles.yml");
        if (!file.exists()) {
            try {
                if (!file.getParentFile().exists() && !file.getParentFile().mkdirs()) {
                    plugin.getLogger().warning("Could not create plugin data directory");
                }
                if (!file.createNewFile()) {
                    plugin.getLogger().warning("Could not create roles.yml");
                }
            } catch (IOException exception) {
                plugin.getLogger().severe("Could not create roles.yml: " + exception.getMessage());
            }
        }
        data = YamlConfiguration.loadConfiguration(file);
        roles.clear();
        ConfigurationSection section = data.getConfigurationSection("roles");
        if (section == null) return;
        for (String key : section.getKeys(false)) {
            try {
                Role role = Role.parse(section.getString(key, "none"));
                if (role != null && role != Role.NONE) roles.put(UUID.fromString(key), role);
            } catch (IllegalArgumentException ignored) {
                plugin.getLogger().warning("Invalid UUID in roles.yml: " + key);
            }
        }
    }

    public Role get(UUID uuid) {
        return roles.getOrDefault(uuid, Role.NONE);
    }

    public void set(UUID uuid, Role role) {
        if (role == Role.NONE) roles.remove(uuid);
        else roles.put(uuid, role);
        data.set("roles." + uuid, role == Role.NONE ? null : role.name().toLowerCase());
        save();
    }

    public long count(Role role) {
        return roles.values().stream().filter(value -> value == role).count();
    }

    private void save() {
        try {
            data.save(file);
        } catch (IOException exception) {
            plugin.getLogger().severe("Could not save roles.yml: " + exception.getMessage());
        }
    }
}
