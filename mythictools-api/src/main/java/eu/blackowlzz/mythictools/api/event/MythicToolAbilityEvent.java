package eu.blackowlzz.mythictools.api.event;

import eu.blackowlzz.mythictools.api.tool.MythicTool;
import eu.blackowlzz.mythictools.api.tool.ToolAbility;
import org.bukkit.entity.Player;
import org.bukkit.event.Cancellable;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;
import org.bukkit.inventory.ItemStack;

/** Fired before a MythicTool ability activates. Cancellable. */
public class MythicToolAbilityEvent extends Event implements Cancellable {

    private static final HandlerList HANDLERS = new HandlerList();

    private final Player player;
    private final MythicTool tool;
    private final ToolAbility ability;
    private final ItemStack item;
    private boolean cancelled;

    public MythicToolAbilityEvent(Player player, MythicTool tool, ToolAbility ability, ItemStack item) {
        this.player = player;
        this.tool = tool;
        this.ability = ability;
        this.item = item;
    }

    public Player getPlayer() { return player; }
    public MythicTool getTool() { return tool; }
    public ToolAbility getAbility() { return ability; }
    public ItemStack getItem() { return item; }

    @Override public boolean isCancelled() { return cancelled; }
    @Override public void setCancelled(boolean cancel) { this.cancelled = cancel; }
    @Override public HandlerList getHandlers() { return HANDLERS; }
    public static HandlerList getHandlerList() { return HANDLERS; }
}
