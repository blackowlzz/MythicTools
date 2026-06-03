package eu.blackowlzz.mythictools.util;

import eu.blackowlzz.mythictools.nms.NMSAdapter;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.NamespacedKey;
import org.bukkit.plugin.Plugin;

import java.util.ArrayList;
import java.util.List;

public final class ItemBuilder {

    private final ItemStack item;
    private final ItemMeta meta;
    private final Plugin plugin;
    private final NMSAdapter nms;

    public ItemBuilder(Plugin plugin, NMSAdapter nms, Material material) {
        this.plugin = plugin;
        this.nms = nms;
        this.item = new ItemStack(material);
        this.meta = item.getItemMeta();
    }

    @SuppressWarnings("deprecation")
    public ItemBuilder name(String name) {
        meta.setDisplayName(TextUtil.color(name));
        return this;
    }

    public ItemBuilder lore(List<String> lines) {
        List<String> colored = new ArrayList<>();
        for (String line : lines) colored.add(TextUtil.color(line));
        meta.setLore(colored);
        return this;
    }

    public ItemBuilder unbreakable(boolean value) {
        meta.setUnbreakable(value);
        if (value) meta.addItemFlags(ItemFlag.HIDE_UNBREAKABLE);
        return this;
    }

    public ItemBuilder hideFlags() {
        meta.addItemFlags(ItemFlag.values());
        return this;
    }

    @SuppressWarnings("deprecation")
    public ItemBuilder efficiency(int level) {
        if (level > 0) {
            Enchantment eff = resolveEfficiency();
            if (eff != null) {
                meta.addEnchant(eff, level, true);
                meta.addItemFlags(ItemFlag.HIDE_ENCHANTS);
            }
        }
        return this;
    }

    @SuppressWarnings("deprecation")
    private static Enchantment resolveEfficiency() {
        // Paper 1.21+ uses Registry; older versions expose the static field
        try {
            return (Enchantment) Enchantment.class.getField("EFFICIENCY").get(null);
        } catch (NoSuchFieldException | IllegalAccessException ignored) {}
        // Registry-based fallback (Paper 1.21+)
        try {
            var reg = org.bukkit.Registry.ENCHANTMENT;
            return reg.get(NamespacedKey.minecraft("efficiency"));
        } catch (Exception ignored) {}
        return null;
    }

    public ItemBuilder miningEfficiency(double value) {
        nms.applyMiningEfficiency(meta, item.getType(), value);
        return this;
    }

    /** Applies ToolComponent rules so the item mines wood/dirt at the correct tool speed. */
    public ItemBuilder multitoolRules() {
        nms.applyMultitoolComponent(meta);
        return this;
    }

    public ItemBuilder pdc(NamespacedKey key, String value) {
        meta.getPersistentDataContainer().set(key, PersistentDataType.STRING, value);
        return this;
    }

    public ItemBuilder pdc(NamespacedKey key, long value) {
        meta.getPersistentDataContainer().set(key, PersistentDataType.LONG, value);
        return this;
    }

    public ItemStack build() {
        item.setItemMeta(meta);
        return item;
    }
}
