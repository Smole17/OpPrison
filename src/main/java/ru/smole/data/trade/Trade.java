package ru.smole.data.trade;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;

import lombok.Getter;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import ru.smole.data.PlayerData;
import ru.xfenilafs.core.ApiManager;
import ru.xfenilafs.core.util.ChatUtil;

@Getter
public class Trade {
    private static final HashMap<String, Trade> trades = new HashMap();
    private static final HashSet<String> trade_enabled = new HashSet();
    private static ItemStack accepted = null;
    private static ItemStack accept = null;
    private static Material maccepted = null;
    private static Material maccept = null;
    private final PlayerData first;
    private final PlayerData second;
    private final Inventory inventory;

    public Trade(PlayerData first, PlayerData second) {
        this.first = first;
        this.second = second;
        inventory = Bukkit.createInventory(null, 54, "Обмен");
        if (accept == null) {
            accept = ApiManager.newItemBuilder(Material.REDSTONE_BLOCK)
                    .setName(ChatUtil.color("&aСогласиться на обмен"))
                    .build();
            accepted = ApiManager.newItemBuilder(Material.EMERALD_BLOCK)
                    .setName(ChatUtil.color("&2Согласие на обмен дано"))
                    .build();

            maccepted = accepted.getType();
            maccept = accept.getType();
        }


        ItemStack corner = ApiManager.newItemBuilder(Material.IRON_FENCE)
                .setName(ChatUtil.text("%s - %s", first.getName(), second.getName()))
                .build();

        for (int i = 4; i < 54; i += 9) {
            inventory.setItem(i, corner);
        }

        inventory.setItem(0, accept);
        inventory.setItem(8, accept);
        trades.put(first.getName(), this);
        trades.put(second.getName(), this);
        first.getPlayer().openInventory(inventory);
        second.getPlayer().openInventory(inventory);
    }

    public List<ItemStack> getFirstContents() {
        List<ItemStack> list = new ArrayList();

        for(int i = 1; i < inventory.getSize(); ++i) {
            if (i % 9 < 4) {
                ItemStack is = inventory.getItem(i);
                if (is != null) {
                    list.add(is);
                }
            }
        }

        return list;
    }

    public List<ItemStack> getSecondContents() {
        List<ItemStack> list = new ArrayList();

        for(int i = 0; i < inventory.getSize(); ++i) {
            if (i % 9 > 4 && i != 8) {
                ItemStack is = inventory.getItem(i);
                if (is != null) {
                    list.add(is);
                }
            }
        }

        return list;
    }

    public void handleQuit() {
        trades.remove(first.getName());
        trades.remove(second.getName());
        first.getPlayer().closeInventory();
        second.getPlayer().closeInventory();
        ChatUtil.sendMessage(first.getPlayer(), "&8[&aТорговля&8] &cТорговля была прервана!");
        ChatUtil.sendMessage(second.getPlayer(), "&8[&aТорговля&8] &cТорговля была прервана!");

        for (ItemStack itemStack : getFirstContents()) {
            first.getPlayer().getInventory().addItem(itemStack);
        }

        for (ItemStack itemStack : getSecondContents()) {
            second.getPlayer().getInventory().addItem(itemStack);
        }

        inventory.clear();
    }

    public void updateInventory() {
        first.getPlayer().updateInventory();
        second.getPlayer().updateInventory();
    }

    private void handleFirstPlayerClick(InventoryClickEvent event) {
        int slot = event.getRawSlot();
        Material material1 = inventory.getItem(0).getType();
        Material material2 = inventory.getItem(8).getType();
        if (slot % 9 < 4) {
            if (slot == 0) {
                event.setCancelled(true);
                if (material1 == maccept) {
                    inventory.setItem(0, accepted);
                    if (material2 == maccepted) {
                        makeTradeDone();
                    }
                }
            } else {
                if (material1 == maccepted) {
                    inventory.setItem(0, accept);
                }

                if (material2 == maccepted) {
                    inventory.setItem(8, accept);
                }
            }
        } else {
            event.setCancelled(true);
        }

        updateInventory();
    }

    private void handleSecondPlayerClick(InventoryClickEvent event) {
        int slot = event.getRawSlot();
        Material material1 = inventory.getItem(0).getType();
        Material material2 = inventory.getItem(8).getType();
        if (slot % 9 > 4) {
            if (slot == 8) {
                event.setCancelled(true);
                if (material2 == maccept) {
                    inventory.setItem(8, accepted);
                    if (material1 == maccepted) {
                        makeTradeDone();
                    }
                }
            } else {
                if (material1 == maccepted) {
                    inventory.setItem(0, accept);
                }

                if (material2 == maccepted) {
                    inventory.setItem(8, accept);
                }
            }
        } else {
            event.setCancelled(true);
        }

        updateInventory();
    }

    public void makeTradeDone() {
        trades.remove(first.getName());
        trades.remove(second.getName());
        first.getPlayer().closeInventory();
        second.getPlayer().closeInventory();
        ChatUtil.sendMessage(first.getPlayer(), "&8[&aТорговля&8] &cТорговля была прервана!");
        ChatUtil.sendMessage(second.getPlayer(), "&8[&aТорговля&8] &cТорговля была прервана!");

        for (ItemStack itemStack : getFirstContents()) {
            first.getPlayer().getInventory().addItem(itemStack);
        }

        for (ItemStack itemStack : getSecondContents()) {
            second.getPlayer().getInventory().addItem(itemStack);
        }

        inventory.clear();
    }

    public void handleClick(InventoryClickEvent event) {
        int slot = event.getRawSlot();
        if (slot < inventory.getSize()) {
            Player p = (Player) event.getWhoClicked();
            Inventory top = event.getView().getTopInventory();
            if (top.getName().equals("Обмен")) {
                ClickType click = event.getClick();
                if (click != ClickType.DOUBLE_CLICK && click != ClickType.SHIFT_LEFT && click != ClickType.SHIFT_RIGHT) {
                    String name = ChatUtil.color(top.getItem(4).getItemMeta().getDisplayName());
                    String[] args = name.split(" - ");
                    if (args[0].equalsIgnoreCase(p.getName())) {
                        handleFirstPlayerClick(event);
                    } else if (args[1].equalsIgnoreCase(p.getName())) {
                        handleSecondPlayerClick(event);
                    } else {
                        event.setCancelled(true);
                    }

                } else {
                    event.setCancelled(true);
                }
            }
        }
    }

    public static boolean isTrading(PlayerData playerData) {
        return trades.containsKey(playerData.getName());
    }

    public static Trade getTrading(PlayerData playerData) {
        return trades.get(playerData.getName());
    }

    public static boolean isTradeEnabled(PlayerData playerData) {
        return trade_enabled.contains(playerData.getName());
    }

    public static void toggleTradeEnabled(PlayerData playerData) {
        String name = playerData.getName();
        if (trade_enabled.contains(name)) {
            trade_enabled.remove(name);
            ChatUtil.sendMessage(playerData.getPlayer(), "&8[&2Торговля&8] &aВам больше не могут предлагать торговлю.");
        } else {
            trade_enabled.add(name);
            ChatUtil.sendMessage(playerData.getPlayer(), "&8[&2Торговля&8] &aТеперь вам могут предлагать торговлю.");
        }

    }
}