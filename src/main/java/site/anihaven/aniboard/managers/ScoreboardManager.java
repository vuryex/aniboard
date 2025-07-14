package site.anihaven.aniboard.managers;

import site.anihaven.aniboard.Aniboard;
import site.anihaven.aniboard.utils.ColorUtils;
import site.anihaven.aniboard.utils.PlaceholderUtils;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scoreboard.*;

import java.util.*;

public class ScoreboardManager {

    private final Aniboard plugin;
    private final Set<UUID> enabledPlayers;
    private final Map<UUID, String> playerLayouts;
    private BukkitRunnable updateTask;

    public ScoreboardManager(Aniboard plugin) {
        this.plugin = plugin;
        this.enabledPlayers = new HashSet<>();
        this.playerLayouts = new HashMap<>();
        startUpdateTask();
    }

    public boolean toggleScoreboard(Player player) {
        return toggleScoreboard(player, null);
    }

    public boolean toggleScoreboard(Player player, String layoutName) {
        UUID uuid = player.getUniqueId();

        if (enabledPlayers.contains(uuid)) {
            if (layoutName != null && plugin.getConfigManager().layoutExists(layoutName)) {
                playerLayouts.put(uuid, layoutName);
                updateScoreboard(player);
                return true;
            } else {
                enabledPlayers.remove(uuid);
                playerLayouts.remove(uuid);
                removeScoreboard(player);
                return false;
            }
        } else {
            enabledPlayers.add(uuid);
            String layout = layoutName != null && plugin.getConfigManager().layoutExists(layoutName)
                    ? layoutName : plugin.getConfigManager().getDefaultLayout();
            playerLayouts.put(uuid, layout);
            createScoreboard(player);
            return true;
        }
    }

    public boolean setPlayerLayout(Player player, String layoutName) {
        if (!plugin.getConfigManager().layoutExists(layoutName)) {
            return false;
        }

        UUID uuid = player.getUniqueId();
        playerLayouts.put(uuid, layoutName);

        if (enabledPlayers.contains(uuid)) {
            updateScoreboard(player);
        }

        return true;
    }

    public String getPlayerLayout(Player player) {
        return playerLayouts.getOrDefault(player.getUniqueId(), plugin.getConfigManager().getDefaultLayout());
    }

    public void createScoreboard(Player player) {
        if (!enabledPlayers.contains(player.getUniqueId())) {
            return;
        }

        String layoutName = getPlayerLayout(player);

        org.bukkit.scoreboard.ScoreboardManager scoreboardManager = Bukkit.getScoreboardManager();
        Scoreboard scoreboard = scoreboardManager.getNewScoreboard();

        Objective objective = scoreboard.registerNewObjective("aniboard", Criteria.DUMMY,
                ColorUtils.colorizeComponent(plugin.getConfigManager().getLayoutTitle(layoutName)));

        objective.setDisplaySlot(DisplaySlot.SIDEBAR);
        updateScoreboardContent(player, objective, layoutName);
        player.setScoreboard(scoreboard);
    }

    public void removeScoreboard(Player player) {
        player.setScoreboard(Bukkit.getScoreboardManager().getMainScoreboard());
    }

    public void removeAllScoreboards() {
        for (Player player : Bukkit.getOnlinePlayers()) {
            if (enabledPlayers.contains(player.getUniqueId())) {
                removeScoreboard(player);
            }
        }

        if (updateTask != null) {
            updateTask.cancel();
        }
    }

    public void updateAllScoreboards() {
        for (Player player : Bukkit.getOnlinePlayers()) {
            if (enabledPlayers.contains(player.getUniqueId())) {
                updateScoreboard(player);
            }
        }
    }

    public void updateScoreboard(Player player) {
        if (!enabledPlayers.contains(player.getUniqueId())) {
            return;
        }

        String layoutName = getPlayerLayout(player);

        Scoreboard scoreboard = player.getScoreboard();
        if (scoreboard == null || scoreboard.equals(Bukkit.getScoreboardManager().getMainScoreboard())) {
            createScoreboard(player);
            return;
        }

        Objective objective = scoreboard.getObjective("aniboard");
        if (objective == null) {
            createScoreboard(player);
            return;
        }

        objective.displayName(ColorUtils.colorizeComponent(plugin.getConfigManager().getLayoutTitle(layoutName)));

        for (String entry : scoreboard.getEntries()) {
            scoreboard.resetScores(entry);
        }

        updateScoreboardContent(player, objective, layoutName);
    }

    private void updateScoreboardContent(Player player, Objective objective, String layoutName) {
        List<String> lines = plugin.getConfigManager().getLayoutLines(layoutName);

        for (String entry : objective.getScoreboard().getEntries()) {
            objective.getScoreboard().resetScores(entry);
        }

        lines.removeIf(Objects::isNull);

        for (int i = 0; i < lines.size() && i < 15; i++) {
            String line = lines.get(i);
            line = PlaceholderUtils.setPlaceholders(player, line);
            line = ColorUtils.colorize(line);

            if (line.trim().isEmpty()) {
                line = " ";
            }

            String uniqueLine = ensureUniqueLine(objective, line);

            Score score = objective.getScore(uniqueLine);
            score.setScore(lines.size() - i);
        }
    }

    private String ensureUniqueLine(Objective objective, String line) {
        String originalLine = line;
        int attempt = 0;
        while (objective.getScoreboard().getEntries().contains(line)) {
            line = originalLine + "§" + attempt + "§r";
            attempt++;
        }
        return line;
    }

    private void startUpdateTask() {
        int updateInterval = plugin.getConfigManager().getUpdateInterval();

        updateTask = new BukkitRunnable() {
            @Override
            public void run() {
                updateAllScoreboards();
            }
        };

        updateTask.runTaskTimer(plugin, 0L, updateInterval * 20L);
    }

    public void addPlayer(Player player) {
        enabledPlayers.add(player.getUniqueId());
        if (!playerLayouts.containsKey(player.getUniqueId())) {
            playerLayouts.put(player.getUniqueId(), plugin.getConfigManager().getDefaultLayout());
        }
        if (plugin.getConfigManager().isEnabledByDefault()) {
            createScoreboard(player);
        }

        // Check for PlaceholderAPI and notify ops
        if (!Bukkit.getPluginManager().isPluginEnabled("PlaceholderAPI")) {
            notifyOpAboutMissingPAPI(player);
        }
    }

    private void notifyOpAboutMissingPAPI(Player player) {
        if (!player.isOp() || !plugin.getConfigManager().shouldNotifyOpsMissingPAPI()) {
            return;
        }

        new BukkitRunnable() {
            @Override
            public void run() {
                String message = ColorUtils.colorize(
                        "§e[AniBoard] §7It seems like you do not have PlaceholderAPI installed. " +
                                "§7We recommend downloading the PlaceholderAPI plugin to make full use of our features! " +
                                "§7To ignore this message, execute '§e/aniboard notify false§7'"
                );
                player.sendMessage(message);
            }
        }.runTaskLater(plugin, 60L);
    }

    public void removePlayer(Player player) {
        enabledPlayers.remove(player.getUniqueId());
        playerLayouts.remove(player.getUniqueId());
    }
}