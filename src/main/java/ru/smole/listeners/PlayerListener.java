package ru.smole.listeners;

import com.google.common.collect.Lists;
import lombok.val;
import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.chat.BaseComponent;
import net.md_5.bungee.api.chat.HoverEvent;
import net.md_5.bungee.api.chat.TextComponent;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.HumanEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.entity.FoodLevelChangeEvent;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.inventory.*;
import org.bukkit.event.player.*;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import ru.luvas.rmcs.player.RPlayer;
import ru.luvas.rmcs.utils.UtilChat;
import ru.smole.OpPrison;
import ru.smole.data.cases.Case;
import ru.smole.data.PlayerData;
import ru.smole.data.PlayerDataManager;
import ru.smole.data.items.Items;
import ru.smole.data.items.crates.Crate;
import ru.smole.data.items.pickaxe.Pickaxe;
import ru.smole.data.items.pickaxe.PickaxeManager;
import ru.smole.data.items.pickaxe.Upgrade;
import ru.smole.data.OpPlayer;
import ru.smole.data.trade.Trade;
import ru.smole.guis.CaseLootGui;
import ru.smole.guis.PickaxeGui;
import ru.smole.utils.ItemStackUtils;
import ru.smole.utils.StringUtils;
import ru.smole.utils.hologram.Hologram;
import ru.smole.utils.hologram.HologramManager;
import ru.smole.utils.leaderboard.LeaderBoard;
import ru.xfenilafs.core.ApiManager;
import ru.xfenilafs.core.util.ChatUtil;
import ru.xfenilafs.core.util.ItemUtil;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;

public class PlayerListener implements Listener {

    private final PlayerDataManager dataManager = OpPrison.getInstance().getPlayerDataManager();

    @EventHandler
    public void onJoin(PlayerJoinEvent event) {
        Player player = event.getPlayer();

        dataManager.load(player);
    }

    @EventHandler
    public void onQuit(PlayerQuitEvent event) {
        Player player = event.getPlayer();

        dataManager.unload(player);
    }

    @EventHandler
    public void onKick(PlayerKickEvent event) {
        Player player = event.getPlayer();

        dataManager.unload(player);
    }

    @EventHandler
    public void onDeath(PlayerDeathEvent event) {
        event.setKeepInventory(true);
        event.setDroppedExp(0);
    }

    @EventHandler
    public void onRespawn(PlayerRespawnEvent event) {
        Player player = event.getPlayer();
        ChatUtil.sendMessage(player, "yep");

        LeaderBoard.holograms.forEach(s -> {
            Hologram hologram = OpPrison.getInstance().getHologramManager().getCachedHologram(s);

            hologram.removeReceiver(player);
            hologram.addReceiver(player);
            hologram.refreshHologram();
        });
    }

    @EventHandler
    public void onChat(AsyncPlayerChatEvent event) {
        sendChat(event);
    }

    @EventHandler
    public void onInteract(PlayerInteractEvent event) {
        Items.interact(event);

        Player player = event.getPlayer();
        Block block = event.getClickedBlock();
        Action action = event.getAction();

        if (event.hasBlock()) {
            Case customCase = Case.getCustomCaseByLocation(block);

            if (block.getType() == Material.CHEST && event.getHand() == EquipmentSlot.HAND && customCase != null)
                switch (action) {
                    case LEFT_CLICK_BLOCK:
                        new CaseLootGui(customCase).openInventory(player);
                        event.setCancelled(true);

                        break;
                    case RIGHT_CLICK_BLOCK:
                        event.setCancelled(true);

                        break;
                }
        }
    }

    @EventHandler
    public void onSwitch(PlayerSwapHandItemsEvent event) {
        event.setCancelled(true);
    }

    @EventHandler
    public void onCraft(CraftItemEvent event) {
        event.setCancelled(true);
    }

    @EventHandler
    public void onOpen(InventoryOpenEvent event) {
        Player player = Bukkit.getPlayer(event.getPlayer().getName());
        OpPlayer opPlayer = new OpPlayer(player);
        ChatUtil.sendMessage(player, event.getInventory().getType().name());

        if (event.getInventory().getType() == InventoryType.PLAYER) {
            ChatUtil.sendMessage(player, event.getInventory().getName());
            opPlayer.set(Items.getItem("pickaxe", player.getName()), 1);
        }

        opPlayer.set(Items.getItem("pickaxe", player.getName()), 1);
    }

    @EventHandler
    public void onClose(InventoryCloseEvent event) {
        Inventory inv = event.getInventory();
        val crates = Crate.crates;

        if (!crates.isEmpty())
            crates.forEach((s, crate) -> {
                if (inv.getName().equals(crate.getType().getName())) {
                    List<ItemStack> items = crate.getItems();

                    if (!items.isEmpty()) {
                        items.forEach(itemStack -> {
                            Player player = Bukkit.getPlayer(event.getPlayer().getName());

                            new OpPlayer(player).add(itemStack);
                            ChatUtil.sendMessage(player, OpPrison.PREFIX + "Вы получили недостающий предмет %s", itemStack.getItemMeta().getDisplayName());
                        });

                        items.clear();
                    }
                }
            });
    }

