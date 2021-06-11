package ru.smole.guis.warps;

import lombok.NonNull;
import org.bukkit.Material;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import ru.smole.OpPrison;
import ru.smole.player.OpPlayer;
import ru.smole.rank.Rank;
import ru.smole.rank.RankManager;
import ru.xfenilafs.core.ApiManager;
import ru.xfenilafs.core.inventory.impl.BaseSimpleInventory;
import ru.xfenilafs.core.regions.Region;

public class PrestigeWarpGui extends BaseSimpleInventory {
    public PrestigeWarpGui() {
        super(6, "����� ������������");
    }

    @Override
    public void drawInventory(@NonNull Player player) {
        for (int i = 1; i < inventory.getSize(); i++) {
            if (i == 45 || i == 46 || i == 48 || i >= 50)
                inventory.setItem(i,
                        ApiManager.newItemBuilder(Material.STAINED_GLASS_PANE)
                                .setName(" ")
                                .setDurability(7)
                                .build());
        }

        addItemSelect(47,
                ApiManager.newItemBuilder(Material.DIAMOND)
                        .setName("����� ����������")
                        .build(), (baseInventory, inventoryClickEvent)
                        -> new DonateWarpGui().drawInventory(player));

        addItemSelect(49,
                ApiManager.newItemBuilder(Material.IRON_INGOT)
                        .setName("����� ������")
                        .build(), (baseInventory, inventoryClickEvent)
                        -> new DonateWarpGui().drawInventory(player));
    }
}
