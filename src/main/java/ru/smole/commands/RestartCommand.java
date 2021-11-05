package ru.smole.commands;

import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import ru.smole.utils.server.ServerUtil;
import ru.xfenilafs.core.command.BukkitCommand;
import ru.xfenilafs.core.command.annotation.CommandPermission;

@CommandPermission(permission = "opprison.admin")
public class RestartCommand extends BukkitCommand<CommandSender> {
    public RestartCommand() {
        super("oprestart");
    }

    @Override
    protected void onExecute(CommandSender sender, String[] strings) {
        ServerUtil.restart();
    }
}
