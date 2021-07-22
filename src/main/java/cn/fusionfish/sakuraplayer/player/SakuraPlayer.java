package cn.fusionfish.sakuraplayer.player;

import cn.fusionfish.sakuraplayer.Main;
import com.google.common.collect.Sets;
import org.bukkit.Bukkit;
import org.bukkit.configuration.file.FileConfiguration;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Set;
import java.util.UUID;

/**
 * 代表一个服务器玩家的JavaBean
 * @author JeremyHu
 */
public class SakuraPlayer {

    //储存玩家名称
    private final String name;

    //储存玩家UUID
    private final UUID uuid;

    //储存玩家在线总时长
    private long activeTime;

    //储存玩家IP
    private final String ip;

    //储存玩家活跃点数
    private double activePoints;

    //储存今日获得活跃点数
    private double activePointsToday;

    //上次获得点数的日期
    private long lastAddActive;

    //储存绑定的玩家
    private final Set<UUID> bindings = Sets.newLinkedHashSet();



    private transient boolean idle = false;

    public SakuraPlayer(String name, UUID uuid, long activeTime, String ip) {
        this.name = name;
        this.uuid = uuid;
        this.activeTime = activeTime;
        this.ip = ip;
    }

    public String getName() {
        return name;
    }

    public UUID getUuid() {
        return uuid;
    }

    public long getActiveTime() {
        return activeTime;
    }

    public String getIp() {
        return ip;
    }

    public double getActivePoints() {
        return activePoints;
    }

    public Set<UUID> getBindings() {
        return bindings;
    }

    public double getActivePointsToday() {
        return activePointsToday;
    }

    public long getLastAddActive() {
        return lastAddActive;
    }

    public void setActivePoints(int activePoints) {
        this.activePoints = activePoints;
    }

    public void setActivePointsToday(double activePointsToday) {
        this.activePointsToday = activePointsToday;
    }

    public boolean isIdle() {
        return idle;
    }

    public void setIdle(boolean idle) {
        this.idle = idle;
    }

    public void bind(UUID uuid) {
        PlayerManager manager = PlayerManager.getInstance();
        FileConfiguration config = Main.getInstance().getConfig();
        SakuraPlayer player = manager.getPlayer(uuid);

        if (player == null) {
            //被绑定玩家未登陆过服务器
            throw new IllegalArgumentException();
        }

        if (uuid.equals(getUuid())) {
            //被绑定玩家为自己
            throw new IllegalArgumentException();
        }

        if (manager.getPlayers()
                .stream()
                .anyMatch(p -> player.getIp().equals(p.getIp()))) {
            //被绑定玩家的IP已被注册过
            throw new IllegalArgumentException();
        }

        if (player.getActiveTime() > config.getLong("bind.newbie-active-threshold", 86400L)) {
            //被绑定玩家的在线时长超过阈值
            throw new IllegalArgumentException();
        }
        bindings.add(uuid);
    }

    public void bind(String name) {
        bind(Bukkit.getPlayerUniqueId(name));
    }

    /**
     * 增加活跃时长
     * @param period 活跃时长
     */
    public void addOnlineTime(int period) {
        activeTime += period;
    }

    /**
     * 增加活跃点数
     * @param points 活跃度点数
     */
    public void addActivePoints(double points) {
        FileConfiguration config = Main.getInstance().getConfig();

        Date now = new Date();
        Date last = new Date(getLastAddActive());
        SimpleDateFormat fmt = new SimpleDateFormat("yyyyMMdd");
        //是否为同一天
        final boolean isSameDay = fmt.format(now).equals(fmt.format(last));

        if (isIdle()) points *= config.getDouble("idle.reduce-rate", 0.5);

        if (!isSameDay) {
            //不是同一天，归零
            setActivePointsToday(0);
        }
        if ((10D - getActivePointsToday()) >= points) {
            activePoints += points;
            activePointsToday += points;
        } else {
            activePoints += 10D - getActivePointsToday();
            activePointsToday = 10D;
        }

        lastAddActive = now.getTime();
    }
}
