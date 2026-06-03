package eu.blackowlzz.mythictools.economy;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.lang.reflect.Method;
import java.util.OptionalDouble;

/**
 * Reads sell prices from EconomyShopGUI / EconomyShopGUI-Pro via reflection.
 * API: EconomyShopGUI.getPlugin().getShopManager().getSellPrice(ItemStack) → double
 */
public class EconomyShopGuiPriceProvider implements PriceProvider {

    private Method getPlugin;
    private Method getShopManager;
    private Method getSellPrice;

    public EconomyShopGuiPriceProvider() {
        try {
            Class<?> main = Class.forName("me.deadlight.economyshopgui.EconomyShopGUI");
            getPlugin = main.getMethod("getPlugin");
            Object plugin = getPlugin.invoke(null);
            getShopManager = plugin.getClass().getMethod("getShopManager");
            Object mgr = getShopManager.invoke(plugin);
            getSellPrice = mgr.getClass().getMethod("getSellPrice", ItemStack.class);
        } catch (Exception ignored) {
            getPlugin = null;
        }
    }

    @Override public String getName() { return "EconomyShopGUI"; }

    @Override
    public boolean isAvailable() {
        return getPlugin != null
                && (Bukkit.getPluginManager().getPlugin("EconomyShopGUI") != null
                || Bukkit.getPluginManager().getPlugin("EconomyShopGUI-Pro") != null);
    }

    @Override
    public OptionalDouble getSellPrice(Player player, ItemStack item) {
        if (!isAvailable() || getSellPrice == null) return OptionalDouble.empty();
        try {
            Object plugin = getPlugin.invoke(null);
            Object mgr    = getShopManager.invoke(plugin);
            Object price  = getSellPrice.invoke(mgr, item);
            if (price == null) return OptionalDouble.empty();
            double val = ((Number) price).doubleValue();
            return val > 0 ? OptionalDouble.of(val) : OptionalDouble.empty();
        } catch (Exception e) {
            return OptionalDouble.empty();
        }
    }
}
