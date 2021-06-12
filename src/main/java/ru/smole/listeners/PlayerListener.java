package ru.smole.listeners;

import com.google.common.collect.Lists;
import net.luckperms.api.LuckPermsProvider;
import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.chat.BaseComponent;
import net.md_5.bungee.api.chat.HoverEvent;
import net.md_5.bungee.api.chat.TextComponent;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.entity.FoodLevelChangeEvent;
import org.bukkit.event.player.*;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;
import org.bukkit.inventory.meta.ItemMeta;
import ru.smole.OpPrison;
import ru.smole.cases.Case;
import ru.smole.data.PlayerData;
import ru.smole.data.PlayerDataManager;
import ru.smole.guis.CaseLootGui;
import ru.smole.items.Items;
import ru.smole.player.OpPlayer;
import ru.smole.utils.ItemStackUtils;
import ru.smole.utils.StringUtils;
import ru.xfenilafs.core.util.ChatUtil;

import java.util.List;
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
    public void onBreak(BlockBreakEvent event) {
        if (!event.isCancelled()) {
            Player player = event.getPlayer();
            PlayerData playerData = dataManager.getPlayerDataMap().get(player.getName());

            playerData.setBlocks(playerData.getBlocks() + 1);

            event.setDropItems(false);
            event.setExpToDrop(0);
        }
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
            if (item.getType() == Material.MAGMA_CREAM) {
                ItemMeta itemMeta = item.getItemMeta();

                if (itemMeta.hasDisplayName()) {
                    String itemName = itemMeta.getDisplayName();

                    if (itemName.contains("⛃")) {
                        double count = Double.parseDouble(StringUtils.replaceComma(itemName.split("⛃")[1]));

                        playerData.setToken(playerData.getToken() + count);
                        item.setAmount(0);
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
    public void onDrop(PlayerDropItemEvent event) {

    }

    @EventHandler
    public void onFood(FoodLevelChangeEvent event) {
        event.setFoodLevel(20);
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

        if (msg.contains("#рука")) {
            int amount = item.getAmount();
            String text_peace = amount == 1 ? "" : " §fx" + amount;
            String text = String.format("§8[%s%s§8]",
                    meta.getDisplayName(), text_peace);

            String show = "";

            if (lore != null && !lore.isEmpty()) {
                for (String s : lore) {
                    show = show + "\n" + s;
                }
            }

            BaseComponent[] itemComponent = ChatUtil.newBuilder()
                    .setText(text)
                    .setHoverEvent(HoverEvent.Action.SHOW_TEXT, show)
                    .build();

            return new TextComponent(itemComponent);
        }

        return new TextComponent(msg);
    }
}
