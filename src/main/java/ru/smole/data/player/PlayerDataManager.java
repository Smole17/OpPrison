package ru.smole.data.player;

import lombok.Getter;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import ru.smole.OpPrison;
import ru.smole.commands.HideCommand;
import ru.smole.commands.KitCommand;
import ru.smole.data.group.GroupsManager;
import ru.smole.data.items.Items;
import ru.smole.data.items.pickaxe.Pickaxe;
import ru.smole.data.items.pickaxe.PickaxeManager;
import ru.smole.data.mysql.PlayerDataSQL;
import ru.smole.data.player.OpPlayer;
import ru.smole.data.player.PlayerData;
import ru.smole.scoreboard.ScoreboardManager;
import ru.smole.utils.leaderboard.LeaderBoard;

import java.util.*;

public class PlayerDataManager {

    private @Getter Map<String, PlayerData> playerDataMap;

    public PlayerDataManager() {
        playerDataMap = new HashMap<>();
    }

    public void create(Player player) {
        OpPlayer opPlayer = new OpPlayer(player);
        PickaxeManager pickaxeManager = opPlayer.getPickaxeManager();
        String name = player.getName();

        PlayerDataSQL.load(name, pickaxeManager, playerName -> {
            List<String> access = new ArrayList<>();
            playerDataMap.put(name, new PlayerData(name, 0, 0, 0 ,0, GroupsManager.Group.MANTLE, 0, false, access));

            opPlayer.set(Items.getItem("pickaxe", name), 1);
            ScoreboardManager.loadScoreboard(player);
            OpPrison.BAR.removeAll();
            Bukkit.getOnlinePlayers().forEach(onPlayer -> OpPrison.BAR.addPlayer(player));
        });

        opPlayer.getBoosterManager().load();
    }

    public void load(Player player) {
        OpPlayer opPlayer = new OpPlayer(player);
        PickaxeManager pickaxeManager = opPlayer.getPickaxeManager();
        String name = player.getName();

        HideCommand.hide.forEach(hiders -> {
            if (!hiders.isEmpty())
                hiders.hidePlayer(OpPrison.getInstance(), player);
        });

        create(player);

        double blocks = (double) PlayerDataSQL.get(name, "blocks");
        double money = (double) PlayerDataSQL.get(name, "money");
        double token = (double) PlayerDataSQL.get(name, "token");
        double multiplier = (double) PlayerDataSQL.get(name, "multiplier");
        GroupsManager.Group group = GroupsManager.Group.valueOf((String) PlayerDataSQL.get(name, "rank"));
        double prestige = (double) PlayerDataSQL.get(name, "prestige");
        boolean fly = ((int) PlayerDataSQL.get(name, "fly")) == 1;
        Pickaxe pickaxe = PickaxeManager.getPickaxes().get(name);
        String access = (String) PlayerDataSQL.get(name, "access");

        playerDataMap.put(name, new PlayerData(name, blocks, money, token, multiplier, group, prestige, fly, getList(access)));
        PickaxeManager.getPickaxes().put(name, pickaxe);

        pickaxeManager.load();
        opPlayer.getBoosterManager().load();
        KitCommand.KitsGui.load(name);
        ScoreboardManager.loadScoreboard(player);

        OpPrison.BAR.removeAll();
        Bukkit.getOnlinePlayers().forEach(onPlayer -> OpPrison.BAR.addPlayer(player));
        LeaderBoard.holograms.forEach(simpleHolographic -> {
            simpleHolographic.addReceivers(player);
            simpleHolographic.addViewers(player);
            simpleHolographic.remove();
            simpleHolographic.spawn();
        });
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
        List<String> access = data.getAccess();

        HideCommand.hide.remove(player);
        PlayerDataSQL.save(name, blocks, money, token, multiplier, group, prestige, fly, pickaxe, KitCommand.KitsGui.save(name), getString(access));
        opPlayer.getBoosterManager().unload();
        pickaxeManager.unload();
        playerDataMap.remove(name);

        OpPrison.BAR.removeAll();
        Bukkit.getOnlinePlayers().forEach(onPlayer -> OpPrison.BAR.addPlayer(player));
    }

    public void updateTop(Player player) {
        String name = player.getName();
        PlayerData data = playerDataMap.get(name);

        PlayerDataSQL.set(name, "blocks", data.getBlocks());
        PlayerDataSQL.set(name, "prestige", data.getPrestige());
    }

    protected List<String> getList(String str) {
        return new ArrayList<>(Arrays.asList(str.split(",")));
    }

    protected String getString(List<String> list) {
        StringBuilder sb = new StringBuilder();
        String format = "%s,";

        int i = 1;
        for (String s : list) {
            if (i == list.size())
                format = format.replace(",", "");

            sb.append(String.format(format, s));
        }

        return sb.toString();
    }
}
