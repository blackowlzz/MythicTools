package eu.blackowlzz.mythictools.util;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

/**
 * Checks the Modrinth API for the latest MythicTools version and notifies
 * server operators both in console (on startup) and in-game (on join).
 *
 * Set {@code modrinth-project-id} in config.yml to your Modrinth project ID/slug.
 * Leave blank to disable the check.
 */
public class VersionChecker implements Listener {

    private static final String API_BASE = "https://api.modrinth.com/v2/project/%s/version?loaders=[%%22paper%%22]";
    private static final Pattern VERSION_PATTERN = Pattern.compile("\"version_number\"\\s*:\\s*\"([^\"]+)\"");

    private final JavaPlugin plugin;
    private final String currentVersion;
    private final String projectId;

    private volatile String latestVersion = null;
    private volatile boolean outdated = false;

    public VersionChecker(JavaPlugin plugin, String projectId) {
        this.plugin = plugin;
        this.currentVersion = plugin.getDescription().getVersion();
        this.projectId = projectId;
    }

    public boolean isEnabled() { return projectId != null && !projectId.isBlank(); }

    public void checkAsync() {
        if (!isEnabled()) return;
        plugin.getServer().getScheduler().runTaskAsynchronously(plugin, () -> {
            try {
                String url = String.format(API_BASE, projectId);
                HttpURLConnection conn = (HttpURLConnection) new URL(url).openConnection();
                conn.setRequestMethod("GET");
                conn.setRequestProperty("User-Agent",
                        "MythicTools/" + currentVersion + " (github.com/blackowlzz/MythicTools)");
                conn.setConnectTimeout(5_000);
                conn.setReadTimeout(5_000);

                int code = conn.getResponseCode();
                if (code == 404) {
                    plugin.getLogger().info("[Update] Project not found on Modrinth (ID: " + projectId + ").");
                    return;
                }
                if (code != 200) {
                    plugin.getLogger().warning("[Update] Modrinth returned HTTP " + code + ".");
                    return;
                }

                try (BufferedReader reader = new BufferedReader(new InputStreamReader(conn.getInputStream()))) {
                    String body = reader.lines().collect(Collectors.joining());
                    Matcher m = VERSION_PATTERN.matcher(body);
                    if (!m.find()) { plugin.getLogger().info("[Update] Could not parse Modrinth response."); return; }
                    latestVersion = m.group(1);
                }

                outdated = !currentVersion.equalsIgnoreCase(latestVersion);
                if (outdated) {
                    plugin.getLogger().warning("══════════════════════════════════════════");
                    plugin.getLogger().warning("  MythicTools " + latestVersion + " is available!");
                    plugin.getLogger().warning("  Current: " + currentVersion);
                    plugin.getLogger().warning("  https://modrinth.com/plugin/" + projectId);
                    plugin.getLogger().warning("══════════════════════════════════════════");
                } else {
                    plugin.getLogger().info("[Update] MythicTools is up to date (" + currentVersion + ").");
                }

            } catch (Exception e) {
                plugin.getLogger().warning("[Update] Could not check for updates: " + e.getMessage());
            }
        });
    }

    @EventHandler
    public void onOpJoin(PlayerJoinEvent event) {
        if (!outdated || latestVersion == null) return;
        Player player = event.getPlayer();
        if (!player.hasPermission("mythictools.update-notify")) return;
        // Delay 1 tick so the join message doesn't interfere
        plugin.getServer().getScheduler().runTaskLater(plugin, () ->
                player.sendMessage("§8[§dMythicTools§8] §eUpdate available: §f" + latestVersion
                        + " §e(current: §f" + currentVersion + "§e) — "
                        + "§bmodrinth.com/plugin/" + projectId), 20L);
    }
}
