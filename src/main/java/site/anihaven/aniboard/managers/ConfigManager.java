package site.anihaven.aniboard.managers;

import site.anihaven.aniboard.Aniboard;
import org.bukkit.configuration.file.FileConfiguration;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class ConfigManager {

    private final Aniboard plugin;
    private FileConfiguration config;

    public ConfigManager(Aniboard plugin) {
        this.plugin = plugin;
        loadConfig();
    }

    private void loadConfig() {
        plugin.saveDefaultConfig();
        config = plugin.getConfig();

        setDefaults();
    }

    private void setDefaults() {
        if (!config.contains("scoreboard.title")) {
            config.set("scoreboard.title", "§6§lAni<#FFD700>§lBoard");
        }

        if (!config.contains("scoreboard.lines")) {
            config.set("scoreboard.lines", Arrays.asList(
                    " ",
                    "§7Welcome <#00FFFF>%player_name%§7!",
                    "§7Rank: §e%vault_rank%",
                    " ",
                    "§7Players Online: §b%server_online%",
                    "§7Server: <#FF69B4>mcserver.com",
                    " "
            ));
        }

        if (!config.contains("settings.update-interval")) {
            config.set("settings.update-interval", 1);
        }

        if (!config.contains("settings.enabled-by-default")) {
            config.set("settings.enabled-by-default", true);
        }

        if (!config.contains("settings.auto-enable-on-join")) {
            config.set("settings.auto-enable-on-join", true);
        }

        plugin.saveConfig();
    }

    public void reloadConfig() {
        plugin.reloadConfig();
        config = plugin.getConfig();
    }

    public String getScoreboardTitle() {
        return config.getString("scoreboard.title", "§6§lAni<#FFD700>Board");
    }

    public List<String> getScoreboardLines() {
        List<String> lines = config.getStringList("scoreboard.lines");
        // No more processing for ">" - just return the lines as-is
        return lines;
    }

    public int getUpdateInterval() {
        return config.getInt("settings.update-interval", 1);
    }

    public boolean isEnabledByDefault() {
        return config.getBoolean("settings.enabled-by-default", true);
    }

    public boolean isAutoEnableOnJoin() {
        return config.getBoolean("settings.auto-enable-on-join", true);
    }
}