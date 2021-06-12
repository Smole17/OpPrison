package ru.smole.data.items;

import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import ru.smole.utils.StringUtils;
import ru.xfenilafs.core.ApiManager;

public class Items {

    public Items() {
    }

    public ItemStack getToken(double count) {
        return ApiManager
                .newItemBuilder(Material.MAGMA_CREAM)
                .setName(String.format("§e⛃%s", StringUtils._formatDouble(count)))
                .addLore("§7Нажмите для активации")
                .build();
    }

    public Key getKeyFromString(String key) {
        for (Key type : Key.values())
            if (type.equals(Key.valueOf(key.toUpperCase())))
                return type;

        return null;
    }
}
