package eu.blackowlzz.mythictools.tool.ability;

import eu.blackowlzz.mythictools.api.tool.MythicTool;
import eu.blackowlzz.mythictools.api.tool.ToolAbility;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.inventory.ItemStack;

/** Handles one specific {@link ToolAbility} in response to Bukkit events. */
public interface AbilityHandler {

    /** The ability this handler is responsible for. */
    ToolAbility getAbility();

    /**
     * Called when the ability should fire.
     * Returns true if the ability was successfully applied.
     */
    boolean handle(Player player, MythicTool tool, ItemStack item, Event event);
}
