package ru.smole.data.player;

import lombok.Data;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import ru.luvas.rmcs.player.RPlayer;
import ru.smole.data.gang.GangData;
import ru.smole.data.group.GroupsManager;
import ru.smole.data.npc.question.Question;
import sexy.kostya.mineos.achievements.Achievement;
import sexy.kostya.mineos.achievements.Achievements;

import java.util.List;
import java.util.Map;

@Data public class PlayerData {

    private String name;
    private double blocks;
    private double money;
    private double token;
    private double multiplier;
    private GroupsManager.Group group;
    private double prestige;
    private boolean fly;
    private List<String> access;
    private Map<String, Question> questions;

    public PlayerData(String name, double blocks, double money, double token, double multiplier,
                      GroupsManager.Group group, double prestige, boolean fly, List<String> access,
                      Map<String, Question> questions) {
        this.name = name;
        this.blocks = blocks;
        this.money = money;
        this.token = token;
        this.multiplier = multiplier;
        this.group = group;
        this.prestige = prestige;
        this.fly = fly;
        this.access = access;
        this.questions = questions;
    }

    public Player getPlayer() {
        return Bukkit.getPlayer(name);
    }

    public double addBlocks(double count) {
        double added = blocks + count;
        setBlocks(added);

        Achievements achievements = RPlayer.checkAndGet(name).getAchievements();

        if (blocks >= 10000)
            achievements.addAchievement(Achievement.OP_NEW_MINER);

        if (blocks >= 100000)
            achievements.addAchievement(Achievement.OP_MIDDLE_MINER);

        if (blocks >= 1000000)
            achievements.addAchievement(Achievement.OP_MASTER_MINER);

        return added;
    }

    public double addMoney(double count) {
        double added = money + count;
        setMoney(added);
        return added;
    }

    public double addToken(double count) {
        double added = token + count;
        setToken(added);

        if (token >= 1000000000000D) {
            Achievements achievements = RPlayer.checkAndGet(name).getAchievements();

            achievements.addAchievement(Achievement.OP_1T_TOKENS);
        }

        return added;
    }

    public double addMultiplier(double count) {
        double added = multiplier + count;

        if (added > 5000)
            return 0.0;

        setMultiplier(added);
        return added;
    }

    public double addPrestige(double count) {
        double added = prestige + count;
        setPrestige(added);
        return added;
    }
}
