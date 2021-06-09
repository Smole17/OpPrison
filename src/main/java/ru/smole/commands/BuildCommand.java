package ru.smole.commands;

import org.bukkit.entity.Player;
import ru.xfenilafs.core.command.BukkitCommand;
import ru.xfenilafs.core.command.annotation.CommandPermission;
import ru.xfenilafs.core.util.ChatUtil;

import static ru.smole.OpPrison.BUILD_MODE;

@CommandPermission(permission = "mlf.admin")
public class BuildCommand extends BukkitCommand<Player> {

    public BuildCommand() {
        super("build");
    }

    @Override
    protected void onExecute(Player player, String[] args) {
        boolean contains = BUILD_MODE.contains(player);
        if (contains) {
            BUILD_MODE.remove(player);
            ChatUtil.sendMessage(player, "&8[&bКоманды&8] &cБольше вы не можете вносить изменения на карте!");
        } else {
            BUILD_MODE.add(player);
            ChatUtil.sendMessage(player, "&8[&bКоманды&8] &aТеперь вы можете вносить изменения на карте!");
        }
    }
}
