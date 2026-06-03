package eu.blackowlzz.mythictools.tool.ability;

import eu.blackowlzz.mythictools.api.tool.MythicTool;
import eu.blackowlzz.mythictools.api.tool.ToolAbility;
import eu.blackowlzz.mythictools.nms.NMSAdapter;
import org.bukkit.Material;
import org.bukkit.Particle;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.block.data.BlockData;
import org.bukkit.block.data.type.Leaves;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.*;

/**
 * Cascading tree chopper:
 *  - Breaks all connected logs of the same type (BFS, max 150)
 *  - House protection: aborts if ≥ 6 logs on the same Y level (walls/floors)
 *  - After logs, removes only the tree's natural (non-persistent) leaves
 *  - Slow cascading break with particles (2 ticks/log, 1 tick/leaf)
 */
public class TreeChopperAbilityHandler implements AbilityHandler {

    private static final int MAX_LOGS           = 150;
    private static final int MAX_LOGS_PER_LAYER = 5;   // >5 → probably a wall/house
    private static final int MAX_LEAF_HOPS      = 5;   // BFS hops log→leaf and leaf→leaf
    private static final int MAX_LEAVES         = 400;
    private static final int LOG_BREAK_DELAY    = 2;   // ticks between each log break
    private static final int LEAF_BREAK_DELAY   = 1;   // ticks between each leaf break

    private static final Set<Material> LOG_MATERIALS = new HashSet<>();

    static {
        for (Material m : Material.values()) {
            String n = m.name();
            if (n.endsWith("_LOG") || n.endsWith("_WOOD")) LOG_MATERIALS.add(m);
        }
    }

    private final JavaPlugin plugin;
    private final NMSAdapter nms;

    public TreeChopperAbilityHandler(JavaPlugin plugin, NMSAdapter nms) {
        this.plugin = plugin;
        this.nms = nms;
    }

    @Override
    public ToolAbility getAbility() { return ToolAbility.TREE_CHOPPER; }

    @Override
    public boolean handle(Player player, MythicTool tool, ItemStack item, Event event) {
        if (!(event instanceof BlockBreakEvent bbe)) return false;
        Block origin = bbe.getBlock();
        if (!LOG_MATERIALS.contains(origin.getType())) return false;

        Material logType = origin.getType();
        List<Block> logs = findConnectedLogs(origin, logType);
        if (logs == null) {
            player.sendMessage("§8[§dMythicTools§8] §cLarge structure detected — chopping cancelled.");
            return false;
        }

        Material leafType = getLeafMaterial(logType);
        List<Block> leaves = leafType != null ? findNaturalLeaves(origin, logs, leafType) : Collections.emptyList();

        ItemStack toolCopy = item.clone();

        // Schedule cascading log breaks
        for (int i = 0; i < logs.size(); i++) {
            final Block log = logs.get(i);
            final long delay = (long)(i + 1) * LOG_BREAK_DELAY;
            plugin.getServer().getScheduler().runTaskLater(plugin, () -> {
                if (log.getType() == logType) {
                    spawnChopParticles(log);
                    log.breakNaturally(toolCopy);
                }
            }, delay);
        }

        // Schedule leaf breaks after all logs are done
        long leafStart = (long)(logs.size() + 2) * LOG_BREAK_DELAY;
        for (int i = 0; i < leaves.size(); i++) {
            final Block leaf = leaves.get(i);
            final long delay = leafStart + (long) i * LEAF_BREAK_DELAY;
            plugin.getServer().getScheduler().runTaskLater(plugin, () -> {
                if (isNaturalLeaf(leaf)) {
                    spawnLeafParticles(leaf);
                    leaf.breakNaturally();
                }
            }, delay);
        }

        return true;
    }

    // ── Log BFS ───────────────────────────────────────────────────────────

    private static List<Block> findConnectedLogs(Block origin, Material logType) {
        Map<Integer, Integer> logsPerLayer = new HashMap<>();
        Set<Block> visited = new LinkedHashSet<>();
        Queue<Block> queue = new ArrayDeque<>();

        queue.add(origin);
        visited.add(origin);

        while (!queue.isEmpty() && visited.size() < MAX_LOGS) {
            Block current = queue.poll();
            int y = current.getY();

            int layerCount = logsPerLayer.merge(y, 1, Integer::sum);
            if (layerCount > MAX_LOGS_PER_LAYER) {
                return null; // house / structure detected
            }

            for (int dx = -1; dx <= 1; dx++) {
                for (int dy = -1; dy <= 1; dy++) {
                    for (int dz = -1; dz <= 1; dz++) {
                        if (dx == 0 && dy == 0 && dz == 0) continue;
                        Block n = current.getRelative(dx, dy, dz);
                        if (!visited.contains(n) && n.getType() == logType) {
                            visited.add(n);
                            queue.add(n);
                        }
                    }
                }
            }
        }

        // Return all blocks except origin (origin is broken by the original event)
        List<Block> result = new ArrayList<>(visited);
        result.remove(origin);
        return result;
    }

