package ru.smole.data.items.pickaxe;

import lombok.AllArgsConstructor;
import lombok.Getter;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import ru.smole.OpPrison;
import ru.smole.data.PlayerData;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

@AllArgsConstructor public enum Upgrade {

    EFFICIENCY("§7Эффективность", 0,10, 300,  20000, Material.ANVIL),
    FORTUNE("§7Удача", 0,15, 500000, 680, Material.DIAMOND),
    TOKEN_MINER("§eУдача на токены", 0, 100, 5000, 2500000, Material.DOUBLE_PLANT),
    KEY_FINDER("§4Удача на ключи", 2, 0, 50, 1000000000, Material.TRIPWIRE_HOOK),
    EXPLOSIVE("§4Взрыв", 5, 0, 500, 150000000, Material.TNT),
    HASTE("§eСпешка", 1,10, 5, 25000, Material.GLOWSTONE_DUST),
    SPEED("§fСкорость", 1,10, 5, 25000, Material.SUGAR),
    JUMP_BOOST("§aПрыгучесть", 1,10, 5, 25000, Material.SLIME_BALL),
    NIGHT_VISION("§5Ночное зрение", 1, 0, 1, 25000, Material.BROWN_MUSHROOM);

    private @Getter String name;
    private @Getter double need_level_pickaxe;
    private @Getter double start_level;
    private @Getter double max_level;
    private @Getter double start_cost;
    private @Getter Material material;

    public Object[] getMaxUpgrades(PlayerData playerData, double level, int limit) {
        boolean is = false;
        Object[] obj = {0, 0, 0};
        double upgrades = (double) obj[0];
        double tokens = (double) obj[1];
        List<Double> token = new ArrayList<>();

        while (!is) {
            if (upgrades == limit && limit != -1)
                is = true;

            if (playerData.getToken() >= getNeedTokens(level)) {
                upgrades++;
                token.add(getNeedTokens(level));
                tokens = tokens + getNeedTokens(level);
                level++;
                continue;
            }

            is = true;
        }

        obj[0] = upgrades;
        obj[1] = tokens;
        obj[2] = token;

        return obj;
    }

    public double getNeedTokens(double level) {
        if (isMaxLevel(level))
            return 0.0;

        double form = start_cost * 0.05 * level;
        return start_cost + form;
    }

    public boolean isMaxLevel(double level) {
        return level == max_level;
    }
}
