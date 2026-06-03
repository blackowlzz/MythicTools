package eu.blackowlzz.mythictools.listener;

import eu.blackowlzz.mythictools.api.event.MythicToolAbilityEvent;
import eu.blackowlzz.mythictools.api.event.MythicToolExpireEvent;
import eu.blackowlzz.mythictools.api.tool.MythicTool;
import eu.blackowlzz.mythictools.api.tool.ToolAbility;
import eu.blackowlzz.mythictools.tool.ToolRegistry;
import eu.blackowlzz.mythictools.tool.ability.AbilityHandler;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.inventory.ItemStack;

import java.util.List;
import java.util.Optional;

public class BlockBreakListener implements Listener {

    private final ToolRegistry registry;
    private final List<AbilityHandler> handlers;

    public BlockBreakListener(ToolRegistry registry, List<AbilityHandler> handlers) {
        this.registry = registry;
        this.handlers = handlers;
    }

    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public void onBlockBreak(BlockBreakEvent event) {
        Player player = event.getPlayer();
        ItemStack hand = player.getInventory().getItemInMainHand();

        Optional<MythicTool> optTool = registry.getToolFromItem(hand);
        if (optTool.isEmpty()) return;
        MythicTool tool = optTool.get();

        if (registry.isExpired(hand)) {
            event.setCancelled(true);
            removeExpiredTool(player, hand, tool);
            return;
        }

        for (AbilityHandler handler : handlers) {
            ToolAbility ability = handler.getAbility();
            if (!tool.getAbilities().contains(ability)) continue;

            MythicToolAbilityEvent apiEvent = new MythicToolAbilityEvent(player, tool, ability, hand);
            player.getServer().getPluginManager().callEvent(apiEvent);
            if (apiEvent.isCancelled()) continue;

            handler.handle(player, tool, hand, event);
        }
    }

    static void removeExpiredTool(Player player, ItemStack item, MythicTool tool) {
        player.getInventory().setItemInMainHand(new ItemStack(Material.AIR));
        player.sendMessage("§8[§dMythicTools§8] §cYour §d" + tool.getDisplayName()
                + " §chas expired and was removed!");
        MythicToolExpireEvent expireEvent = new MythicToolExpireEvent(player, tool, item.clone());
        player.getServer().getPluginManager().callEvent(expireEvent);
    }
}
