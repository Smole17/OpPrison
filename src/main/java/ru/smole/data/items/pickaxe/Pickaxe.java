package ru.smole.data.items.pickaxe;

import lombok.AllArgsConstructor;
import lombok.Data;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Item;
import org.bukkit.entity.Player;
import org.bukkit.entity.Rabbit;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.inventory.ItemStack;
import ru.luvas.rmcs.player.RPlayer;
import ru.smole.OpPrison;
import ru.smole.data.player.PlayerData;
import ru.smole.data.items.Items;
import ru.smole.data.player.OpPlayer;
import ru.smole.mines.Mine;
import ru.smole.utils.StringUtils;
import ru.xfenilafs.core.regions.Region;
import ru.xfenilafs.core.util.ChatUtil;
import ru.xfenilafs.core.util.cuboid.Cuboid;
import sexy.kostya.mineos.achievements.Achievement;
import sexy.kostya.mineos.achievements.Achievements;

import javax.swing.*;
import java.util.Arrays;
import java.util.Date;
import java.util.Map;
import java.util.Random;
import java.util.function.Predicate;

import static ru.smole.OpPrison.MINES;
import static ru.smole.OpPrison.REGIONS;

@AllArgsConstructor
@Data public class Pickaxe {

    private Player player;
    private String name;
    private double exp;
    private double level;
    private Map<Upgrade, Upgrade.UpgradeStat> upgrades;

    public double getNeedExp() {
        return 1000D * (level <= 0 ? 1 : level) * 10;
    }

    public void addExp(double count) {
        setExp(exp + count);

        if (exp >= getNeedExp()) {
            addLevel(1);

            Arrays.stream(player.getInventory().getStorageContents())
                    .parallel()
                    .filter(itemStack1 -> itemStack1 != null && itemStack1.getType() == Material.DIAMOND_PICKAXE)
                    .forEach(itemStack1 -> itemStack1.setAmount(0));

            OpPlayer.add(player, Items.getItem("pickaxe", player.getName()));
        }
    }

    public void addLevel(double count) {
        setLevel(level + count);
        setExp(0);

        if (level >= 40) {
            Achievement ach = Achievement.OP_PICKAXE_LEVEL;
            Achievements achievements = RPlayer.checkAndGet(player.getName()).getAchievements();

            achievements.addAchievement(ach);
        }

        if (level % 5 == 0) {
            ItemStack itemStack = Items.getItem("mine_key", 4.0);

            if (itemStack == null)
                return;

            OpPlayer.add(player, itemStack.clone());
            ChatUtil.sendMessage(player, OpPrison.PREFIX + "Вы получили %s &fx%s", itemStack.getItemMeta().getDisplayName(), itemStack.getAmount());
        }

        if (level % 10 == 0) {
            ItemStack itemStack = Items.getItem("epic_key", 2.0);

            if (itemStack == null)
                return;

            OpPlayer.add(player, itemStack.clone());
            ChatUtil.sendMessage(player, OpPrison.PREFIX + "Вы получили %s &fx%s", itemStack.getItemMeta().getDisplayName(), itemStack.getAmount());
        }

        ItemStack itemStack = Items.getItem("mine_key", 2.0);

        if (itemStack == null)
            return;

        OpPlayer.add(player, itemStack.clone());
        ChatUtil.sendMessage(player, OpPrison.PREFIX + "Вы получили %s &fx%s", itemStack.getItemMeta().getDisplayName(), itemStack.getAmount());
        ChatUtil.sendMessage(player, OpPrison.PREFIX + "Вы прокачали уровень кирки до %s", StringUtils._fixDouble(0, level));
    }

    protected int explosive(Block block) {
        Location loc = block.getLocation();
        int i = 0;

        for (int x = -2; x <= 2; x++) {
            for (int y = -2; y <= 2; y++) {
                for (int z = -2; z <= 2; z++) {
                    Block block1 = loc.getWorld().getBlockAt(
                            loc.getBlockX() + x,
                            loc.getBlockY() + y,
                            loc.getBlockZ() + z);

                    if (MINES.get(-1).getZone().contains(block1))
                        return i;

                    Predicate<Mine> predicate = mine -> mine.getZone().contains(block1);
                    boolean noneMine = MINES.values().stream().noneMatch(predicate);

                    if (noneMine)
                        continue;

                    if (block1.getType() == Material.AIR || block1.getType() == Material.CHEST)
                        continue;

                    block1.setType(Material.AIR);
                    i = i + 1;
                }
            }
        }

        return i;
    }

