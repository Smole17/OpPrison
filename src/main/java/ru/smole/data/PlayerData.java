package ru.smole.data;

import lombok.Data;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import ru.smole.data.rank.Rank;

@Data public class PlayerData {

    private String name;
    private double blocks;
    private double money;
    private double token;
    private double multiplier;
    private Rank rank;
    private double prestige;
    private boolean fly;

    public PlayerData(String name, double blocks, double money, double token, double multiplier, Rank rank, double prestige, boolean fly) {
        this.name = name;
        this.blocks = blocks;
        this.money = money;
        this.token = token;
        this.multiplier = multiplier;
        this.rank = rank;
        this.prestige = prestige;
        this.fly = fly;
    }

    public Player getPlayer() {
        return Bukkit.getPlayer(name);
    }

    public double addBlocks(double count) {
        double added = blocks + count;
        setBlocks(blocks + count);
        return added;
    }

    public double addMoney(double count) {
        double added = money + count;
        setMoney(added);
        return added;
    }

    public double addToken(double count) {
        double added = token + count;
        setToken(token + count);
        return added;
    }
}
