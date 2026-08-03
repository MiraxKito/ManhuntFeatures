package ru.kitoro.mhfeatures.world;

import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.WorldCreator;
import org.bukkit.WorldType;
import org.bukkit.entity.Player;
import ru.kitoro.mhfeatures.MHFeaturesPlugin;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.ThreadLocalRandom;

public final class ManhuntWorldManager {
    private static final String MARKER = ".mhfeatures-owned";
    private final MHFeaturesPlugin plugin;

    public ManhuntWorldManager(MHFeaturesPlugin plugin) {
        this.plugin = plugin;
    }

    public String originalName() { return plugin.getConfig().getString("original-world", "world"); }
    public String overworldName() { return plugin.getConfig().getString("manhunt.overworld", "world_mh"); }
    public String netherName() { return plugin.getConfig().getString("manhunt.nether", "world_mh_nether"); }
    public String endName() { return plugin.getConfig().getString("manhunt.end", "world_mh_the_end"); }
    public boolean isManhuntWorld(World world) { return world != null && isManhuntWorld(world.getName()); }
    public boolean isManhuntWorld(String name) {
        return worldNames().contains(name);
    }

    private Set<String> worldNames() {
        Set<String> names = new HashSet<String>();
        names.add(overworldName());
        names.add(netherName());
        names.add(endName());
        return names;
    }

    public void ensureWorlds() {
        if (originalName().equals(overworldName())
                || originalName().equals(netherName())
                || originalName().equals(endName())
                || worldNames().size() != 3) {
            plugin.getLogger().severe("Manhunt world names must be unique and must not equal original-world.");
            return;
        }

        ensureWorld(overworldName(), World.Environment.NORMAL);
        ensureWorld(netherName(), World.Environment.NETHER);
        ensureWorld(endName(), World.Environment.THE_END);

        if (Bukkit.getWorld(overworldName()) == null || Bukkit.getWorld(netherName()) == null || Bukkit.getWorld(endName()) == null) {
            plugin.getLogger().warning("Manhunt worlds are not fully initialized. Use /mhfeatures reset.");
        }
    }

    private void ensureWorld(String name, World.Environment environment) {
        if (Bukkit.getWorld(name) != null) return;
        Path folder = Bukkit.getWorldContainer().toPath().resolve(name);
        if (!Files.isDirectory(folder)) return;

        plugin.getLogger().info("Loading existing Manhunt world: " + name);
        WorldCreator creator = new WorldCreator(name)
                .environment(environment)
                .type(WorldType.NORMAL)
                .generateStructures(true);
        World world = Bukkit.createWorld(creator);
        if (world != null) markOwned(world);
    }

    /**
     * Marks an existing configured world as plugin-owned during first initialization.
     * This is intentionally limited to exact configured names; arbitrary folders are never adopted.
     */
    public void adoptExistingWorld(String name) {
        if (!isManhuntWorld(name) || name.equals(originalName())) return;
        World world = Bukkit.getWorld(name);
        Path folder = Bukkit.getWorldContainer().toPath().resolve(name);
        if (world != null && !isOwned(world)) {
            plugin.getLogger().warning("Adopting existing configured manhunt world: " + name);
            markOwned(world);
        } else if (world == null && Files.isDirectory(folder)) {
            plugin.getLogger().warning("Adopting existing configured manhunt world folder: " + name);
            try {
                Files.createFile(folder.resolve(MARKER));
            } catch (IOException exception) {
                throw new IllegalStateException("Could not mark existing world folder: " + name, exception);
            }
        }
    }

    public World getOverworld() { return Bukkit.getWorld(overworldName()); }

    public void deleteForPendingReset() {
        for (String name : new String[]{overworldName(), netherName(), endName()}) {
            World world = Bukkit.getWorld(name);
            if (world != null) Bukkit.unloadWorld(world, false);
            Path folder = Bukkit.getWorldContainer().toPath().resolve(name);
            if (!Files.exists(folder)) continue;
            if (!Files.exists(folder.resolve(MARKER))) {
                try { Files.createFile(folder.resolve(MARKER)); }
                catch (IOException exception) { throw new IllegalStateException("Could not mark world: " + name, exception); }
            }
            if (!deleteOwnedWorld(name)) throw new IllegalStateException("Could not delete world: " + name);
        }
    }

    public void createAll(long seed) {
        if (createWorld(overworldName(), World.Environment.NORMAL, seed) == null
                || createWorld(netherName(), World.Environment.NETHER, seed) == null
                || createWorld(endName(), World.Environment.THE_END, seed) == null) {
            throw new IllegalStateException("Could not generate all manhunt worlds");
        }
    }

    public World createWorld(String name, World.Environment environment, long seed) {
        WorldCreator creator = new WorldCreator(name)
                .environment(environment)
                .type(WorldType.NORMAL)
                .seed(seed)
                .generateStructures(true);
        World world = Bukkit.createWorld(creator);
        if (world != null) markOwned(world);
        return world;
    }

    public long chooseSeed(Long supplied) {
        if (supplied != null) return supplied;
        return ThreadLocalRandom.current().nextLong();
    }

    public void markOwned(World world) {
        try {
            Files.createFile(world.getWorldFolder().toPath().resolve(MARKER));
        } catch (IOException ignored) {
            // Marker already exists or cannot be written; ownership is also checked by name.
        }
    }

    public boolean isOwned(World world) {
        return world != null && Files.exists(world.getWorldFolder().toPath().resolve(MARKER));
    }

    public boolean unload(World world) {
        return world == null || Bukkit.unloadWorld(world, false);
    }

    public boolean deleteOwnedWorld(String name) {
        Path folder = Bukkit.getWorldContainer().toPath().resolve(name);
        if (!Files.exists(folder)) return true;
        Path marker = folder.resolve(MARKER);
        if (!Files.exists(marker)) {
            plugin.getLogger().warning("Refusing to delete unmarked world: " + name);
            return false;
        }
        try (java.util.stream.Stream<Path> paths = Files.walk(folder)) {
            List<Path> files = new ArrayList<Path>();
            paths.forEach(files::add);
            files.sort((a, b) -> b.compareTo(a));
            for (Path path : files) Files.deleteIfExists(path);
            return true;
        } catch (IOException exception) {
            plugin.getLogger().severe("Could not delete world " + name + ": " + exception.getMessage());
            return false;
        }
    }

    public void teleportToOriginalSpawn(Player player) {
        World original = Bukkit.getWorld(originalName());
        if (original != null) player.teleport(original.getSpawnLocation());
    }
}
