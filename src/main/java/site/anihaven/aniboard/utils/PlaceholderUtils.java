package site.anihaven.aniboard.utils;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.attribute.Attribute;

public class PlaceholderUtils {

    private static final boolean PLACEHOLDER_API_ENABLED =
            Bukkit.getPluginManager().isPluginEnabled("PlaceholderAPI");

    public static String setPlaceholders(Player player, String text) {
        if (text == null) return "";

        if (PLACEHOLDER_API_ENABLED) {
            try {
                Class<?> placeholderAPI = Class.forName("me.clip.placeholderapi.PlaceholderAPI");
                text = (String) placeholderAPI.getMethod("setPlaceholders", Player.class, String.class)
                        .invoke(null, player, text);
            } catch (Exception ignored) {
                // PlaceholderAPI not available or reflection failed
            }
        }

        text = setInternalPlaceholders(player, text);

        return text;
    }

    private static String setInternalPlaceholders(Player player, String text) {
        if (player == null) return text;

        text = text.replace("%player_name%", player.getName());
        text = text.replace("%player_display_name%", player.displayName().toString());
        text = text.replace("%player_uuid%", player.getUniqueId().toString());
        text = text.replace("%player_world%", player.getWorld().getName());
        text = text.replace("%player_x%", String.valueOf(player.getLocation().getBlockX()));
        text = text.replace("%player_y%", String.valueOf(player.getLocation().getBlockY()));
        text = text.replace("%player_z%", String.valueOf(player.getLocation().getBlockZ()));
        text = text.replace("%player_health%", String.valueOf((int) player.getHealth()));

        // Try different attribute names for compatibility
        try {
            var maxHealthAttr = player.getAttribute(Attribute.MAX_HEALTH);
            if (maxHealthAttr != null) {
                text = text.replace("%player_max_health%", String.valueOf((int) maxHealthAttr.getValue()));
            } else {
                text = text.replace("%player_max_health%", "20");
            }
        } catch (Exception e) {
            // Fallback if attribute access fails
            text = text.replace("%player_max_health%", "20");
        }

        text = text.replace("%player_food%", String.valueOf(player.getFoodLevel()));
        text = text.replace("%player_level%", String.valueOf(player.getLevel()));
        text = text.replace("%player_exp%", String.valueOf(player.getExp()));
        text = text.replace("%server_online%", String.valueOf(Bukkit.getOnlinePlayers().size()));
        text = text.replace("%server_max%", String.valueOf(Bukkit.getMaxPlayers()));
        text = text.replace("%server_version%", Bukkit.getVersion());

        return text;
    }
}