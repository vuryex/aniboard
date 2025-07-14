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
    private BukkitRunnable updateTask;

    public ScoreboardManager(Aniboard plugin) {
        this.plugin = plugin;
        this.enabledPlayers = new HashSet<>();
        startUpdateTask();
    }

    public boolean toggleScoreboard(Player player) {
        UUID uuid = player.getUniqueId();

        if (enabledPlayers.contains(uuid)) {
            enabledPlayers.remove(uuid);
            removeScoreboard(player);
            return false;
        } else {
            enabledPlayers.add(uuid);
            createScoreboard(player);
            return true;
        }
    }

    public void createScoreboard(Player player) {
        if (!enabledPlayers.contains(player.getUniqueId())) {
            return;
        }

        org.bukkit.scoreboard.ScoreboardManager scoreboardManager = Bukkit.getScoreboardManager();
        Scoreboard scoreboard = scoreboardManager.getNewScoreboard();

        Objective objective = scoreboard.registerNewObjective("aniboard", Criteria.DUMMY,
                ColorUtils.colorizeComponent(plugin.getConfigManager().getScoreboardTitle()));

        try {
            objective.numberFormat(io.papermc.paper.scoreboard.numbers.NumberFormat.blank());
        } catch (Exception e) {
            plugin.getLogger().warning("Could not hide scoreboard numbers - requires Minecraft 1.20.3+ client and server");
        }

        objective.setDisplaySlot(DisplaySlot.SIDEBAR);
        updateScoreboardContent(player, objective);
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

        objective.displayName(ColorUtils.colorizeComponent(plugin.getConfigManager().getScoreboardTitle()));

        try {
            objective.numberFormat(io.papermc.paper.scoreboard.numbers.NumberFormat.blank());
        } catch (Exception e) {
            plugin.getLogger().warning("Could not hide scoreboard numbers - requires Minecraft 1.20.3+ client and server");
        }

        for (String entry : scoreboard.getEntries()) {
            scoreboard.resetScores(entry);
        }

        updateScoreboardContent(player, objective);
    }

    private void updateScoreboardContent(Player player, Objective objective) {
        List<String> lines = plugin.getConfigManager().getScoreboardLines();

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

            Score score = objective.getScore(line);
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
        if (plugin.getConfigManager().isEnabledByDefault()) {
            createScoreboard(player);
        }
    }

    public void removePlayer(Player player) {
        enabledPlayers.remove(player.getUniqueId());
    }
}