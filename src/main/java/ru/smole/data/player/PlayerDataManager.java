package ru.smole.data.player;

import lombok.Getter;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.player.PlayerEvent;
import org.bukkit.event.player.PlayerLoginEvent;
import ru.luvas.rmcs.player.RPlayer;
import ru.luvas.rmcs.utils.UtilBungee;
import ru.smole.OpPrison;
import ru.smole.commands.GangCommand;
import ru.smole.commands.KitCommand;
import ru.smole.data.battlepass.BattlePass;
import ru.smole.data.group.GroupsManager;
import ru.smole.data.items.Items;
import ru.smole.data.items.pickaxe.Pickaxe;
import ru.smole.data.items.pickaxe.PickaxeManager;
import ru.smole.data.mysql.GangDataSQL;
import ru.smole.data.mysql.PlayerDataSQL;
import ru.smole.data.npc.question.Question;
import ru.smole.scoreboard.ScoreboardManager;
import ru.smole.utils.leaderboard.LeaderBoard;
import ru.smole.utils.server.ServerUtil;
import ru.xfenilafs.core.util.Base64Util;
import ru.xfenilafs.core.util.ChatUtil;
import ru.xfenilafs.core.util.CooldownUtil;
import sexy.kostya.mineos.achievements.Achievement;
import sexy.kostya.mineos.network.server.BungeeClient;

import java.io.IOException;
import java.sql.SQLException;
import java.util.*;
import java.util.concurrent.ThreadLocalRandom;

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
                double multiplier = resultSet.getDouble("multiplier");
                GroupsManager.Group group = GroupsManager.Group.valueOf(resultSet.getString("rank"));
                double prestige = resultSet.getDouble("prestige");
                boolean fly = resultSet.getInt("fly") == 1;
                Pickaxe pickaxe = PickaxeManager.getPickaxes().get(name);
                String access = resultSet.getString("access");
                Map<String, Question> questions = getQuestionsFromString(resultSet.getString("questions"));
                BattlePass.BattlePassPlayer passPlayer = getPassPlayer(resultSet.getString("battlepass"), player);

                playerDataMap.put(
                        name,
                        new PlayerData(
                                name, blocks, money, token, multiplier,
                                group, prestige, fly, getListFromString(access),
                                questions, passPlayer
                        )
                );

                PickaxeManager.getPickaxes().put(name, pickaxe);

                pickaxeManager.load(resultSet.getString("pickaxe"));
                KitCommand.KitsGui.load(name, resultSet.getString("kit"));
            } catch (SQLException | IOException ex) {
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
        double multiplier = data.getMultiplier();
        GroupsManager.Group group = data.getGroup();
        double prestige = data.getPrestige();
        int fly = data.isFly() ? 1 : 0;
        String pickaxe = pickaxeManager.getStats();
        List<String> access = data.getAccess();
        Map<String, Question> questions = data.getQuestions();

        PlayerDataSQL.save(
                name, blocks, money, token, multiplier, group, prestige, fly, pickaxe,
                KitCommand.KitsGui.save(name), getStringFromList(access), getStringFromQuestions(questions),
                savePassPlayer(data)
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
            i++;
        }

        return sb.toString();
    }

    protected BattlePass.BattlePassPlayer getPassPlayer(String str, Player player) throws IOException {
        if (str == null || str.equals("null"))
            return null;

        str = Base64Util.decodeSimple(str);

        BattlePass battlePass = OpPrison.getInstance().getBattlePass();
        String[] data = str.split("-");

        double exp = Double.parseDouble(data[0]);
        BattlePass.BattlePassPlayer.BattlePassLevel passLevel = battlePass.getLevel(Integer.parseInt(data[1]));
        boolean premium = Boolean.parseBoolean(data[2]);
        String[] tasks = data[3].split(";");

        List<BattlePass.BattlePassTask> taskList = new ArrayList<>();
        Arrays.stream(tasks)
                .filter(Objects::nonNull)
                .forEach(s -> {
                    String[] taskData = s.split(":");

                    taskList.add(
                            battlePass.getTask(Integer.parseInt(taskData[0]))
                                    .setComplete(Boolean.parseBoolean(taskData[1]))
                    );
                });

        return new BattlePass.BattlePassPlayer(
                exp,
                passLevel,
                player,
                premium,
                taskList
                );
    }

    protected String savePassPlayer(PlayerData playerData) {
        StringBuilder builder = new StringBuilder();
        String format = "%s-";
        String format1 = "%s;";

        BattlePass.BattlePassPlayer battlePass = playerData.getBattlePass();

        builder.append(String.format(format, battlePass.getExp()));
        builder.append(String.format(format, battlePass.getBattlePassLevel().getLevel()));
        builder.append(String.format(format, battlePass.isPremium()));

        StringBuilder builder1 = new StringBuilder();

        int i = 1;
        for (BattlePass.BattlePassTask task : battlePass.getTasks()) {
            if (i == battlePass.getTasks().size())
                format1 = "%s";

            builder1.append(String.format(format1, task.getI() + ":" + task.isComplete()));
            i++;
        }

        builder.append(String.format(format, builder1));
        return Base64Util.encodeSimple(builder.toString());
    }
}
