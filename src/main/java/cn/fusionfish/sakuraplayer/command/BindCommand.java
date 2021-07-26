package cn.fusionfish.sakuraplayer.command;

import cn.fusionfish.sakuraplayer.Main;
import cn.fusionfish.sakuraplayer.player.PlayerManager;
import cn.fusionfish.sakuraplayer.player.SakuraPlayer;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import java.util.UUID;

public class BindCommand extends SimpleCommand{

    private final PlayerManager manager = PlayerManager.getInstance();

    public BindCommand(PlayerMainCommand command) {
        super(Main.getInstance(), command, "bind");
        setUsage("/player bind [玩家]");
        setDescription("绑定一名邀请的玩家");
    }

    @Override
    public void onCommand() {

        if (!(sender instanceof Player)) return;

        String name = args[1];
        boolean match = manager.getPlayers()
                .stream()
                .map(SakuraPlayer::getName)
                .anyMatch(name::equalsIgnoreCase);
        if (!match) {
            //找不到该玩家
            sendMsg("找不到该玩家！");
            return;
        }

        UUID uuid = Bukkit.getPlayerUniqueId(name);
        SakuraPlayer player = manager.getPlayer((Player) sender);

        try {
            player.bind(uuid);
            sendMsg("新玩家" + name + "成功与您绑定！");
        } catch (IllegalArgumentException e) {
            sendMsg("绑定玩家失败！");
        }
    }
}
