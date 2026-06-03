package eu.blackowlzz.mythictools.nms.v1_20;

import eu.blackowlzz.mythictools.nms.NMSAdapter;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Particle;
import org.bukkit.attribute.Attribute;
import org.bukkit.attribute.AttributeModifier;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.UUID;

/** Adapter for Minecraft 1.19 – 1.20.4 (pre-component API). */
public class NMSAdapterImpl implements NMSAdapter {

    private static final UUID EFFICIENCY_UUID = UUID.fromString("f0a5dce5-1337-4444-8888-abcdef123456");

    @Override
    public void applyMultitoolComponent(org.bukkit.inventory.meta.ItemMeta meta) {
        // ToolComponent requires Paper 1.20.5+ — no-op on 1.19–1.20.4.
        // MultiToolListener applies a Haste-based fallback on these versions.
    }

    @Override
    @SuppressWarnings("deprecation")
    public void applyMiningEfficiency(ItemMeta meta, Material material, double value) {
        // Use GENERIC_ATTACK_SPEED slot as HAND modifier; mining speed is approximated
        // through the enchantment path in older versions — this method is called only
        // when the server does NOT yet support Attribute.MINING_EFFICIENCY (< 1.21).
        // We leave the meta as-is; the efficiency is applied via enchantment in ItemBuilder.
    }

    @Override
    public void spawnAmethystParticles(Location location) {
        if (location.getWorld() == null) return;
        location.getWorld().spawnParticle(
                Particle.WITCH,
                location.clone().add(0.5, 0.5, 0.5),
                30,
                0.4, 0.4, 0.4,
                0.01
        );
        location.getWorld().spawnParticle(
                Particle.ENCHANTED_HIT,
                location.clone().add(0.5, 0.5, 0.5),
                10,
                0.3, 0.3, 0.3,
                0.0
        );
    }
}
