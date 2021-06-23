package ru.smole.data.items.pickaxe;

import lombok.AllArgsConstructor;
import lombok.Data;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.Player;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.inventory.ItemStack;
import ru.smole.OpPrison;
import ru.smole.data.PlayerData;
import ru.smole.data.items.Key;
import ru.smole.data.OpPlayer;
import ru.smole.utils.BlockUtil;
import ru.smole.utils.StringUtils;
import ru.xfenilafs.core.util.ChatUtil;

import java.util.List;
import java.util.Map;
import java.util.Random;

@AllArgsConstructor
@Data public class Pickaxe {

    private Player player;
    private String name;
    private double exp;
    private double level;
    private List<Map<Upgrade, Double>> upgrades;

    public double getNeedExp() {
        double needXp = 1000D;
        if (level != 0) {
            double form = needXp * level * 0.1;
            needXp = needXp + form;
        }

        return needXp;
    }

    public void addExp(double count) {
        setExp(exp + count);

        if (exp == getNeedExp()) {
            addLevel(1);
        }
    }

    public void addLevel(double count) {
        setLevel(level + count);
    }

    public void explosive(Block block) {
        Location loc = block.getLocation();

        for (int x = -3; x <= 3; x++) {
            for (int y = -3; y <= 3; y++) {
                for (int z = -3; z <= 3; z++) {
                    loc.getWorld().getBlockAt(
                            loc.getBlockX() + x,
                            loc.getBlockY() + y,
                            loc.getBlockZ() + z).breakNaturally();
                }
            }
        }
    }

    protected String getRandomReward(Player player) {
        OpPlayer opPlayer = new OpPlayer(player);
        Random random = new Random();
        PlayerData playerData = OpPrison.getInstance().getPlayerDataManager().getPlayerDataMap().get(player.getName());

        switch (random.nextInt(4)) {
            case 0:
            case 1:
                return "§a$" + StringUtils.formatDouble(1, playerData.addMoney(playerData.getMoney() / 50));
            case 2:
                return "§e⛃" + StringUtils.formatDouble(1, playerData.addToken(50000000000D));
            case 3:
                Key.MINE.getStack().setAmount(16);
                opPlayer.add(Key.MINE.getStack());
                return String.format("%s §fx16", Key.MINE.getStack().getItemMeta().getDisplayName());
            case 4:
                Key.EPIC.getStack().setAmount(4);
                opPlayer.add(Key.EPIC.getStack());
                return String.format("%s §fx4", Key.EPIC.getStack().getItemMeta().getDisplayName());
        }

        return "ничего";
    }

