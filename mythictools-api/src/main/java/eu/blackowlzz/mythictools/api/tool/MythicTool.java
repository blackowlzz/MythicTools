package eu.blackowlzz.mythictools.api.tool;

import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;

import java.util.List;
import java.util.Set;

/** Immutable definition of a mythic tool loaded from tools.yml. */
public interface MythicTool {

    /** Internal identifier (e.g. "amethyst_drill"). */
    String getId();

    /** Display name, legacy colour codes accepted (&d, &6, …). */
    String getDisplayName();

    /** Base Minecraft material. */
    Material getMaterial();

    /** Shop worth in currency units. */
    int getWorth();

    /** Lines shown in item lore below the worth. */
    List<String> getDescription();

    /** Abilities attached to this tool. */
    Set<ToolAbility> getAbilities();

    /** Whether the tool is unbreakable. */
    boolean isIndestructible();

    /** Efficiency enchantment level (0 = none). */
    int getEfficiencyLevel();

    /** Create an ItemStack with no expiry. */
    ItemStack createItem();

    /** Create an ItemStack that expires at the given Unix epoch millisecond. */
    ItemStack createItem(long expiryTimestampMillis);
}
