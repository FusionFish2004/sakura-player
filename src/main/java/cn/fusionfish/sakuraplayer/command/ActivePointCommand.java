package cn.fusionfish.sakuraplayer.command;

import cn.fusionfish.sakuraplayer.Main;
import cn.fusionfish.sakuraplayer.player.PlayerManager;
import cn.fusionfish.sakuraplayer.player.SakuraPlayer;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import java.util.UUID;

public class ActivePointCommand extends SimpleCommand{

    private final PlayerManager manager = PlayerManager.getInstance();

    protected ActivePointCommand(PlayerMainCommand command) {
        super(Main.getInstance(), command, "acpt");
        setUsage("/player acpt [玩家]");
        setDescription("查看玩家的活跃点");

        new ActivePointTopCommand(this);
    }

    @Override
    public void onCommand() {
        if (!(sender instanceof Player)) return;

        String name;

        if (args.length == 1) {
            name = sender.getName();
        } else if (args.length == 2) {
            name = args[1];
        } else {
            sendMsg(getUsage());
            return;
        }

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
        SakuraPlayer player = manager.getPlayer(uuid);
        String pts = String.format("%.2f", player.getActivePoints());

        sendMsg(" - " + player.getName() + " - ");
        sendMsg("活跃点数： " + pts + " pts");
    }
}
