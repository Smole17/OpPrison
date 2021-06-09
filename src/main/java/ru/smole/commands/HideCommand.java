package ru.smole.commands;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import ru.smole.OpPrison;
import ru.smole.player.OpPlayer;
import ru.xfenilafs.core.command.BukkitCommand;

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
            opPlayer.sendMessage("Вы скрыли игроков");

            return;
        }

        Bukkit.getOnlinePlayers().forEach(players -> player.hidePlayer(main, players));
        opPlayer.sendMessage("Вы расскрыли игроков");
    }
}
