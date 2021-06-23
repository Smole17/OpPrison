package ru.smole.data.items;

import org.bukkit.Material;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import ru.smole.data.items.pickaxe.Pickaxe;
import ru.smole.data.items.pickaxe.Upgrade;
import ru.smole.data.OpPlayer;
import ru.smole.utils.StringUtils;
import ru.xfenilafs.core.ApiManager;

import java.util.ArrayList;
import java.util.List;

public class Items {

    private Player player;

    public Items(Player player) {
        this.player = player;
    }

    public ItemStack getToken(double count) {
        return ApiManager
                .newItemBuilder(Material.MAGMA_CREAM)
                .setName(String.format("§e⛃%s", StringUtils.replaceComma(count)))
                .addLore("§7Нажмите для активации")
                .build();
    }

    public ItemStack getFlyVoucher() {
        return ApiManager
                .newItemBuilder(Material.FEATHER)
                .setName("§fДоступ к полёту §7(/fly)")
                .addLore("§7Нажмите для активации")
                .build();
    }

    public ItemStack getPickaxe() {
        OpPlayer opPlayer = new OpPlayer(player);
        Pickaxe pickaxe = opPlayer.getPickaxeManager().getPickaxes().get(player.getName());

        List<String> lore = new ArrayList<>();
        lore.add("");
        for (Upgrade upgrade : Upgrade.values()) {
            double count = pickaxe.getUpgrades().get(upgrade.ordinal()).get(upgrade);
            if (count == 0)
                continue;

            lore.add(String.format("%s %s", upgrade.getName(), count));
        }
        lore.add("");
        lore.add(String.format("§fУровень: §b%s", pickaxe.getLevel()));
        lore.add(String.format("§fОпыт: §b%s/%s", pickaxe.getExp(), pickaxe.getNeedExp()));

        double efficiency = pickaxe.getUpgrades().get(Upgrade.EFFICIENCY.ordinal()).get(Upgrade.EFFICIENCY);

        return ApiManager.newItemBuilder(Material.DIAMOND_PICKAXE)
                .setUnbreakable(true)
                .addItemFlag(ItemFlag.HIDE_ATTRIBUTES)
                .addItemFlag(ItemFlag.HIDE_DESTROYS)
                .addItemFlag(ItemFlag.HIDE_ENCHANTS)
                .setName(pickaxe.getName())
                .addEnchantment(Enchantment.DIG_SPEED, (int) efficiency)
                .setLore(lore)
                .build();
    }

    public Key getKeyFromString(String key) {
        for (Key type : Key.values())
            if (type.equals(Key.valueOf(key.toUpperCase())))
                return type;

        return null;
    }

    public Key getKeyFromInt(int i) {
        for (Key type : Key.values())
            if (type == Key.values()[i - 1])
                return type;

        return null;
    }
}
