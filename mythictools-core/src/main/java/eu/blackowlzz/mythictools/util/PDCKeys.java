package eu.blackowlzz.mythictools.util;

import org.bukkit.NamespacedKey;
import org.bukkit.plugin.Plugin;

/** Central registry of all PersistentDataContainer keys used by MythicTools. */
public final class PDCKeys {

    /** The tool definition ID (e.g. "amethyst_drill"). */
    public static NamespacedKey TOOL_ID;

    /** Unix epoch millisecond at which this tool expires (absent = never). */
    public static NamespacedKey EXPIRY;

    /** Snapshot of the worth value for quick retrieval. */
    public static NamespacedKey WORTH;

    public static void init(Plugin plugin) {
        TOOL_ID = new NamespacedKey(plugin, "tool_id");
        EXPIRY  = new NamespacedKey(plugin, "expiry");
        WORTH   = new NamespacedKey(plugin, "worth");
    }

    private PDCKeys() {}
}
