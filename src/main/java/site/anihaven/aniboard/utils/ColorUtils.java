package site.anihaven.aniboard.utils;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import net.kyori.adventure.text.format.TextColor;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class ColorUtils {

    private static final Pattern HEX_PATTERN = Pattern.compile("<#([A-Fa-f0-9]{6})>");
    private static final LegacyComponentSerializer LEGACY_SERIALIZER = LegacyComponentSerializer.legacySection();

    public static String colorize(String text) {
        if (text == null) return "";

        text = translateLegacyColors(text);
        text = translateHexColors(text);

        return text;
    }

    private static String translateLegacyColors(String text) {
        return text.replaceAll("&([0-9a-fk-orA-FK-OR])", "§$1");
    }

    public static Component colorizeComponent(String text) {
        if (text == null) return Component.empty();

        text = translateLegacyColors(text);

        Component component = Component.empty();
        Matcher matcher = HEX_PATTERN.matcher(text);
        int lastEnd = 0;

        while (matcher.find()) {
            String beforeHex = text.substring(lastEnd, matcher.start());
            if (!beforeHex.isEmpty()) {
                component = component.append(LEGACY_SERIALIZER.deserialize(beforeHex));
            }

            String hexCode = matcher.group(1);
            TextColor color = TextColor.fromHexString("#" + hexCode);

            int nextStart = matcher.end();
            int nextHexStart = text.indexOf("<#", nextStart);
            String coloredText;

            if (nextHexStart == -1) {
                coloredText = text.substring(nextStart);
                lastEnd = text.length();
            } else {
                coloredText = text.substring(nextStart, nextHexStart);
                lastEnd = nextHexStart;
            }

            if (!coloredText.isEmpty()) {
                Component coloredComponent = LEGACY_SERIALIZER.deserialize(coloredText).color(color);
                component = component.append(coloredComponent);
            }
        }

        if (lastEnd < text.length()) {
            String remaining = text.substring(lastEnd);
            component = component.append(LEGACY_SERIALIZER.deserialize(remaining));
        }

        if (component.equals(Component.empty())) {
            return LEGACY_SERIALIZER.deserialize(text);
        }

        return component;
    }

    private static String translateHexColors(String text) {
        Matcher matcher = HEX_PATTERN.matcher(text);
        StringBuffer buffer = new StringBuffer();

        while (matcher.find()) {
            String hexCode = matcher.group(1);
            String replacement = "§x";

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

        text = HEX_PATTERN.matcher(text).replaceAll("");

        return text.replaceAll("§[0-9a-fk-orA-FK-OR]", "");
    }
}