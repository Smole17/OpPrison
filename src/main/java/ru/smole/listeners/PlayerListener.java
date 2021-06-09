package ru.smole.listeners;

import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.entity.FoodLevelChangeEvent;
import org.bukkit.event.player.PlayerDropItemEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import ru.smole.OpPrison;
import ru.smole.cases.Case;
import ru.smole.data.PlayerData;
import ru.smole.data.PlayerDataManager;
import ru.smole.guis.CaseLootGui;
import ru.smole.utils.ItemStackUtils;
import ru.xfenilafs.core.util.ChatUtil;

import java.util.Objects;

public class PlayerListener implements Listener {

    private final PlayerDataManager dataManager = OpPrison.getInstance().getPlayerDataManager();

    @EventHandler
    public void onJoin(PlayerJoinEvent e) {
        Player player = e.getPlayer();

        dataManager.load(player);
    }

    @EventHandler
    public void onQuit(PlayerQuitEvent e) {
        Player player = e.getPlayer();

        dataManager.unload(player);
    }

    @EventHandler
    public void onBreak(BlockBreakEvent e) {
        Player player = e.getPlayer();
        PlayerData playerData = dataManager.getPlayerDataMap().get(player.getName());

        Block block = e.getBlock();

        playerData.setBlocks(playerData.getBlocks() + 1);

        e.setDropItems(false);
        e.setExpToDrop(0);
    }

    @EventHandler
    public void onInteract(PlayerInteractEvent event) {
        ItemStack item = event.getItem();
        ItemStack mainHand = event.getPlayer().getInventory().getItemInMainHand();

        if (item == null)
            return;

        Material type = item.getType();

        if (type == Material.AIR)
            return;

        Player player = event.getPlayer();
        PlayerData playerData = dataManager.getPlayerDataMap().get(player.getName());
        Action action = event.getAction();

        if (action == Action.RIGHT_CLICK_AIR || action == Action.RIGHT_CLICK_BLOCK) {
            ItemMeta itemMeta = item.getItemMeta();
            if (type == Material.MAGMA_CREAM) {
                if (itemMeta.hasDisplayName()) {
                    String itemName = itemMeta.getDisplayName();

                    if (itemName.contains("⛃")) {
                        double count = Double.parseDouble(itemName.split("⛃")[1]);

                        playerData.setToken(playerData.getToken() + count);
                        player.getInventory().remove(item);
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
    public void onDrop(PlayerDropItemEvent e) {

    }

    @EventHandler
    public void onFood(FoodLevelChangeEvent e) {
        e.setFoodLevel(20);
    }
}
