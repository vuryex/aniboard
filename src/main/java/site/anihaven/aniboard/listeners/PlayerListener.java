package site.anihaven.aniboard.listeners;

import site.anihaven.aniboard.Aniboard;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.scheduler.BukkitRunnable;

public class PlayerListener implements Listener {

    private final Aniboard plugin;

    public PlayerListener(Aniboard plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
        Player player = event.getPlayer();

        new BukkitRunnable() {
            @Override
            public void run() {
                if (plugin.getConfigManager().isAutoEnableOnJoin()) {
                    plugin.getScoreboardManager().addPlayer(player);
                }
            }
        }.runTaskLater(plugin, 20L);
    }

    @EventHandler
    public void onPlayerQuit(PlayerQuitEvent event) {
        Player player = event.getPlayer();
        plugin.getScoreboardManager().removePlayer(player);
    }
}