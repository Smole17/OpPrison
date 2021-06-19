package ru.smole.listeners;

import com.google.common.collect.Lists;
import net.luckperms.api.LuckPermsProvider;
import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.chat.BaseComponent;
import net.md_5.bungee.api.chat.HoverEvent;
import net.md_5.bungee.api.chat.TextComponent;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.HumanEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.entity.FoodLevelChangeEvent;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.event.player.*;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import ru.smole.OpPrison;
import ru.smole.data.cases.Case;
import ru.smole.data.PlayerData;
import ru.smole.data.PlayerDataManager;
import ru.smole.data.items.Items;
import ru.smole.data.items.pickaxe.Pickaxe;
import ru.smole.data.items.pickaxe.Upgrade;
import ru.smole.data.player.OpPlayer;
import ru.smole.data.trade.Trade;
import ru.smole.guis.CaseLootGui;
import ru.smole.guis.PickaxeGui;
import ru.smole.utils.ItemStackUtils;
import ru.smole.utils.StringUtils;
import ru.xfenilafs.core.util.ChatUtil;

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
    public void onChat(AsyncPlayerChatEvent event) {
        Player player = event.getPlayer();
        String msg = event.getMessage();
        ItemStack item = event.getPlayer().getInventory().getItemInMainHand();

        PlayerData playerData = OpPrison.getInstance().getPlayerDataManager().getPlayerDataMap().get(player.getName());

        String prefix = Objects.requireNonNull(LuckPermsProvider.get()
                .getUserManager().getUser(player.getName()))
                .getCachedData().getMetaData(LuckPermsProvider.get().getContextManager().getQueryOptions(player))
                .getPrefix();

        String _format = String.format("[%s] %s %s",
                msg.startsWith("!") ? "G" : "L", prefix, player.getName()
        );

        String format = String.format("§7: §f%s",
                msg.startsWith("!") ? msg.substring(1) : msg
        );

        List<String> lore = Lists.newArrayList(
                String.format("&fНик: &b%s %s", prefix, player.getName()),
                "&fПрестиж: &b" + StringUtils.formatDouble(0, playerData.getPrestige()),
                "&fРанк: &b" + playerData.getRank().getName(),
                "&fТокенов: &b" + StringUtils.formatDouble(1, playerData.getToken()),
                "&fБлоков вскопано: &b" + StringUtils._fixDouble(0, playerData.getBlocks())
        );
        BaseComponent[] comps = new BaseComponent[lore.size()];

        for (int i = 0; i < lore.size(); ++i) {
            comps[i] = new TextComponent(ChatColor.translateAlternateColorCodes('&', String.format("%s%s", lore.get(i), i == lore.size() - 1 ? "" : "\n")));
        }

        TextComponent _component = new TextComponent(_format);
        _component.setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, comps));
        _component.addExtra(getHand(format, item));

        event.setCancelled(true);
        if (msg.startsWith("!")) {
            Bukkit.getOnlinePlayers().forEach(players -> players.spigot().sendMessage(_component));
        } else {
            player.getLocation().getNearbyPlayers(200).forEach(players -> players.spigot().sendMessage(_component));
        }

        Bukkit.getConsoleSender().sendMessage(format);
    }

    @EventHandler
    public void onInteract(PlayerInteractEvent event) {
        Player player = event.getPlayer();
        ItemStack item = player.getInventory().getItemInMainHand();

        if (item == null)
            return;

        Material type = item.getType();

        if (type == Material.AIR)
            return;

        PlayerData playerData = dataManager.getPlayerDataMap().get(player.getName());
        Action action = event.getAction();

        if (action == Action.RIGHT_CLICK_AIR || action == Action.RIGHT_CLICK_BLOCK) {
            ItemMeta itemMeta = item.getItemMeta();
            if (itemMeta.hasDisplayName()) {
                String itemName = itemMeta.getDisplayName();
                Items items = new OpPlayer(player).getItems();

                if (type == Material.MAGMA_CREAM) {
                    if (itemName.contains("⛃")) {
                        double count = Double.parseDouble(StringUtils.unReplaceComma(itemName.split("⛃")[1]));

                        playerData.setToken(playerData.getToken() + count);
                        item.setAmount(0);
                        return;
                    }
                }

                if (type == Material.FEATHER) {
                    if (item == items.getFlyVoucher()) {
                        if (playerData.isFly())
                            return;

                        playerData.setFly(true);
                        item.setAmount(0);
                        return;
                    }
                }

                if (type == Material.DIAMOND_PICKAXE) {
                    if (item.hasItemMeta()) {
                        new PickaxeGui().openInventory(player);
                    }
                }
            }
        }

        if (action == Action.RIGHT_CLICK_BLOCK && event.getClickedBlock() != null) {
            if (event.getClickedBlock().getType() == Material.CHEST && event.getHand() == EquipmentSlot.HAND && Case.getCustomCaseByLocation(event.getClickedBlock()) != null) {
                event.setCancelled(true);
                Case customCase = Case.getCustomCaseByLocation(event.getClickedBlock());
                if (event.getAction() == Action.RIGHT_CLICK_BLOCK) {
                    if (player.getInventory().getItemInMainHand() == null) {
                        ChatUtil.sendMessage(player, OpPrison.PREFIX + "Для открытия этого сундука вам необходим %s", Objects.requireNonNull(customCase).getKey());
                        return;
                    }

                    ItemStack is = player.getInventory().getItemInMainHand();
                    if (!ItemStackUtils.hasName(is, Objects.requireNonNull(customCase).getKey())) {
                        ChatUtil.sendMessage(player, OpPrison.PREFIX + "Для открытия этого сундука вам необходим %s" + customCase.getKey());
                        return;
                    }

                    customCase.open(player, player.isSneaking());
                }
            }
        }

        if (event.getAction() == Action.LEFT_CLICK_BLOCK) {
            if (event.getClickedBlock().getType() == Material.CHEST && event.getHand() == EquipmentSlot.HAND && Case.getCustomCaseByLocation(event.getClickedBlock()) != null) {
                event.setCancelled(true);
                Case customCase = Case.getCustomCaseByLocation(event.getClickedBlock());
                new CaseLootGui(customCase).openInventory(player);
            }
        }
    }

    @EventHandler
    public void onClick(InventoryClickEvent event) {
        HumanEntity player = event.getWhoClicked();
        ClickType type = event.getClick();
        InventoryType inventoryType = event.getClickedInventory().getType();
        ItemStack pickaxe = new OpPlayer(player.getKiller()).getItems().getPickaxe();

        if (inventoryType == InventoryType.PLAYER) {
            if (event.getCurrentItem() == pickaxe && event.getSlot() == 0) {
                if (type == ClickType.NUMBER_KEY) {
                    event.setCancelled(true);
                }

                event.setCancelled(true);
            }

            return;
        }

        PlayerData playerData = OpPrison.getInstance().getPlayerDataManager().getPlayerDataMap().get(player.getName());
        Trade trade = Trade.getTrading(playerData);
        if (trade != null) {
            trade.handleClick(event);
        }
    }

    @EventHandler
    public void onDrop(PlayerDropItemEvent event) {
        OpPlayer opPlayer = new OpPlayer(event.getPlayer());
        ItemStack item = event.getItemDrop().getItemStack();

        if (item.getType() == Material.AIR)
            return;

        if (item == opPlayer.getItems().getPickaxe()) {
            event.setCancelled(true);
        }
    }

    @EventHandler
    public void onFood(FoodLevelChangeEvent event) {
        event.setFoodLevel(20);
    }

    @EventHandler
    public void heldItem(PlayerItemHeldEvent e) {
        Player player = e.getPlayer();
        ItemStack item = player.getActiveItem();

        OpPlayer opPlayer = new OpPlayer(player);
        ItemStack pickItem = opPlayer.getItems().getPickaxe();

        if (item == pickItem) {
            String name = player.getName();
            Pickaxe pickaxe = opPlayer.getPickaxeManager().getPickaxes().get(name);
            List<Map<Upgrade, Double>> upgrades = pickaxe.getUpgrades();

            double hasteLevel = upgrades.get(0).get(Upgrade.HASTE);
            double speedLevel = upgrades.get(0).get(Upgrade.SPEED);
            double jump_boostLevel = upgrades.get(0).get(Upgrade.JUMP_BOOST);
            double night_visionLevel = upgrades.get(0).get(Upgrade.NIGHT_VISION);

            PotionEffect haste = new PotionEffect(PotionEffectType.FAST_DIGGING, 9999, (int) hasteLevel);
            PotionEffect speed = new PotionEffect(PotionEffectType.SPEED, 9999, (int) speedLevel);
            PotionEffect jump = new PotionEffect(PotionEffectType.JUMP, 9999, (int) jump_boostLevel);
            PotionEffect night = new PotionEffect(PotionEffectType.NIGHT_VISION, 9999, (int) night_visionLevel);

            player.addPotionEffect(haste);
            player.addPotionEffect(speed);
            player.addPotionEffect(jump);
            player.addPotionEffect(night);
        }
    }


    public TextComponent getHand(String msg, ItemStack item) {
        if (item == null)
            return new TextComponent(msg);

        if (item.getType() == Material.AIR)
            return new TextComponent(msg);

        ItemMeta meta = item.getItemMeta();

        if (meta == null)
            return new TextComponent(msg);

        List<String> lore = meta.getLore();

        if (msg.equals("#рука")) {
            int amount = item.getAmount();
            String text_peace = amount == 1 ? "" : " §fx" + amount;
            String text = String.format("§8[%s%s§8]",
                    meta.getDisplayName(), text_peace);

            StringBuilder show = new StringBuilder();

            if (lore != null && !lore.isEmpty()) {
                for (String s : lore) {
                    show.append("\n").append(s);
                }
            }

            BaseComponent[] itemComponent = ChatUtil.newBuilder()
                    .setText(text)
                    .setHoverEvent(HoverEvent.Action.SHOW_TEXT, show.toString())
                    .build();

            return new TextComponent(itemComponent);
        }

        return new TextComponent(msg);
    }
}
