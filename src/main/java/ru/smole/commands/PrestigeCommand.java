package ru.smole.commands;

import org.bukkit.entity.Player;
import ru.smole.data.player.OpPlayer;
import ru.smole.data.prestige.PrestigeManager;
import ru.xfenilafs.core.command.BukkitCommand;

public class PrestigeCommand extends BukkitCommand<Player> {
    public PrestigeCommand() {
        super("prestige", "pr", "pres");
    }

    @Override
    protected void onExecute(Player player, String[] args) {
        PrestigeManager prestigeManager = new OpPlayer(player).getPrestigeManager();

        if (args.length == 1) {
            if (args[0].equalsIgnoreCase("max")) {
                prestigeManager.up(2);

                return;
            }
        }

        prestigeManager.up(1);
    }
}
