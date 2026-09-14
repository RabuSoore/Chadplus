package com.rabusoore.chadplus.util;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public final class ColorUtils {

    private static final Pattern HEX_PATTERN_1 = Pattern.compile("(?i)&#([A-Fa-f0-9]{6})");
    private static final Pattern HEX_PATTERN_2 = Pattern.compile("(?i)#([A-Fa-f0-9]{6})");

    private ColorUtils() {}

    public static Component parse(String input) {
        if (input == null || input.isEmpty()) {
            return Component.empty();
        }

        // Convert legacy Hex format &#RRGGBB and #RRGGBB to MiniMessage <color:#RRGGBB>
        Matcher matcher1 = HEX_PATTERN_1.matcher(input);
        StringBuffer sb = new StringBuffer();
        while (matcher1.find()) {
            matcher1.appendReplacement(sb, "<color:#" + matcher1.group(1) + ">");
        }
        matcher1.appendTail(sb);
        input = sb.toString();

        Matcher matcher2 = HEX_PATTERN_2.matcher(input);
        sb = new StringBuffer();
        while (matcher2.find()) {
            // Check if it's already wrapped in MiniMessage tag format
            int start = matcher2.start();
            if (start > 0 && input.charAt(start - 1) == ':') {
                matcher2.appendReplacement(sb, matcher2.group(0));
            } else {
                matcher2.appendReplacement(sb, "<color:#" + matcher2.group(1) + ">");
            }
        }
        matcher2.appendTail(sb);
        input = sb.toString();

        // Convert standard legacy ampersand codes to MiniMessage syntax
        input = convertLegacyToMiniMessage(input);

        return MiniMessage.miniMessage().deserialize(input);
    }

    private static String convertLegacyToMiniMessage(String input) {
        return input.replace("&0", "<black>")
                    .replace("&1", "<dark_blue>")
                    .replace("&2", "<dark_green>")
                    .replace("&3", "<dark_aqua>")
                    .replace("&4", "<dark_red>")
                    .replace("&5", "<dark_purple>")
                    .replace("&6", "<gold>")
                    .replace("&7", "<gray>")
                    .replace("&8", "<dark_gray>")
                    .replace("&9", "<blue>")
                    .replace("&a", "<green>")
                    .replace("&b", "<aqua>")
                    .replace("&c", "<red>")
                    .replace("&d", "<light_purple>")
                    .replace("&e", "<yellow>")
                    .replace("&f", "<white>")
                    .replace("&k", "<obfuscated>")
                    .replace("&l", "<bold>")
                    .replace("&m", "<strikethrough>")
                    .replace("&n", "<underlined>")
                    .replace("&o", "<italic>")
                    .replace("&r", "<reset>");
    }

    public static String toLegacyString(Component component) {
        return LegacyComponentSerializer.legacyAmpersand().serialize(component);
    }
}
