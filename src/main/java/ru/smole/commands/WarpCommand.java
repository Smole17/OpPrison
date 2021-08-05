package ru.smole.commands;

import org.bukkit.entity.Player;
import ru.smole.OpPrison;
import ru.smole.guis.warps.WarpGui;
import ru.xfenilafs.core.command.BukkitCommand;

public class WarpCommand extends BukkitCommand<Player> {
    public WarpCommand() {
        super("warp");
    }

    @Override
    protected void onExecute(Player player, String[] strings) {
        new WarpGui(OpPrison.getInstance().getConfigManager())
                .openInventory(player);
    }
}
