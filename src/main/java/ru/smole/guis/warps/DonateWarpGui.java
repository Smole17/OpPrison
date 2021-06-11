package ru.smole.guis.warps;

import lombok.NonNull;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import ru.xfenilafs.core.ApiManager;
import ru.xfenilafs.core.inventory.impl.BaseSimpleInventory;

public class DonateWarpGui extends BaseSimpleInventory {
    public DonateWarpGui() {
        super(6, "Точки телепортации");
    }

    @Override
    public void drawInventory(@NonNull Player player) {
        for (int i = 1; i < inventory.getSize(); i++) {
            if (i == 45 || i == 46 || i == 47 || i == 48 || i == 50 || i >= 52)
                inventory.setItem(i,
                        ApiManager.newItemBuilder(Material.STAINED_GLASS_PANE)
                                .setName(" ")
                                .setDurability(7)
                                .build());
        }

        addItemSelect(49,
                ApiManager.newItemBuilder(Material.IRON_INGOT)
                        .setName("Шахты ранков")
                        .build(), (baseInventory, inventoryClickEvent)
                        -> new DonateWarpGui().drawInventory(player));

        addItemSelect(51,
                ApiManager.newItemBuilder(Material.NETHER_STAR)
                        .setName("Шахты престижей")
                        .build(), (baseInventory, inventoryClickEvent)
                        -> new DonateWarpGui().drawInventory(player));
    }
}
