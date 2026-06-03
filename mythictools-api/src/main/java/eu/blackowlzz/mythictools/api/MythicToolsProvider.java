package eu.blackowlzz.mythictools.api;

import eu.blackowlzz.mythictools.api.tool.MythicTool;
import org.bukkit.inventory.ItemStack;

import java.util.Collection;
import java.util.Optional;

/** Service interface implemented by the core module. */
public interface MythicToolsProvider {

    /** All registered tool definitions. */
    Collection<MythicTool> getTools();

    /** Look up a tool by its identifier. */
    Optional<MythicTool> getTool(String id);

    /**
     * Returns the {@link MythicTool} definition for the given item, or empty if
     * the item is not a MythicTool.
     */
    Optional<MythicTool> getToolFromItem(ItemStack item);

    /**
     * Whether the item is a MythicTool that has passed its expiry timestamp.
     * Returns false for non-tool items.
     */
    boolean isExpired(ItemStack item);
}
