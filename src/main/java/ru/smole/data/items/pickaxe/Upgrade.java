package ru.smole.data.items.pickaxe;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.Getter;
import lombok.Setter;
import net.md_5.bungee.api.ChatMessageType;
import net.md_5.bungee.api.chat.TextComponent;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import ru.smole.data.player.PlayerData;
import ru.smole.data.group.GroupsManager;
import ru.xfenilafs.core.util.ChatUtil;

import java.util.Random;
import java.util.function.BiConsumer;

@AllArgsConstructor public enum Upgrade {

    EFFICIENCY("§7Эффективность",
            "Увеличивает скорость добычи блоков",
            0,10, 300,  35,
            Material.ANVIL, GroupsManager.Group.MANTLE, false, UpgradeType.TOKEN),

    FORTUNE("§7Шахтёр",
            "Увеличивает количество добываемых денег",
            0,1, 300000, 120,
            Material.DIAMOND, GroupsManager.Group.MANTLE, false, UpgradeType.TOKEN),

    TOKEN_MINER("§eДобыча токенов",
            "Увеличивает количество добываемых токенов",
            0, 1, 3000, 2550,
            Material.DOUBLE_PLANT, GroupsManager.Group.MANTLE, false, UpgradeType.TOKEN),

    HASTE("§eСпешка",
            "Выдаёт эффект ускоренного копания",
            2,0, 5, 25000,
            Material.GLOWSTONE_DUST, GroupsManager.Group.MANTLE, false, UpgradeType.TOKEN),

    SPEED("§fСкорость",
            "Выдаёт эффект ускоренного передвижения",
            3,0, 5, 10,
            Material.SUGAR, GroupsManager.Group.MANTLE, false, UpgradeType.GEMS),

    KEY_FINDER("§4Добыча Ключей",
            "Увеличивает количество добываемых ключей",
            2, 0, 50, 225000,
            Material.TRIPWIRE_HOOK, GroupsManager.Group.MANTLE, true, UpgradeType.TOKEN),

    EXPLOSIVE("§4Взрыв",
            "Увеличивает шанс на взрыв по площади 5x5x5 блоков",
            3, 0, 500, 100000,
            Material.TNT, GroupsManager.Group.MANTLE, false, UpgradeType.TOKEN),

    LUCKY("§9Удача",
            "Выдаёт случайное количество токенов, денег, ключей",
            10, 0, 50, 17500000D,
            Material.LAPIS_ORE, GroupsManager.Group.MANTLE, true, UpgradeType.TOKEN),

    BLESSINGS("§bБлагославление",
            "Выдаёт токены всем на сервере. Количество зависит от Добычи Токенов",
            50, 0, 1000, 1,
            Material.MAGMA_CREAM, GroupsManager.Group.MANTLE, true, UpgradeType.GEMS),

    PRESTIGE_FINDER("§5Добыча Престижей",
            "Увеличивает шанс при копание найти престижи",
            75, 0, 2500, 75000,
            Material.BEACON, GroupsManager.Group.MANTLE, false, UpgradeType.TOKEN),

    JACKPOT("§3Джекпот",
            "Работает как \"Удача\", но лучше!",
            75, 0, 10, 2500,
            Material.DIAMOND_BLOCK, GroupsManager.Group.AQUA, true, UpgradeType.GEMS),

    ZEUS("§bУдар Зевса",
            "Вызывает молнию, которая приносит гемы",
            85, 0, 10, 2500,
            Material.DIAMOND_BLOCK, GroupsManager.Group.AQUA, true, UpgradeType.GEMS),

    PRESTIGE_MERCHANT("§2Множитель Престижей",
            "Умножает добываемые престижи от прокачки \"Добыча Престижей\"",
            100, 0, 10000, 12500,
            Material.EYE_OF_ENDER, GroupsManager.Group.MANTLE, false, UpgradeType.TOKEN),

    TOKEN_MERCHANT("§eМножитель Токенов",
            "Умножает добываемые токены",
            100, 0, 10000, 1000,
            Material.ENDER_PEARL, GroupsManager.Group.MANTLE, true, UpgradeType.TOKEN),

    IG_MONEY("§4Мистер Крабс",
            "С маленьким шансом \n\nвыдаст Вам чек на 50 рублей",
            150, 0, 10, 10000000000D,
            Material.PAPER, GroupsManager.Group.COSMOS, true, UpgradeType.TOKEN);

    private @Getter @Setter String name;
    private final @Getter String describe;
    private final @Getter double need_level_pickaxe;
    private final @Getter double start_level;
    private final @Getter double max_level;
    private final @Getter double start_cost;
    private final @Getter Material material;
    private final @Getter GroupsManager.Group group;
    private final @Getter boolean needMessage;
    private final @Getter UpgradeType type;

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

    public void sendProcMessagePlayer(Player player, String reward) {
        if (needMessage)
            if (PickaxeManager.getPickaxes().get(player.getName()).getUpgrades().get(this).isMessage())
                player.spigot().sendMessage(ChatMessageType.ACTION_BAR, new TextComponent(ChatUtil.text("§f+%s", reward)));
    }

    @Data
    @AllArgsConstructor
    public static class UpgradeStat {

        private double count;
        private boolean is;
        private boolean isMessage;
    }

    public enum UpgradeType {

        TOKEN,
        GEMS
    }
}
