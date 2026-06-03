package eu.blackowlzz.mythictools.tool.ability;

import eu.blackowlzz.mythictools.api.tool.MythicTool;
import eu.blackowlzz.mythictools.api.tool.ToolAbility;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.inventory.ItemStack;

/**
 * Ensures blocks always drop their items when broken with the Multitool,
 * regardless of tool-material mismatch (e.g. pickaxe on wood).
 * Break speed is handled by the Haste effect applied in {@link
 * eu.blackowlzz.mythictools.listener.MultiToolListener}.
 */
public class MultiToolAbilityHandler implements AbilityHandler {

    @Override
    public ToolAbility getAbility() { return ToolAbility.MULTITOOL; }

    @Override
    public boolean handle(Player player, MythicTool tool, ItemStack item, Event event) {
        if (!(event instanceof BlockBreakEvent bbe)) return false;
        bbe.setDropItems(true);
        return true;
    }
}
