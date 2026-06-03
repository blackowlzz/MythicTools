package eu.blackowlzz.mythictools.util;

import org.bukkit.Bukkit;

public final class VersionUtil {

    private VersionUtil() {}

    private static final int MINOR;

    static {
        String ver = Bukkit.getBukkitVersion();
        String[] parts = ver.split("\\.");
        int minor = 20;
        try {
            minor = Integer.parseInt(parts[1].split("-")[0]);
        } catch (Exception ignored) {}
        MINOR = minor;
    }

    public static int getMinorVersion() { return MINOR; }
    public static boolean isAtLeast(int minor) { return MINOR >= minor; }
}
