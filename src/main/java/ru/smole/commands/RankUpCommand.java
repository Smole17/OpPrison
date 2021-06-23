package ru.smole.commands;

import org.bukkit.entity.Player;
import ru.smole.data.OpPlayer;
import ru.xfenilafs.core.command.BukkitCommand;

public class RankUpCommand extends BukkitCommand<Player> {
    public RankUpCommand() {
        super("rankup", "ru", "ranku", "rup");
    }

    @Override
    protected void onExecute(Player player, String[] strings) {
        new OpPlayer(player).getRankManager().up();
    }
}
