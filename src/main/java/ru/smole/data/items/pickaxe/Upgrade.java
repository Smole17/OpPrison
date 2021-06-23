package ru.smole.data.items.pickaxe;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import ru.smole.OpPrison;
import ru.smole.data.PlayerData;
import ru.xfenilafs.core.util.ChatUtil;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

@AllArgsConstructor public enum Upgrade {

    EFFICIENCY("§7Эффективность", 0,10, 300,  20000, Material.ANVIL, false),
    FORTUNE("§7Шахтёр", 0,15, 500000, 680, Material.DIAMOND, false),
    TOKEN_MINER("§eДобыча токенов", 0, 100, 15000, 2500000, Material.DOUBLE_PLANT, false),
    HASTE("§eСпешка", 1,0, 5, 25000, Material.GLOWSTONE_DUST, false),
    SPEED("§fСкорость", 1,0, 5, 25000, Material.SUGAR, false),
    JUMP_BOOST("§aПрыгучесть", 1,0, 5, 25000, Material.SLIME_BALL, false),
    NIGHT_VISION("§5Ночное зрение", 1, 0, 1, 25000, Material.BROWN_MUSHROOM, false),
    KEY_FINDER("§4Добыча ключей", 2, 0, 50, 1000000000, Material.TRIPWIRE_HOOK, true),
    EXPLOSIVE("§4Взрыв", 5, 0, 500, 150000000, Material.TNT, false),
    LUCKY("§9Удача", 10, 0, 20, 2000000000, Material.LAPIS_ORE, true),
    BLESSINGS("§bБлагославление", 15, 0, 1000, 500000000, Material.MAGMA_CREAM, true),
    TOKEN_MERCHANT("§eМножитель токенов", 20, 0, 5000, 50000000, Material.ENDER_PEARL, true),
    MULTI_FINDER("§5Нахождение множителя", 25, 0, 1000, 500000000, Material.BOOK, true),
    JACK_HAMMER("§cУдар тора", 40, 0, 500, 350000000, Material.DIAMOND_PICKAXE, false),
    PRESTIGE_FINDER("§5Добыча престижей", 50, 0, 1000, 500000000, Material.BEACON, false),
    PRESTIGE_MERCHANT("§2Множитель престижей", 55, 0, 5000, 200000000, Material.EYE_OF_ENDER, true);

    private @Getter @Setter String name;
    private @Getter double need_level_pickaxe;
    private @Getter double start_level;
    private @Getter double max_level;
    private @Getter double start_cost;
    private @Getter Material material;
    private @Getter @Setter boolean isMessage;

    public Object[] getMaxUpgrades(PlayerData playerData, double level, int limit) {
        boolean is = false;
        Object[] obj = {0.0D, 0.0D, 0.0D};
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

    public void sendProcMessage(Player player, String reward) {
        if (isMessage)
            ChatUtil.sendMessage(player, "%s &7>> &fПринёс вам %s", name, reward);
    }
}
