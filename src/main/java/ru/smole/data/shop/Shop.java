package ru.smole.data.shop;

import com.mojang.authlib.BaseUserAuthentication;
import lombok.AllArgsConstructor;
import lombok.Data;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.player.PlayerPickupArrowEvent;
import org.bukkit.inventory.ItemStack;
import ru.smole.OpPrison;
import ru.smole.data.items.Items;
import ru.smole.data.items.pickaxe.Upgrade;
import ru.smole.data.player.OpPlayer;
import ru.smole.data.player.PlayerData;
import ru.smole.utils.StringUtils;
import ru.xfenilafs.core.ApiManager;
import ru.xfenilafs.core.inventory.BaseInventory;
import ru.xfenilafs.core.inventory.BaseInventoryItem;
import ru.xfenilafs.core.inventory.handler.impl.BaseInventoryClickHandler;
import ru.xfenilafs.core.inventory.impl.BaseSimpleInventory;
import ru.xfenilafs.core.inventory.item.BaseInventoryClickItem;
import ru.xfenilafs.core.inventory.item.BaseInventoryStackItem;
import ru.xfenilafs.core.util.ChatUtil;
import ru.xfenilafs.core.util.ItemUtil;

import java.util.Objects;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.function.Function;

@Data
public abstract class Shop {

    private double balance;
    private BaseInventory inventory;

    public Shop(double balance, int rows, String name) {
        this.balance = balance;
        this.inventory = ApiManager.createSimpleInventory(
                rows,
                "§7Магазин (" + name + ")",
                (player, baseInventory) -> render(baseInventory, player)
        );
    }

    public abstract void render(BaseInventory baseInventory, Player player);

    public void openShop(Player player) {
        inventory.openInventory(player);
    }

    public void setBuyItem(int slot, double cost, BiConsumer<Double, PlayerData> dataConsumer, String item, Object... objects) {
        slot = slot - 1;
        ItemUtil.ItemBuilder builder = ApiManager.newItemBuilder(Objects.requireNonNull(Items.getItem(item, objects)));

        builder.addLore(
                "",
                "§fСтоимость: §3❅" + StringUtils._fixDouble(0, cost),
                "",
                "§eНажмите, чтобы приобрести!"
        );

        addItem(
                new BaseInventoryClickItem(
                        slot,
                        builder.build(),
                        (baseInventory, inventoryClickEvent) -> {
                            Player player = Bukkit.getPlayer(inventoryClickEvent.getWhoClicked().getName());

                            if (balance < cost) {
                                ChatUtil.sendMessage(player, OpPrison.PREFIX_N + "У вас недостаточно средств для приобретения!");
                                player.closeInventory();
                                return;
                            }

                            dataConsumer.accept(cost, OpPrison.getInstance().getPlayerDataManager().getPlayerDataMap().get(player.getName()));
                            OpPlayer.add(player, Objects.requireNonNull(Items.getItem(item, objects)).clone());
                        }
                )
        );
    }

    public void addItem(BaseInventoryItem item) {
        inventory.addItem(item);
    }

    public void addItem(int slot, ItemStack itemStack, BaseInventoryClickHandler handler) {
        addItem(new BaseInventoryClickItem(slot - 1, itemStack, handler));
    }

    public void addItem(int slot, ItemStack itemStack) {
        addItem(new BaseInventoryStackItem(slot - 1, itemStack));
    }

    public void setGlassPanel() {
        for (int i = 1; i <= inventory.getInventoryInfo().getInventoryRows() * 9; i++) {
            BaseInventoryItem item = inventory.getInventoryInfo().getItem(i - 2);

            if (item == null)
                addItem(
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
