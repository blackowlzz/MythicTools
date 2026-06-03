package eu.blackowlzz.mythictools.nms;

import org.bukkit.Bukkit;

/**
 * Detects the running server version and instantiates the appropriate
 * {@link NMSAdapter} at plugin startup.
 */
public final class NMSAdapterLoader {

    private NMSAdapterLoader() {}

    public static NMSAdapter load() {
        int minor = getMinorVersion();
        String implClass;

        if (minor >= 21) {
            implClass = "eu.blackowlzz.mythictools.nms.v1_21.NMSAdapterImpl";
        } else {
            implClass = "eu.blackowlzz.mythictools.nms.v1_20.NMSAdapterImpl";
        }

        try {
            return (NMSAdapter) Class.forName(implClass).getDeclaredConstructor().newInstance();
        } catch (Exception e) {
            Bukkit.getLogger().warning("[MythicTools] Could not load NMS adapter " + implClass
                    + " — falling back to v1_20 adapter. Some features may behave differently.");
            try {
                return (NMSAdapter) Class.forName("eu.blackowlzz.mythictools.nms.v1_20.NMSAdapterImpl")
                        .getDeclaredConstructor().newInstance();
            } catch (Exception ex) {
                throw new RuntimeException("Failed to load any NMS adapter", ex);
            }
        }
    }

    private static int getMinorVersion() {
        String ver = Bukkit.getBukkitVersion(); // e.g. "1.21.1-R0.1-SNAPSHOT"
        String[] parts = ver.split("\\.");
        try {
            return Integer.parseInt(parts[1].split("-")[0]);
        } catch (Exception e) {
            return 20;
        }
    }
}
