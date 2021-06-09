package ru.smole.player;

import lombok.Getter;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import ru.xfenilafs.core.util.ChatUtil;

public class OpPlayer {

    private @Getter Player player;

    public OpPlayer(Player player) {
        this.player = player;
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
