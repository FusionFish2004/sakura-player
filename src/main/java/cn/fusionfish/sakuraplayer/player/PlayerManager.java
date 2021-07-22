package cn.fusionfish.sakuraplayer.player;

import cn.fusionfish.sakuraplayer.Main;
import cn.fusionfish.sakuraplayer.utils.FileUtil;
import com.google.common.collect.Lists;
import com.google.gson.Gson;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

/**
 * 玩家信息管理类
 */
public class PlayerManager {

    //储存所有玩家
    private final List<SakuraPlayer> players = Lists.newArrayList();

    public static final File PLAYERS_FOLDER = new File(Main.getInstance().getDataFolder(), "players");

    //更新玩家在线时长的BukkitRunnable
    private final BukkitRunnable onlineTimeUpdater = new BukkitRunnable() {
        @Override
        public void run() {
            Bukkit.getOnlinePlayers()
                    .stream()
                    .map(PlayerManager.this::getPlayer)
                    .forEach(player -> player.addOnlineTime(1000));
        }
    };

    public PlayerManager() {
        init();
    }

    /**
     * 获取所有玩家
     * @return 所有玩家
     */
    public List<SakuraPlayer> getPlayers() {
        return players;
    }

    /**
     * 初始化管理器
     */
    private void init() {
        //读取本地文件，放入列表中
        Objects.requireNonNull(FileUtil.getFiles(PLAYERS_FOLDER))
                .stream()
                .map(FileUtil::getJson)
                .map(jsonObject -> new Gson().fromJson(jsonObject, SakuraPlayer.class))
                .forEach(players::add);

        //启动BukkitRunnable
        onlineTimeUpdater.runTaskTimerAsynchronously(Main.getInstance(), 0L, 20L);
    }

    /**
     * 玩家加入时触发
     * @param player 玩家
     */
    public void join(Player player) {

        final String name = player.getName();
        final UUID uuid = player.getUniqueId();
        final String ip = Objects.requireNonNull(player.getAddress()).getHostString();

        final File playerFile = new File(PLAYERS_FOLDER, name + ".json");

        if (playerFile.exists()) {
            //玩家信息存在
            Main.log("\"" + name + ".json\"存在...");
            return;
        }

        SakuraPlayer sakuraPlayer = new SakuraPlayer(name,uuid,0L,ip);
        Main.log("将\"" + name + "\"的信息存入内存...");
        players.add(sakuraPlayer);
        save(sakuraPlayer);
    }

    public static void save(SakuraPlayer player) {

        final String name = player.getName();
        final File playerFile = new File(PLAYERS_FOLDER, name + ".json");

        try {
            if (!playerFile.exists()) {
                try {
                    boolean result = playerFile.createNewFile();
                    if (result) {
                        Main.log("正在创建\"" + name + ".json\"...");
                    } else {
                        throw new RuntimeException();
                    }
                } catch (IOException e) {
                    Main.log("创建\"" + name + ".json\"失败!");
                    e.printStackTrace();
                }
            }

            String json = new Gson().toJson(player);

            Main.log("正在保存\"" + name + ".json\"...");
            Writer write = new OutputStreamWriter(new FileOutputStream(playerFile), StandardCharsets.UTF_8);
            write.write(json);
            write.flush();
            write.close();
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    public static PlayerManager getInstance() {
        return Main.getInstance().getManager();
    }

    /**
     * 按UUID获取玩家
     * @param uuid UUID
     * @return 玩家
     */
    public SakuraPlayer getPlayer(UUID uuid) {
        return players.stream()
                .filter(player -> player.getUuid().equals(uuid))
                .findFirst()
                .orElse(null);
    }

    /**
     * 获取玩家
     * @param player Bukkit玩家类
     * @return 玩家
     */
    public SakuraPlayer getPlayer(Player player) {
        return getPlayer(player.getUniqueId());
    }

    /**
     * 储存所有玩家数据
     */
    public void saveAll() {
        players.forEach(PlayerManager::save);
    }
}
