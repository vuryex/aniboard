package site.anihaven.aniboard.commands;

import site.anihaven.aniboard.Aniboard;
import site.anihaven.aniboard.utils.ColorUtils;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

public class AniBoardCommand implements CommandExecutor, TabCompleter {

    private final Aniboard plugin;

    public AniBoardCommand(Aniboard plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        try {
            if (args.length == 0) {
                if (!(sender instanceof Player player)) {
                    sender.sendMessage(ColorUtils.colorize("§cThis command can only be used by players!"));
                    return true;
                }

                toggleScoreboard(player, null);
                return true;
            }

            String subCommand = args[0].toLowerCase();

            switch (subCommand) {
                case "toggle":
                    if (!(sender instanceof Player player)) {
                        sender.sendMessage(ColorUtils.colorize("§cThis command can only be used by players!"));
                        return true;
                    }

                    String layoutName = null;
                    if (args.length > 1) {
                        layoutName = args[1];

                        if (!plugin.getConfigManager().isLayoutSwitchingAllowed() &&
                                !sender.hasPermission("aniboard.admin")) {
                            sender.sendMessage(ColorUtils.colorize("§cLayout switching is disabled!"));
                            return true;
                        }

                        if (!plugin.getConfigManager().layoutExists(layoutName)) {
                            sender.sendMessage(ColorUtils.colorize("§cLayout '§e" + layoutName + "§c' does not exist!"));
                            sender.sendMessage(ColorUtils.colorize("§7Available layouts: §e" +
                                    String.join("§7, §e", plugin.getConfigManager().getAvailableLayouts())));
                            return true;
                        }
                    }

                    toggleScoreboard(player, layoutName);
                    break;

                case "layout":
                    if (!(sender instanceof Player player)) {
                        sender.sendMessage(ColorUtils.colorize("§cThis command can only be used by players!"));
                        return true;
                    }

                    if (args.length < 2) {
                        String currentLayout = plugin.getScoreboardManager().getPlayerLayout(player);
                        sender.sendMessage(ColorUtils.colorize("§7Current layout: §e" + currentLayout));
                        sender.sendMessage(ColorUtils.colorize("§7Available layouts: §e" +
                                String.join("§7, §e", plugin.getConfigManager().getAvailableLayouts())));
                        return true;
                    }

                    if (!plugin.getConfigManager().isLayoutSwitchingAllowed() &&
                            !sender.hasPermission("aniboard.admin")) {
                        sender.sendMessage(ColorUtils.colorize("§cLayout switching is disabled!"));
                        return true;
                    }

                    String newLayout = args[1];
                    if (plugin.getScoreboardManager().setPlayerLayout(player, newLayout)) {
                        sender.sendMessage(ColorUtils.colorize("§aChanged scoreboard layout to: §e" + newLayout));
                    } else {
                        sender.sendMessage(ColorUtils.colorize("§cLayout '§e" + newLayout + "§c' does not exist!"));
                    }
                    break;

                case "list":
                    Set<String> layouts = plugin.getConfigManager().getAvailableLayouts();
                    sender.sendMessage(ColorUtils.colorize("§6Available Scoreboard Layouts:"));
                    for (String layout : layouts) {
                        String title = plugin.getConfigManager().getLayoutTitle(layout);
                        sender.sendMessage(ColorUtils.colorize("§e" + layout + " §8- " + title));
                    }
                    break;

                case "reload":
                    if (!sender.hasPermission("aniboard.reload")) {
                        sender.sendMessage(ColorUtils.colorize("§cYou don't have permission to use this command!"));
                        return true;
                    }
                    plugin.reload();
                    sender.sendMessage(ColorUtils.colorize("§aAniBoard configuration reloaded!"));
                    break;

                case "help":
                    sendHelpMessage(sender);
                    break;

                default:
                    sender.sendMessage(ColorUtils.colorize("§cUnknown subcommand. Use /aniboard help for help."));
                    break;
            }

            return true;
        } catch (Exception e) {
            sender.sendMessage(ColorUtils.colorize("§cAn error occurred while executing the command."));
            plugin.getLogger().severe("Error in command execution: " + e.getMessage());
            return true;
        }
    }

    private void toggleScoreboard(Player player, String layoutName) {
        boolean enabled = plugin.getScoreboardManager().toggleScoreboard(player, layoutName);

        if (enabled) {
            String layout = layoutName != null ? layoutName : plugin.getScoreboardManager().getPlayerLayout(player);
            player.sendMessage(ColorUtils.colorize("§aScoreboard enabled! §7(Layout: §e" + layout + "§7)"));
        } else {
            player.sendMessage(ColorUtils.colorize("§cScoreboard disabled!"));
        }
    }

    private void sendHelpMessage(CommandSender sender) {
        sender.sendMessage(ColorUtils.colorize("§6§l=== AniBoard Help ==="));
        sender.sendMessage(ColorUtils.colorize("§e/aniboard §7- Toggle scoreboard"));
        sender.sendMessage(ColorUtils.colorize("§e/aniboard toggle [layout] §7- Toggle with specific layout"));
        sender.sendMessage(ColorUtils.colorize("§e/aniboard layout [name] §7- Change/view current layout"));
        sender.sendMessage(ColorUtils.colorize("§e/aniboard list §7- List available layouts"));
        sender.sendMessage(ColorUtils.colorize("§e/aniboard help §7- Show this help"));

        if (sender.hasPermission("aniboard.reload")) {
            sender.sendMessage(ColorUtils.colorize("§e/aniboard reload §7- Reload configuration"));
        }
    }

    @Override
    public @Nullable List<String> onTabComplete(@NotNull CommandSender sender, @NotNull Command command, @NotNull String alias, @NotNull String[] args) {
        List<String> completions = new ArrayList<>();

        if (args.length == 1) {
            String input = args[0].toLowerCase();

            if ("toggle".startsWith(input)) completions.add("toggle");
            if ("layout".startsWith(input)) completions.add("layout");
            if ("list".startsWith(input)) completions.add("list");
            if ("help".startsWith(input)) completions.add("help");

            if (sender.hasPermission("aniboard.reload") && "reload".startsWith(input)) {
                completions.add("reload");
            }
        } else if (args.length == 2) {
            String subCommand = args[0].toLowerCase();
            String input = args[1].toLowerCase();

            if (subCommand.equals("toggle") || subCommand.equals("layout")) {
                if (plugin.getConfigManager().isLayoutSwitchingAllowed() ||
                        sender.hasPermission("aniboard.admin")) {

                    for (String layout : plugin.getConfigManager().getAvailableLayouts()) {
                        if (layout.toLowerCase().startsWith(input)) {
                            completions.add(layout);
                        }
                    }
                }
            }
        }

        return completions;
    }
}