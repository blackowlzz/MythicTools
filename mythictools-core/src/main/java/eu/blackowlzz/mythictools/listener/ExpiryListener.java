package eu.blackowlzz.mythictools.listener;

import eu.blackowlzz.mythictools.api.event.MythicToolExpireEvent;
import eu.blackowlzz.mythictools.api.tool.MythicTool;
import eu.blackowlzz.mythictools.tool.MythicToolImpl;
import eu.blackowlzz.mythictools.tool.ToolRegistry;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryOpenEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerItemHeldEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.Optional;

public class ExpiryListener implements Listener {

    private final ToolRegistry registry;
    private final JavaPlugin plugin;
    private final boolean removeExpired;

    public ExpiryListener(ToolRegistry registry, JavaPlugin plugin, boolean removeExpired) {
        this.registry = registry;
        this.plugin = plugin;
        this.removeExpired = removeExpired;
    }

    /** Scan own inventory whenever any inventory is opened (includes pressing E). */
    @EventHandler
    public void onInventoryOpen(InventoryOpenEvent event) {
        if (!(event.getPlayer() instanceof Player player)) return;
        scanPlayerInventory(player);
    }

    /** Check the newly held item immediately when the player switches slots. */
    @EventHandler
    public void onItemHeld(PlayerItemHeldEvent event) {
        Player player = event.getPlayer();
        ItemStack item = player.getInventory().getItem(event.getNewSlot());
        if (item == null) return;
        Optional<MythicTool> optTool = registry.getToolFromItem(item);
        if (optTool.isEmpty()) return;
        if (registry.isExpired(item)) {
            if (removeExpired) expireItem(player, item, event.getNewSlot(), optTool.get());
        } else {
            MythicToolImpl.refreshExpiry(item);
        }
    }

    /** Check on join in case the tool expired while the player was offline. */
    @EventHandler
    public void onJoin(PlayerJoinEvent event) {
        // Delay one tick so the inventory is fully loaded
        plugin.getServer().getScheduler().runTaskLater(plugin,
                () -> scanPlayerInventory(event.getPlayer()), 1L);
    }

    /** Called by the scheduler every N ticks to sweep all online players. */
    public void scanAllOnline() {
        for (Player player : plugin.getServer().getOnlinePlayers()) {
            scanPlayerInventory(player);
        }
    }

    // ── Internals ─────────────────────────────────────────────────────────

    private void scanPlayerInventory(Player player) {
        PlayerInventory inv = player.getInventory();
        for (int i = 0; i < inv.getSize(); i++) {
            ItemStack item = inv.getItem(i);
            if (item == null) continue;

            Optional<MythicTool> optTool = registry.getToolFromItem(item);
            if (optTool.isEmpty()) continue;

            if (registry.isExpired(item)) {
                if (removeExpired) expireItem(player, item, i, optTool.get());
            } else {
                MythicToolImpl.refreshExpiry(item);
            }
        }
    }

    private void expireItem(Player player, ItemStack item, int slot, MythicTool tool) {
        player.getInventory().setItem(slot, null);
        player.sendMessage("§8[§dMythicTools§8] §cYour §d" + tool.getDisplayName()
                + " §chas expired and was removed!");
        MythicToolExpireEvent expireEvent = new MythicToolExpireEvent(player, tool, item.clone());
        plugin.getServer().getPluginManager().callEvent(expireEvent);
    }
}
