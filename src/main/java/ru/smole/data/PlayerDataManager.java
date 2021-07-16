package ru.smole.data;

import lombok.Getter;
import org.bukkit.entity.Player;
import ru.smole.OpPrison;
import ru.smole.commands.HideCommand;
import ru.smole.data.group.GroupsManager;
import ru.smole.data.items.Items;
import ru.smole.data.items.pickaxe.Pickaxe;
import ru.smole.data.items.pickaxe.PickaxeManager;
import ru.smole.data.mysql.PlayerDataSQL;
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
        PickaxeManager pickaxeManager = opPlayer.getPickaxeManager();
        String name = player.getName();

        playerDataMap.put(name, new PlayerData(name, 0, 0, 0 ,0, GroupsManager.Group.MANTLE, 0, false));
        pickaxeManager.create();
        PlayerDataSQL.create(name, pickaxeManager.getStats());
    }

    public void load(Player player) {
        OpPlayer opPlayer = new OpPlayer(player);
        PickaxeManager pickaxeManager = opPlayer.getPickaxeManager();
        String name = player.getName();

        HideCommand.hide.forEach(hiders -> {
            if (!hiders.isEmpty())
                hiders.hidePlayer(OpPrison.getInstance(), player);
        });

        if (!PlayerDataSQL.playerExists(name)) {
            create(player);
            opPlayer.set(Items.getItem("pickaxe", name), 1);
            ScoreboardManager.loadScoreboard(player);
            OpPrison.BAR.addPlayer(player);

            return;
        }

        double blocks = (double) PlayerDataSQL.get(name, "blocks");
        double money = (double) PlayerDataSQL.get(name, "money");
        double token = (double) PlayerDataSQL.get(name, "token");
        double multiplier = (double) PlayerDataSQL.get(name, "multiplier");
        GroupsManager.Group group = GroupsManager.Group.valueOf((String) PlayerDataSQL.get(name, "rank"));
        double prestige = (double) PlayerDataSQL.get(name, "prestige");
        boolean fly = ((int) PlayerDataSQL.get(name, "fly")) == 1;
        Pickaxe pickaxe = PickaxeManager.getPickaxes().get(name);

        playerDataMap.put(name, new PlayerData(name, blocks, money, token, multiplier, group, prestige, fly));
        PickaxeManager.getPickaxes().put(name, pickaxe);

        pickaxeManager.load();
        opPlayer.getBoosterManager().load();
        ScoreboardManager.loadScoreboard(player);
        OpPrison.BAR.addPlayer(player);
    }

    public void unload(Player player) {
        String name = player.getName();

        if (!playerDataMap.containsKey(name))
            return;

        PlayerData data = playerDataMap.get(name);
        OpPlayer opPlayer = new OpPlayer(player);
        PickaxeManager pickaxeManager = opPlayer.getPickaxeManager();

        double blocks = data.getBlocks();
        double money = data.getMoney();
        double token = data.getToken();
        double multiplier = data.getMultiplier();
        GroupsManager.Group group = data.getGroup();
        double prestige = data.getPrestige();
        int fly = data.isFly() ? 1 : 0;
        String pickaxe = pickaxeManager.getStats();

        HideCommand.hide.remove(player);
        PlayerDataSQL.save(name, blocks, money, token, multiplier, group, prestige, fly, pickaxe);
        opPlayer.getBoosterManager().unload();
        pickaxeManager.unload();
        playerDataMap.remove(name);
    }

    public void updateTop(Player player) {
        String name = player.getName();
        PlayerData data = playerDataMap.get(name);

        PlayerDataSQL.set(name, "blocks", String.valueOf(data.getBlocks()));
        PlayerDataSQL.set(name, "prestige", String.valueOf(data.getPrestige()));
    }
}
