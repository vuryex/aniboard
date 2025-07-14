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

                toggleScoreboard(player);
                return true;
            }

            String subCommand = args[0].toLowerCase();

            switch (subCommand) {
                case "toggle":
                    if (!(sender instanceof Player player)) {
                        sender.sendMessage(ColorUtils.colorize("§cThis command can only be used by players!"));
                        return true;
                    }
                    toggleScoreboard(player);
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

    private void toggleScoreboard(Player player) {
        boolean enabled = plugin.getScoreboardManager().toggleScoreboard(player);

        if (enabled) {
            player.sendMessage(ColorUtils.colorize("§aScoreboard enabled!"));
        } else {
            player.sendMessage(ColorUtils.colorize("§cScoreboard disabled!"));
        }
    }

    private void sendHelpMessage(CommandSender sender) {
        sender.sendMessage(ColorUtils.colorize("§6§l=== AniBoard Help ==="));
        sender.sendMessage(ColorUtils.colorize("§e/aniboard §7- Toggle scoreboard"));
        sender.sendMessage(ColorUtils.colorize("§e/aniboard toggle §7- Toggle scoreboard"));
        sender.sendMessage(ColorUtils.colorize("§e/aniboard help §7- Show this help"));

        if (sender.hasPermission("aniboard.reload")) {
            sender.sendMessage(ColorUtils.colorize("§e/aniboard reload §7- Reload configuration"));
        }
    }

    @Override
    public @Nullable List<String> onTabComplete(@NotNull CommandSender sender, @NotNull Command command, @NotNull String alias, @NotNull String[] args) {
        if (args.length == 1) {
            List<String> completions = new ArrayList<>();
            String input = args[0].toLowerCase();

            // Add basic completions
            if ("toggle".startsWith(input)) completions.add("toggle");
            if ("help".startsWith(input)) completions.add("help");

            // Add reload if player has permission
            if (sender.hasPermission("aniboard.reload") && "reload".startsWith(input)) {
                completions.add("reload");
            }

            return completions;
        }
        return new ArrayList<>(); // Return empty list instead of null
    }
}