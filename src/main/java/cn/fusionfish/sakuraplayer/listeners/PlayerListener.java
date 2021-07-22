package cn.fusionfish.sakuraplayer.listeners;

import cn.fusionfish.sakuraplayer.player.PlayerManager;
import cn.fusionfish.sakuraplayer.player.SakuraPlayer;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;

/**
 * 玩家事件监听器
 * @author JeremyHu
 */
public class PlayerListener implements Listener {

    /**
     * 玩家加入事件
     * @param event 事件
     */
    @EventHandler
    public void onJoin(PlayerJoinEvent event) {
        Player player = event.getPlayer();
        PlayerManager.getInstance().join(player);
    }

    /**
     * 玩家退出事件
     * @param event 事件
     */
    @EventHandler
    public void onQuit(PlayerQuitEvent event) {
        final PlayerManager manager = PlayerManager.getInstance();
        SakuraPlayer player = manager.getPlayer(event.getPlayer());
        //保存数据文件
        PlayerManager.save(player);
    }
}
