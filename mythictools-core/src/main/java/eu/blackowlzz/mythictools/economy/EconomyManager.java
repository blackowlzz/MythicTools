package eu.blackowlzz.mythictools.economy;

import net.milkbowl.vault.economy.Economy;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.RegisteredServiceProvider;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.List;
import java.util.OptionalDouble;

public class EconomyManager {

    private Economy vaultEconomy;
    private PriceProvider priceProvider;
    private final JavaPlugin plugin;

    public EconomyManager(JavaPlugin plugin) {
        this.plugin = plugin;
    }

    public void setup() {
        // ── Vault economy ──────────────────────────────────────────────────
        if (plugin.getServer().getPluginManager().getPlugin("Vault") == null) {
            plugin.getLogger().warning("Vault not found — SELL_CHEST ability disabled.");
            return;
        }
        RegisteredServiceProvider<Economy> rsp =
                plugin.getServer().getServicesManager().getRegistration(Economy.class);
        if (rsp == null) {
            plugin.getLogger().warning("No Economy provider in Vault — SELL_CHEST disabled.");
            return;
        }
        vaultEconomy = rsp.getProvider();
        plugin.getLogger().info("Economy: " + vaultEconomy.getName());

        // ── Price providers (first available wins) ─────────────────────────
        List<PriceProvider> candidates = List.of(
                new ShopGuiPlusPriceProvider(),
                new EconomyShopGuiPriceProvider(),
                new EssentialsPriceProvider()
        );

        for (PriceProvider p : candidates) {
            if (p.isAvailable()) {
                priceProvider = p;
                plugin.getLogger().info("Sell prices: " + p.getName());
                return;
            }
        }

        plugin.getLogger().warning(
                "No shop plugin found (ShopGUI+, EconomyShopGUI, EssentialsX). "
                + "SELL_CHEST will report $0 for all items. "
                + "Install a supported shop plugin to enable dynamic pricing.");
    }

    // ── API ───────────────────────────────────────────────────────────────

    public boolean isVaultReady() { return vaultEconomy != null; }

    public boolean hasPriceSource() { return priceProvider != null; }

    public String getPriceSourceName() {
        return priceProvider != null ? priceProvider.getName() : "none";
    }

    public OptionalDouble getSellPrice(Player player, ItemStack item) {
        if (priceProvider == null) return OptionalDouble.empty();
        return priceProvider.getSellPrice(player, item);
    }

    public void deposit(Player player, double amount) {
        if (vaultEconomy == null) return;
        vaultEconomy.depositPlayer(player, amount);
    }

    public String format(double amount) {
        if (vaultEconomy == null) return String.format("%.2f", amount);
        return vaultEconomy.format(amount);
    }
}
