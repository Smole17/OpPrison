package ru.smole.commands;

import org.bukkit.entity.Player;
import ru.smole.rank.RankManager;
import ru.smole.player.OpPlayer;
import ru.xfenilafs.core.command.BukkitCommand;

public class RankUpCommand extends BukkitCommand<Player> {
    public RankUpCommand() {
        super("rankup", "ru", "ranku", "rup");
    }

    @Override
    protected void onExecute(Player player, String[] strings) {
        OpPlayer opPlayer = new OpPlayer(player);
        RankManager rankManager = new RankManager(opPlayer);

        rankManager.up();
    }
}
