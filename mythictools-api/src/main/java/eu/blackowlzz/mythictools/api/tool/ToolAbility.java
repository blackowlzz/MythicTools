package eu.blackowlzz.mythictools.api.tool;

public enum ToolAbility {
    /** Breaks a 3×3 area of blocks at once */
    DRILL_3X3,
    /** Spawns purple amethyst particles when breaking blocks */
    PURPLE_PARTICLES,
    /** Auto-switches tool behaviour: pickaxe / axe / shovel */
    MULTITOOL,
    /** Cascading break — chops the entire connected tree */
    TREE_CHOPPER,
    /** Right-click a container to sell all its contents via economy */
    SELL_CHEST
}
