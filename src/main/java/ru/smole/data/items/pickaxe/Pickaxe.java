package ru.smole.data.items.pickaxe;

import lombok.AllArgsConstructor;
import lombok.Data;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.inventory.ItemStack;
import ru.smole.OpPrison;
import ru.smole.data.player.PlayerData;
import ru.smole.data.items.Items;
import ru.smole.data.player.OpPlayer;
import ru.smole.mines.Mine;
import ru.smole.utils.StringUtils;
import ru.xfenilafs.core.util.cuboid.Cuboid;

import java.util.Map;
import java.util.Random;
import java.util.function.Predicate;

import static ru.smole.OpPrison.MINES;

@AllArgsConstructor
@Data public class Pickaxe {

    private Player player;
    private String name;
    private double exp;
    private double level;
    private Map<Upgrade, Upgrade.UpgradeStat> upgrades;

    public double getNeedExp() {
        double needXp = 1000D;
        if (level != 0) {
            double form = needXp * level * 1.5 * (level / 8);
            needXp = needXp + form;
        }

        return needXp;
    }

    public void addExp(double count) {

        setExp(exp + count);

        if (exp >= getNeedExp()) {
            OpPlayer opPlayer = new OpPlayer(player);

            addLevel(1);
            opPlayer.set(Items.getItem("pickaxe", player.getName()), 1);
        }
    }

