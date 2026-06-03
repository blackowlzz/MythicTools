package eu.blackowlzz.mythictools.api;

import eu.blackowlzz.mythictools.api.tool.MythicTool;

import java.util.Collection;
import java.util.Optional;

/**
 * Static entry-point for the MythicTools public API.
 *
 * <p>Other plugins can depend on mythictools-api and call {@link #get()} to retrieve
 * a {@link MythicToolsProvider} after the plugin has been enabled.
 */
public final class MythicToolsAPI {

    private static MythicToolsProvider provider;

    private MythicToolsAPI() {}

    public static MythicToolsProvider get() {
        if (provider == null) {
            throw new IllegalStateException("MythicTools is not loaded or not yet enabled.");
        }
        return provider;
    }

    /** Called internally by the plugin during onEnable. */
    public static void setProvider(MythicToolsProvider newProvider) {
        provider = newProvider;
    }

    /** Called internally during onDisable. */
    public static void clearProvider() {
        provider = null;
    }

    // ── Convenience shortcuts ──────────────────────────────────────────────

    public static Collection<MythicTool> getTools() {
        return get().getTools();
    }

    public static Optional<MythicTool> getTool(String id) {
        return get().getTool(id);
    }
}
