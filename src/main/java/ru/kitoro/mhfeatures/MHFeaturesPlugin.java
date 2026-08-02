package ru.kitoro.mhfeatures;

import org.bukkit.ChatColor;
import org.bukkit.command.PluginCommand;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import ru.kitoro.mhfeatures.command.MHCommand;
import ru.kitoro.mhfeatures.config.ConfigMenu;
import ru.kitoro.mhfeatures.compass.CompassListener;
import ru.kitoro.mhfeatures.compass.CompassManager;
import ru.kitoro.mhfeatures.compass.CompassTracker;
import ru.kitoro.mhfeatures.game.GameManager;
import ru.kitoro.mhfeatures.game.GameListener;
import ru.kitoro.mhfeatures.listener.PlayerListener;
import ru.kitoro.mhfeatures.role.RoleManager;
import ru.kitoro.mhfeatures.tab.TabManager;
import ru.kitoro.mhfeatures.world.ManhuntWorldManager;
import ru.kitoro.mhfeatures.world.ResetManager;

public final class MHFeaturesPlugin extends JavaPlugin {
    private RoleManager roleManager;
    private ManhuntWorldManager worldManager;
    private ResetManager resetManager;
    private CompassTracker compassTracker;
    private CompassManager compassManager;
    private Long startupSeed;
    private TabManager tabManager;
    private GameManager gameManager;
    private FileConfiguration messages;
    private ConfigMenu configMenu;

    @Override
    public void onLoad() {
        saveDefaultConfig();
        loadMessages();
        worldManager = new ManhuntWorldManager(this);
        resetManager = new ResetManager(this, worldManager);
        resetManager.preparePendingReset();
    }

    @Override
    public void onEnable() {
        if (worldManager == null) worldManager = new ManhuntWorldManager(this);
        if (resetManager == null) resetManager = new ResetManager(this, worldManager);
        roleManager = new RoleManager(this);
        if (resetManager.isRecoveryActive()) roleManager.clearAll();
        compassTracker = new CompassTracker(this);
        compassManager = new CompassManager(this);
        tabManager = new TabManager(this);
        gameManager = new GameManager(this);
        configMenu = new ConfigMenu(this);
        if (startupSeed != null) {
            worldManager.createAll(startupSeed);
            startupSeed = null;
        } else {
            worldManager.ensureWorlds();
            gameManager.restore();
        }

        MHCommand command = new MHCommand(this);
        PluginCommand pluginCommand = getCommand("mhfeatures");
        if (pluginCommand != null) {
            pluginCommand.setExecutor(command);
            pluginCommand.setTabCompleter(command);
        }
        getServer().getPluginManager().registerEvents(new CompassListener(this), this);
        getServer().getPluginManager().registerEvents(new PlayerListener(this), this);
        getServer().getPluginManager().registerEvents(new GameListener(this), this);
        getServer().getPluginManager().registerEvents(configMenu, this);
        tabManager.updateAll();
        getLogger().info("MHFeatures enabled for Bukkit/Spigot/Paper 1.20.1");
    }

    public RoleManager getRoleManager() { return roleManager; }
    public ManhuntWorldManager getWorldManager() { return worldManager; }
    public ResetManager getResetManager() { return resetManager; }
    public CompassTracker getCompassTracker() { return compassTracker; }
    public CompassManager getCompassManager() { return compassManager; }
    public void setStartupSeed(Long seed) { this.startupSeed = seed; }
    public TabManager getTabManager() { return tabManager; }
    public GameManager getGameManager() { return gameManager; }
    public ConfigMenu getConfigMenu() { return configMenu; }

    public void loadMessages() {
        String language = getConfig().getString("language", "en").toLowerCase();
        if (!language.equals("ru")) language = "en";
        String resource = "messages-" + language + ".yml";
        saveResource(resource, false);
        messages = YamlConfiguration.loadConfiguration(new File(getDataFolder(), resource));
    }

    public String message(String key) {
        return messageValue(messages.getString(key, key));
    }

    public String messageValue(String value) {
        return color(messageRaw("prefix") + value);
    }

    public String messageRaw(String key) {
        return messages.getString(key, key);
    }

    public String message(String key, String placeholder, Object value) {
        return messageValue(messages.getString(key, key).replace(placeholder, String.valueOf(value)));
    }

    public String color(String value) { return ChatColor.translateAlternateColorCodes('&', value); }

    public void broadcast(String message) {
        getServer().broadcastMessage(messageValue(message));
    }
}
