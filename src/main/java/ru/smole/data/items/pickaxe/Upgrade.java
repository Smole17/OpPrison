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
            0,1, 300000, 10,
            Material.DIAMOND, GroupsManager.Group.MANTLE, false, UpgradeType.TOKEN),

    TOKEN_MINER("§eДобыча токенов",
            "Увеличивает количество добываемых токенов",
            0, 1, 3000, 750,
            Material.DOUBLE_PLANT, GroupsManager.Group.MANTLE, false, UpgradeType.TOKEN),

    EXPLOSIVE("§4Взрыв",
            "Увеличивает шанс на взрыв по площади 5x5x5 блоков",
            1, 0, 500, 20000,
            Material.TNT, GroupsManager.Group.MANTLE, false, UpgradeType.TOKEN),

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
            Material.TRIPWIRE_HOOK, GroupsManager.Group.MANTLE, false, UpgradeType.TOKEN),

    LUCKY("§9Удача",
            "Выдаёт случайное количество токенов, денег, ключей",
            25, 0, 50, 17500000D,
            Material.LAPIS_ORE, GroupsManager.Group.MANTLE, true, UpgradeType.TOKEN),

    BLESSINGS("§bБлагославление",
            "Выдаёт токены всем на сервере",
            50, 0, 1000, 0.15,
            Material.MAGMA_CREAM, GroupsManager.Group.MANTLE, true, UpgradeType.GEMS),

    PRESTIGE_FINDER("§5Добыча Престижей",
            "Увеличивает шанс при копание найти престижи",
            75, 0, 1000, 1750000,
            Material.BEACON, GroupsManager.Group.MANTLE, false, UpgradeType.TOKEN),

    ZEUS("§9Удар Зевса",
            "Вызывает молнию, которая приносит гемы",
            75, 0, 10, 75,
            Material.NETHER_STAR, GroupsManager.Group.MANTLE, true, UpgradeType.GEMS),

    CRATE_FINDER("§9Нахождение Ящиков",
            "Может выдать ящик с бронёй, зельями, лутбокс",
            100, 0, 100, 12.5,
            Material.ENDER_CHEST, GroupsManager.Group.SUN, true, UpgradeType.GEMS),

    JACKPOT("§3Джекпот",
            "Работает как Удача, но лучше!",
            125, 0, 10, 2500,
            Material.DIAMOND_BLOCK, GroupsManager.Group.AQUA, true, UpgradeType.GEMS),

    PRESTIGE_MERCHANT("§2Множитель Престижей",
            "Умножает добываемые престижи",
            100, 0, 100, 32500000,
            Material.EYE_OF_ENDER, GroupsManager.Group.AIR, false, UpgradeType.TOKEN),

    TOKEN_MERCHANT("§eМножитель Токенов",
            "Умножает добываемые токены",
            100, 0, 1000, 25000,
            Material.ENDER_PEARL, GroupsManager.Group.MANTLE, true, UpgradeType.TOKEN),

    IG_MONEY("§4Мистер Крабс",
            "С маленьким шансом выдаст чек на 50 рублей",
            150, 0, 10, 6000000000D,
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

    public Object[] getMaxUpgrades(double level, double token) {
        Object[] obj = {null, null};
        double upgrades = 0;
        double tokens = 0;
        double need = 0;

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
            if (PickaxeManager.getPickaxes().get(player.getName()).getUpgrades().get(this).isMessage()) {
                if (reward != null) {
                    ChatUtil.sendMessage(player, "%s &8> &aСработало и принесло вам %s§a!", name, reward);
                    return;
                }

                ChatUtil.sendMessage(player, "%s &8> &aСработало и принесло вам хорошую награду!", name);
            }
    }

    public void sendProcMessagePlayer(Player player, String reward) {
        if (needMessage)
            if (PickaxeManager.getPickaxes().get(player.getName()).getUpgrades().get(this).isMessage())
                player.spigot().sendMessage(ChatMessageType.ACTION_BAR, new TextComponent(ChatUtil.text("§a+%s", reward)));
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
