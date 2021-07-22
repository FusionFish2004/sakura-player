package cn.fusionfish.sakuraplayer.listeners;

import cn.fusionfish.sakuraplayer.Main;
import cn.fusionfish.sakuraplayer.player.PlayerManager;
import cn.fusionfish.sakuraplayer.player.SakuraPlayer;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;

/**
 * 玩家事件监听器
 * @author JeremyHu
 */
public class PlayerListener implements Listener {

    private final PlayerManager manager = PlayerManager.getInstance();
    private final FileConfiguration config = Main.getInstance().getConfig();

    /**
     * 玩家加入事件
     * @param event 事件
     */
    @EventHandler
    public void onJoin(PlayerJoinEvent event) {
        Player player = event.getPlayer();
        manager.join(player);
    }

    /**
     * 玩家退出事件
     * @param event 事件
     */
    @EventHandler
    public void onQuit(PlayerQuitEvent event) {
        Player player = event.getPlayer();
        //保存数据文件
        PlayerManager.save(player);
    }

    /**
     * 玩家挖掘方块事件
     * @param event 事件
     */
    @EventHandler
    public void onBreak(BlockBreakEvent event) {
        SakuraPlayer player = manager.getPlayer(event.getPlayer());
        if (!config.getBoolean("active-points.mine.enable", true)) return;
        player.addActivePoints(config.getDouble("active-points.mine.points", 0.02D));
    }

    /**
     * 玩家放置方块事件
     * @param event 事件
     */
    @EventHandler
    public void onPlace(BlockPlaceEvent event) {
        SakuraPlayer player = manager.getPlayer(event.getPlayer());
        if (!config.getBoolean("active-points.place.enable", true)) return;
        player.addActivePoints(config.getDouble("active-points.place.points", 0.01D));
    }

    @EventHandler
    public void onChat(AsyncPlayerChatEvent event) {
        SakuraPlayer player = manager.getPlayer(event.getPlayer());
        Main.log(String.valueOf(player.isIdle()));
    }




}
