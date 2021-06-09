package ru.smole.data;

import lombok.Data;

@Data public class PlayerData {

    private String name;
    private double blocks;
    private double money;
    private double token;
    private double multiplier;
    private String level;
    private double prestige;

    public PlayerData(String name, double blocks, double money, double token, double multiplier, String level, double prestige) {
        this.name = name;
        this.blocks = blocks;
        this.money = money;
        this.token = token;
        this.multiplier = multiplier;
        this.level = level;
        this.prestige = prestige;
    }
}
