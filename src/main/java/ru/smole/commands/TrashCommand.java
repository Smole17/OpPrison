package ru.smole.commands;

import lombok.NonNull;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import ru.xfenilafs.core.command.BukkitCommand;
import ru.xfenilafs.core.inventory.impl.BaseSimpleInventory;

public class TrashCommand extends BukkitCommand<Player> {
    public TrashCommand() {
        super("trash", "rubbish");
    }

    @Override
    protected void onExecute(Player player, String[] strings) {
        Inventory inv = Bukkit.createInventory(null, 54, "§7Мусорка");

        player.openInventory(inv);
    }
}
