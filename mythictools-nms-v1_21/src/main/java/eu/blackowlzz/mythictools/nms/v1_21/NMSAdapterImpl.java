package eu.blackowlzz.mythictools.nms.v1_21;

import eu.blackowlzz.mythictools.nms.NMSAdapter;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Particle;
import org.bukkit.Tag;
import org.bukkit.attribute.Attribute;
import org.bukkit.attribute.AttributeModifier;
import org.bukkit.inventory.EquipmentSlotGroup;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.components.ToolComponent;
import org.bukkit.NamespacedKey;

import java.util.ArrayList;
import java.util.List;

/** Adapter for Minecraft 1.21+ (component-based item system). */
public class NMSAdapterImpl implements NMSAdapter {

    // Netherite tool base speed — same value for pickaxe/axe/shovel on their correct blocks
    private static final float NETHERITE_SPEED = 9.0f;

    @Override
    public void applyMiningEfficiency(ItemMeta meta, Material material, double value) {
        try {
            Attribute miningEffAttr = Attribute.valueOf("MINING_EFFICIENCY");
            NamespacedKey key = new NamespacedKey("mythictools", "mining_efficiency");
            AttributeModifier mod = new AttributeModifier(
                    key, value, AttributeModifier.Operation.ADD_NUMBER, EquipmentSlotGroup.MAINHAND);
            meta.addAttributeModifier(miningEffAttr, mod);
        } catch (IllegalArgumentException ignored) {}
    }

    /**
     * Extends the item's ToolComponent so it mines wood at axe speed and
     * dirt/gravel/sand at shovel speed — in addition to its default pickaxe rules.
     * Efficiency enchantments then apply to all three block sets.
     */
    @Override
    public void applyMultitoolComponent(ItemMeta meta) {
        try {
            ToolComponent tool = meta.getTool();
            if (tool == null) return;

            // Axe blocks (logs, planks, fences, bamboo …)
            List<Material> axeBlocks = new ArrayList<>(Tag.MINEABLE_AXE.getValues());
            tool.addRule(axeBlocks, NETHERITE_SPEED, true);

            // Shovel blocks (dirt, gravel, sand, clay, snow, soul sand, mud …)
            List<Material> shovelBlocks = new ArrayList<>(Tag.MINEABLE_SHOVEL.getValues());
            tool.addRule(shovelBlocks, NETHERITE_SPEED, true);

            meta.setTool(tool);
        } catch (Exception ignored) {
            // ToolComponent not available on this build — silently skip
        }
    }

    @Override
    public void spawnAmethystParticles(Location location) {
        if (location.getWorld() == null) return;
        try {
            Particle.DustOptions opts = new Particle.DustOptions(
                    org.bukkit.Color.fromRGB(180, 100, 220), 1.2f);
            location.getWorld().spawnParticle(
                    Particle.DUST, location.clone().add(0.5, 0.5, 0.5),
                    40, 0.5, 0.5, 0.5, 0.0, opts);
        } catch (Exception e) {
            location.getWorld().spawnParticle(
                    Particle.WITCH, location.clone().add(0.5, 0.5, 0.5),
                    30, 0.4, 0.4, 0.4, 0.01);
        }
    }
}
