package eu.blackowlzz.mythictools.config;

import org.bukkit.Material;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

public class ConfigManager {

    private final JavaPlugin plugin;
    private FileConfiguration config;
    private final Map<Material, Double> itemPrices = new HashMap<>();

    public ConfigManager(JavaPlugin plugin) {
        this.plugin = plugin;
    }

    public void load() {
        plugin.saveDefaultConfig();
        plugin.reloadConfig();
        config = plugin.getConfig();

        loadPrices();
    }

    private void loadPrices() {
        itemPrices.clear();
        File file = new File(plugin.getDataFolder(), "prices.yml");
        if (!file.exists()) {
            plugin.saveResource("prices.yml", false);
        }
        YamlConfiguration prices = YamlConfiguration.loadConfiguration(file);
        for (String key : prices.getKeys(false)) {
            try {
                Material mat = Material.valueOf(key.toUpperCase());
                itemPrices.put(mat, prices.getDouble(key));
            } catch (IllegalArgumentException e) {
                plugin.getLogger().warning("Unknown material in prices.yml: " + key);
            }
        }
        plugin.getLogger().info("Loaded " + itemPrices.size() + " item price(s).");
    }

    public Map<Material, Double> getItemPrices() {
        return Collections.unmodifiableMap(itemPrices);
    }

    public boolean isExpiryEnabled() {
        return config.getBoolean("expiry.enabled", true);
    }

    public long getExpiryCheckIntervalTicks() {
        return config.getLong("expiry.check-interval-ticks", 1200L); // every minute
    }

    public boolean isRemoveExpiredFromInventory() {
        return config.getBoolean("expiry.remove-from-inventory", true);
    }

    public String getPrefix() {
        return config.getString("prefix", "&8[&dMythicTools&8] &r");
    }

    public boolean isUpdateCheckEnabled() {
        return config.getBoolean("update-check.enabled", true);
    }

    public String getModrinthProjectId() {
        return config.getString("update-check.modrinth-project-id", "");
    }
}
