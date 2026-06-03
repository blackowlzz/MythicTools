package eu.blackowlzz.mythictools.listener;

import eu.blackowlzz.mythictools.api.event.MythicToolAbilityEvent;
import eu.blackowlzz.mythictools.api.tool.MythicTool;
import eu.blackowlzz.mythictools.api.tool.ToolAbility;
import eu.blackowlzz.mythictools.tool.ToolRegistry;
import eu.blackowlzz.mythictools.tool.ability.AbilityHandler;
import org.bukkit.event.block.Action;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;

import java.util.List;
import java.util.Optional;

public class PlayerInteractListener implements Listener {

    private final ToolRegistry registry;
    private final List<AbilityHandler> handlers;

    public PlayerInteractListener(ToolRegistry registry, List<AbilityHandler> handlers) {
        this.registry = registry;
        this.handlers = handlers;
    }

    @EventHandler(priority = EventPriority.HIGH)
    public void onInteract(PlayerInteractEvent event) {
        if (event.getAction() != Action.RIGHT_CLICK_BLOCK) return;

        Player player = event.getPlayer();
        ItemStack hand = player.getInventory().getItemInMainHand();

        Optional<MythicTool> optTool = registry.getToolFromItem(hand);
        if (optTool.isEmpty()) return;
        MythicTool tool = optTool.get();

        if (registry.isExpired(hand)) {
            event.setCancelled(true);
            BlockBreakListener.removeExpiredTool(player, hand, tool);
            return;
        }

        for (AbilityHandler handler : handlers) {
            ToolAbility ability = handler.getAbility();
            if (!tool.getAbilities().contains(ability)) continue;

            // Only interact-based abilities here (e.g. SELL_CHEST)
            if (ability != ToolAbility.SELL_CHEST) continue;

            MythicToolAbilityEvent apiEvent = new MythicToolAbilityEvent(player, tool, ability, hand);
            player.getServer().getPluginManager().callEvent(apiEvent);
            if (apiEvent.isCancelled()) continue;

            if (handler.handle(player, tool, hand, event)) {
                event.setCancelled(true); // prevent container opening after sell
            }
        }
    }
}
