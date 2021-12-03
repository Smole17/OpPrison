package ru.smole.guis;

import lombok.NonNull;
import org.bukkit.Material;
import org.bukkit.entity.EnderCrystal;
import org.bukkit.entity.Player;
import org.bukkit.event.block.Action;
import org.bukkit.event.inventory.InventoryAction;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import ru.smole.OpPrison;
import ru.smole.commands.EnderChestCommand;
import ru.smole.commands.GangCommand;
import ru.smole.commands.GangSetCommand;
import ru.smole.data.gang.GangData;
import ru.smole.data.gang.GangDataManager;
import ru.smole.data.player.OpPlayer;
import ru.smole.data.player.PlayerData;
import ru.smole.data.shop.GemsShop;
import ru.smole.guis.warps.WarpGui;
import ru.smole.utils.StringUtils;
import ru.xfenilafs.core.ApiManager;
import ru.xfenilafs.core.inventory.BaseInventoryItem;
import ru.xfenilafs.core.inventory.impl.BaseSimpleInventory;
import ru.xfenilafs.core.util.ItemUtil;

public class MenuGui extends BaseSimpleInventory {
    public MenuGui() {
        super(6, "§7Меню Режима");
    }

    @Override
    public void drawInventory(@NonNull Player player) {
        String playerName = player.getName();
        PlayerData playerData = OpPrison.getInstance().getPlayerDataManager().getPlayerDataMap().get(playerName);
        GangDataManager gangDataManager = OpPrison.getInstance().getGangDataManager();

        ItemStack item = ApiManager.newItemBuilder(Material.SKULL_ITEM)
                .setName("§aВаш Профиль")
                .setLore(
                        "§e❏ §fБлоки §e" + StringUtils.replaceComma(playerData.getBlocks()),
                        "§a❖ §fПрестиж §a" + StringUtils.replaceComma(playerData.getPrestige()),
                        "§d❃ §fМножитель §d" + StringUtils.replaceComma(playerData.getMultiplier()),
                        "§f➲ §fГруппа " + playerData.getGroup().getName(),
                        "§6✈ §fБанда " + (gangDataManager.playerHasGang(playerName) ? gangDataManager.getGangFromPlayer(playerName).getName() : "отсутствует")
                )
                .setDurability(3)
                .setPlayerSkull(playerName)
                .build();

        addItem(
                23,
                item
        );

        item = ApiManager.newItemBuilder(Material.BARRIER)
                .setName("§cЗакрыть")
                .build();

        addItem(50,
                item,
                (inv, click) -> closeInventory(player));

        item = ApiManager.newItemBuilder(Material.ENDER_CHEST)
                .setName("§dЛичное Хранилище")
                .setLore(
                        "§7Ваше личное хранилище,",
                        "§7эндер-сундук.",
                        "",
                        "§eНажмите для открытия!"
                )
                .build();

        addItem(34,
                item,
                (inv, click) -> player.openInventory(player.getEnderChest()));

        item = ApiManager.newItemBuilder(Material.BEACON)
                .setName("§bУвеличить Престиж")
                .setLore(
                        "§7Используйте предоставленные",
                        "§7варианты увеличения,",
                        "§7Вашего престижа.",
                        "",
                        "§eНажмите ЛКМ для увеличения на 1 ед.!",
                        "§eНажмите ПКМ для увеличения на все деньги!"
                )
                .build();

        OpPlayer opPlayer = new OpPlayer(player);

        addItem(
                33,
                item,
                (inv, click) -> {
                    if (click.isLeftClick()) {
                        opPlayer.getPrestigeManager().up(1);
                        return;
                    }

                    if (click.isRightClick()) {
                        opPlayer.getPrestigeManager().up(2);
                        return;
                    }

                    closeInventory(player);
                }
        );

        item = ApiManager.newItemBuilder(Material.SKULL_ITEM)
                .setName("§2Локации Режима")
                .setLore(
                        "§7Телепортация на локации режима.",
                        "",
                        "§eНажмите для выбора локации!"
                )
                .setDurability(3)
                .setPlayerSkull("__planet")
                .build();

        addItem(32,
                item,
                (inv, click) -> new WarpGui(OpPrison.getInstance().getConfigManager()).openInventory(player));


        item = ApiManager.newItemBuilder(Material.SKULL_ITEM)
                .setName("§3Магазин Гемов")
                .setLore(
                        "§7Выдаёт список предметом,",
                        "§7которые можно приобрести",
                        "§7за гемы.",
                        "",
                        "§eНажмите для открытия!"
                )
                .setDurability(3)
                .setPlayerSkull("_Olympia_")
                .build();

        addItem(31,
                item,
                (inv, click) ->
                        new GemsShop(playerData.getGems()).openShop(player)
                );

        item = ApiManager.newItemBuilder(Material.GOLD_SWORD)
                .setName("§6Банда")
                .setLore(
                        "§7Информация о вашей банде.",
                        "",
                        "§eНажмите для открытия!"
                )
                .build();

        addItem(30,
                item,
                (inv, click) ->
                        new GangGui(OpPrison.getInstance().getGangDataManager().getGangFromPlayer(playerName)).open(player)
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
