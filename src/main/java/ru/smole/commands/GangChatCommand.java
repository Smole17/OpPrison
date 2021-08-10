package ru.smole.commands;

import lombok.val;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import ru.smole.OpPrison;
import ru.smole.data.gang.GangData;
import ru.smole.data.gang.GangDataManager;
import ru.xfenilafs.core.command.BukkitCommand;
import ru.xfenilafs.core.util.ChatUtil;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

public class GangChatCommand extends BukkitCommand<Player> {
    public GangChatCommand() {
        super("gangchat", "gc");
    }

    @Override
    protected void onExecute(Player player, String[] args) {
        OpPrison main = OpPrison.getInstance();
        String playerName = player.getName();
        GangDataManager gangDataManager = main.getGangDataManager();

        boolean playerHasGang = gangDataManager.playerHasGang(playerName);

        if (!playerHasGang) {
            ChatUtil.sendMessage(player, OpPrison.PREFIX + "Вы не состоите в банде");
            return;
        }

        String message = String.join(" ", args);
        GangData gangData = gangDataManager.getGangFromPlayer(playerName);
        String gangName = gangData.getName();

        GangData.GangPlayer gangPlayer = gangData.getGangPlayer(playerName);

        main.getDiscordBot().sendMessage(
                "gang-" + gangName,
                ChatColor.stripColor(String.format("%s &7%s: &f%s", gangPlayer.getType().getName(), playerName, message).replace("&", "§"))
        );

        gangData.sendMessage(gangPlayer.getType().getName() + " &7" + playerName + ": &f" + message);
    }
}