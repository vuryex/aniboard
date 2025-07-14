package site.anihaven.aniboard.managers;

import site.anihaven.aniboard.Aniboard;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;

import java.util.*;

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
        if (!config.contains("layouts")) {
            setupDefaultLayouts();
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

        if (!config.contains("settings.default-layout")) {
            config.set("settings.default-layout", "default");
        }

        if (!config.contains("settings.allow-layout-switching")) {
            config.set("settings.allow-layout-switching", true);
        }

        if (!config.contains("settings.notify-ops-missing-papi")) {
            config.set("settings.notify-ops-missing-papi", true);
        }

        plugin.saveConfig();
    }

    private void setupDefaultLayouts() {
        Map<String, Object> defaultLayout = new HashMap<>();
        defaultLayout.put("title", "§6§lAni<#FFD700>Board");
        defaultLayout.put("lines", Arrays.asList(
                " ",
                "§7Welcome <#00FFFF>%player_name%</#00FFFF>§7!",
                "§7Rank: %vault_prefix%",
                "§7Balance: §a$%vault_eco_balance_fixed%",
                " ",
                "§7Health: §c%player_health%§7/§c%player_max_health%",
                "§7Level: <#00BFFF>%player_level%</#00BFFF>",
                " ",
                "§7Players Online: §e%server_online%§7/§e%server_max%",
                "§7Server: <#FF69B4>mcserver.com</#FF69B4>",
                " "
        ));

        Map<String, Object> pvpLayout = new HashMap<>();
        pvpLayout.put("title", "§c§lPvP <#FF0000>Arena</#FF0000>");
        pvpLayout.put("lines", Arrays.asList(
                " ",
                "§c§7Player: <#FF6B6B>%player_name%",
                "§7Kills: §a%statistic_player_kills%",
                "§7Deaths: §c%statistic_deaths%",
                " ",
                "§7Health: §c%player_health%§7/§c%player_max_health%",
                "§7Food: §6%player_food%§7/20",
                " ",
                "§7Online: §b%server_online% §7fighters",
                " "
        ));

        config.set("layouts.default", defaultLayout);
        config.set("layouts.pvp", pvpLayout);
    }

    public void reloadConfig() {
        plugin.reloadConfig();
        config = plugin.getConfig();
    }

    public Set<String> getAvailableLayouts() {
        ConfigurationSection layoutsSection = config.getConfigurationSection("layouts");
        return layoutsSection != null ? layoutsSection.getKeys(false) : new HashSet<>();
    }

    public boolean layoutExists(String layoutName) {
        return config.contains("layouts." + layoutName);
    }

    public String getLayoutTitle(String layoutName) {
        return config.getString("layouts." + layoutName + ".title", "§6§lAniBoard");
    }

    public List<String> getLayoutLines(String layoutName) {
        return config.getStringList("layouts." + layoutName + ".lines");
    }

    public String getDefaultLayout() {
        return config.getString("settings.default-layout", "default");
    }

    public boolean isLayoutSwitchingAllowed() {
        return config.getBoolean("settings.allow-layout-switching", true);
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

    public boolean shouldNotifyOpsMissingPAPI() {
        return config.getBoolean("settings.notify-ops-missing-papi", true);
    }

    public void setNotifyOpsMissingPAPI(boolean notify) {
        config.set("settings.notify-ops-missing-papi", notify);
        plugin.saveConfig();
    }

    // Legacy methods for backward compatibility
    public String getScoreboardTitle() {
        return getLayoutTitle(getDefaultLayout());
    }

    public List<String> getScoreboardLines() {
        return getLayoutLines(getDefaultLayout());
    }
}