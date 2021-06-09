package ru.smole.commands;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import ru.smole.OpPrison;
import ru.smole.items.Items;
import ru.smole.items.Key;
import ru.smole.player.OpPlayer;
import ru.smole.utils.StringUtils;
import ru.xfenilafs.core.command.BukkitCommand;
import ru.xfenilafs.core.command.annotation.CommandPermission;
import ru.xfenilafs.core.util.ChatUtil;

@CommandPermission(permission = "opprison.admin")
public class ItemsCommand extends BukkitCommand<Player> {

    public ItemsCommand() {
        super("items");
    }

    @Override
    protected void onExecute(Player player, String[] args) {
        OpPlayer opPlayer = new OpPlayer(player);
        String name = player.getName();

        if (args.length == 3 || args.length == 4) {
            Player target = Bukkit.getPlayer(args[1]);

            if (target == null) {
                ChatUtil.sendMessage(player, OpPrison.PREFIX + "Игрок не в сети");
                return;
            }

            String arg = args[0].toLowerCase();
            Items items = opPlayer.getItems();

            if (arg.equals("key")) {
                Key type = items.getKeyFromString(args[2]);

                if (type == null) {
                    ChatUtil.sendMessage(player, OpPrison.PREFIX + "Предмет не найден");
                    return;
                }

                ItemStack item = type.getStack();
                int amount = 1;

                try {
                    amount = Integer.parseInt(args[3]);
                } catch (Exception ignored) {}

                item.setAmount(amount);
                opPlayer.add(item);
                ChatUtil.sendMessage(player, OpPrison.PREFIX + "Игроку &b%s &f был выдан &b%s &fключ &fx%s", name, type.getName(), amount);
                return;
            }

            if (arg.equals("token")) {
                int amount = 1;

                try {
                    amount = Integer.parseInt(args[2]);
                } catch (Exception ignored) {}

                opPlayer.add(opPlayer.getItems().getToken(amount));
                ChatUtil.sendMessage(player, OpPrison.PREFIX + "Игроку &b%s &f был выдан токен &e⛃%s", name, StringUtils._formatDouble(amount));
                return;
            }
        }

        ChatUtil.sendMessage(player, OpPrison.PREFIX + "/items key/token name key_type/count amount/empty");
    }
}
