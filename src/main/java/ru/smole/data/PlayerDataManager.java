package ru.smole.data;

import lombok.Getter;
import org.bukkit.entity.Player;
import ru.smole.commands.HideCommand;
import ru.smole.data.mysql.PlayerDataSQL;
import ru.smole.scoreboard.ScoreboardManager;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class PlayerDataManager {

    private @Getter Map<String, PlayerData> playerDataMap;

    public PlayerDataManager() {
        playerDataMap = new HashMap<>();
    }

    public void create(String name) {
        playerDataMap.put(name, new PlayerData(name, 0, 0, 0 ,0, "A", 0));
        PlayerDataSQL.create(name);
    }

    public void load(Player player) {
        String name = player.getName();

        if (!PlayerDataSQL.playerExists(name)) {
            create(name);
            ScoreboardManager.loadScoreboard(player);

            return;
        }

        double blocks = (double) PlayerDataSQL.get(name, "blocks");
        double money = (double) PlayerDataSQL.get(name, "money");
        double token = (double) PlayerDataSQL.get(name, "token");
        double multiplier = (double) PlayerDataSQL.get(name, "multiplier");
        String level = (String) PlayerDataSQL.get(name, "level");
        double prestige = (double) PlayerDataSQL.get(name, "prestige");

        playerDataMap.put(name, new PlayerData(name, blocks, money, token, multiplier, level, prestige));
        ScoreboardManager.loadScoreboard(player);
    }

    public void unload(Player player) {
        String name = player.getName();

        if (!playerDataMap.containsKey(name))
            return;

        PlayerData data = playerDataMap.get(name);

        double blocks = data.getBlocks();
        double money = data.getMoney();
        double token = data.getToken();
        double multiplier = data.getMultiplier();
        String level = data.getLevel();
        double prestige = data.getPrestige();

        player.kickPlayer("bb");
        HideCommand.hide.remove(player);
        PlayerDataSQL.save(name, blocks, money, token, multiplier, level, prestige);
    }
}
