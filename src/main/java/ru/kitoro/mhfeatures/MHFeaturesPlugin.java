package ru.kitoro.mhfeatures;

import org.bukkit.ChatColor;
import org.bukkit.command.PluginCommand;
import org.bukkit.plugin.java.JavaPlugin;
import ru.kitoro.mhfeatures.command.MHCommand;
import ru.kitoro.mhfeatures.compass.CompassListener;
import ru.kitoro.mhfeatures.compass.CompassManager;
import ru.kitoro.mhfeatures.compass.CompassTracker;
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

    @Override
    public void onLoad() {
        saveDefaultConfig();
        worldManager = new ManhuntWorldManager(this);
        resetManager = new ResetManager(this, worldManager);
        resetManager.preparePendingReset();
    }

    @Override
    public void onEnable() {
        if (worldManager == null) worldManager = new ManhuntWorldManager(this);
        if (resetManager == null) resetManager = new ResetManager(this, worldManager);
        roleManager = new RoleManager(this);
        compassTracker = new CompassTracker(this);
        compassManager = new CompassManager(this);
        tabManager = new TabManager(this);
        if (startupSeed != null) {
            worldManager.createAll(startupSeed);
            startupSeed = null;
        } else {
            worldManager.ensureWorlds();
        }

        MHCommand command = new MHCommand(this);
        PluginCommand pluginCommand = getCommand("mhfeatures");
        if (pluginCommand != null) {
            pluginCommand.setExecutor(command);
            pluginCommand.setTabCompleter(command);
        }
        getServer().getPluginManager().registerEvents(new CompassListener(this), this);
        getServer().getPluginManager().registerEvents(new PlayerListener(this), this);
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

    public String message(String path) {
        String prefix = getConfig().getString("messages.prefix", "[Manhunt] ");
        String value = getConfig().getString(path, path);
        return color(prefix + value);
    }

    public String color(String value) { return ChatColor.translateAlternateColorCodes('&', value); }

    public void broadcast(String message) {
        getServer().broadcastMessage(color(getConfig().getString("messages.prefix", "[Manhunt] ") + message));
    }
}
