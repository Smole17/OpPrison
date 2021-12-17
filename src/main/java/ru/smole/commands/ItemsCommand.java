package ru.smole.commands;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.ConsoleCommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import ru.smole.OpPrison;
import ru.smole.data.items.Items;
import ru.smole.data.items.pickaxe.PickaxeManager;
import ru.smole.data.items.pickaxe.Upgrade;
import ru.smole.data.player.OpPlayer;
import ru.xfenilafs.core.command.BukkitCommand;
import ru.xfenilafs.core.command.annotation.CommandPermission;
import ru.xfenilafs.core.util.ChatUtil;

import javax.naming.PartialResultException;
import java.util.Arrays;

@CommandPermission(permission = "opprison.admin")
public class ItemsCommand extends BukkitCommand<CommandSender> {

    public ItemsCommand() {
        super("items");
    }

    @Override
    protected void onExecute(CommandSender sender, String[] args) {
        if (sender instanceof Player) {
            Player player = (Player) sender;

            if (args.length < 1) {
                ChatUtil.sendMessage(player, OpPrison.PREFIX + "/items name type amount");
                return;
            }

            Player target = Bukkit.getPlayer(args[0]);

            if (target == null) {
                ChatUtil.sendMessage(player, OpPrison.PREFIX_N + "Игрок не найден");
                return;
            }

            switch (args[1]) {
                default:
                    OpPlayer opPlayer = new OpPlayer(target);

                    double amount = 1;

                    try {
                        amount = Double.parseDouble(args[2]);
                    } catch (Exception ignored) {
                    }

                    ItemStack itemStack = Items.getItem(args[1], amount);

                    if (itemStack == null) {
                        ChatUtil.sendMessage(player, OpPrison.PREFIX_N + "Предмет не найден. Воспользуйтесь списком: %s", ChatColor.stripColor(Arrays.toString(Items.getCreators().keySet().toArray())));
                        return;
                    }

                    opPlayer.add(itemStack);
                    ChatUtil.sendMessage(target, OpPrison.PREFIX + "Вы получили новый предмет %s", itemStack.getItemMeta().getDisplayName() == null ? itemStack.getType().name() : itemStack.getItemMeta().getDisplayName());

                    break;
            }

            return;
        }

        if (sender instanceof ConsoleCommandSender) {
            if (args.length < 1) {
                ChatUtil.sendMessage(sender, OpPrison.PREFIX + "/items name type amount");
                return;
            }

            Player target = Bukkit.getPlayer(args[0]);

            if (target == null) {
                ChatUtil.sendMessage(sender, OpPrison.PREFIX_N + "Игрок не найден");
                return;
            }

            switch (args[1]) {
                default:
                    OpPlayer opPlayer = new OpPlayer(target);

                    double amount = 1;

                    try {
                        amount = Double.parseDouble(args[2]);
                    } catch (Exception ignored) {
                    }

                    ItemStack itemStack = Items.getItem(args[1], amount);

                    if (itemStack == null) {
                        ChatUtil.sendMessage(sender, OpPrison.PREFIX_N + "Предмет не найден. Воспользуйтесь списком: %s", ChatColor.stripColor(Arrays.toString(Items.getCreators().keySet().toArray())));
                        return;
                    }

                    opPlayer.add(itemStack.clone());
                    ChatUtil.sendMessage(target, OpPrison.PREFIX + "Вы получили новый предмет %s", itemStack.getItemMeta().getDisplayName() == null ? itemStack.getType().name() : itemStack.getItemMeta().getDisplayName());

                    break;
            }
        }
    }
}
