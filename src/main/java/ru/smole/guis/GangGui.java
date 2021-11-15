package ru.smole.guis;

import lombok.AccessLevel;
import lombok.NonNull;
import lombok.experimental.FieldDefaults;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import ru.smole.OpPrison;
import ru.smole.data.gang.GangData;
import ru.smole.utils.StringUtils;
import ru.xfenilafs.core.ApiManager;
import ru.xfenilafs.core.inventory.BaseInventoryItem;
import ru.xfenilafs.core.inventory.impl.BaseSimpleInventory;
import ru.xfenilafs.core.util.ChatUtil;

import java.util.ArrayList;
import java.util.List;

@FieldDefaults(makeFinal = true, level = AccessLevel.PRIVATE)
public class GangGui extends BaseSimpleInventory {

    GangData gangData;

    public GangGui(GangData gangData) {
        super(4, "§7Банда");
        this.gangData = gangData;
    }

    @Override
    public void drawInventory(@NonNull Player player) {
        enableAutoUpdate(player, null, 20);
        ItemStack item;

        GangData.GangPlayer gangPlayer = gangData.findGangPlayers(GangData.GangPlayer.GangPlayerType.LEADER).stream().findFirst().orElse(null);

        if (gangPlayer != null) {
            item = ApiManager.newItemBuilder(Material.SKULL_ITEM)
                    .setName("§aВаша Банда")
                    .setLore(
                            "§f✍ §fНазвание " + gangData.getName(),
                            "§6☀ §fОчки " + StringUtils.replaceComma(gangData.getScore()),
                            "§e❃ §fГлава " + gangPlayer.getPlayerName(),
                            String.format("§a✌ §fУчастники %s/10", gangData.getGangPlayerMap().size())
                    )
                    .setDurability(3)
                    .setPlayerSkull(gangPlayer.getPlayerName())
                    .build();

            addItem(14,
                    item
            );
        }

        item = ApiManager.newItemBuilder(Material.CHEST)
                .setName("§6Хранилище")
                .setLore(
                        "",
                        "§eНажмите для открытия!"
                )
                .build();

        addItem(22,
                item,
                (inv, click) -> gangData.openVault(player)
        );

        List<String> lore = new ArrayList<>();

        int[] i = {1};
        gangData.findGangPlayers(
                GangData.GangPlayer.GangPlayerType.values()
        )
                .forEach(gangPlayer1 -> {
                    lore.add(String.format(
                            "§7%s. %s §7%s §6☀ %s §8(%s§8)",
                            i[0], gangPlayer1.getType().getName(), gangPlayer1.getPlayerName(),
                            StringUtils.replaceComma(gangPlayer1.getScore()), gangPlayer1.getPlayer().isOnline() ? "§a+" : "§c-")
                    );
                    i[0]++;
                });

        item = ApiManager.newItemBuilder(Material.CHEST)
                .setName("§bУчастники")
                .setLore(lore)
                .build();

        addItem(24,
                item
        );

        item = ApiManager.newItemBuilder(Material.BARRIER)
                .setName("§cЗакрыть")
                .build();

        addItem(32,
                item,
                (inv, click) -> closeInventory(player)
        );

        setGlassPanel();
    }

    void setGlassPanel() {
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

    void open(@NonNull Player player) {
        if (gangData == null || !gangData.hasGangPlayer(player.getName())) {
            ChatUtil.sendMessage(player, OpPrison.PREFIX_N + "Вы не состоите в банде");
            return;
        }

        openInventory(player);
    }
}
