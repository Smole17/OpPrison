package ru.smole.guis;

import lombok.NonNull;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import ru.smole.OpPrison;
import ru.smole.data.player.PlayerData;
import ru.smole.guis.warps.WarpGui;
import ru.smole.utils.StringUtils;
import ru.xfenilafs.core.ApiManager;
import ru.xfenilafs.core.inventory.BaseInventoryItem;
import ru.xfenilafs.core.inventory.impl.BaseSimpleInventory;
import ru.xfenilafs.core.util.ItemUtil;

public class MenuGui extends BaseSimpleInventory {
    public MenuGui() {
        super(1, "Меню Режима");
    }

    @Override
    public void drawInventory(@NonNull Player player) {
        String playerName = player.getName();
        PlayerData playerData = OpPrison.getInstance().getPlayerDataManager().getPlayerDataMap().get(playerName);

        ItemStack item = ApiManager.newItemBuilder(Material.SKULL_ITEM)
                .setName("§aВаш Профиль")
                .setLore(
                        "§e▦ Блоки §f" + StringUtils.replaceComma(playerData.getBlocks()),
                        "§8❖ Престиж §f" + StringUtils.replaceComma(playerData.getPrestige()),
                        "§d❃ Множитель §f" + StringUtils.replaceComma(playerData.getMultiplier()),
                        "§f➲ Группа " + playerData.getGroup().getName()
                )
                .setDurability(3)
                .setPlayerSkull(playerName)
                .build();

        addItem(
                5,
                item
        );

        item = ApiManager.newItemBuilder(Material.SKULL_ITEM)
                .setName("§bЛокации Режима")
                .setLore(
                        "§7Телепортация на локации режима",
                        "",
                        "§eНажмите для выбора локации!"
                )
                .setDurability(3)
                .setPlayerSkull("speu")
                .build();

        addItem(1,
                item,
                (inv, click) -> new WarpGui(OpPrison.getInstance().getConfigManager()).openInventory(player));

        item = ApiManager.newItemBuilder(Material.BARRIER)
                .setName("§cЗакрыть")
                .build();

        addItem(9,
                item,
                (inv, click) -> closeInventory(player)
                );

        setGlassPanel();
    }

    private void setGlassPanel() {
        for (int i = 1; i <= this.getInventoryRows() * 9; i++) {
            BaseInventoryItem item = this.getInventoryInfo().getItem(i - 2);

            if (item == null)
                this.addItem(
                        i,
                        ApiManager
                                .newItemBuilder(Material.STAINED_GLASS_PANE)
                                .setName(" ")
                                .setDurability(7)
                                .build()
                );
        }
    }
}
