package ru.smole.commands;

import org.bukkit.entity.Player;
import ru.xfenilafs.core.command.BukkitCommand;

public class EnderChestCommand extends BukkitCommand<Player> {
    public EnderChestCommand() {
        super("enderchest", "ec");
    }

    @Override
    protected void onExecute(Player player, String[] strings) {
        player.openInventory(player.getEnderChest());
    }
}
