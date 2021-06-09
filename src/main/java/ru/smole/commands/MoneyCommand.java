package ru.smole.commands;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import ru.smole.OpPrison;
import ru.smole.data.PlayerData;
import ru.smole.player.OpPlayer;
import ru.xfenilafs.core.command.BukkitCommand;

public class MoneyCommand extends BukkitCommand<Player> {

    public MoneyCommand() {
        super("money", "bal", "balance");
    }

    @Override
    protected void onExecute(Player player, String[] args) {
        OpPlayer opPlayer = new OpPlayer(player);
        PlayerData playerData = OpPrison.getInstance().getPlayerDataManager().getPlayerDataMap().get(player.getName());

        if (args.length == 1) {
            Player target = Bukkit.getPlayer(args[0]);
            if (target == null) {
                opPlayer.sendMessage("Игрок не найден");
                return;
            }
            String targetName = target.getName();
            PlayerData targetData = OpPrison.getInstance().getPlayerDataManager().getPlayerDataMap().get(targetName);

            opPlayer.sendMessage(targetName + ": $" + targetData.getMoney());
            return;
        }

        opPlayer.sendMessage("$" + playerData.getMoney());
    }
}
