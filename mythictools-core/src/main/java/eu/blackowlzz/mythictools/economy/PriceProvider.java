package eu.blackowlzz.mythictools.economy;

import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.util.OptionalDouble;

/** Abstraction over any shop/economy plugin that knows item sell prices. */
public interface PriceProvider {
    String getName();
    boolean isAvailable();
    OptionalDouble getSellPrice(Player player, ItemStack item);
}