    public void procUpgrades(BlockBreakEvent event) {
        Player player = event.getPlayer();
        String name = player.getName();

        Block block = event.getBlock();
        Material blockType = block.getType();
        BlockFace blockFace = BlockUtil.getAvailableRelativeByType(block, blockType, 5);

        PlayerData playerData = OpPrison.getInstance().getPlayerDataManager().getPlayerDataMap().get(player.getName());
        OpPlayer opPlayer = new OpPlayer(player);

        Pickaxe pickaxe = opPlayer.getPickaxeManager().getPickaxes().get(name);
        List<Map<Upgrade, Double>> upgrades = pickaxe.getUpgrades();

        double fortuneLevel = upgrades.get(0).get(Upgrade.FORTUNE);
        double token_minerLevel = upgrades.get(0).get(Upgrade.TOKEN_MINER);
        double key_finderLevel = upgrades.get(0).get(Upgrade.KEY_FINDER);
        double explosiveLevel = upgrades.get(0).get(Upgrade.EXPLOSIVE);
        double jack_hammerLevel = upgrades.get(0).get(Upgrade.JACK_HAMMER);
        double token_merchantLevel = upgrades.get(0).get(Upgrade.TOKEN_MERCHANT);
        double luckyLevel = upgrades.get(0).get(Upgrade.LUCKY);
        double multi_finderLevel = upgrades.get(0).get(Upgrade.MULTI_FINDER);
        double prestige_finderLevel = upgrades.get(0).get(Upgrade.PRESTIGE_FINDER);
        double prestige_merchantLevel = upgrades.get(0).get(Upgrade.PRESTIGE_MERCHANT);
        double blessingsLevel = upgrades.get(0).get(Upgrade.BLESSINGS);

        ChatUtil.sendMessage(player, String.valueOf(blockFace.ordinal()));
        double cost = 750 * fortuneLevel * playerData.getMultiplier();
        double token = 750 * token_minerLevel;

        if (prestige_finderLevel > 0) {
            double chance = prestige_finderLevel / 10000;
            Random random = new Random();

            if (random.nextFloat() <= chance) {
                double prestige = prestige_finderLevel;
                if (prestige_merchantLevel > 0) {
                    double mer_chance = prestige_merchantLevel / 60000;
                    if (random.nextFloat() <= mer_chance)
                        prestige = prestige_finderLevel + (prestige_finderLevel + prestige_merchantLevel / 2);
                }

                playerData.addPrestige(prestige);
            }
        }

        if (blessingsLevel > 0) {
            double chance = blessingsLevel /  10000;
            if (new Random().nextFloat() <= chance) {
                double bless = token / 2;
                Upgrade.BLESSINGS.setName(String.format("%s §b%s", Upgrade.BLESSINGS.getName(), name));
                Upgrade.BLESSINGS.sendProcMessage(player, StringUtils.replaceComma(bless));
            }
        }

        if (luckyLevel > 0) {
            double chance = (luckyLevel / 2) / 40;
            if (new Random().nextFloat() <= chance) {
                Upgrade.LUCKY.sendProcMessage(player, getRandomReward(player));
            }
        }

        if (token_merchantLevel > 0) {
            double chance = (token_merchantLevel / 5) / 2500;
            if (new Random().nextFloat() <= chance) {
                double t_merchant = token_merchantLevel == 0 ? 1 : token_merchantLevel / 2;
                token = token + token * t_merchant;
                Upgrade.TOKEN_MERCHANT.sendProcMessage(player, StringUtils.replaceComma(playerData.addToken(token)));

                token = 750 * token_minerLevel;
            }
        }


        if (explosiveLevel > 0) {
            double chance = (explosiveLevel / 5) / 100;
            if (new Random().nextFloat() <= chance) {
                cost = cost * blockFace.ordinal();
                token = token * blockFace.ordinal();
                new Location(player.getWorld(), blockFace.getModX(), blockFace.getModY(), blockFace.getModZ()).getBlock().setType(Material.AIR);
            }
        }

        if (jack_hammerLevel > 0) {
            double chance = (jack_hammerLevel / 5) / 100;
            if (new Random().nextFloat() <= chance) {
                int i = 1;
                cost = cost * 1;
                token = token * 1;
            }
        }

        if (key_finderLevel > 0) {
            Random random = new Random();
            double chance = (key_finderLevel / 5) / 100;
            if (random.nextFloat() <= chance) {
                int type = random.nextInt(3);
                Key key = opPlayer.getItems().getKeyFromInt(type == 2 ? 0 : type);
                ItemStack keyItem = key.getStack();

                opPlayer.add(keyItem);
                Upgrade.KEY_FINDER.sendProcMessage(player, keyItem.getItemMeta().getDisplayName());
            }
        }

        if (multi_finderLevel > 0) {
            double chance = (multi_finderLevel / 5) / 1000;
            Random random = new Random();
            if (random.nextFloat() <= chance) {
                int multi = random.nextInt(9) + 1;
                Upgrade.MULTI_FINDER.sendProcMessage(player, StringUtils.replaceComma(playerData.addMultiplier(multi)));
            }
        }

        playerData.addBlocks(1);
        playerData.addMoney(cost);
        playerData.addToken(token);
        pickaxe.addExp(1);
    }
}
