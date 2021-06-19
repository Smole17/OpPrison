package ru.smole.data.player;

import lombok.Getter;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;
import ru.smole.data.items.Items;
import ru.smole.data.items.pickaxe.PickaxeManager;
import ru.smole.data.prestige.PrestigeManager;
import ru.smole.data.rank.RankManager;

public class OpPlayer {

    private @Getter Player player;
    private @Getter Items items;
    private @Getter RankManager rankManager;
    private @Getter PrestigeManager prestigeManager;
    private @Getter PickaxeManager pickaxeManager;
    public OpPlayer(Player player) {
        this.player = player;
        items = new Items(player);
        rankManager = new RankManager(player);
        prestigeManager = new PrestigeManager(player);
        pickaxeManager = new PickaxeManager(player);
    }

    public void add(ItemStack stack) {
        if (isFull()) {
            player.getWorld().dropItem(player.getLocation(), stack);
            return;
        }

        player.getInventory().addItem(stack);
    }

    public void set(ItemStack stack, int slot) {
        PlayerInventory playerInventory = player.getInventory();

        playerInventory.remove(playerInventory.getItem(slot - 1));
        playerInventory.setItem(slot - 1, stack);
    }

    public boolean isFull() {
        return player.getInventory().firstEmpty() == -1;
    }
}