    protected int zeus(Block block, int level) {
        Location loc = block.getLocation();

        int[] i = {0};
        REGIONS.values()
                .stream()
                .parallel()
                .filter(region1 -> region1.getZone().contains(block))
                .findFirst()
                .ifPresent(region -> {
                    for (int y = 1; y <= level * 10; y++) {
                        Block block1 = loc.getWorld().getBlockAt(
                                loc.getBlockX(),
                                loc.getBlockY() + y,
                                loc.getBlockZ());

                        Predicate<Mine> predicate = mine -> mine.getZone().contains(block1);
                        boolean noneMine = MINES.values().stream().noneMatch(predicate);

                        if (noneMine)
                            continue;

                        if (block1.getType() == Material.AIR || block1.getType() == Material.CHEST)
                            continue;

                        block1.setType(Material.AIR);
                        i[0] = i[0] + 1;
                    }

                    loc.getWorld().strikeLightningEffect(loc);
                });

        return i[0];
    }

    protected String getRandomRewardLucky(double luckyLevel, Random random) {
        PlayerData playerData = OpPrison.getInstance().getPlayerDataManager().getPlayerDataMap().get(player.getName());

        int i = luckyLevel >= 25 ? 6 : 3;

        switch (random.nextInt(i)) {
            case 0:
                double money = luckyLevel >= 25 ? 250000000D : 100000000D;

                playerData.addMoney(money);
                return "§6$" + StringUtils.replaceComma(money);
            case 1:
            case 2:
                double token = luckyLevel >= 25 ? 1500000D : 500000D;

                playerData.addToken(token);
                return "§e⛃" + StringUtils.replaceComma(token);
            case 3:
                double count = 3;
                ItemStack mineKey = Items.getItem("mine_key", count);

                if (mineKey == null)
                    break;

                OpPlayer.add(player, mineKey);
                return String.format("%s §fx4", mineKey.getItemMeta().getDisplayName());
            case 4:
            case 5:
                count = 1;
                ItemStack epicKey = Items.getItem("epic_key", count);

                if (epicKey == null)
                    break;

                OpPlayer.add(player, epicKey);
                return String.format("%s §fx2", epicKey.getItemMeta().getDisplayName());
        }

        return "ничего";
    }

    protected String getRandomRewardJackPot(double jackPotLevel, Random random) {
        PlayerData playerData = OpPrison.getInstance().getPlayerDataManager().getPlayerDataMap().get(player.getName());

        int i = jackPotLevel >= 3 ? 4 : 2;

        switch (random.nextInt(i)) {
            case 0:
                double money = 300000000D;

                playerData.addMoney(money);
                return "§6$" + StringUtils.replaceComma(money);
            case 1:
                double token = 1500000000D;

                playerData.addToken(token);
                return "§e⛃" + StringUtils.replaceComma(token);
            case 2:
                double count = 6;
                ItemStack legKey = Items.getItem("legendary_key", count);

                if (legKey == null)
                    break;

                OpPlayer.add(player, legKey);
                return String.format("%s §fx%s", legKey.getItemMeta().getDisplayName(), StringUtils._fixDouble(0, count));
            case 3:
                double count1 = 3;
                ItemStack mythKey = Items.getItem("mythical_key", count1);

                if (mythKey == null)
                    break;

                OpPlayer.add(player, mythKey);
                return String.format("%s §fx%s", mythKey.getItemMeta().getDisplayName(), StringUtils._fixDouble(0, count1));
        }

        return "ничего";
    }

