package ru.smole.listeners;

import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.Item;
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
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import ru.smole.OpPrison;
import ru.smole.data.PlayerData;
import ru.smole.data.PlayerDataManager;

public class PlayerHandler implements Listener {

    private PlayerDataManager dataManager = OpPrison.getInstance().getPlayerDataManager();

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
    public void onInteract(PlayerInteractEvent e) {
        ItemStack item = e.getItem();

        if (item == null)
            return;

        Material type = item.getType();

        if (type == Material.AIR)
            return;

        Player player = e.getPlayer();
        PlayerData playerData = dataManager.getPlayerDataMap().get(player.getName());
        Action action = e.getAction();

        if (action.equals(Action.RIGHT_CLICK_AIR) || action.equals(Action.RIGHT_CLICK_BLOCK)) {
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
    }

    @EventHandler
    public void onDrop(PlayerDropItemEvent e) {

    }

    @EventHandler
    public void onFood(FoodLevelChangeEvent e) {
        e.setFoodLevel(20);
    }
}
