package eu.blackowlzz.mythictools.api.event;

import eu.blackowlzz.mythictools.api.tool.MythicTool;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;
import org.bukkit.inventory.ItemStack;

/** Fired when a MythicTool item expires and is removed from a player's inventory. */
public class MythicToolExpireEvent extends Event {

    private static final HandlerList HANDLERS = new HandlerList();

    private final Player player;
    private final MythicTool tool;
    private final ItemStack item;

    public MythicToolExpireEvent(Player player, MythicTool tool, ItemStack item) {
        this.player = player;
        this.tool = tool;
        this.item = item;
    }

    public Player getPlayer() { return player; }
    public MythicTool getTool() { return tool; }
    public ItemStack getItem() { return item; }

    @Override public HandlerList getHandlers() { return HANDLERS; }
    public static HandlerList getHandlerList() { return HANDLERS; }
}
