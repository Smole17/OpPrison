package ru.smole.commands;

import org.bukkit.entity.Player;
import ru.smole.utils.ServerUtil;
import ru.xfenilafs.core.command.BukkitCommand;
import ru.xfenilafs.core.command.annotation.CommandPermission;

@CommandPermission(permission = "opprison.admin")
public class RestartCommand extends BukkitCommand<Player> {
    public RestartCommand() {
        super("restart");
    }

    @Override
    protected void onExecute(Player player, String[] strings) {
        ServerUtil.restart();
    }
}
