package ru.smole.commands;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import ru.smole.OpPrison;
import ru.smole.player.OpPlayer;
import ru.xfenilafs.core.command.BukkitCommand;
import ru.xfenilafs.core.util.ChatUtil;

import java.util.ArrayList;
import java.util.List;

public class HideCommand extends BukkitCommand<Player> {
    public HideCommand() {
        super("hide");

        hide = new ArrayList<>();
    }

    public static List<Player> hide;

    @Override
    protected void onExecute(Player player, String[] strings) {
        OpPlayer opPlayer = new OpPlayer(player);
        OpPrison main = OpPrison.getInstance();

        if (hide.contains(player)) {
            hide.remove(player);
            Bukkit.getOnlinePlayers().forEach(players -> player.showPlayer(main, players));
            ChatUtil.sendMessage(player, OpPrison.PREFIX + "Вы скрыли игроков");

            return;
        }

        Bukkit.getOnlinePlayers().forEach(players -> player.hidePlayer(main, players));
        hide.add(player);
        ChatUtil.sendMessage(player, OpPrison.PREFIX + "Вы расскрыли игроков");
    }
}
