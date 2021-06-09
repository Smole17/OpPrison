package ru.smole.commands;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import ru.smole.OpPrison;
import ru.smole.data.PlayerData;
import ru.smole.items.Items;
import ru.smole.items.KeyType;
import ru.smole.player.OpPlayer;
import ru.smole.utils.StringUtils;
import ru.xfenilafs.core.command.BukkitCommand;
import ru.xfenilafs.core.command.annotation.CommandPermission;

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
                opPlayer.sendMessage("Игрок не в сети");
                return;
            }

            String arg = args[0].toLowerCase();

            if (arg.equals("key")) {
                KeyType type = KeyType.valueOf(args[2].toUpperCase());
                ItemStack item = type.getStack();

                if (item == null) {
                    opPlayer.sendMessage("Предмет не найден");
                    return;
                }

                int amount = 1;

                try {
                    amount = Integer.parseInt(args[3]);
                } catch (Exception ignored) {}

                item.setAmount(amount);
                opPlayer.add(item);
                opPlayer.sendMessage("Игроку §b" + name + "§f был выдан §b" + type.getName() + " §fключ");
                return;
            }

            if (arg.equals("token")) {
                int amount = 1;

                try {
                    amount = Integer.parseInt(args[2]);
                } catch (Exception ignored) {}

                opPlayer.add(Items.getToken(amount));
                opPlayer.sendMessage("Игроку §b" + name + "§f был выдан токен §e⛃" + StringUtils._formatDouble(amount));
                return;
            }
        }

        opPlayer.sendMessage("/items key/token name key_type/count amount/empty");
    }
}
