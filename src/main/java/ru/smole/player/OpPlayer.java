package ru.smole.player;

import lombok.Getter;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import ru.smole.items.Items;
import ru.smole.rank.RankManager;

public class OpPlayer {

    private @Getter Player player;
    private @Getter Items items;
    private @Getter RankManager rankManager;

    public OpPlayer(Player player) {
        this.player = player;
        items = new Items();
        rankManager = new RankManager(player);
    }

    public void add(ItemStack stack) {
        if (isFull()) {
            player.getWorld().dropItem(player.getLocation(), stack);
            return;
        }

        player.getInventory().addItem(stack);
    }

    public boolean isFull() {
        return player.getInventory().firstEmpty() == -1;
    }
}
