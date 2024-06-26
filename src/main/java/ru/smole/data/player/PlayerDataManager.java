package ru.smole.data.player;

import lombok.Getter;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import ru.luvas.rmcs.player.RPlayer;
import ru.smole.OpPrison;
import ru.smole.commands.GangCommand;
import ru.smole.commands.KitCommand;
import ru.smole.data.group.GroupsManager;
import ru.smole.data.items.Items;
import ru.smole.data.items.pickaxe.Pickaxe;
import ru.smole.data.items.pickaxe.PickaxeManager;
import ru.smole.data.mysql.PlayerDataSQL;
import ru.smole.utils.leaderboard.LeaderBoard;
import ru.xfenilafs.core.util.ChatUtil;
import sexy.kostya.mineos.achievements.Achievement;

import java.io.IOException;
import java.sql.SQLException;
import java.util.*;

public class PlayerDataManager {

    private final @Getter Map<String, PlayerData> playerDataMap;

    public PlayerDataManager() {
        playerDataMap = new HashMap<>();
    }

    public void load(Player player) {
        OpPlayer opPlayer = new OpPlayer(player);
        PickaxeManager pickaxeManager = opPlayer.getPickaxeManager();
        String name = player.getName();

        PlayerDataSQL.tryLoad(name, pickaxeManager);

        opPlayer.getBoosterManager().load();

        PlayerDataSQL.get(name, resultSet -> {
            try {
                if (!resultSet.next())
                    return;

                double blocks = resultSet.getDouble("blocks");
                double money = resultSet.getDouble("money");
                double token = resultSet.getDouble("token");
                double gems = resultSet.getDouble("gems");
                double multiplier = resultSet.getDouble("multiplier");
                GroupsManager.Group group = GroupsManager.Group.valueOf(resultSet.getString("rank"));
                double prestige = resultSet.getDouble("prestige");
                boolean fly = resultSet.getInt("fly") == 1;
                Pickaxe pickaxe = PickaxeManager.getPickaxes().get(name);
                String access = resultSet.getString("access");

                playerDataMap.put(
                        name,
                        new PlayerData(
                                name, blocks, money, token, gems,
                                multiplier, group, prestige, fly,
                                getListFromString(access)
                        )
                );

                PickaxeManager.getPickaxes().put(name, pickaxe);

                pickaxeManager.load(resultSet.getString("pickaxe"));
                KitCommand.KitsGui.load(name, resultSet.getString("kit"));
            } catch (SQLException ex) {
                ChatUtil.sendMessage(player, "§c§lВаши данные не были загружены, сообщите об этом Smole17#7425 | https://vk.com/smole17");
                player.sendTitle("§c§lВаши данные не были загружены,", "сообщите об этом Smole17#7425 | https://vk.com/smole17", 20, 20, 20);

                Bukkit.getOnlinePlayers().forEach(player1 -> {
                    if (player1.hasPermission("opprison.admin")) {
                        ChatUtil.sendMessage(player, "&c&lCould not load PlayerData with " + name);
                        ChatUtil.sendMessage(player,"&c&lError: &f&o" + ex);
                    }
                });
            }
        });

        OpPrison.getInstance().getScoreboardManager().loadScoreboard(player);
        GangCommand.invitedList.put(name.toLowerCase(), new ArrayList<>());

        OpPrison.BAR.removeAll();
        Bukkit.getOnlinePlayers().forEach(onPlayer -> {
            if (OpPrison.BOOSTER >= 20) {
                RPlayer.checkAndGet(onPlayer.getName()).getAchievements().addAchievement(Achievement.OP_BOOSTER_20);
            }

            OpPrison.BAR.addPlayer(onPlayer);
        });

        LeaderBoard.holograms.forEach(simpleHolographic -> {
            if (!simpleHolographic.getLocation().getWorld().equals(player.getWorld()))
                return;

            simpleHolographic.spawn();
        });

        if (playerDataMap.get(name).isFly()) {
            player.setAllowFlight(true);
            player.setFlying(true);
        }

        Arrays.stream(player.getInventory().getStorageContents())
                .parallel()
                .filter(itemStack1 -> itemStack1 != null && itemStack1.getType() == Material.NETHER_STAR)
                .forEach(itemStack1 -> itemStack1.setAmount(0));


        opPlayer.add(Items.getItem("location_gui"));
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
        double gems = data.getGems();
        double multiplier = data.getMultiplier();
        GroupsManager.Group group = data.getGroup();
        double prestige = data.getPrestige();
        int fly = data.isFly() ? 1 : 0;
        String pickaxe = pickaxeManager.getStats();
        List<String> access = data.getAccess();

        PlayerDataSQL.save(
                name, blocks, money, token, gems,
                multiplier, group, prestige, fly, pickaxe,
                KitCommand.KitsGui.save(name), getStringFromList(access)
        );

        OpPrison.getInstance().getScoreboardManager().unloadScoreboard(player);
        opPlayer.getBoosterManager().unload();
        pickaxeManager.unload();
        playerDataMap.remove(name);

        OpPrison.BAR.removeAll();
        Bukkit.getOnlinePlayers().forEach(onPlayer -> {
            if (OpPrison.BOOSTER >= 20) {
                RPlayer.checkAndGet(onPlayer.getName()).getAchievements().addAchievement(Achievement.OP_BOOSTER_20);
            }

            OpPrison.BAR.addPlayer(onPlayer);
        });

        if (OpPrison.getInstance().getPvPCooldown().getPlayers().contains(player)) {
            player.setHealth(0);
            RPlayer.checkAndGet(name).getAchievements().addAchievement(Achievement.OP_LEAVE_PVP);
        }

        OpPrison.getInstance().getWorldStatistic().save(player);
    }

    public void updateTop(Player player) {
        String name = player.getName();
        PlayerData data = playerDataMap.get(name);

        PlayerDataSQL.set(name, "blocks", "prestige", data.getBlocks(), data.getPrestige());
    }

    protected List<String> getListFromString(String str) {
        if (str == null || str.equals("null"))
            return new ArrayList<>();

        return new ArrayList<>(Arrays.asList(str.split(",")));
    }

    protected String getStringFromList(List<String> list) {
        StringBuilder sb = new StringBuilder();
        String format = "%s,";

        int i = 1;
        for (String s : list) {
            if (i == list.size())
                format = format.replace(",", "");

            sb.append(String.format(format, s));
            i++;
        }

        return sb.toString();
    }
}
