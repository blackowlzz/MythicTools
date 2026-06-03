package eu.blackowlzz.mythictools.util;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.chat.*;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitTask;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.time.Instant;
import java.time.format.DateTimeParseException;

public class VersionChecker {

    private static final String API_URL  = "https://api.modrinth.com/v2/project/%s/version";
    private static final String PAGE_URL = "https://modrinth.com/plugin/%s";

    private final JavaPlugin plugin;
    private final String projectId;
    private final HttpClient http;

    private BukkitTask task;
    private Listener joinListener;
    private volatile String lastNotifiedVersion;

    public VersionChecker(JavaPlugin plugin, String projectId) {
        this.plugin    = plugin;
        this.projectId = projectId;
        this.http      = HttpClient.newHttpClient();
    }

    public void start() {
        if (projectId == null || projectId.isBlank()) return;

        long freqMinutes = plugin.getConfig().getLong("update-check.frequency-minutes", 360L);
        if (freqMinutes <= 0) freqMinutes = 360L;
        long ticks = freqMinutes * 60L * 20L;

        // First check after 1 second, then every freqMinutes
        task = Bukkit.getScheduler().runTaskTimerAsynchronously(plugin, this::check, 20L, ticks);

        // Notify ops that join while an update is pending
        joinListener = new Listener() {
            @EventHandler
            public void onJoin(PlayerJoinEvent event) {
                String latest = lastNotifiedVersion;
                if (latest == null) return;
                if (latest.equals(plugin.getDescription().getVersion())) return;

                Player p = event.getPlayer();
                if (p.isOp() || p.hasPermission("mythictools.update-notify")) {
                    Bukkit.getScheduler().runTaskLater(plugin,
                            () -> sendClickable(p, latest), 20L);
                }
            }
        };
        Bukkit.getPluginManager().registerEvents(joinListener, plugin);
    }

    public void stop() {
        if (task != null)         { task.cancel(); task = null; }
        if (joinListener != null) { HandlerList.unregisterAll(joinListener); joinListener = null; }
    }

    // ── Core check ────────────────────────────────────────────────────────

    private void check() {
        try {
            HttpRequest req = HttpRequest.newBuilder()
                    .uri(URI.create(String.format(API_URL, projectId)))
                    .timeout(Duration.ofSeconds(10))
                    .header("User-Agent", "MythicTools/" + plugin.getDescription().getVersion()
                            + " (github.com/blackowlzz/MythicTools)")
                    .GET().build();

            HttpResponse<String> resp = http.send(req, HttpResponse.BodyHandlers.ofString());

            if (resp.statusCode() == 404) {
                plugin.getLogger().info("[Update] Project not found on Modrinth (id: " + projectId + ").");
                return;
            }
            if (resp.statusCode() != 200) {
                plugin.getLogger().warning("[Update] Modrinth returned HTTP " + resp.statusCode() + ".");
                return;
            }

            // Find the entry with the most recent date_published
            JsonArray versions = JsonParser.parseString(resp.body()).getAsJsonArray();
            JsonObject latest   = null;
            Instant latestDate  = Instant.MIN;

            for (JsonElement el : versions) {
                if (!el.isJsonObject()) continue;
                JsonObject candidate = el.getAsJsonObject();
                if (!candidate.has("version_number")) continue;

                Instant published = Instant.MIN;
                if (candidate.has("date_published")) {
                    try { published = Instant.parse(candidate.get("date_published").getAsString()); }
                    catch (DateTimeParseException ignored) {}
                }
                if (latest == null || published.isAfter(latestDate)) {
                    latest     = candidate;
                    latestDate = published;
                }
            }

            if (latest == null) return;

            String latestVer  = latest.get("version_number").getAsString();
            String currentVer = plugin.getDescription().getVersion();

            if (latestVer.equals(currentVer)) {
                lastNotifiedVersion = null;
                plugin.getLogger().info("[Update] MythicTools is up to date (" + currentVer + ").");
                return;
            }
            if (latestVer.equals(lastNotifiedVersion)) return; // already notified

            lastNotifiedVersion = latestVer;
            String url = String.format(PAGE_URL, projectId);

            // Console warning (plain text)
            plugin.getLogger().warning("===========================================");
            plugin.getLogger().warning("  MythicTools " + latestVer + " is available!");
            plugin.getLogger().warning("  You are on: " + currentVer);
            plugin.getLogger().warning("  " + url);
            plugin.getLogger().warning("===========================================");

            // Notify all online ops / players with permission
            Bukkit.getOnlinePlayers().stream()
                    .filter(p -> p.isOp() || p.hasPermission("mythictools.update-notify"))
                    .forEach(p -> Bukkit.getScheduler().runTask(plugin, () -> sendClickable(p, latestVer)));

        } catch (Exception e) {
            plugin.getLogger().warning("[Update] Check failed: " + e.getMessage());
        }
    }

    // ── Clickable chat component ──────────────────────────────────────────

    private void sendClickable(Player p, String latestVer) {
        String currentVer = plugin.getDescription().getVersion();
        String url        = String.format(PAGE_URL, projectId);

        String body = "§8[§dMythicTools§8] §eNew version §f" + latestVer
                + " §eis available §8(§7current: §f" + currentVer + "§8)§e. Download: ";

        BaseComponent[] bodyParts = TextComponent.fromLegacyText(body);

        TextComponent link = new TextComponent(url);
        link.setColor(ChatColor.AQUA);
        link.setUnderlined(true);
        link.setClickEvent(new ClickEvent(ClickEvent.Action.OPEN_URL, url));
        link.setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT,
                TextComponent.fromLegacyText("§7Click to open Modrinth page")));

        BaseComponent[] full = new BaseComponent[bodyParts.length + 1];
        System.arraycopy(bodyParts, 0, full, 0, bodyParts.length);
        full[bodyParts.length] = link;

        p.spigot().sendMessage(full);
    }
}
