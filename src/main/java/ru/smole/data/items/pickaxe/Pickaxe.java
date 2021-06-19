package ru.smole.data.items.pickaxe;

import lombok.AllArgsConstructor;
import lombok.Data;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.Player;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;
import ru.smole.OpPrison;
import ru.smole.data.PlayerData;
import ru.smole.data.items.Key;
import ru.smole.data.player.OpPlayer;
import ru.smole.utils.BlockUtil;
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

        ChatUtil.sendMessage(player, String.valueOf(blockFace.ordinal()));
        double cost = 750 * fortuneLevel * playerData.getMultiplier();
        double token = 750 * token_minerLevel;

        if (explosiveLevel > 0) {
            double chance = (explosiveLevel / 5) / 100;
            if (new Random().nextFloat() <= chance) {
                cost = cost * blockFace.ordinal();
                token = token * blockFace.ordinal();
                new Location(player.getWorld(), blockFace.getModX(), blockFace.getModY(), blockFace.getModZ()).getBlock().setType(Material.AIR);
            }
        }

        if (key_finderLevel > 0) {
            Random random = new Random();
            double chance = (key_finderLevel / 5) / 100;
            if (random.nextFloat() <= chance) {
                int type = random.nextInt(3);
                Key key = opPlayer.getItems().getKeyFromInt(type);
                ItemStack keyItem = key.getStack();

                opPlayer.add(keyItem);
                ChatUtil.sendMessage(player, OpPrison.getInstance() + String.format("Вы получили %s", keyItem.getI18NDisplayName()));
            }
        }

        playerData.addBlocks(1);
        playerData.addMoney(cost);
        playerData.addToken(token);
        pickaxe.addExp(1);
    }
}
