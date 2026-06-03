package eu.blackowlzz.mythictools.command;

import eu.blackowlzz.mythictools.api.tool.MythicTool;
import eu.blackowlzz.mythictools.tool.ToolRegistry;
import eu.blackowlzz.mythictools.util.TextUtil;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

public class MythicToolsCommand implements CommandExecutor, TabCompleter {

    private static final String PREFIX = ChatColor.translateAlternateColorCodes('&',
            "&8[&dMythicTools&8] &r");
    private static final String LINE = "&8&m-----------------------------------------";

    private final ToolRegistry registry;
    private final Runnable reloader;

    public MythicToolsCommand(ToolRegistry registry, Runnable reloader) {
        this.registry = registry;
        this.reloader = reloader;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (args.length == 0) { sendHelp(sender); return true; }
        return switch (args[0].toLowerCase()) {
            case "give"   -> handleGive(sender, args);
            case "list"   -> handleList(sender);
            case "reload" -> handleReload(sender);
            case "about"  -> handleAbout(sender);
            default       -> { sendHelp(sender); yield true; }
        };
    }

    // ── Sub-commands ──────────────────────────────────────────────────────

    private boolean handleGive(CommandSender sender, String[] args) {
        if (!sender.hasPermission("mythictools.give")) {
            sender.sendMessage(PREFIX + ChatColor.RED + "No permission.");
            return true;
        }
        if (args.length < 3) {
            sender.sendMessage(PREFIX + ChatColor.YELLOW + "Usage: /mt give <player> <id> [duration]");
            return true;
        }

        Player target = sender.getServer().getPlayer(args[1]);
        if (target == null) {
            sender.sendMessage(PREFIX + ChatColor.RED + "Player not found: " + args[1]);
            return true;
        }

        Optional<MythicTool> optTool = registry.getTool(args[2].toLowerCase());
        if (optTool.isEmpty()) {
            sender.sendMessage(PREFIX + ChatColor.RED + "Unknown tool: " + args[2]
                    + ". Use /mt list to see available tools.");
            return true;
        }
        MythicTool tool = optTool.get();

        ItemStack item;
        if (args.length >= 4) {
            long ms = TextUtil.parseDuration(args[3]);
            if (ms <= 0) {
                sender.sendMessage(PREFIX + ChatColor.RED + "Invalid duration. Examples: 5d, 12h, 30m, 5d12h.");
                return true;
            }
            item = tool.createItem(System.currentTimeMillis() + ms);
        } else {
            item = tool.createItem();
        }

        target.getInventory().addItem(item);
        sender.sendMessage(PREFIX + ChatColor.GREEN + "Given "
                + ChatColor.LIGHT_PURPLE + TextUtil.color(tool.getDisplayName())
                + ChatColor.GREEN + " to " + target.getName() + ".");
        target.sendMessage(PREFIX + ChatColor.GREEN + "You received "
                + ChatColor.LIGHT_PURPLE + TextUtil.color(tool.getDisplayName()) + ChatColor.GREEN + "!");
        return true;
    }

    private boolean handleList(CommandSender sender) {
        if (!sender.hasPermission("mythictools.list")) {
            sender.sendMessage(PREFIX + ChatColor.RED + "No permission.");
            return true;
        }
        var tools = registry.getTools();
        c(sender, LINE);
        c(sender, "  &5* &dMythicTools &8| &7" + tools.size() + " tool(s) registered");
        c(sender, LINE);
        for (MythicTool tool : tools) {
            String abilities = tool.getAbilities().stream()
                    .map(a -> "&5" + a.name())
                    .reduce((a, b) -> a + "&8, " + b)
                    .orElse("&8none");
            c(sender, "  &d" + tool.getId()
                    + " &8| &7" + tool.getDisplayName()
                    + " &8| &a$" + tool.getWorth()
                    + " &8| " + abilities);
        }
        c(sender, LINE);
        return true;
    }

    private boolean handleReload(CommandSender sender) {
        if (!sender.hasPermission("mythictools.reload")) {
            sender.sendMessage(PREFIX + ChatColor.RED + "No permission.");
            return true;
        }
        reloader.run();
        sender.sendMessage(PREFIX + ChatColor.GREEN + "Configuration reloaded.");
        return true;
    }

    private boolean handleAbout(CommandSender sender) {
        String version = "?";
        var p = sender.getServer().getPluginManager().getPlugin("MythicTools");
        if (p != null) version = p.getDescription().getVersion();
        c(sender, LINE);
        c(sender, "  &5o &d&lMythicTools &8v&7" + version + "  &8|  &7by &5blackowlzz");
        c(sender, "  &8| &7License &8>> &7GNU GPL v3 + Attribution");
        c(sender, "  &8| &7Source  &8>> &bgithub.com/blackowlzz/MythicTools");
        c(sender, "  &eForking? Visible credit is required -- see LICENSE.");
        c(sender, LINE);
        return true;
    }

    private void sendHelp(CommandSender sender) {
        c(sender, LINE);
        c(sender, "  &5o &d&lMythicTools &8| &7Commands");
        c(sender, "  &8> &d/mt give &8<&fplayer&8> <&fid&8> &8[&fduration&8]  &7e.g. &f7d&7, &f12h");
        c(sender, "  &8> &d/mt list  &8> &d/mt reload  &8> &d/mt about");
        c(sender, LINE);
    }

    private static void c(CommandSender s, String msg) {
        s.sendMessage(TextUtil.color(msg));
    }

    // ── Tab completion ────────────────────────────────────────────────────

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        if (args.length == 1) return Arrays.asList("give", "list", "reload", "about");
        if (args.length == 2 && args[0].equalsIgnoreCase("give")) {
            return sender.getServer().getOnlinePlayers().stream()
                    .map(Player::getName)
                    .filter(n -> n.toLowerCase().startsWith(args[1].toLowerCase()))
                    .collect(Collectors.toList());
        }
        if (args.length == 3 && args[0].equalsIgnoreCase("give")) {
            return registry.getTools().stream()
                    .map(MythicTool::getId)
                    .filter(id -> id.startsWith(args[2].toLowerCase()))
                    .collect(Collectors.toList());
        }
        if (args.length == 4 && args[0].equalsIgnoreCase("give")) {
            return Arrays.asList("1d", "3d", "7d", "30d", "12h", "1h");
        }
        return new ArrayList<>();
    }
}
