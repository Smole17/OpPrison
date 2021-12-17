package ru.smole.commands;


import org.bukkit.entity.Player;
import ru.smole.OpPrison;
import ru.xfenilafs.core.command.BukkitCommand;
import ru.xfenilafs.core.util.TeleportUtil;

public class SpawnCommand extends BukkitCommand<Player> {
    public SpawnCommand() {
        super("spawn");
    }

    @Override
    protected void onExecute(Player player, String[] strings) {
        new TeleportUtil(OpPrison.getInstance()).teleport(player, OpPrison.REGIONS.get("spawn").getSpawnLocation(), "§bТелепортация...", "§7Подождите немного");
    }
}
