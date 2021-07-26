package cn.fusionfish.sakuraplayer.player;

import cn.fusionfish.sakuraplayer.Main;
import cn.fusionfish.sakuraplayer.utils.FileUtil;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.gson.Gson;
import org.bukkit.Bukkit;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.Vector;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.*;

/**
 * 玩家信息管理类
 */
public class PlayerManager {

    //储存所有玩家
    private final List<SakuraPlayer> players = Lists.newArrayList();

    public static final File PLAYERS_FOLDER = new File(Main.getInstance().getDataFolder(), "players");

    //获取配置
    private final FileConfiguration config = Main.getInstance().getConfig();

    //更新玩家在线时长的BukkitRunnable
    private final BukkitRunnable onlineTimeUpdater = new BukkitRunnable() {
        @Override
        public void run() {
            Bukkit.getOnlinePlayers()
                    .stream()
                    .map(PlayerManager.this::getPlayer)
                    .filter(player -> !player.isIdle())
                    .forEach(player -> player.addOnlineTime(1));
        }
    };

    //检测玩家挂机的BukkitRunnable
    private final BukkitRunnable idleChecker = new BukkitRunnable() {

        private final Map<Player, Vector> locationBuffer = Maps.newHashMap();

        private final Map<Player, Integer> playerIdleTime = Maps.newHashMap();



        @Override
        public void run() {
            Bukkit.getOnlinePlayers()
                    .forEach(player -> {
                        playerIdleTime.putIfAbsent(player, 0);

                        int idleTime = playerIdleTime.get(player);
                        SakuraPlayer sakuraPlayer = getPlayer(player);

                        if (idleTime > config.getInt("idle.threshold", 3) && !sakuraPlayer.isIdle()) {
                            //标记为挂机状态
                            Main.log("玩家\"" + sakuraPlayer.getName() + "\"进入挂机状态...");
                            sakuraPlayer.setIdle(true);
                        }

                        //获取玩家所在方块坐标
                        Vector vector = player.getLocation().toBlockLocation().toVector();
                        locationBuffer.putIfAbsent(player, vector);

                        Vector buffer = locationBuffer.get(player);
                        if (buffer.equals(vector)) {
                            //如果玩家坐标未发生变化
                            playerIdleTime.merge(player, 1, Integer::sum);
                            return;
                        }

                        //归零挂机时间
                        playerIdleTime.put(player, 0);
                        //存入当前坐标
                        locationBuffer.put(player, vector);

                        if (sakuraPlayer.isIdle()) {
                            Main.log("玩家\"" + sakuraPlayer.getName() + "\"退出挂机状态...");
                            sakuraPlayer.setIdle(false);
                        }
                    });

        }
    };

    //自动保存BukkitRunnable
    private final BukkitRunnable autosaver = new BukkitRunnable() {
        @Override
        public void run() {
            saveAll();
        }
    };

    //给予玩家邀请活跃奖励的BukkitRunnable
    private final BukkitRunnable bindPointsGiver = new BukkitRunnable() {
        @Override
        public void run() {
            Bukkit.getOnlinePlayers()
                    .stream()
                    .map(PlayerManager.this::getPlayer)
                    .filter(player -> !player.isIdle())
                    .forEach(player ->
                        player.getBindings()
                                .stream()
                                .map(PlayerManager.this::getPlayer)
                                .filter(sakuraPlayer -> !sakuraPlayer.isIdle())
                                .forEach(sakuraPlayer ->
                                    player.addActivePoints(config.getDouble("active-points.bind.points", 0.0002D))
                                )
                    );
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
        idleChecker.runTaskTimerAsynchronously(Main.getInstance(), 0L, 20L);

        if (config.getBoolean("auto-save.enable", true)) {
            //自动保存开启
            autosaver.runTaskTimerAsynchronously(Main.getInstance(), 0L, config.getLong("auto-save.period", 6000));
        }

        if (config.getBoolean("active-points.mine.enable", true)) {
            //邀请活动奖励开启
            bindPointsGiver.runTaskTimerAsynchronously(Main.getInstance(), 0L, 20L);
        }
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

    /**
     * 保存玩家数据
     * @param player 玩家
     */
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

    /**
     * 保存玩家数据
     * @param player 玩家
     */
    public static void save(Player player) {
        SakuraPlayer sakuraPlayer = getInstance().getPlayer(player);
        save(sakuraPlayer);
    }

    /**
     * 获取管理器实例
     * @return 管理器实例
     */
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
