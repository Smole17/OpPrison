package ru.smole.commands;

import org.bukkit.entity.Player;
import ru.smole.OpPrison;
import ru.smole.data.player.PlayerData;
import ru.xfenilafs.core.command.BukkitCommand;
import ru.xfenilafs.core.util.ChatUtil;

public class FlyCommand extends BukkitCommand<Player> {

    protected boolean is;

    public FlyCommand() {
        super("fly");
        is = false;
    }

    @Override
    protected void onExecute(Player player, String[] args) {
        PlayerData playerData = OpPrison.getInstance().getPlayerDataManager().getPlayerDataMap().get(player.getName());

        if (player.getWorld().getName().contains("gangs")) {
            ChatUtil.sendMessage(player, OpPrison.PREFIX + "Вы не можете включить здесь полёт");
            return;
        }

        if (playerData.isFly()) {
            is = !is;
            player.setAllowFlight(is);
            player.setFlying(is);

            ChatUtil.sendMessage(player, OpPrison.PREFIX + "Вы %s полёт", is ? "включили" : "выключили");
            return;
        }

        ChatUtil.sendMessage(player, OpPrison.PREFIX + "У вас нет доступа к полёту");
    }
}
