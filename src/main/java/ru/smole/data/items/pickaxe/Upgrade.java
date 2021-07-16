package ru.smole.data.items.pickaxe;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.Getter;
import lombok.Setter;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import ru.smole.OpPrison;
import ru.smole.data.PlayerData;
import ru.smole.data.group.GroupsManager;
import ru.xfenilafs.core.util.ChatUtil;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

@AllArgsConstructor public enum Upgrade {

    EFFICIENCY("§7Эффективность",
            "Увеличивает скорость добычи блоков",
            0,10, 300,  50000,
            Material.ANVIL, GroupsManager.Group.MANTLE),

    FORTUNE("§7Шахтёр",
            "Увеличивает количество добываемых денег",
            0,100, 500000, 15000,
            Material.DIAMOND, GroupsManager.Group.MANTLE),

    TOKEN_MINER("§eДобыча токенов",
            "Увеличивает количество добываемых токенов",
            0, 20, 50000, 1000000,
            Material.DOUBLE_PLANT, GroupsManager.Group.MANTLE),

    HASTE("§eСпешка",
            "Выдаёт эффект для ускоренного копания",
            1,0, 5, 25000,
            Material.GLOWSTONE_DUST, GroupsManager.Group.MANTLE),

    SPEED("§fСкорость",
            "Выдаёт эффект для ускоренного передвижения",
            1,0, 5, 25000,
            Material.SUGAR, GroupsManager.Group.MANTLE),

    JUMP_BOOST("§aПрыгучесть",
            "Выдаёт эффект для повышенной прыгучести",
            1,0, 5, 25000,
            Material.SLIME_BALL, GroupsManager.Group.MANTLE),

    NIGHT_VISION("§5Ночное зрение",
            "Выдаёт эффект ночного зрения",
            1, 0, 1, 25000,
            Material.BROWN_MUSHROOM, GroupsManager.Group.MANTLE),

    KEY_FINDER("§4Добыча ключей",
            "Увеличивает количество добываемых ключей",
            2, 0, 50, 2000000000,
            Material.TRIPWIRE_HOOK, GroupsManager.Group.MANTLE),

    EXPLOSIVE("§4Взрыв",
            "Увеличивает шанс на взрыв по площади 5x5x5 блоков",
            5, 0, 500, 150000000,
            Material.TNT, GroupsManager.Group.MANTLE),

    LUCKY("§9Удача",
            "Выдаёт случайно токены, монеты, а начиная с 25 уровня - ключи",
            10, 0, 50, 7000000000D,
            Material.LAPIS_ORE, GroupsManager.Group.MANTLE),

    BLESSINGS("§bБлагославление",
            "Выдаёт токены всем на сервере. Количество зависит от Добычи токенов",
            15, 0, 1000, 125000000,
            Material.MAGMA_CREAM, GroupsManager.Group.MANTLE),

    TOKEN_MERCHANT("§eМножитель токенов",
            "Выдаёт умноженное количество токенов",
            20, 0, 5000, 50000000,
            Material.ENDER_PEARL, GroupsManager.Group.MANTLE),

    MULTI_FINDER("§dНахождение множителя",
            "Выдаёт от 1-го до 3-ёх множителей Вам",
            25, 0, 1000, 500000000,
            Material.BOOK, GroupsManager.Group.MANTLE),

    JACK_HAMMER("§cУдар тора",
            "Увеличивает шанс на ломание целого слоя в шахте",
            40, 0, 500, 350000000,
            Material.DIAMOND_PICKAXE, GroupsManager.Group.MANTLE),

    PRESTIGE_FINDER("§5Добыча престижей",
            "Увеличивает шанс при копание найти престижи. Количество зависит от уровня",
            50, 0, 1000, 500000000,
            Material.BEACON, GroupsManager.Group.MANTLE),

    PRESTIGE_MERCHANT("§2Множитель престижей",
            "Умножает добываемые престижей от прокачки \"Добыча престижей\"",
            55, 0, 5000, 200000000,
            Material.EYE_OF_ENDER, GroupsManager.Group.MANTLE),

    IG_MONEY("§4Мистер Крабс",
            "С ОЧЕНЬ маленьким шансом выдаст Вам чек, при активации которого вы получите донат валюту",
            70, 0, 10, 1250000000000D,
            Material.PAPER, GroupsManager.Group.COSMOS);

    private @Getter @Setter String name;
    private @Getter String describe;
    private @Getter double need_level_pickaxe;
    private @Getter double start_level;
    private @Getter double max_level;
    private @Getter double start_cost;
    private @Getter Material material;
    private @Getter GroupsManager.Group group;

    public Object[] getMaxUpgrades(PlayerData playerData, double level) {
        Object[] obj = {null, null};
        double upgrades = 0;
        double tokens = 0;
        double need = 0;

        double token = playerData.getToken();

        while (token >= need) {
            if (level >= max_level)
                break;

            if (need >= token)
                break;

            need = need + getNeedTokens(level);

            if (need >= token)
                break;

            level = level + 1;

            tokens = need;
            upgrades = upgrades + 1;
        }

        obj[0] = upgrades;
        obj[1] = tokens;

        return obj;
    }

    public Object[] get10Upgrades(double level) {
        Object[] obj = {0.0D, 0.0};
        double upgrades = (double) obj[0];
        double tokens = (double) obj[1];
        int i = 0;

        while (i != 10) {
            if (level > max_level)
                break;

            double need = getNeedTokens(level);
            tokens = tokens + need;
            level = level + 1;
            upgrades = upgrades + 1;
            i = i + 1;
        }

        obj[0] = upgrades;
        obj[1] = tokens;

        return obj;
    }

    public double getNeedTokens(double level) {
        if (isMaxLevel(level - 1))
            return 0.0;

        double form = start_cost * 0.05 * level;
        return start_cost + form;
    }

    public boolean isUnlock(double level) {
        return level >= need_level_pickaxe;
    }

    public boolean isMaxLevel(double level) {
        return level == max_level;
    }

    public void sendProcMessage(Player player, String reward) {
        if (PickaxeManager.getPickaxes().get(player.getName()).getUpgrades().get(this).isMessage())
            ChatUtil.sendMessage(player, "%s &7>> &fПринесло вам %s", name, reward);
    }

    public void sendProcMessagePlayer(Player player, String name, String reward) {
        if (PickaxeManager.getPickaxes().get(player.getName()).getUpgrades().get(this).isMessage())
            ChatUtil.sendMessage(player, "%s %s &7>> &fПринесло вам %s", this.name, name, reward);
    }

    @Data
    @AllArgsConstructor
    public static class UpgradeStat {

        private double count;
        private boolean is;
        private boolean isMessage;
    }
}
