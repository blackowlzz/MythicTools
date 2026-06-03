package eu.blackowlzz.mythictools.tool.ability;

import eu.blackowlzz.mythictools.api.tool.MythicTool;
import eu.blackowlzz.mythictools.api.tool.ToolAbility;
import eu.blackowlzz.mythictools.economy.EconomyManager;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.Container;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;
import java.util.List;
import java.util.OptionalDouble;

/** Right-click a container to sell all its contents using dynamic shop prices. */
public class SellAbilityHandler implements AbilityHandler {

    private static final String PRE = "§8[§dMythicTools§8] ";

    private final EconomyManager economy;

    public SellAbilityHandler(EconomyManager economy) {
        this.economy = economy;
    }

    @Override
    public ToolAbility getAbility() { return ToolAbility.SELL_CHEST; }

    @Override
    public boolean handle(Player player, MythicTool tool, ItemStack item, Event event) {
        if (!(event instanceof PlayerInteractEvent pie)) return false;
        Block block = pie.getClickedBlock();
        if (block == null || !(block.getState() instanceof Container container)) return false;

        if (!economy.isVaultReady()) {
            player.sendMessage(PRE + "§cEconomy unavailable (Vault not installed).");
            return false;
        }
        if (!economy.hasPriceSource()) {
            player.sendMessage(PRE + "§cNo shop plugin found. Install ShopGUI+, EconomyShopGUI or EssentialsX.");
            return false;
        }

        Inventory inv = container.getInventory();
        double total = 0.0;
        int stacks = 0;
        int items  = 0;

        // Collect items with prices
        List<Integer> slotsToEmpty = new ArrayList<>();
        for (int i = 0; i < inv.getSize(); i++) {
            ItemStack stack = inv.getItem(i);
            if (stack == null || stack.getType() == Material.AIR) continue;
            OptionalDouble price = economy.getSellPrice(player, stack);
            if (price.isEmpty() || price.getAsDouble() <= 0) continue;
            total += price.getAsDouble() * stack.getAmount();
            items += stack.getAmount();
            stacks++;
            slotsToEmpty.add(i);
        }

        if (stacks == 0) {
            player.sendMessage(PRE + "§7None of the items in this container have a sell price.");
            return false;
        }

        for (int slot : slotsToEmpty) inv.setItem(slot, null);
        economy.deposit(player, total);

        player.sendMessage(PRE
                + "§aSold §f" + items + " item" + (items == 1 ? "" : "s")
                + " §a(§f" + stacks + " stack" + (stacks == 1 ? "" : "s") + "§a) "
                + "for §6" + economy.format(total) + "§a! "
                + "§8[§7via " + economy.getPriceSourceName() + "§8]");
        return true;
    }
}
