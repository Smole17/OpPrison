package ru.smole.data;

import lombok.Data;
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
}
