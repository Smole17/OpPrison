package ru.smole.commands;

import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import ru.smole.OpPrison;
import ru.smole.data.group.GroupsManager;
import ru.smole.data.player.PlayerData;
import ru.xfenilafs.core.ApiManager;
import ru.xfenilafs.core.command.BukkitCommand;
import ru.xfenilafs.core.util.ChatUtil;

public class RepairCommand extends BukkitCommand<Player> {
    public RepairCommand() {
        super("repair");
    }

    @Override
    protected void onExecute(Player player, String[] args) {
        PlayerData playerData = OpPrison.getInstance().getPlayerDataManager().getPlayerDataMap().get(player.getName());

        if (!GroupsManager.Group.SUN.isCan(playerData.getGroup())) {
            ChatUtil.sendMessage(player, OpPrison.PREFIX + "Доступно от %s", GroupsManager.Group.SUN.getName());
            return;
        }

        ItemStack itemStack = player.getInventory().getItemInMainHand();

        if (itemStack == null || itemStack.getType() == Material.AIR) {
            ChatUtil.sendMessage(player, OpPrison.PREFIX + "Возьмите в руку корректный предмет");
            return;
        }

        if (itemStack.getType().getMaxDurability() == 0)
            return;

        itemStack.setDurability((short) 0);
        ChatUtil.sendMessage(player, OpPrison.PREFIX + "Вы починили предмет");
    }
}
