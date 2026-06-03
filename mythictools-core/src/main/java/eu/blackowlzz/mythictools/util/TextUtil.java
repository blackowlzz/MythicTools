package eu.blackowlzz.mythictools.util;

import org.bukkit.ChatColor;

import java.util.List;
import java.util.stream.Collectors;

public final class TextUtil {

    private TextUtil() {}

    public static String color(String text) {
        return ChatColor.translateAlternateColorCodes('&', text);
    }

    public static List<String> color(List<String> lines) {
        return lines.stream().map(TextUtil::color).collect(Collectors.toList());
    }

    /**
     * Formats a duration in milliseconds as "Xd Yh Zm" (omitting zero fields).
     * Returns "Expired" if remaining <= 0.
     */
    public static String formatDuration(long millis) {
        if (millis <= 0) return "Expired";
        long seconds = millis / 1000;
        long days    = seconds / 86400;
        long hours   = (seconds % 86400) / 3600;
        long minutes = (seconds % 3600) / 60;

        StringBuilder sb = new StringBuilder();
        if (days > 0)    sb.append(days).append("d ");
        if (hours > 0)   sb.append(hours).append("h ");
        if (minutes > 0) sb.append(minutes).append("m");
        String result = sb.toString().trim();
        return result.isEmpty() ? "<1m" : result;
    }

    /**
     * Parses a duration string like "5d", "12h", "30m", "5d12h30m" into milliseconds.
     * Returns -1 if the format is invalid.
     */
    public static long parseDuration(String input) {
        if (input == null || input.isBlank()) return -1;
        long total = 0;
        String current = "";
        for (char c : input.toLowerCase().toCharArray()) {
            if (Character.isDigit(c)) {
                current += c;
            } else {
                if (current.isEmpty()) return -1;
                long value = Long.parseLong(current);
                current = "";
                long add = switch (c) {
                    case 'd' -> value * 86_400_000L;
                    case 'h' -> value * 3_600_000L;
                    case 'm' -> value * 60_000L;
                    case 's' -> value * 1_000L;
                    default  -> -1L;
                };
                if (add < 0) return -1;
                total += add;
            }
        }
        if (!current.isEmpty()) return -1; // trailing number with no unit
        return total;
    }
}
