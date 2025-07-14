package site.anihaven.aniboard.utils;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import net.kyori.adventure.text.format.TextColor;
import net.kyori.adventure.text.format.TextDecoration;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class ColorUtils {

    private static final Pattern HEX_PATTERN = Pattern.compile("<#([A-Fa-f0-9]{6})>(.*?)</#([A-Fa-f0-9]{6})>");
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
            String content = matcher.group(2);
            String closingHex = matcher.group(3);

            if (!hexCode.equalsIgnoreCase(closingHex)) {
                continue;
            }

            TextColor color = TextColor.fromHexString("#" + hexCode);

            Component hexComponent = Component.text("");

            StringBuilder currentText = new StringBuilder();
            boolean isBold = false, isItalic = false, isUnderline = false, isStrike = false, isMagic = false;

            for (int i = 0; i < content.length(); i++) {
                char c = content.charAt(i);

                if (c == '§' && i + 1 < content.length()) {
                    char code = content.charAt(i + 1);

                    if (currentText.length() > 0) {
                        Component textComp = Component.text(currentText.toString()).color(color);
                        if (isBold) textComp = textComp.decoration(TextDecoration.BOLD, true);
                        if (isItalic) textComp = textComp.decoration(TextDecoration.ITALIC, true);
                        if (isUnderline) textComp = textComp.decoration(TextDecoration.UNDERLINED, true);
                        if (isStrike) textComp = textComp.decoration(TextDecoration.STRIKETHROUGH, true);
                        if (isMagic) textComp = textComp.decoration(TextDecoration.OBFUSCATED, true);

                        hexComponent = hexComponent.append(textComp);
                        currentText.setLength(0);
                    }

                    switch (Character.toLowerCase(code)) {
                        case 'l' -> isBold = true;
                        case 'o' -> isItalic = true;
                        case 'n' -> isUnderline = true;
                        case 'm' -> isStrike = true;
                        case 'k' -> isMagic = true;
                        case 'r' -> {
                            isBold = false;
                            isItalic = false;
                            isUnderline = false;
                            isStrike = false;
                            isMagic = false;
                        }
                    }
                    i++;
                } else {
                    currentText.append(c);
                }
            }

            if (currentText.length() > 0) {
                Component textComp = Component.text(currentText.toString()).color(color);
                if (isBold) textComp = textComp.decoration(TextDecoration.BOLD, true);
                if (isItalic) textComp = textComp.decoration(TextDecoration.ITALIC, true);
                if (isUnderline) textComp = textComp.decoration(TextDecoration.UNDERLINED, true);
                if (isStrike) textComp = textComp.decoration(TextDecoration.STRIKETHROUGH, true);
                if (isMagic) textComp = textComp.decoration(TextDecoration.OBFUSCATED, true);

                hexComponent = hexComponent.append(textComp);
            }

            component = component.append(hexComponent);
            lastEnd = matcher.end();
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
            String content = matcher.group(2);
            String closingHex = matcher.group(3);

            if (!hexCode.equalsIgnoreCase(closingHex)) {
                continue;
            }

            String hexStart = "§x";
            for (char c : hexCode.toCharArray()) {
                hexStart += "§" + c;
            }

            String replacement = hexStart + content + "§r";
            matcher.appendReplacement(buffer, replacement);
        }
        matcher.appendTail(buffer);

        return buffer.toString();
    }

    public static String stripColors(String text) {
        if (text == null) return "";

        text = HEX_PATTERN.matcher(text).replaceAll("$2");

        return text.replaceAll("§[0-9a-fk-orA-FK-OR]", "");
    }
}