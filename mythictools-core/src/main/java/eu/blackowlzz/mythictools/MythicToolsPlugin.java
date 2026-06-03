package eu.blackowlzz.mythictools;

import eu.blackowlzz.mythictools.api.MythicToolsAPI;
import eu.blackowlzz.mythictools.command.MythicToolsCommand;
import eu.blackowlzz.mythictools.config.ConfigManager;
import eu.blackowlzz.mythictools.economy.EconomyManager;
import eu.blackowlzz.mythictools.listener.BlockBreakListener;
import eu.blackowlzz.mythictools.listener.ExpiryListener;
import eu.blackowlzz.mythictools.listener.MultiToolListener;
import eu.blackowlzz.mythictools.listener.PlayerInteractListener;
import eu.blackowlzz.mythictools.nms.NMSAdapter;
import eu.blackowlzz.mythictools.nms.NMSAdapterLoader;
import eu.blackowlzz.mythictools.tool.ToolRegistry;
import eu.blackowlzz.mythictools.tool.ability.*;
import eu.blackowlzz.mythictools.util.PDCKeys;
import eu.blackowlzz.mythictools.util.VersionChecker;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.ArrayList;
import java.util.List;

public final class MythicToolsPlugin extends JavaPlugin {

    private NMSAdapter nmsAdapter;
    private ConfigManager configManager;
    private EconomyManager economyManager;
    private ToolRegistry toolRegistry;
    private ExpiryListener expiryListener;
    private VersionChecker versionChecker;

    @Override
    public void onEnable() {
        printBanner();
        getLogger().info("Server: " + getServer().getBukkitVersion()
                + "  Java: " + System.getProperty("java.version"));
        try {
            PDCKeys.init(this);
            getLogger().info("[1/6] PDC keys ready.");

            nmsAdapter = NMSAdapterLoader.load();
            getLogger().info("[2/6] NMS: " + nmsAdapter.getClass().getSimpleName());

            configManager = new ConfigManager(this);
            configManager.load();
            getLogger().info("[3/6] Config loaded.");

            economyManager = new EconomyManager(this);
            economyManager.setup();
            getLogger().info("[4/6] Economy ready — price source: " + economyManager.getPriceSourceName());

            toolRegistry = new ToolRegistry(this, nmsAdapter);
            toolRegistry.load();
            MythicToolsAPI.setProvider(toolRegistry);
            getLogger().info("[5/6] Tools: " + toolRegistry.getTools().size() + " loaded.");

            registerListeners();
            registerCommands();
            startExpiryTask();
            getLogger().info("[6/6] Listeners & commands registered.");

            // Periodic version check
            if (configManager.isUpdateCheckEnabled()) {
                versionChecker = new VersionChecker(this, configManager.getModrinthProjectId());
                versionChecker.start();
            }

            getLogger().info("MythicTools v" + getDescription().getVersion() + " enabled.");

        } catch (Throwable t) {
            getLogger().severe("Failed to enable MythicTools: " + t);
            t.printStackTrace();
            getServer().getPluginManager().disablePlugin(this);
        }
    }

    @Override
    public void onDisable() {
        if (versionChecker != null) versionChecker.stop();
        MythicToolsAPI.clearProvider();
        getLogger().info("MythicTools disabled.");
    }

    // ── Private setup ─────────────────────────────────────────────────────

    private void registerListeners() {
        List<AbilityHandler> handlers = buildHandlers();
        expiryListener = new ExpiryListener(toolRegistry, this, configManager.isRemoveExpiredFromInventory());

        getServer().getPluginManager().registerEvents(new BlockBreakListener(toolRegistry, handlers), this);
        getServer().getPluginManager().registerEvents(new PlayerInteractListener(toolRegistry, handlers), this);
        getServer().getPluginManager().registerEvents(expiryListener, this);
        getServer().getPluginManager().registerEvents(new MultiToolListener(toolRegistry), this);
    }

    private List<AbilityHandler> buildHandlers() {
        List<AbilityHandler> list = new ArrayList<>();
        list.add(new DrillAbilityHandler(nmsAdapter, this));
        list.add(new TreeChopperAbilityHandler(this, nmsAdapter));
        list.add(new MultiToolAbilityHandler());
        list.add(new SellAbilityHandler(economyManager));
        return list;
    }

    private void registerCommands() {
        MythicToolsCommand cmd = new MythicToolsCommand(toolRegistry, this::reload);
        var executor = getCommand("mythictools");
        if (executor != null) {
            executor.setExecutor(cmd);
            executor.setTabCompleter(cmd);
        }
    }

    private void startExpiryTask() {
        long interval = configManager.getExpiryCheckIntervalTicks();
        getServer().getScheduler().runTaskTimer(this,
                () -> { if (expiryListener != null) expiryListener.scanAllOnline(); },
                interval, interval);
    }

    public void reload() {
        configManager.load();
        toolRegistry.load();
        getLogger().info("Reloaded.");
    }

    // ── Getters ───────────────────────────────────────────────────────────

    public NMSAdapter getNmsAdapter()          { return nmsAdapter; }
    public ToolRegistry getToolRegistry()      { return toolRegistry; }
    public ConfigManager getConfigManager()    { return configManager; }
    public EconomyManager getEconomyManager()  { return economyManager; }

    private void printBanner() {
        getLogger().info("  __  ___      __  __  _          ______           __    ");
        getLogger().info(" /  |/  /_  __/ /_/ /_(_)____    /_  __/___  ___  / /____");
        getLogger().info("/ /|_/ / / / / __/ __/ / ___/     / / / __ \\/ _ \\/ / ___/");
        getLogger().info("/ /  / / /_/ / /_/ /_/ / /__      / / / /_/ /  __/ (__  ) ");
        getLogger().info("/_/  /_/\\__, /\\__/\\__/_/\\___/     /_/  \\____/\\___/_/____/  ");
        getLogger().info("       /____/   by blackowlzz  |  GPL-3.0 + Attribution");
    }
}
