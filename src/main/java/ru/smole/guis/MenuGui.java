package ru.smole.guis;

import lombok.NonNull;
import org.bukkit.Material;
import org.bukkit.entity.EnderCrystal;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import ru.smole.OpPrison;
import ru.smole.commands.EnderChestCommand;
import ru.smole.commands.GangCommand;
import ru.smole.commands.GangSetCommand;
import ru.smole.data.gang.GangData;
import ru.smole.data.gang.GangDataManager;
import ru.smole.data.player.PlayerData;
import ru.smole.guis.warps.WarpGui;
import ru.smole.utils.StringUtils;
import ru.xfenilafs.core.ApiManager;
import ru.xfenilafs.core.inventory.BaseInventoryItem;
import ru.xfenilafs.core.inventory.impl.BaseSimpleInventory;
import ru.xfenilafs.core.util.ItemUtil;

public class MenuGui extends BaseSimpleInventory {
    public MenuGui() {
        super(5, "§lМеню режима");
    }

    @Override
    public void drawInventory(@NonNull Player player) {
        String playerName = player.getName();
        PlayerData playerData = OpPrison.getInstance().getPlayerDataManager().getPlayerDataMap().get(playerName);

        ItemStack item = ApiManager.newItemBuilder(Material.SKULL_ITEM)
                .setName("§aВаш Профиль")
                .setLore(
                        "§e▦  §fБлоки: §e" + StringUtils.replaceComma(playerData.getBlocks()),
                        "§a❖ §fПрестиж: §a" + StringUtils.replaceComma(playerData.getPrestige()),
                        "§d❃ §fМножитель: §d" + StringUtils.replaceComma(playerData.getMultiplier()),
                        "§f➲ §fГруппа: " + playerData.getGroup().getName()
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

        addItem(41,
                item,
                (inv, click) -> new WarpGui(OpPrison.getInstance().getConfigManager()).openInventory(player));

        item = ApiManager.newItemBuilder(Material.ENDER_CHEST)
                .setName("§bЛичное хранилище")
                .setLore(
                        "",
                        "§eНажмите для открытия!"
                )
                .setDurability(0)
                .build();

       addItem(25,
                item,
                (inv, click) -> player.openInventory(player.getEnderChest()));

        item = ApiManager.newItemBuilder(Material.CHEST)
                .setName("§bХранилище банды")
                .setLore(
                        "",
                        "§eНажмите для открытия!"
                )
                .setDurability(0)
                .build();

       addItem(21,
                item,
                (inv, click) -> OpPrison.getInstance().getGangDataManager().getGangFromPlayer(playerName).openVault(player));

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
