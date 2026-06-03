package eu.blackowlzz.mythictools.tool.ability;

import eu.blackowlzz.mythictools.api.tool.MythicTool;
import eu.blackowlzz.mythictools.api.tool.ToolAbility;
import eu.blackowlzz.mythictools.nms.NMSAdapter;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.ArrayList;
import java.util.List;

/**
 * Breaks a 3×3 area around the mined block.
 *
 * The centre block breaks at normal game speed (determined by the item's
 * Efficiency enchantment level).  The surrounding 8 blocks each get:
 *   1. A crack animation ({@code Player#sendBlockDamage}) to show they are
 *      being "drilled".
 *   2. A scheduled {@code breakNaturally} call that fires after the same
 *      number of ticks the block would take to break naturally with this tool.
 *
 * Result: the 3×3 feels like actual mining — not instant teleportation.
 */
public class DrillAbilityHandler implements AbilityHandler {

    // Netherite pickaxe base dig speed (same tier as axe/shovel)
    private static final float NETHERITE_BASE_SPEED = 9.0f;

    private final NMSAdapter nms;
    private final JavaPlugin plugin;

    public DrillAbilityHandler(NMSAdapter nms, JavaPlugin plugin) {
        this.nms = nms;
        this.plugin = plugin;
    }

    @Override
    public ToolAbility getAbility() { return ToolAbility.DRILL_3X3; }

    @Override
    public boolean handle(Player player, MythicTool tool, ItemStack item, Event event) {
        if (!(event instanceof BlockBreakEvent bbe)) return false;
        Block origin = bbe.getBlock();

        List<Block> toBreak = get3x3Blocks(origin, player).stream()
                .filter(b -> !b.equals(origin) && !b.isEmpty() && !b.isLiquid()
                        && b.getType().getHardness() >= 0)
                .toList();

        nms.spawnAmethystParticles(origin.getLocation());
        if (toBreak.isEmpty()) return true;

        // Dig speed mirrors the tool's actual Efficiency level
        int effLevel = tool.getEfficiencyLevel();
        float digSpeed = NETHERITE_BASE_SPEED + (effLevel > 0 ? effLevel * effLevel + 1 : 0);

        ItemStack toolCopy = item.clone();

        for (Block b : toBreak) {
            float hardness = b.getType().getHardness();
            if (hardness < 0) continue;

            // Ticks this block would take to break: hardness * 30 / digSpeed
            int breakTicks = Math.max(2, Math.round(hardness * 30f / digSpeed));

            // Show crack animation one tick before the block breaks
            long crackAt = Math.max(1L, breakTicks - 1L);
            plugin.getServer().getScheduler().runTaskLater(plugin, () -> {
                if (!b.isEmpty() && !b.isLiquid()) sendCrack(player, b, 0.8f);
            }, crackAt);

            // Break the block
            plugin.getServer().getScheduler().runTaskLater(plugin, () -> {
                if (b.isEmpty() || b.isLiquid() || b.getType().getHardness() < 0) return;
                nms.spawnAmethystParticles(b.getLocation());
                b.breakNaturally(toolCopy);
            }, (long) breakTicks);
        }

        return true;
    }

    // ── Helpers ───────────────────────────────────────────────────────────

    /** Sends a block-damage packet (crack texture). Silently no-ops on unsupported versions. */
    private static void sendCrack(Player player, Block block, float progress) {
        try {
            // Paper 1.18+: Player#sendBlockDamage(Block, float)
            player.getClass()
                    .getMethod("sendBlockDamage", Block.class, float.class)
                    .invoke(player, block, progress);
        } catch (Exception ignored) {}
    }

    private static List<Block> get3x3Blocks(Block center, Player player) {
        BlockFace face = getClickedFace(player);
        int[][] offsets = switch (face) {
            case NORTH, SOUTH -> new int[][]{ {-1,-1,0},{0,-1,0},{1,-1,0},{-1,0,0},{0,0,0},{1,0,0},{-1,1,0},{0,1,0},{1,1,0} };
            case EAST,  WEST  -> new int[][]{ {0,-1,-1},{0,-1,0},{0,-1,1},{0,0,-1},{0,0,0},{0,0,1},{0,1,-1},{0,1,0},{0,1,1} };
            default           -> new int[][]{ {-1,0,-1},{0,0,-1},{1,0,-1},{-1,0,0},{0,0,0},{1,0,0},{-1,0,1},{0,0,1},{1,0,1} };
        };
        List<Block> blocks = new ArrayList<>(9);
        for (int[] o : offsets) blocks.add(center.getRelative(o[0], o[1], o[2]));
        return blocks;
    }

    private static BlockFace getClickedFace(Player player) {
        float pitch = player.getLocation().getPitch();
        if (pitch > 45)  return BlockFace.DOWN;
        if (pitch < -45) return BlockFace.UP;
        float yaw = ((player.getLocation().getYaw() % 360) + 360) % 360;
        if (yaw < 45 || yaw >= 315) return BlockFace.SOUTH;
        if (yaw < 135)              return BlockFace.WEST;
        if (yaw < 225)              return BlockFace.NORTH;
        return BlockFace.EAST;
    }
}
