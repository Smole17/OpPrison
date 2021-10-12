package ru.smole.data.player;

import lombok.Getter;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import ru.smole.OpPrison;
import ru.smole.commands.GangCommand;
import ru.smole.commands.HideCommand;
import ru.smole.commands.KitCommand;
import ru.smole.data.group.GroupsManager;
import ru.smole.data.items.pickaxe.Pickaxe;
import ru.smole.data.items.pickaxe.PickaxeManager;
import ru.smole.data.mysql.PlayerDataSQL;
import ru.smole.data.npc.question.Question;
import ru.smole.scoreboard.ScoreboardManager;
import ru.smole.utils.leaderboard.LeaderBoard;
import ru.xfenilafs.core.util.ChatUtil;

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

        HideCommand.hide.forEach(hiders -> {
            if (!hiders.isEmpty())
                hiders.hidePlayer(OpPrison.getInstance(), player);
        });

        PlayerDataSQL.tryLoad(name, pickaxeManager);

        opPlayer.getBoosterManager().load();

        PlayerDataSQL.get(name, resultSet -> {
            try {
                if (!resultSet.next())
                    return;

                double blocks = resultSet.getDouble("blocks");
                double money = resultSet.getDouble("money");
                double token = resultSet.getDouble("token");
                double multiplier = resultSet.getDouble("multiplier");
                GroupsManager.Group group = GroupsManager.Group.valueOf(resultSet.getString("rank"));
                double prestige = resultSet.getDouble("prestige");
                boolean fly = resultSet.getInt("fly") == 1;
                Pickaxe pickaxe = PickaxeManager.getPickaxes().get(name);
                String access = resultSet.getString("access");
                Map<String, Question> questions = getQuestionsFromString(resultSet.getString("questions"));

                playerDataMap.put(
                        name,
                        new PlayerData(
                                name, blocks, money, token, multiplier,
                                group, prestige, fly, getListFromString(access),
                                questions
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

        ScoreboardManager.loadScoreboard(player);
        GangCommand.invitedList.put(name, new ArrayList<>());

        OpPrison.BAR.removeAll();
        Bukkit.getOnlinePlayers().forEach(onPlayer -> OpPrison.BAR.addPlayer(onPlayer));

        LeaderBoard.holograms.forEach(simpleHolographic -> {
            if (!simpleHolographic.getLocation().getWorld().equals(player.getWorld()))
                return;

            simpleHolographic.spawn();
        });

        if (playerDataMap.get(name).isFly()) {
            player.setAllowFlight(true);
            player.setFlying(true);
        }


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
        Map<String, Question> questions = data.getQuestions();

        HideCommand.hide.remove(player);
        PlayerDataSQL.save(
                name, blocks, money, token, multiplier, group, prestige, fly, pickaxe,
                KitCommand.KitsGui.save(name), getStringFromList(access), getStringFromQuestions(questions)
        );
        opPlayer.getBoosterManager().unload();
        pickaxeManager.unload();
        playerDataMap.remove(name);

        OpPrison.BAR.removeAll();
        Bukkit.getOnlinePlayers().forEach(onPlayer -> OpPrison.BAR.addPlayer(onPlayer));
        OpPrison.getInstance().getWorldStatistic().save(player);
    }

    public void updateTop(Player player) {
        String name = player.getName();
        PlayerData data = playerDataMap.get(name);

        PlayerDataSQL.set(name, "blocks", data.getBlocks());
        PlayerDataSQL.set(name, "prestige", data.getPrestige());
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
        }

        return sb.toString();
    }

    protected Map<String, Question> getQuestionsFromString(String str) {
        Map<String, Question> questions = new HashMap<>();

        if (str == null || str.equals("null"))
            return questions;

        for (String s : str.split(",")) {
            if (s == null)
                break;

            String[] args = s.split("\\.");

            if (args.length <= 1)
                break;

            questions.put(args[0], new Question(Question.QuestionStep.valueOf(args[1].toUpperCase())));
        }

        return questions;
    }

    protected String getStringFromQuestions(Map<String, Question> questions) {
        StringBuilder sb = new StringBuilder();
        String format = "%s.%s,";

        int i = 1;
        for (String s : questions.keySet()) {
            Question question = questions.get(s);
            if (i == questions.size())
                format = format.replace(",", "");

            sb.append(String.format(format, s, question.getStep().name().toUpperCase()));
        }

        return sb.toString();
    }
}
