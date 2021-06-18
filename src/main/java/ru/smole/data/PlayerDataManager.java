package ru.smole.data;

import lombok.Getter;
import org.bukkit.entity.Player;
import ru.smole.OpPrison;
import ru.smole.commands.HideCommand;
import ru.smole.data.items.pickaxe.PickaxeManager;
import ru.smole.data.mysql.PlayerDataSQL;
import ru.smole.data.player.OpPlayer;
import ru.smole.data.rank.Rank;
import ru.smole.scoreboard.ScoreboardManager;

import java.util.HashMap;
import java.util.Map;

public class PlayerDataManager {

    private @Getter Map<String, PlayerData> playerDataMap;

    public PlayerDataManager() {
        playerDataMap = new HashMap<>();
    }

    public void create(Player player) {
        OpPlayer opPlayer = new OpPlayer(player);
        String name = player.getName();

        playerDataMap.put(name, new PlayerData(name, 0, 0, 0 ,0, Rank.A, 0, false));
        PlayerDataSQL.create(name, opPlayer.getPickaxeManager().getStats());
    }

    public void load(Player player) {
        PickaxeManager pickaxeManager = new OpPlayer(player).getPickaxeManager();
        String name = player.getName();

        HideCommand.hide.forEach(hiders -> {
            if (!hiders.isEmpty())
                hiders.hidePlayer(OpPrison.getInstance(), player);
        });

        if (!PlayerDataSQL.playerExists(name)) {
            create(player);
            pickaxeManager.create();
            ScoreboardManager.loadScoreboard(player);

            return;
        }

        double blocks = (double) PlayerDataSQL.get(name, "blocks");
        double money = (double) PlayerDataSQL.get(name, "money");
        double token = (double) PlayerDataSQL.get(name, "token");
        double multiplier = (double) PlayerDataSQL.get(name, "multiplier");
        Rank rank = Rank.valueOf((String) PlayerDataSQL.get(name, "rank"));
        double prestige = (double) PlayerDataSQL.get(name, "prestige");
        boolean fly = ((int) PlayerDataSQL.get(name, "fly")) == 1;

        playerDataMap.put(name, new PlayerData(name, blocks, money, token, multiplier, rank, prestige, fly));
        pickaxeManager.load();
        ScoreboardManager.loadScoreboard(player);
    }

    public void unload(Player player) {
        OpPlayer opPlayer = new OpPlayer(player);
        String name = player.getName();

        if (!playerDataMap.containsKey(name))
            return;

        PlayerData data = playerDataMap.get(name);

        double blocks = data.getBlocks();
        double money = data.getMoney();
        double token = data.getToken();
        double multiplier = data.getMultiplier();
        Rank rank = data.getRank();
        double prestige = data.getPrestige();
        int fly = data.isFly() ? 1 : 0;
        String pickaxe = new OpPlayer(player).getPickaxeManager().getStats();

        HideCommand.hide.remove(player);
        player.kickPlayer("bb");
        opPlayer.getPickaxeManager().unload();
        PlayerDataSQL.save(name, blocks, money, token, multiplier, rank, prestige, fly, pickaxe);
    }
}
