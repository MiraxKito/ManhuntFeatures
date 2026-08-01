package ru.kitoro.mhfeatures.command;

import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;
import ru.kitoro.mhfeatures.MHFeaturesPlugin;
import ru.kitoro.mhfeatures.role.Role;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Locale;

public final class MHCommand implements CommandExecutor, TabCompleter {
    private final MHFeaturesPlugin plugin;

    public MHCommand(MHFeaturesPlugin plugin) { this.plugin = plugin; }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (args.length == 0) { help(sender); return true; }
        if (plugin.getResetManager().isResetting() && plugin.getConfig().getBoolean("reset.block-all-plugin-commands", true)) {
            sender.sendMessage(plugin.message("messages.prefix") + "Commands are locked during reset.");
            return true;
        }
        switch (args[0].toLowerCase(Locale.ROOT)) {
            case "role": role(sender, args); break;
            case "reset": reset(sender, args); break;
            case "compass": compass(sender, args); break;
            case "mhworld": teleport(sender, args, true); break;
            case "ogworld": teleport(sender, args, false); break;
            case "status": status(sender); break;
            case "reload": reload(sender); break;
            default: help(sender); break;
        }
        return true;
    }

    private void role(CommandSender sender, String[] args) {
        if (!sender.hasPermission("mhfeatures.admin.role")) { deny(sender); return; }
        if (args.length < 3) { sender.sendMessage("Usage: /mhfeatures role <player|selector> <hunter|runner|clear>"); return; }
        Role role = Role.parse(args[2]);
        if (role == null) { sender.sendMessage("Role must be hunter, runner, or clear."); return; }
        List<Player> targets = select(sender, args[1]);
        if (targets.isEmpty()) { sender.sendMessage("No players found."); return; }
        for (Player target : targets) {
            plugin.getRoleManager().set(target.getUniqueId(), role);
            if (role == Role.HUNTER) plugin.getCompassManager().ensure(target);
            else plugin.getCompassManager().removeAll(target);
            plugin.getTabManager().update(target);
            target.sendMessage(plugin.color("&aYour role: &f" + role.name().toLowerCase(Locale.ROOT)));
        }
        sender.sendMessage(plugin.color("&aRole applied to players: &f" + targets.size()));
    }

    private void reset(CommandSender sender, String[] args) {
        if (!sender.hasPermission("mhfeatures.admin.reset")) { deny(sender); return; }
        Long seed = null;
        if (args.length >= 2) {
            try { seed = Long.parseLong(args[1]); }
            catch (NumberFormatException exception) { sender.sendMessage("Seed must be an integer."); return; }
        }
        final Long selectedSeed = seed;
        Bukkit.getScheduler().runTask(plugin, () -> plugin.getResetManager().reset(selectedSeed));
    }

    private void compass(CommandSender sender, String[] args) {
        if (!sender.hasPermission("mhfeatures.admin.role")) { deny(sender); return; }
        String selector = args.length >= 2 ? args[1] : "@s";
        List<Player> targets = select(sender, selector);
        if (targets.isEmpty()) { sender.sendMessage("No players found."); return; }
        for (Player target : targets) plugin.getCompassManager().ensure(target);
        sender.sendMessage(plugin.color("&aManhunt Compass given to players: &f" + targets.size()));
    }

    private void teleport(CommandSender sender, String[] args, boolean toMh) {
        if (!sender.hasPermission(toMh ? "mhfeatures.admin.mhworld" : "mhfeatures.admin.ogworld")) { deny(sender); return; }
        String selector = args.length >= 2 ? args[1] : "@s";
        List<Player> targets = select(sender, selector);
        if (targets.isEmpty()) { sender.sendMessage("No players found."); return; }
        World world = toMh ? plugin.getWorldManager().getOverworld() : Bukkit.getWorld(plugin.getWorldManager().originalName());
        if (world == null) { sender.sendMessage("Target world not found."); return; }
        for (Player target : targets) target.teleport(world.getSpawnLocation());
        sender.sendMessage(plugin.color("&aPlayers moved: &f" + targets.size()));
    }

    private void status(CommandSender sender) {
        sender.sendMessage(plugin.color("&6MHFeatures 1.3.0 status"));
        sender.sendMessage("Platform API: version-specific Bukkit/Spigot/Paper build");
        sender.sendMessage("Reset: " + (plugin.getResetManager().isResetting() ? "running" : "not running"));
        sender.sendMessage("Worlds: " + (Bukkit.getWorld(plugin.getWorldManager().overworldName()) != null ? "Overworld " : "")
                + (Bukkit.getWorld(plugin.getWorldManager().netherName()) != null ? "Nether " : "")
                + (Bukkit.getWorld(plugin.getWorldManager().endName()) != null ? "End" : ""));
        sender.sendMessage("Hunters: " + plugin.getRoleManager().count(Role.HUNTER) + ", runners: " + plugin.getRoleManager().count(Role.RUNNER));
    }

    private void reload(CommandSender sender) {
        if (!sender.hasPermission("mhfeatures.admin.reload")) { deny(sender); return; }
        plugin.reloadConfig();
        sender.sendMessage(plugin.color("&aConfiguration reloaded."));
    }

    private List<Player> select(CommandSender sender, String selector) {
        if (selector.equalsIgnoreCase("@s")) {
            if (sender instanceof Player) return Collections.singletonList((Player) sender);
            return Collections.emptyList();
        }
        if (selector.equalsIgnoreCase("@a")) return new ArrayList<Player>(Bukkit.getOnlinePlayers());
        if (selector.equalsIgnoreCase("@p")) {
            if (!(sender instanceof Player)) return Collections.emptyList();
            Player player = (Player) sender;
            Player nearest = null;
            double distance = Double.MAX_VALUE;
            for (Player candidate : Bukkit.getOnlinePlayers()) {
                double candidateDistance = candidate.getLocation().distanceSquared(player.getLocation());
                if (candidateDistance < distance) {
                    distance = candidateDistance;
                    nearest = candidate;
                }
            }
            return nearest == null ? Collections.<Player>emptyList() : Collections.singletonList(nearest);
        }
        if (selector.equalsIgnoreCase("@r")) {
            List<Player> players = new ArrayList<Player>(Bukkit.getOnlinePlayers());
            if (players.isEmpty()) return Collections.emptyList();
            Collections.shuffle(players);
            return Collections.singletonList(players.get(0));
        }
        if (selector.toLowerCase(Locale.ROOT).startsWith("@e") && selector.toLowerCase(Locale.ROOT).contains("type=player")) {
            return new ArrayList<Player>(Bukkit.getOnlinePlayers());
        }
        Player exact = Bukkit.getPlayerExact(selector);
        return exact == null ? Collections.<Player>emptyList() : Collections.singletonList(exact);
    }

    private void deny(CommandSender sender) { sender.sendMessage(plugin.color("&cYou do not have permission.")); }
    private void help(CommandSender sender) { sender.sendMessage(plugin.color("&6/mhfeatures role|compass|reset|mhworld|ogworld|status|reload")); }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        if (args.length == 1) return partial(args[0], Arrays.asList("role", "reset", "compass", "mhworld", "ogworld", "status", "reload"));
        if ((args[0].equalsIgnoreCase("role")) && args.length == 3) return partial(args[2], Arrays.asList("hunter", "runner", "clear"));
        if (args.length == 2 && Arrays.asList("role", "compass", "mhworld", "ogworld").contains(args[0].toLowerCase(Locale.ROOT))) {
            List<String> values = new ArrayList<String>();
            for (Player player : Bukkit.getOnlinePlayers()) values.add(player.getName());
            values.addAll(Arrays.asList("@s", "@a", "@p", "@r", "@e[type=player]"));
            return partial(args[1], values);
        }
        return Collections.emptyList();
    }

    private List<String> partial(String value, List<String> options) {
        List<String> result = new ArrayList<String>();
        String lower = value.toLowerCase(Locale.ROOT);
        for (String option : options) {
            if (option.toLowerCase(Locale.ROOT).startsWith(lower)) result.add(option);
        }
        return result;
    }
}
