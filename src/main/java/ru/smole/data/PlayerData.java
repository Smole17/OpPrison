package ru.smole.data;

import lombok.Data;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import ru.smole.data.group.GroupsManager;

@Data public class PlayerData {

    private String name;
    private double blocks;
    private double money;
    private double token;
    private double multiplier;
    private GroupsManager.Group group;
    private double prestige;
    private boolean fly;

    public PlayerData(String name, double blocks, double money, double token, double multiplier, GroupsManager.Group group, double prestige, boolean fly) {
        this.name = name;
        this.blocks = blocks;
        this.money = money;
        this.token = token;
        this.multiplier = multiplier;
        this.group = group;
        this.prestige = prestige;
        this.fly = fly;
    }

    public Player getPlayer() {
        return Bukkit.getPlayer(name);
    }

    public double addBlocks(double count) {
        double added = blocks + count;
        setBlocks(added);
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
        return added;
    }

    public double addMultiplier(double count) {
        double added = multiplier + count;

        if (added >= 5000)
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