    @EventHandler
    public void onClick(InventoryClickEvent event) {
        Player player = Bukkit.getPlayer(event.getWhoClicked().getName());
        ItemStack curItem = event.getCurrentItem();

        if(event.getHotbarButton() != -1) {
            ItemStack item = player.getInventory().getContents()[event.getHotbarButton()];
            if(item != null)
                if (Items.isSomePickaxe(item, player.getName()))
                    event.setCancelled(true);
        }

        if (curItem != null) {
            if (Items.isSomePickaxe(curItem, player.getName())) {
                event.setCancelled(true);
            }
        }

        PlayerData playerData = OpPrison.getInstance().getPlayerDataManager().getPlayerDataMap().get(player.getName());
        Trade trade = Trade.getTrading(playerData);
        if (trade != null) {
            trade.handleClick(event);
        }
    }

    @EventHandler
    public void onDrop(PlayerDropItemEvent event) {
        ItemStack item = event.getItemDrop().getItemStack();

        if (item.getType() == Material.AIR)
            return;

        if (Items.isSomePickaxe(item, event.getPlayer().getName())) {
            event.setCancelled(true);
        }
    }

    @EventHandler
    public void onFood(FoodLevelChangeEvent event) {
        event.setFoodLevel(20);
    }

    public void sendChat(AsyncPlayerChatEvent event) {
        Player player = event.getPlayer();
        String name = player.getName();
        String msg = event.getMessage();
        ItemStack item = event.getPlayer().getInventory().getItemInMainHand();

        PlayerData playerData = OpPrison.getInstance().getPlayerDataManager().getPlayerDataMap().get(player.getName());

        String prefix = RPlayer.checkAndGet(name).getLongPrefix();

        String format = String.format("§8[§a%s§8] %s%s§7: §f",
                StringUtils.formatDouble(String.valueOf(playerData.getPrestige()).length() <= 3 ? 0 : 2, playerData.getPrestige()),
                prefix.replace('&', '§'), player.getName()
        );

        List<String> lore = Lists.newArrayList(
                String.format("&fНик: &b%s %s", prefix, name),
                "&fПрестижи: &b" + StringUtils.formatDouble(2, playerData.getPrestige()),
                "&fДобыто блоков: &e" + StringUtils._fixDouble(0, playerData.getBlocks()),
                "&fГруппа: &b" + playerData.getGroup().getName(),
                "",
                "&fДеньги: &a" + StringUtils.formatDouble(2, playerData.getMoney()),
                "&fТокенов: &e" + StringUtils.formatDouble(2, playerData.getToken()),
                "&fМножитель: &d" + StringUtils._fixDouble(0, playerData.getMultiplier())
        );
        BaseComponent[] comps = new BaseComponent[lore.size()];

        for (int i = 0; i < lore.size(); ++i) {
            comps[i] = new TextComponent(ChatUtil.color(String.format("%s%s", lore.get(i), i == lore.size() - 1 ? "" : "\n")));
        }

        TextComponent _component = new TextComponent(format.replaceFirst("!", ""));
        _component.setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, comps));
        _component.addExtra(getHand(msg, item));

        event.setCancelled(true);
        Bukkit.getOnlinePlayers().forEach(players -> players.spigot().sendMessage(_component));

        Bukkit.getConsoleSender().sendMessage(format + msg);
    }


    public TextComponent getHand(String msg, ItemStack item) {
        if (item == null)
            return new TextComponent(msg);

        if (item.getType() == Material.AIR)
            return new TextComponent(msg);

        if (msg.equals("#рука") || msg.equals("#hand") || msg.equals("[item]")) {
            int amount = item.getAmount();
            String text_peace = amount == 1 ? "" : " §fx" + amount;
            String text = String.format("§8[§f%s%s§8]",
                    item.hasItemMeta() ? item.getItemMeta().getDisplayName()
                            : Bukkit.getServer().getItemFactory().getItemMeta(item.getType()).getDisplayName(), text_peace);

//            msg = text;
//
//            ItemMeta meta = item.getItemMeta();
//            if (meta == null)
//                return new TextComponent(msg);
//
//            StringBuilder show = new StringBuilder();
//            List<String> lore = meta.getLore();
//
//            if (lore != null && !lore.isEmpty()) {
//                for (String s : lore) {
//                    show.append("\n").append(s);
//                }
//            }

            BaseComponent[] itemComponent = ChatUtil.newBuilder()
                    .setText(text)
                    .setHoverEvent(HoverEvent.Action.SHOW_ITEM, ItemStackUtils.convertItemStackToJsonRegular(item))
                    .build();

            return new TextComponent(itemComponent);
        }

        return new TextComponent(msg);
    }
}
