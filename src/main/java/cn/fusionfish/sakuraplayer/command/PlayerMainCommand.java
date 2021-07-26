package cn.fusionfish.sakuraplayer.command;

import cn.fusionfish.sakuraplayer.Main;
import org.bukkit.command.Command;

public class PlayerMainCommand extends SimpleCommand{

    public PlayerMainCommand() {
        super(Main.getInstance(), "player", "p", "pl");
        registerSubCommands();
    }

    @Override
    public void onCommand() {
        getSubCommands().values()
                .stream()
                .map(Command::getUsage)
                .forEach(this::sendMsg);
    }

    private void registerSubCommands() {
        new BindCommand(this);
        new ActivePointCommand(this);
    }
}
