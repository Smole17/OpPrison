package ru.smole.data.player;

import lombok.AllArgsConstructor;
import lombok.Data;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import ru.luvas.rmcs.player.RPlayer;
import ru.smole.data.battlepass.BattlePass;
import ru.smole.data.gang.GangData;
import ru.smole.data.group.GroupsManager;
import ru.smole.data.npc.question.Question;
import sexy.kostya.mineos.achievements.Achievement;
import sexy.kostya.mineos.achievements.Achievements;

import java.util.List;
import java.util.Map;

@AllArgsConstructor
@Data
public class PlayerData {

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
    private BattlePass.BattlePassPlayer battlePass;

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

        if (added > 8000)
            return 0.0;

        setMultiplier(added);

        if (multiplier >= 5000) {
            RPlayer.checkAndGet(name).getAchievements().addAchievement(Achievement.OP_MORE_MULTI);
        }

        return added;
    }

    public double addPrestige(double count) {
        double added = prestige + count;
        setPrestige(added);

        Achievements achievements = RPlayer.checkAndGet(name).getAchievements();

        if (prestige >= 15000000) {
            achievements.addAchievement(Achievement.OP_PRESTIGE_15M);
        }

        if (prestige >= 150000000) {
            achievements.addAchievement(Achievement.OP_PRESTIGE_150M);
        }

        if (prestige >= 500000000) {
            achievements.addAchievement(Achievement.OP_PRESTIGE_500M);
        }

        if (prestige >= 1000000000) {
            achievements.addAchievement(Achievement.OP_PRESTIGE_1B);
        }

        return added;
    }
}
