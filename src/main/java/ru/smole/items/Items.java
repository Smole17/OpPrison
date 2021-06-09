package ru.smole.items;

import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import ru.xfenilafs.core.ApiManager;

public class Items {

    public static ItemStack getToken(int count) {
        return ApiManager.newItemBuilder(Material.MAGMA_CREAM).setName("§e⛃" + count).addLore("§7Нажмите для активации").build();
    }
}
