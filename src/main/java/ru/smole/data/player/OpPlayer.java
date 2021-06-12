package ru.smole.data.player;

import lombok.Getter;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import ru.smole.data.items.Items;
import ru.smole.data.prestige.PrestigeManager;
import ru.smole.data.rank.RankManager;

public class OpPlayer {

    private @Getter Player player;
    private @Getter Items items;
    private @Getter RankManager rankManager;
    private @Getter PrestigeManager prestigeManager;

    public OpPlayer(Player player) {
        this.player = player;
        items = new Items();
        rankManager = new RankManager(player);
        prestigeManager = new PrestigeManager(player);
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