    public void addLevel(double count) {
        setLevel(level + count);
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

                    Predicate<Mine> predicate = mine -> mine.getZone().contains(block1);
                    boolean noneMine = MINES.values().stream().noneMatch(predicate);

                    if (noneMine)
                        continue;


                    if (block1.getType() == Material.AIR)
                        continue;


                    block1.setType(Material.AIR);
                    i = i + 1;
                }
            }
        }

        return i;
    }

    protected int breakLayer(Block block) {
        Location loc = block.getLocation();
        final int[] i = {0};

        MINES.values().forEach(mine -> {
            Cuboid cuboid = mine.getZone();

            int minX = cuboid.getMinPoint().getBlockX();
            int maxX = cuboid.getMaxPoint().getBlockX();

            int minZ = cuboid.getMinPoint().getBlockZ();
            int maxZ = cuboid.getMaxPoint().getBlockZ();

            for (int x = minX; x <= maxX; x++) {
                for (int z = minZ; z <= maxZ; z++) {
                    Block block1 = loc.getWorld().getBlockAt(
                            x,
                            loc.getBlockY(),
                            z);

                    boolean noneMine = cuboid.contains(block1);

                    if (!noneMine)
                        continue;


                    if (block1.getType() == Material.AIR)
                        continue;


                    block1.setType(Material.AIR);
                    i[0] = i[0] + 1;
                }
            }
        });

        return i[0];
    }

    protected String getRandomReward(Player player, double luckyLevel) {
        OpPlayer opPlayer = new OpPlayer(player);
        Random random = new Random();
        PlayerData playerData = OpPrison.getInstance().getPlayerDataManager().getPlayerDataMap().get(player.getName());

        int i = luckyLevel >= 25 ? 6 : 3;

        switch (random.nextInt(i)) {
            case 0:
                double money = luckyLevel >= 25 ? 2500000000000D : 1000000000000D;

                playerData.addMoney(money);
                return "§a$" + StringUtils.replaceComma(money);
            case 1:
            case 2:
                double token = luckyLevel >= 25 ? 1500000000D : 500000000D;

                playerData.addToken(token);
                return "§e⛃" + StringUtils.replaceComma(token);
            case 3:
                double count = 4;
                ItemStack mineKey = Items.getItem("mine_key", count);

                if (mineKey == null)
                    break;

                opPlayer.add(mineKey);
                return String.format("%s §fx4", mineKey.getItemMeta().getDisplayName());
            case 4:
            case 5:
                count = 2;
                ItemStack epicKey = Items.getItem("epic_key", count);

                if (epicKey == null)
                    break;

                opPlayer.add(epicKey);
                return String.format("%s §fx2", epicKey.getItemMeta().getDisplayName());
        }

        return "ничего";
    }

    public void procUpgrades(BlockBreakEvent event) {
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
        double jack_hammerLevel = upgrades.get(Upgrade.JACK_HAMMER).getCount();
        double token_merchantLevel = upgrades.get(Upgrade.TOKEN_MERCHANT).getCount();
        double luckyLevel = upgrades.get(Upgrade.LUCKY).getCount();
        double multi_finderLevel = upgrades.get(Upgrade.MULTI_FINDER).getCount();
        double prestige_finderLevel = upgrades.get(Upgrade.PRESTIGE_FINDER).getCount();
        double prestige_merchantLevel = upgrades.get(Upgrade.PRESTIGE_MERCHANT).getCount();
        double blessingsLevel = upgrades.get(Upgrade.BLESSINGS).getCount();
        double ig_moneyLevel = upgrades.get(Upgrade.IG_MONEY).getCount();

        double cost = (upgrades.get(Upgrade.FORTUNE).isIs() ? 800.5 * fortuneLevel : 800.5) * (multiplier == 0 ? 1 : multiplier);
        double token = upgrades.get(Upgrade.TOKEN_MINER).isIs() ? 250 * (token_minerLevel / 2) : 250;

        if (OpPrison.BOOSTER > 0) {
            cost = cost + (cost * OpPrison.BOOSTER / 100);
            token = token + (token * OpPrison.BOOSTER / 100);
        }

        if (prestige_finderLevel > 0 && upgrades.get(Upgrade.PRESTIGE_FINDER).isIs()) {
            double chance = prestige_finderLevel / 65000;
            Random random = new Random();

            if (random.nextFloat() <= chance) {
                double prestige = prestige_finderLevel;
                if (prestige_merchantLevel > 0 && upgrades.get(Upgrade.PRESTIGE_MERCHANT).isIs()) {
                    double mer_chance = prestige_merchantLevel / 80000;
                    if (random.nextFloat() <= mer_chance)
                        prestige = prestige_finderLevel + (prestige_finderLevel + prestige_merchantLevel / 2);
                }

                playerData.addPrestige(prestige);
                Upgrade.PRESTIGE_FINDER.sendProcMessage(player, String.format("§5%s §fпрестижей", StringUtils.replaceComma(prestige)));
            }
        }

        if (blessingsLevel > 0 && upgrades.get(Upgrade.BLESSINGS).isIs()) {
            double chance = blessingsLevel / 60000;
            if (new Random().nextFloat() <= chance) {
                double bless = token * blessingsLevel / 100;
                Upgrade.BLESSINGS.setName("§bБлагославление");
                Bukkit.getOnlinePlayers().forEach(onPlayer -> {
                    Upgrade.BLESSINGS.sendProcMessagePlayer(onPlayer, name, String.format("§e⛃%s", StringUtils.replaceComma(bless)));
                    OpPrison.getInstance().getPlayerDataManager().getPlayerDataMap().get(onPlayer.getName()).addToken(bless);
                });
            }
        }

        if (luckyLevel > 0 && upgrades.get(Upgrade.LUCKY).isIs()) {
            double chance = (luckyLevel / 50) / 40;
            if (new Random().nextFloat() <= chance) {
                Upgrade.LUCKY.sendProcMessage(player, getRandomReward(player, luckyLevel));
            }
        }

        if (key_finderLevel > 0 && upgrades.get(Upgrade.KEY_FINDER).isIs()) {
            Random random = new Random();
            double chance = (key_finderLevel / 50) / 40;
            if (random.nextFloat() <= chance) {
                int type = random.nextInt(3) + 1;
                double count = 1;
                ItemStack key = type == 1 || type == 3 ? Items.getItem("token_key", count) : Items.getItem("mine_key", count);

                if (key == null)
                    return;

                opPlayer.add(key);
                Upgrade.KEY_FINDER.sendProcMessage(player, key.getItemMeta().getDisplayName());
            }
        }

        if (token_merchantLevel > 0 && upgrades.get(Upgrade.TOKEN_MERCHANT).isIs()) {
            double chance = (token_merchantLevel / 5) / 40000;
            if (new Random().nextFloat() <= chance) {
                double t_merchant = token * token_merchantLevel / 10;
                token = token + t_merchant;
                playerData.addToken(token);
                Upgrade.TOKEN_MERCHANT.sendProcMessage(player, String.format("§e⛃%s", StringUtils.replaceComma(t_merchant)));

                token = 750 * token_minerLevel;
            }
        }

        if (multi_finderLevel > 0 && upgrades.get(Upgrade.MULTI_FINDER).isIs()) {
            double chance = (multi_finderLevel / 5) / 40000;
            Random random = new Random();
            if (random.nextFloat() <= chance) {
                int multi = random.nextInt(3) + 1;

                playerData.addMultiplier(multi);
                Upgrade.MULTI_FINDER.sendProcMessage(player, String.format("§d%sx", multi));
            }
        }

        if (ig_moneyLevel > 0 && upgrades.get(Upgrade.IG_MONEY).isIs()) {
            double chance = (ig_moneyLevel / 10) / 25000;
            if (new Random().nextFloat() <= chance) {
                opPlayer.add(Items.getItem("ign"));
                Upgrade.IG_MONEY.sendProcMessage(player, "Чек на 50 рублей §8(ВНУТРИИГРОВЫЕ)");
            }
        }

        if (explosiveLevel > 0 && upgrades.get(Upgrade.EXPLOSIVE).isIs()) {
            double chance = (explosiveLevel / 10) / 100;
            if (new Random().nextFloat() <= chance) {
                int blocks = explosive(block);

                cost = cost * blocks;
                token = token * blocks;
            }

            playerData.addBlocks(1);
            playerData.addMoney(cost);
            playerData.addToken(token);
            pickaxe.addExp(1);

            return;
        }

        if (jack_hammerLevel > 0 && upgrades.get(Upgrade.JACK_HAMMER).isIs()) {
            double chance = (jack_hammerLevel / 10) / 4750;
            if (new Random().nextFloat() <= chance) {
                int blocks = breakLayer(block);

                cost = cost * blocks;
                token = token * blocks;
            }

            playerData.addBlocks(1);
            playerData.addMoney(cost);
            playerData.addToken(token);
            pickaxe.addExp(1);

            return;
        }

        playerData.addBlocks(1);
        playerData.addMoney(cost);
        playerData.addToken(token);
        pickaxe.addExp(1);
    }
}
