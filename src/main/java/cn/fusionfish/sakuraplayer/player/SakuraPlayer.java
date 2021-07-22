package cn.fusionfish.sakuraplayer.player;

import com.google.common.collect.Sets;
import org.bukkit.Bukkit;

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
    private int activePoints;

    //储存绑定的玩家
    private final Set<UUID> bindings = Sets.newLinkedHashSet();

    private transient boolean idle;

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

    public int getActivePoints() {
        return activePoints;
    }

    public Set<UUID> getBindings() {
        return bindings;
    }

    public void setActivePoints(int activePoints) {
        this.activePoints = activePoints;
    }

    public void setActiveTime(long activeTime) {
        this.activeTime = activeTime;
    }

    public boolean isIdle() {
        return idle;
    }

    public void setIdle(boolean idle) {
        this.idle = idle;
    }

    public void bind(UUID uuid) {
        PlayerManager manager = PlayerManager.getInstance();

        SakuraPlayer player = manager.getPlayer(uuid);

        if (player == null) {
            //被绑定玩家未登陆过服务器
            throw new IllegalArgumentException();
        }

        if (manager.getPlayers()
                .stream()
                .anyMatch(p -> player.getIp().equals(p.getIp()))) {
            //被绑定玩家的IP已被注册过
            throw new IllegalArgumentException();
        }

        if (player.getActiveTime() > 86400000L) {
            //被绑定玩家的在线时长超过24小时
            throw new IllegalArgumentException();
        }
        bindings.add(uuid);
    }

    public void bind(String name) {
        bind(Bukkit.getPlayerUniqueId(name));
    }

    public void addOnlineTime(int period) {
        activeTime += period;
    }
}
