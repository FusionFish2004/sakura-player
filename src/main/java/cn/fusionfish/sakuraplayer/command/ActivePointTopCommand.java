package cn.fusionfish.sakuraplayer.command;

import cn.fusionfish.sakuraplayer.Main;
import cn.fusionfish.sakuraplayer.player.PlayerManager;
import cn.fusionfish.sakuraplayer.player.SakuraPlayer;

import java.util.Comparator;

public class ActivePointTopCommand extends SimpleCommand{

    private final PlayerManager manager = PlayerManager.getInstance();

    protected ActivePointTopCommand(ActivePointCommand command) {
        super(Main.getInstance(), command, "top");
        setUsage("/player acpt top");
        setDescription("查看全服玩家活跃点数排名");
    }

    @Override
    public void onCommand() {
        sendMsg(" - 全服玩家活跃点数排名 - ");
        manager.getPlayers()
                .stream()
                .sorted(Comparator.comparing(SakuraPlayer::getActivePoints).reversed())
                .map(sakuraPlayer -> {
                    String pts = String.format("%.2f", sakuraPlayer.getActivePoints());
                    return sakuraPlayer.getName() + " - " + pts + " pts";
                })
                .forEach(this::sendMsg);
    }
}
