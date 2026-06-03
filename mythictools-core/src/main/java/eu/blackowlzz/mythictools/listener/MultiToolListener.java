package eu.blackowlzz.mythictools.listener;

import eu.blackowlzz.mythictools.api.tool.MythicTool;
import eu.blackowlzz.mythictools.api.tool.ToolAbility;
import eu.blackowlzz.mythictools.tool.ToolRegistry;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerItemHeldEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

import java.util.Optional;
import java.util.logging.Logger;

/**
 * Applies a mild Haste effect while the player holds a MULTITOOL.
 *
 * Break-speed correctness is provided by the ToolComponent injected into the
 * item on creation (Paper 1.20.5+): the item is declared as a valid pickaxe,
 * axe AND shovel, so Minecraft applies the right dig speed for every block
 * type automatically — no event hacking needed.
 *
 * Haste II here is just a small quality-of-life bonus; the real speed comes
 * from the Efficiency enchantment + ToolComponent rules on the item itself.
 */
public class MultiToolListener implements Listener {

    // Haste II — a modest bonus on top of Efficiency + ToolComponent
    private static final int HASTE_AMP = 1;

    @SuppressWarnings("deprecation")
    private static final PotionEffectType HASTE = resolveHaste();

    @SuppressWarnings("deprecation")
    private static PotionEffectType resolveHaste() {
        for (String name : new String[]{"HASTE", "FAST_DIGGING"}) {
            try {
                Object o = PotionEffectType.class.getField(name).get(null);
                if (o instanceof PotionEffectType t) return t;
            } catch (Exception ignored) {}
        }
        PotionEffectType byName = PotionEffectType.getByName("HASTE");
        if (byName != null) return byName;
        Logger.getLogger("MythicTools").warning("Could not resolve HASTE PotionEffectType.");
        return null;
    }

    private final ToolRegistry registry;

    public MultiToolListener(ToolRegistry registry) {
        this.registry = registry;
    }

    @EventHandler
    public void onItemHeld(PlayerItemHeldEvent event) {
        Player player = event.getPlayer();
        boolean wasMulti = isMultitool(player.getInventory().getItem(event.getPreviousSlot()));
        boolean isMulti  = isMultitool(player.getInventory().getItem(event.getNewSlot()));
        if (wasMulti && !isMulti) removeHaste(player);
        else if (!wasMulti && isMulti) applyHaste(player);
    }

    @EventHandler
    public void onJoin(PlayerJoinEvent event) {
        if (isMultitool(event.getPlayer().getInventory().getItemInMainHand())) applyHaste(event.getPlayer());
    }

    @EventHandler
    public void onQuit(PlayerQuitEvent event) { removeHaste(event.getPlayer()); }

    // ── Helpers ───────────────────────────────────────────────────────────

    private boolean isMultitool(ItemStack item) {
        if (item == null) return false;
        Optional<MythicTool> tool = registry.getToolFromItem(item);
        return tool.map(t -> t.getAbilities().contains(ToolAbility.MULTITOOL)).orElse(false)
                && !registry.isExpired(item);
    }

    private void applyHaste(Player player) {
        if (HASTE == null) return;
        player.addPotionEffect(new PotionEffect(HASTE, Integer.MAX_VALUE, HASTE_AMP, true, false, true));
    }

    private void removeHaste(Player player) {
        if (HASTE == null) return;
        PotionEffect e = player.getPotionEffect(HASTE);
        if (e != null && e.getAmplifier() == HASTE_AMP) player.removePotionEffect(HASTE);
    }
}