    public void getRandomCrate(Random random) {
        int randomI = random.nextInt(3);

        ItemStack itemStack = null;

        switch (randomI) {
            case 0:
                itemStack = Items.getItem("armor_crate", 1.0);
                break;
            case 1:
                itemStack = Items.getItem("potion_armor_crate", 1.0);
                break;
            case 2:
                itemStack = Items.getItem("loot_box_crate", 1.0);
                break;
        }

        if (itemStack != null)
            OpPlayer.add(player, itemStack.clone());
    }

    public void procUpgrades(BlockBreakEvent event, Random random) {
        Player player = event.getPlayer();
        OpPlayer opPlayer = new OpPlayer(player);

        if (!Items.isSomePickaxe(player.getInventory().getItemInMainHand(), player.getName()))
            return;

        String name = player.getName();
        Block block = event.getBlock();

        PlayerData playerData = OpPrison.getInstance().getPlayerDataManager().getPlayerDataMap().get(player.getName());
        Pickaxe pickaxe = PickaxeManager.getPickaxes().get(name);

        double multiplier = playerData.getMultiplier();

        double fortuneLevel = upgrades.get(Upgrade.FORTUNE).getCount();
        double token_minerLevel = upgrades.get(Upgrade.TOKEN_MINER).getCount();
        double key_finderLevel = upgrades.get(Upgrade.KEY_FINDER).getCount();
        double explosiveLevel = upgrades.get(Upgrade.EXPLOSIVE).getCount();
        double token_merchantLevel = upgrades.get(Upgrade.TOKEN_MERCHANT).getCount();
        double luckyLevel = upgrades.get(Upgrade.LUCKY).getCount();
        double prestige_finderLevel = upgrades.get(Upgrade.PRESTIGE_FINDER).getCount();
        double prestige_merchantLevel = upgrades.get(Upgrade.PRESTIGE_MERCHANT).getCount();
        double blessingsLevel = upgrades.get(Upgrade.BLESSINGS).getCount();
        double jackPotLevel = upgrades.get(Upgrade.JACKPOT).getCount();
        double ig_moneyLevel = upgrades.get(Upgrade.IG_MONEY).getCount();
        double zeusLevel = upgrades.get(Upgrade.ZEUS).getCount();
        double crate_FinderLevel = upgrades.get(Upgrade.CRATE_FINDER).getCount();

        double cost = (upgrades.get(Upgrade.FORTUNE).isIs() ? 1.8 * fortuneLevel : 1.8) * multiplier;
        double token = upgrades.get(Upgrade.TOKEN_MINER).isIs() ? 1.5 * token_minerLevel : 1.5;
        double exp = 1;

        if (prestige_finderLevel > 0 && upgrades.get(Upgrade.PRESTIGE_FINDER).isIs()) {
            double chance = prestige_finderLevel / 24000;

            if (random.nextFloat() <= chance) {
                double prestige = 1;
                if (prestige_merchantLevel > 0 && upgrades.get(Upgrade.PRESTIGE_MERCHANT).isIs()) {
                    double mer_chance = prestige_merchantLevel / 250000;
                    if (random.nextFloat() <= mer_chance)
                        prestige = prestige * 2;
                }

                playerData.addPrestige(prestige);
            }
        }

        if (blessingsLevel > 0 && upgrades.get(Upgrade.BLESSINGS).isIs()) {
            double chance = blessingsLevel / 100000;
            if (new Random().nextFloat() <= chance) {
                double bless = token * blessingsLevel / 175;
                Bukkit.getOnlinePlayers().forEach(onPlayer -> {
                    Upgrade.BLESSINGS.sendProcMessagePlayer(onPlayer, String.format("§e⛃%s", StringUtils.replaceComma(bless)));
                    OpPrison.getInstance().getPlayerDataManager().getPlayerDataMap().get(onPlayer.getName()).addToken(bless);
                });
            }
        }

        if (luckyLevel > 0 && upgrades.get(Upgrade.LUCKY).isIs()) {
            double chance = (luckyLevel / 50) / 100;
            if (random.nextFloat() <= chance) {
                getRandomRewardLucky(luckyLevel, random);
                Upgrade.LUCKY.sendProcMessage(player, null);
            }
        }

        if (key_finderLevel > 0 && upgrades.get(Upgrade.KEY_FINDER).isIs()) {
            double chance = (key_finderLevel / 50) / 65;
            if (random.nextFloat() <= chance) {
                int type = random.nextInt(3) + 1;
                double count = 1;
                ItemStack key = type == 1 || type == 3 ? Items.getItem("token_key", count) : Items.getItem("mine_key", count);

                if (key == null)
                    return;

                opPlayer.add(key);
            }
        }

        if (token_merchantLevel > 0 && upgrades.get(Upgrade.TOKEN_MERCHANT).isIs()) {
            double chance = (token_merchantLevel / 10) / 11000;
            if (new Random().nextFloat() <= chance) {
                double t_merchant = token * token_merchantLevel / 10;
                token = token + t_merchant;
                playerData.addToken(token);
                Upgrade.TOKEN_MERCHANT.sendProcMessage(player, String.format("§e⛃%s", StringUtils.replaceComma(t_merchant)));

                token = 750 * token_minerLevel;
            }
        }

        if (jackPotLevel > 0 && upgrades.get(Upgrade.JACKPOT).isIs()) {
            double chance = (jackPotLevel / 5) / 10000;
            if (random.nextFloat() <= chance) {
                getRandomRewardJackPot(jackPotLevel, random);
                Upgrade.JACKPOT.sendProcMessage(player, null);
            }
        }

        if (crate_FinderLevel > 0 && upgrades.get(Upgrade.CRATE_FINDER).isIs()) {
            double chance = (crate_FinderLevel / 5) / 100000;
            if (random.nextFloat() <= chance) {
                getRandomCrate(random);
                Upgrade.CRATE_FINDER.sendProcMessage(player, null);
            }
        }

        if (ig_moneyLevel > 0 && upgrades.get(Upgrade.IG_MONEY).isIs()) {
            double chance = (ig_moneyLevel / 10) / 1700000;
            if (new Random().nextFloat() <= chance) {
                opPlayer.add(Items.getItem("ign"));
                Upgrade.IG_MONEY.sendProcMessage(player, null);
                System.out.println(new Date() + ": " + player.getName() + " ПОЛУЧИЛ ЧЕК НА 50 РУБЛЕЙ");
            }
        }


        if (zeusLevel > 0 && upgrades.get(Upgrade.ZEUS).isIs()) {
            double chance = zeusLevel / 5000;
            if (random.nextFloat() <= chance) {
                int zeus = zeus(block, (int) zeusLevel);
                Upgrade.ZEUS.sendProcMessage(player, "§3❅" + zeus + " гемов");
                playerData.addGems(zeus);
            }
        }

        if (explosiveLevel > 0 && upgrades.get(Upgrade.EXPLOSIVE).isIs()) {
            double chance = (explosiveLevel / 10) / 125;
            if (new Random().nextFloat() <= chance) {
                int blocks = explosive(block);

                cost = cost * blocks;
                token = token * blocks;
                exp = exp * blocks;
            }

            playerData.addBlocks(1);
            playerData.addMoney(multiplyCost(cost));
            playerData.addToken(multiplyToken(token));
            pickaxe.addExp(exp);

            return;
        }

        playerData.addBlocks(1);
        playerData.addMoney(multiplyCost(cost));
        playerData.addToken(multiplyToken(token));
        pickaxe.addExp(exp);
    }

    private double multiplyCost(double cost) {
        if (OpPrison.BOOSTER > 0) {
            cost = cost + (cost * OpPrison.BOOSTER / 100);
        }

        Mine mine = MINES.values()
                .stream()
                .parallel()
                .filter(mine1 -> mine1.getRegion().getZone().contains(player))
                .findFirst()
                .orElse(null);

        double bonus = mine == null ? 0.01 : mine.getBonus() / 100;

        return cost + (cost * (bonus == 0 ? 0.01 : bonus));
    }

    private double multiplyToken(double token) {
        if (OpPrison.BOOSTER > 0) {
            token = token + (token * OpPrison.BOOSTER / 100);
        }

        return token;
    }
}
