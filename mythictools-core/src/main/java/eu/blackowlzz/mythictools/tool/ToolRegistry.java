package eu.blackowlzz.mythictools.tool;

import eu.blackowlzz.mythictools.MythicToolsPlugin;
import eu.blackowlzz.mythictools.api.MythicToolsProvider;
import eu.blackowlzz.mythictools.api.tool.MythicTool;
import eu.blackowlzz.mythictools.api.tool.ToolAbility;
import eu.blackowlzz.mythictools.nms.NMSAdapter;
import eu.blackowlzz.mythictools.util.PDCKeys;
import org.bukkit.Material;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.inventory.ItemStack;
import org.bukkit.persistence.PersistentDataType;

import java.io.File;
import java.util.*;
import java.util.logging.Logger;

public class ToolRegistry implements MythicToolsProvider {

    private final MythicToolsPlugin plugin;
    private final NMSAdapter nms;
    private final Logger log;

    private final Map<String, MythicTool> tools = new LinkedHashMap<>();

    public ToolRegistry(MythicToolsPlugin plugin, NMSAdapter nms) {
        this.plugin = plugin;
        this.nms = nms;
        this.log = plugin.getLogger();
    }

    public void load() {
        tools.clear();
        File file = new File(plugin.getDataFolder(), "tools.yml");
        if (!file.exists()) {
            plugin.saveResource("tools.yml", false);
        }
        YamlConfiguration cfg = YamlConfiguration.loadConfiguration(file);
        ConfigurationSection section = cfg.getConfigurationSection("tools");
        if (section == null) {
            log.warning("tools.yml has no 'tools:' section — no tools loaded.");
            return;
        }

        for (String id : section.getKeys(false)) {
            ConfigurationSection s = section.getConfigurationSection(id);
            if (s == null) continue;
            try {
                MythicTool tool = parse(id, s);
                tools.put(id, tool);
                log.info("Loaded tool: " + id);
            } catch (Exception e) {
                log.warning("Failed to load tool '" + id + "': " + e.getMessage());
            }
        }
        log.info("Loaded " + tools.size() + " mythic tool(s).");
    }

    private MythicTool parse(String id, ConfigurationSection s) {
        String name         = s.getString("name", "&fUnnamed Tool");
        String matStr       = s.getString("material", "NETHERITE_PICKAXE").toUpperCase();
        Material material   = Material.valueOf(matStr);
        int worth           = s.getInt("worth", 0);
        List<String> desc   = s.getStringList("description");
        boolean unbreakable = s.getBoolean("indestructible", true);
        int effLevel        = s.getInt("efficiency-level", 10);
        int effDisplay      = s.getInt("efficiency-display", 26);

        Set<ToolAbility> abilities = new LinkedHashSet<>();
        for (String abilityStr : s.getStringList("abilities")) {
            try {
                abilities.add(ToolAbility.valueOf(abilityStr.toUpperCase()));
            } catch (IllegalArgumentException e) {
                log.warning("Unknown ability '" + abilityStr + "' in tool '" + id + "'");
            }
        }

        return new MythicToolImpl(plugin, nms, id, name, material, worth, desc,
                abilities, unbreakable, effLevel, effDisplay);
    }

    // ── MythicToolsProvider ────────────────────────────────────────────────

    @Override
    public Collection<MythicTool> getTools() {
        return Collections.unmodifiableCollection(tools.values());
    }

    @Override
    public Optional<MythicTool> getTool(String id) {
        return Optional.ofNullable(tools.get(id));
    }

    @Override
    public Optional<MythicTool> getToolFromItem(ItemStack item) {
        if (item == null || !item.hasItemMeta()) return Optional.empty();
        String id = item.getItemMeta().getPersistentDataContainer()
                .get(PDCKeys.TOOL_ID, PersistentDataType.STRING);
        if (id == null) return Optional.empty();
        return Optional.ofNullable(tools.get(id));
    }

    @Override
    public boolean isExpired(ItemStack item) {
        if (item == null || !item.hasItemMeta()) return false;
        Long expiry = item.getItemMeta().getPersistentDataContainer()
                .get(PDCKeys.EXPIRY, PersistentDataType.LONG);
        if (expiry == null) return false;
        return System.currentTimeMillis() > expiry;
    }
}
