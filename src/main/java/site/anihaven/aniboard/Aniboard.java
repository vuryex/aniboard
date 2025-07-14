package site.anihaven.aniboard;

import site.anihaven.aniboard.commands.AniBoardCommand;
import site.anihaven.aniboard.listeners.PlayerListener;
import site.anihaven.aniboard.managers.ConfigManager;
import site.anihaven.aniboard.managers.ScoreboardManager;
import org.bukkit.Bukkit;
import org.bukkit.plugin.java.JavaPlugin;

public class Aniboard extends JavaPlugin {

    private static Aniboard instance;
    private ConfigManager configManager;
    private ScoreboardManager scoreboardManager;

    @Override
    public void onEnable() {
        instance = this;

        configManager = new ConfigManager(this);
        scoreboardManager = new ScoreboardManager(this);

        registerCommands();
        registerListeners();

        if (Bukkit.getPluginManager().isPluginEnabled("PlaceholderAPI")) {
            getLogger().info("PlaceholderAPI found! Placeholder support enabled.");
        } else {
            getLogger().warning("PlaceholderAPI not found! Some features may not work properly.");
        }

        getLogger().info("AniBoard has been enabled!");
    }

    @Override
    public void onDisable() {
        if (scoreboardManager != null) {
            scoreboardManager.removeAllScoreboards();
        }
        getLogger().info("AniBoard has been disabled!");
    }

    private void registerCommands() {
        getCommand("aniboard").setExecutor(new AniBoardCommand(this));
    }

    private void registerListeners() {
        Bukkit.getPluginManager().registerEvents(new PlayerListener(this), this);
    }

    public static Aniboard getInstance() {
        return instance;
    }

    public ConfigManager getConfigManager() {
        return configManager;
    }

    public ScoreboardManager getScoreboardManager() {
        return scoreboardManager;
    }

    public void reload() {
        configManager.reloadConfig();
        scoreboardManager.updateAllScoreboards();
        getLogger().info("AniBoard configuration reloaded!");
    }
}