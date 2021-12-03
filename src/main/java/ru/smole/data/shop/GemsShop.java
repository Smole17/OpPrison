package ru.smole.data.shop;

import org.bukkit.Material;
import org.bukkit.entity.Player;
import ru.smole.guis.MenuGui;
import ru.xfenilafs.core.ApiManager;
import ru.xfenilafs.core.inventory.BaseInventory;
import ru.xfenilafs.core.inventory.item.BaseInventoryClickItem;

public class GemsShop extends Shop {

    public GemsShop(double balance) {
        super(balance, 3, "Гемы");
    }

    @Override
    public void render(BaseInventory baseInventory, Player player) {
        setBuyItem(
                12,
                150,
                (cost, playerData) -> playerData.setGems(playerData.getGems() - cost),
                "legendary_key",
                2.0
        );

        setBuyItem(
                14,
                50000,
                (cost, playerData) -> playerData.setGems(playerData.getGems() - cost),
                "monthly_crate",
                1.0
        );

        setBuyItem(
                16,
                2500,
                (cost, playerData) -> playerData.setGems(playerData.getGems() - cost),
                "air_group",
                1.0
        );

        addItem(
                23,
                ApiManager.newItemBuilder(Material.ARROW)
                        .setName("§cНазад")
                        .build(),
                (baseInventory1, inventoryClickEvent) ->
                        new MenuGui().openInventory(player)
        );

        setGlassPanel( );
    }
}
