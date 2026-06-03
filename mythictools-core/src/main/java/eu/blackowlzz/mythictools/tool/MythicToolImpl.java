package eu.blackowlzz.mythictools.tool;

import eu.blackowlzz.mythictools.api.tool.MythicTool;
import eu.blackowlzz.mythictools.api.tool.ToolAbility;
import eu.blackowlzz.mythictools.util.ItemBuilder;
import eu.blackowlzz.mythictools.util.PDCKeys;
import eu.blackowlzz.mythictools.util.TextUtil;
import eu.blackowlzz.mythictools.nms.NMSAdapter;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.plugin.Plugin;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;

public class MythicToolImpl implements MythicTool {

    private final Plugin plugin;
    private final NMSAdapter nms;

    private final String id;
    private final String displayName;
    private final Material material;
    private final int worth;
    private final List<String> description;
    private final Set<ToolAbility> abilities;
    private final boolean indestructible;
    private final int efficiencyLevel;
    // raw numeric efficiency value shown in lore (e.g. 36)
    private final int efficiencyDisplay;

    public MythicToolImpl(Plugin plugin, NMSAdapter nms, String id, String displayName,
                          Material material, int worth, List<String> description,
                          Set<ToolAbility> abilities, boolean indestructible,
                          int efficiencyLevel, int efficiencyDisplay) {
        this.plugin = plugin;
        this.nms = nms;
        this.id = id;
        this.displayName = displayName;
        this.material = material;
        this.worth = worth;
        this.description = Collections.unmodifiableList(description);
        this.abilities = Collections.unmodifiableSet(abilities);
        this.indestructible = indestructible;
        this.efficiencyLevel = efficiencyLevel;
        this.efficiencyDisplay = efficiencyDisplay;
    }

    @Override public String getId() { return id; }
    @Override public String getDisplayName() { return displayName; }
    @Override public Material getMaterial() { return material; }
    @Override public int getWorth() { return worth; }
    @Override public List<String> getDescription() { return description; }
    @Override public Set<ToolAbility> getAbilities() { return abilities; }
    @Override public boolean isIndestructible() { return indestructible; }
    @Override public int getEfficiencyLevel() { return efficiencyLevel; }

    @Override
    public ItemStack createItem() {
        return buildItem(-1L);
    }

    @Override
    public ItemStack createItem(long expiryTimestampMillis) {
        return buildItem(expiryTimestampMillis);
    }

    private ItemStack buildItem(long expiry) {
        List<String> lore = buildLore(expiry);

        ItemBuilder builder = new ItemBuilder(plugin, nms, material)
                .name(displayName)
                .lore(lore)
                .unbreakable(indestructible)
                .efficiency(efficiencyLevel)
                .pdc(PDCKeys.TOOL_ID, id)
                .pdc(PDCKeys.WORTH, (long) worth);

        // Inject pickaxe + axe + shovel rules into the ToolComponent (Paper 1.20.5+)
        if (abilities.contains(eu.blackowlzz.mythictools.api.tool.ToolAbility.MULTITOOL)) {
            builder.multitoolRules();
        }

        if (expiry > 0) {
            builder.pdc(PDCKeys.EXPIRY, expiry);
        }

        if (efficiencyDisplay > 0) {
            builder.miningEfficiency(efficiencyDisplay);
        }

        return builder.build();
    }

    private List<String> buildLore(long expiry) {
        List<String> lore = new ArrayList<>();

        // Worth line
        lore.add(ChatColor.GRAY + "Worth: " + ChatColor.GREEN + "$" + worth);
        lore.add("");

        // Custom description
        for (String line : description) {
            lore.add(TextUtil.color(line));
        }

        // Rarity line
        lore.add("");
        lore.add(ChatColor.DARK_PURPLE + "" + ChatColor.ITALIC + "Mythic Tool");

        // Expiry line
        if (expiry > 0) {
            long remaining = expiry - System.currentTimeMillis();
            String formatted = remaining > 0 ? TextUtil.formatDuration(remaining) : "Expired";
            lore.add(ChatColor.RED + "Expires: " + formatted);
        }

        return lore;
    }

    /**
     * Rebuilds the expiry line in an already-created ItemStack so the countdown
     * stays fresh without re-creating the whole item.
     */
    public static void refreshExpiry(ItemStack item) {
        if (item == null || !item.hasItemMeta()) return;
        var meta = item.getItemMeta();
        Long expiry = meta.getPersistentDataContainer().get(PDCKeys.EXPIRY, PersistentDataType.LONG);
        if (expiry == null) return;

        List<String> lore = meta.getLore();
        if (lore == null) return;

        long remaining = expiry - System.currentTimeMillis();
        String newLine = ChatColor.RED + "Expires: " + (remaining > 0 ? TextUtil.formatDuration(remaining) : "Expired");

        for (int i = 0; i < lore.size(); i++) {
            if (lore.get(i).contains("Expires:")) {
                lore.set(i, newLine);
                meta.setLore(lore);
                item.setItemMeta(meta);
                return;
            }
        }
    }
}
