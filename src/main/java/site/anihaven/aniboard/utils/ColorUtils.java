package site.anihaven.aniboard.utils;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class ColorUtils {

    private static final Pattern HEX_PATTERN = Pattern.compile("<#([A-Fa-f0-9]{6})>");

    public static String colorize(String text) {
        if (text == null) return "";

        // First translate hex colors to § format
        text = translateHexColors(text);

        // Then translate & codes to § codes (for LuckPerms/Vault compatibility)
        // But be careful not to replace & inside hex codes
        text = translateLegacyColors(text);

        return text;
    }

    private static String translateLegacyColors(String text) {
        // Replace & with § but only for valid color codes
        return text.replaceAll("&([0-9a-fk-orA-FK-OR])", "§$1");
    }

    public static Component colorizeComponent(String text) {
        if (text == null) return Component.empty();

        // Process hex colors and § codes
        text = colorize(text);

        // Use § as the color code character
        return LegacyComponentSerializer.legacySection().deserialize(text);
    }

    private static String translateHexColors(String text) {
        Matcher matcher = HEX_PATTERN.matcher(text);
        StringBuffer buffer = new StringBuffer();

        while (matcher.find()) {
            String hexCode = matcher.group(1);
            String replacement = "§x";

            // Convert hex to § format: §x§F§F§0§0§F§F
            for (char c : hexCode.toCharArray()) {
                replacement += "§" + c;
            }

            matcher.appendReplacement(buffer, replacement);
        }
        matcher.appendTail(buffer);

        return buffer.toString();
    }

    public static String stripColors(String text) {
        if (text == null) return "";

        // Remove hex colors first
        text = HEX_PATTERN.matcher(text).replaceAll("");

        // Strip § color codes
        return LegacyComponentSerializer.legacySection().serialize(
                Component.text(text)
        );
    }
}