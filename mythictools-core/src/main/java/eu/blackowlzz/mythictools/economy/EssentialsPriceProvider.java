package eu.blackowlzz.mythictools.economy;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.Plugin;

import java.lang.reflect.Method;
import java.util.OptionalDouble;

/**
 * Reads sell prices from EssentialsX worth.yml via reflection.
 * Chain: IEssentials.getWorth().getPrice(ItemStack) → BigDecimal or null
 */
public class EssentialsPriceProvider implements PriceProvider {

    private Plugin ess;
    private Method getWorth;
    private Method getPrice;

    public EssentialsPriceProvider() {
        try {
            ess = Bukkit.getPluginManager().getPlugin("Essentials");
            if (ess == null) return;
            getWorth = ess.getClass().getMethod("getWorth");
            Object worth = getWorth.invoke(ess);
            if (worth != null) getPrice = worth.getClass().getMethod("getPrice", ItemStack.class);
        } catch (Exception ignored) {
            ess = null;
        }
    }

    @Override public String getName() { return "EssentialsX Worth"; }

    @Override
    public boolean isAvailable() {
        return ess != null && getWorth != null && getPrice != null;
    }

    @Override
    public OptionalDouble getSellPrice(Player player, ItemStack item) {
        if (!isAvailable()) return OptionalDouble.empty();
        try {
            Object worth = getWorth.invoke(ess);
            if (worth == null) return OptionalDouble.empty();
            Object price = getPrice.invoke(worth, item);
            if (price == null) return OptionalDouble.empty();
            double val = ((Number) price).doubleValue();
            return val > 0 ? OptionalDouble.of(val) : OptionalDouble.empty();
        } catch (Exception e) {
            return OptionalDouble.empty();
        }
    }
}
