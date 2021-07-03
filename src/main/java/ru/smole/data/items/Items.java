package ru.smole.data.items;

import lombok.AllArgsConstructor;
import lombok.Getter;
import org.bukkit.Material;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import ru.smole.data.items.crates.Crate;
import ru.smole.data.items.crates.CrateItem;
import ru.smole.data.items.pickaxe.Pickaxe;
import ru.smole.data.items.pickaxe.PickaxeManager;
import ru.smole.data.items.pickaxe.Upgrade;
import ru.smole.utils.StringUtils;
import ru.xfenilafs.core.ApiManager;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Items {

    private Player player;
    public static @Getter Map<String, ItemStack> items;

    public Items(Player player) {
        this.player = player;
    }

    public static void init() {
        items = new HashMap<>();

        items.put("fly",
                ApiManager
                        .newItemBuilder(Material.FEATHER)
                        .setName("§fДоступ к полёту §7(/fly)")
                        .addLore("§7Нажмите для активации")
                        .build());
    }

    public ItemStack getItem(String name) {
        return items
                .getOrDefault
                        (name, ApiManager
                                .newItemBuilder(Material.BARRIER)
                                .setName("§cНеверный предмет")
                                .build());
    }

    public ItemStack getToken(double count) {
        return ApiManager
                .newItemBuilder(Material.MAGMA_CREAM)
                .setName(String.format("§e⛃%s", StringUtils.formatDouble(0, count)))
                .addLore(String.format("§fСодержит - §e%s токенов", StringUtils.replaceComma(count)), "§7Нажмите для активации")
                .build();
    }

    public ItemStack getPickaxe() {
        Pickaxe pickaxe = PickaxeManager.getPickaxes().get(player.getName());

        List<String> lore = new ArrayList<>();
        lore.add("");
        for (Upgrade upgrade : Upgrade.values()) {
            double count = pickaxe
                    .getUpgrades()
                    .get(upgrade);

            if (count == 0)
                continue;

            lore.add(String.format("%s %s", upgrade.getName(), StringUtils._fixDouble(0, count)));
        }
        lore.add("");
        lore.add(String.format("§fУровень: §b%s", (int) pickaxe.getLevel()));
        lore.add(String.format("§fОпыт: §b%s/%s", (int) pickaxe.getExp(), (int) pickaxe.getNeedExp()));

        double efficiency = pickaxe.getUpgrades().get(Upgrade.EFFICIENCY);

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

    @AllArgsConstructor
    public enum Key {

        TOKEN("Токен", ApiManager.newItemBuilder(Material.TRIPWIRE_HOOK).setName("§6Токен").build()),
        MINE("Шахтёрский", ApiManager.newItemBuilder(Material.TRIPWIRE_HOOK).setName("§aШахтёрский").build()),
        EPIC("Эпический", ApiManager.newItemBuilder(Material.TRIPWIRE_HOOK).setName("§5Эпический").build()),
        LEGENDARY("Легендарный", ApiManager.newItemBuilder(Material.TRIPWIRE_HOOK).setName("§6Легендарный").build()),
        MYTHICAL("Мифический", ApiManager.newItemBuilder(Material.TRIPWIRE_HOOK).setName("§cМифический").build()),
        SEASON("Сезонный", ApiManager.newItemBuilder(Material.TRIPWIRE_HOOK).setName("§bСезонный").build());

        private @Getter String name;
        private @Getter ItemStack stack;
    }
}
