package ru.smole.data.items;

import lombok.AllArgsConstructor;
import lombok.Getter;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import ru.xfenilafs.core.ApiManager;

@AllArgsConstructor public enum Key {

    TOKEN("Токен", ApiManager.newItemBuilder(Material.TRIPWIRE_HOOK).setName("§6Токен").build()),
    MINE("Шахтёрский", ApiManager.newItemBuilder(Material.TRIPWIRE_HOOK).setName("§aШахтёрский").build()),
    EPIC("Эпический", ApiManager.newItemBuilder(Material.TRIPWIRE_HOOK).setName("§5Эпический").build()),
    LEGENDARY("Легендарный", ApiManager.newItemBuilder(Material.TRIPWIRE_HOOK).setName("§6Легендарный").build()),
    MYTHICAL("Мифический", ApiManager.newItemBuilder(Material.TRIPWIRE_HOOK).setName("§cМифический").build()),
    SEASON("Сезонный", ApiManager.newItemBuilder(Material.TRIPWIRE_HOOK).setName("§bСезонный").build());

    private @Getter String name;
    private @Getter ItemStack stack;
}