    // ── Leaf finder ───────────────────────────────────────────────────────

    /**
     * Finds only the leaves that belong to THIS specific tree.
     *
     * Algorithm:
     *  1. Seed phase — collect leaves adjacent (26-neighbors) to any cut log (hop=1).
     *  2. Expansion phase — BFS leaf→leaf using face-only adjacency (6 faces, not diagonals),
     *     stopping at MAX_LEAF_HOPS.
     *
     * Face-only expansion prevents "bridging" through diagonally touching canopies
     * of adjacent trees. The hop limit (5) ensures we never reach a second tree's
     * leaves unless the two trees are literally overlapping.
     */
    private static List<Block> findNaturalLeaves(Block origin, List<Block> logs, Material leafType) {
        Set<Block> allLogs = new HashSet<>(logs);
        allLogs.add(origin);

        // Block → hop distance from nearest log
        Map<Block, Integer> seen = new LinkedHashMap<>();
        Queue<Block> queue = new ArrayDeque<>();

        // Seed: 26-neighbor adjacency to logs (distance = 1)
        for (Block log : allLogs) {
            for (int dx = -1; dx <= 1; dx++) {
                for (int dy = -1; dy <= 1; dy++) {
                    for (int dz = -1; dz <= 1; dz++) {
                        if (dx == 0 && dy == 0 && dz == 0) continue;
                        Block n = log.getRelative(dx, dy, dz);
                        if (n.getType() == leafType && isNaturalLeaf(n) && !seen.containsKey(n)) {
                            seen.put(n, 1);
                            queue.add(n);
                        }
                    }
                }
            }
        }

        // Expand leaf→leaf using only 6 faces to prevent cross-tree bridging
        final int[][] FACES = {{1,0,0},{-1,0,0},{0,1,0},{0,-1,0},{0,0,1},{0,0,-1}};
        while (!queue.isEmpty() && seen.size() < MAX_LEAVES) {
            Block current = queue.poll();
            int hop = seen.get(current);
            if (hop >= MAX_LEAF_HOPS) continue; // don't expand beyond the limit

            for (int[] f : FACES) {
                Block n = current.getRelative(f[0], f[1], f[2]);
                if (n.getType() == leafType && isNaturalLeaf(n) && !seen.containsKey(n)) {
                    seen.put(n, hop + 1);
                    queue.add(n);
                }
            }
        }

        return new ArrayList<>(seen.keySet());
    }

    // ── Utils ─────────────────────────────────────────────────────────────

    private static boolean isNaturalLeaf(Block block) {
        BlockData data = block.getBlockData();
        if (data instanceof Leaves leaves) return !leaves.isPersistent();
        return false;
    }

    private static Material getLeafMaterial(Material logType) {
        String leafName = logType.name()
                .replace("_LOG", "_LEAVES")
                .replace("_WOOD", "_LEAVES");
        try {
            return Material.valueOf(leafName);
        } catch (IllegalArgumentException e) {
            return null;
        }
    }

    // ── Particles ─────────────────────────────────────────────────────────

    private void spawnChopParticles(Block block) {
        World w = block.getWorld();
        var loc = block.getLocation().add(0.5, 0.5, 0.5);
        // Block crack using the log's own material for color
        try {
            w.spawnParticle(Particle.BLOCK_CRACK, loc, 25, 0.3, 0.3, 0.3, 0.05,
                    block.getType().createBlockData());
        } catch (Exception ignored) {}
        // Small smoke puff
        try {
            w.spawnParticle(Particle.SMOKE_NORMAL, loc, 4, 0.15, 0.15, 0.15, 0.01);
        } catch (Exception ignored) {}
    }

    private void spawnLeafParticles(Block block) {
        World w = block.getWorld();
        var loc = block.getLocation().add(0.5, 0.5, 0.5);
        try {
            w.spawnParticle(Particle.BLOCK_CRACK, loc, 8, 0.25, 0.25, 0.25, 0.02,
                    block.getType().createBlockData());
        } catch (Exception ignored) {}
    }
}
