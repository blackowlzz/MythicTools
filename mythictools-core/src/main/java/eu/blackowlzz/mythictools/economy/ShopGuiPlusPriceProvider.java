package eu.blackowlzz.mythictools.economy;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.lang.reflect.Method;
import java.util.OptionalDouble;

/**
 * Reads sell prices from ShopGUI+ via reflection.
 * API method: {@code ShopGuiPlusApi.getItemStackSellPrice(Player, ItemStack)} → double
 * Returns -1 when the item has no configured sell price.
 */
public class ShopGuiPlusPriceProvider implements PriceProvider {

    private Method sellPriceMethod;

    public ShopGuiPlusPriceProvider() {
        try {
            Class<?> api = Class.forName("net.brcdev.shopgui.ShopGuiPlusApi");
            sellPriceMethod = api.getMethod("getItemStackSellPrice", Player.class, ItemStack.class);
        } catch (Exception ignored) {}
    }

    @Override
    public String getName() { return "ShopGUI+"; }

    @Override
    public boolean isAvailable() {
        return sellPriceMethod != null && Bukkit.getPluginManager().getPlugin("ShopGUIPlus") != null;
    }

    @Override
    public OptionalDouble getSellPrice(Player player, ItemStack item) {
        if (!isAvailable()) return OptionalDouble.empty();
        try {
            double price = (double) sellPriceMethod.invoke(null, player, item);
            return price > 0 ? OptionalDouble.of(price) : OptionalDouble.empty();
        } catch (Exception e) {
            return OptionalDouble.empty();
        }
    }
}
