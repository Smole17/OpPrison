package ru.smole.data.items.pickaxe;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.Getter;
import lombok.Setter;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import ru.smole.data.player.PlayerData;
import ru.smole.data.group.GroupsManager;
import ru.xfenilafs.core.util.ChatUtil;

@AllArgsConstructor public enum Upgrade {

    EFFICIENCY("§7Эффективность",
            "Увеличивает скорость добычи блоков",
            0,10, 300,  70000,
            Material.ANVIL, GroupsManager.Group.MANTLE, false),

    FORTUNE("§7Шахтёр",
            "Увеличивает количество добываемых денег",
            0,100, 1500000, 50000,
            Material.DIAMOND, GroupsManager.Group.MANTLE, false),

    TOKEN_MINER("§eДобыча токенов",
            "Увеличивает количество добываемых токенов",
            0, 20, 75000, 650000,
            Material.DOUBLE_PLANT, GroupsManager.Group.MANTLE, false),

    HASTE("§eСпешка",
            "Выдаёт эффект для ускоренного копания",
            1,0, 5, 25000,
            Material.GLOWSTONE_DUST, GroupsManager.Group.MANTLE, false),

    SPEED("§fСкорость",
            "Выдаёт эффект для ускоренного передвижения",
            1,0, 5, 25000,
            Material.SUGAR, GroupsManager.Group.MANTLE, false),

    JUMP_BOOST("§aПрыгучесть",
            "Выдаёт эффект для повышенной прыгучести",
            1,0, 5, 25000,
            Material.SLIME_BALL, GroupsManager.Group.MANTLE, false),

    NIGHT_VISION("§5Ночное зрение",
            "Выдаёт эффект ночного зрения",
            1, 0, 1, 25000,
            Material.BROWN_MUSHROOM, GroupsManager.Group.MANTLE, false),

    KEY_FINDER("§4Добыча ключей",
            "Увеличивает количество добываемых ключей",
            2, 0, 50, 250000000,
            Material.TRIPWIRE_HOOK, GroupsManager.Group.MANTLE, true),

    EXPLOSIVE("§4Взрыв",
            "Увеличивает шанс на взрыв по площади 5x5x5 блоков",
            5, 0, 500, 100000000,
            Material.TNT, GroupsManager.Group.MANTLE, false),

    LUCKY("§9Удача",
            "Выдаёт случайно токены, монеты, а начиная с 25 уровня - ключи",
            10, 0, 50, 17500000000D,
            Material.LAPIS_ORE, GroupsManager.Group.MANTLE, true),

    BLESSINGS("§bБлагославление",
            "Выдаёт токены всем на сервере. Количество зависит от Добычи токенов",
            15, 0, 1000, 125000000,
            Material.MAGMA_CREAM, GroupsManager.Group.MANTLE, true),

    TOKEN_MERCHANT("§eМножитель токенов",
            "Выдаёт умноженное количество токенов",
            20, 0, 10000, 80000000,
            Material.ENDER_PEARL, GroupsManager.Group.MANTLE, true),

    MULTI_FINDER("§dНахождение множителя",
            "Выдаёт от 1-го до 3-ёх множителей",
            25, 0, 2500, 1000000000,
            Material.BOOK, GroupsManager.Group.MANTLE, true),

    JACK_HAMMER("§cРазрушитель",
            "Увеличивает шанс на ломание целого слоя в шахте",
            25, 0, 750, 3500000000D,
            Material.DIAMOND_PICKAXE, GroupsManager.Group.MANTLE, false),

    LEPRECHAUN("§aЛепрекон",
            "Выдаёт деньги всем на сервере. Количество зависит от уровня",
            35, 0, 2500, 500000000,
            Material.EMERALD, GroupsManager.Group.MANTLE, true),

    JACKPOT("§3Джекпот",
            "Выдаёт случайно токены, деньги. С 3 уровня - ключи",
            40, 0, 5, 45000000000000D,
            Material.DIAMOND_BLOCK, GroupsManager.Group.AQUA, true),

    PRESTIGE_FINDER("§5Добыча престижей",
            "Увеличивает шанс при копание найти престижи. Количество зависит от уровня",
            50, 0, 2500, 3000000000D,
            Material.BEACON, GroupsManager.Group.MANTLE, false),

    PRESTIGE_MERCHANT("§2Множитель престижей",
            "Умножает добываемые престижей от прокачки \"Добыча престижей\"",
            55, 0, 10000, 500000000,
            Material.EYE_OF_ENDER, GroupsManager.Group.MANTLE, false),

    IG_MONEY("§4Мистер Крабс",
            "С ОЧЕНЬ маленьким шансом выдаст Вам чек, при активации которого вы получите донат валюту",
            75, 0, 10, 75000000000000D,
            Material.PAPER, GroupsManager.Group.COSMOS, true);

    private @Getter @Setter String name;
    private final @Getter String describe;
    private final @Getter double need_level_pickaxe;
    private final @Getter double start_level;
    private final @Getter double max_level;
    private final @Getter double start_cost;
    private final @Getter Material material;
    private final @Getter GroupsManager.Group group;
    private final @Getter boolean needMessage;

    public Object[] getMaxUpgrades(PlayerData playerData, double level) {
        Object[] obj = {null, null};
        double upgrades = 0;
        double tokens = 0;
        double need = 0;

        double token = playerData.getToken();

        while (token >= need) {
            if (level >= max_level + 1)
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

        double form = start_cost * 0.1 * level;
        return start_cost + form;
    }

    public boolean isMaxLevel(double level) {
        return level == max_level;
    }

    public void sendProcMessage(Player player, String reward) {
        if (needMessage)
            if (PickaxeManager.getPickaxes().get(player.getName()).getUpgrades().get(this).isMessage())
                ChatUtil.sendMessage(player, "&8[%s&8] &fПринесло вам %s", name, reward);
    }

    public void sendProcMessagePlayer(Player player, String name, String reward) {
        if (needMessage)
            if (PickaxeManager.getPickaxes().get(player.getName()).getUpgrades().get(this).isMessage())
                ChatUtil.sendMessage(player, "&8[%s %s&8] &fПринесло вам %s", this.name, name, reward);
    }

    @Data
    @AllArgsConstructor
    public static class UpgradeStat {

        private double count;
        private boolean is;
        private boolean isMessage;
        private boolean isCompleteQ;
    }
}
