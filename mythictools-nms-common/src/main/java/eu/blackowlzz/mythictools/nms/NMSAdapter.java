package eu.blackowlzz.mythictools.nms;

import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

/**
 * Version-specific adapter.
 * Implementations live in mythictools-nms-v1_20 and mythictools-nms-v1_21.
 */
public interface NMSAdapter {

    /**
     * Apply a mining-efficiency attribute to the given meta.
     * On 1.20.x this uses a generic attribute modifier; on 1.21+ the new
     * Attribute.MINING_EFFICIENCY API is used.
     *
     * @param meta     the ItemMeta to mutate
     * @param material base material (needed for slot detection on older API)
     * @param value    efficiency bonus value
     */
    void applyMiningEfficiency(ItemMeta meta, Material material, double value);

    /**
     * Spawn amethyst-style particles at the target block.
     * Extracted here because the Particle enum sometimes changes between versions.
     */
    void spawnAmethystParticles(org.bukkit.Location location);

    /**
     * Injects ToolComponent rules into the ItemMeta so that the item behaves as
     * a netherite axe on wood and as a netherite shovel on dirt/gravel/sand,
     * in addition to its default pickaxe behaviour.
     * No-op on servers older than Paper 1.20.5 (ToolComponent unavailable).
     */
    void applyMultitoolComponent(org.bukkit.inventory.meta.ItemMeta meta);

    /**
     * Returns a descriptive string like "+36" for the lore line.
     */
    default String formatEfficiency(double value) {
        return "+" + (int) value;
    }
}
