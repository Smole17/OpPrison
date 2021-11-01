package ru.smole.data.pvpcd;

import lombok.Getter;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import ru.smole.OpPrison;
import ru.xfenilafs.core.util.ChatUtil;
import ru.xfenilafs.core.util.Schedules;

import java.util.ArrayList;
import java.util.List;

public class PvPCooldown {

    private final @Getter List<Player> players;

    public PvPCooldown() {
        players = new ArrayList<>();
    }

    public void addPlayer(Player player) {
        removePlayer(player, false);
        players.add(player);
        ChatUtil.sendMessage(player, OpPrison.PREFIX + "Вы вошли в §cPvP§f. Не выходите из игры 5 секунд");

        Schedules.runAsync(
                () -> removePlayer(player, true),
                20 * 5
                );
    }

    public void removePlayer(Player player, boolean message) {
        if (message)
            ChatUtil.sendMessage(player, OpPrison.PREFIX + "Вы вышли из §cPvP");

        players.remove(player);
    }
}
