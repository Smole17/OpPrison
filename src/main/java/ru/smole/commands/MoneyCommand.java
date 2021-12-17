package ru.smole.commands;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import ru.smole.OpPrison;
import ru.smole.data.player.PlayerData;
import ru.smole.utils.StringUtils;
import ru.xfenilafs.core.command.BukkitCommand;
import ru.xfenilafs.core.util.ChatUtil;

public class MoneyCommand extends BukkitCommand<Player> {

    public MoneyCommand() {
        super("money", "bal", "balance");
    }

    @Override
    protected void onExecute(Player player, String[] args) {
        PlayerData playerData = OpPrison.getInstance().getPlayerDataManager().getPlayerDataMap().get(player.getName());

        if (args.length == 1) {
            Player target = Bukkit.getPlayer(args[0]);
            if (target == null) {
                ChatUtil.sendMessage(player, OpPrison.PREFIX_N + "Игрок не найден");
                return;
            }
            String targetName = target.getName();
            PlayerData targetData = OpPrison.getInstance().getPlayerDataManager().getPlayerDataMap().get(targetName);

            ChatUtil.sendMessage(player, OpPrison.PREFIX + targetName + ": $" + StringUtils.formatDouble(1, targetData.getMoney()));
            return;
        }

        ChatUtil.sendMessage(player, OpPrison.PREFIX + "$" + StringUtils.formatDouble(1, playerData.getMoney()));
    }
}
