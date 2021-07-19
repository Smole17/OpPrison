package ru.smole.commands;

import lombok.var;
import net.md_5.bungee.api.chat.BaseComponent;
import net.md_5.bungee.api.chat.HoverEvent;
import net.md_5.bungee.api.chat.TextComponent;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import ru.smole.OpPrison;
import ru.smole.data.OpPlayer;
import ru.smole.data.items.Items;
import ru.smole.event.OpEvent;
import ru.smole.event.OpEvents;
import ru.smole.utils.ItemStackUtils;
import ru.xfenilafs.core.command.BukkitCommand;
import ru.xfenilafs.core.command.annotation.CommandPermission;
import ru.xfenilafs.core.util.ChatUtil;

import java.util.Random;

@CommandPermission(permission = "opprison.admin")
public class EventCommand extends BukkitCommand<Player> {
    public EventCommand() {
        super("event");
    }

    @Override
    protected void onExecute(Player player, String[] args) {
        if (args.length == 2) {
            String playerName = player.getName();
            String name = args[0].toLowerCase();
            int i;

            if (!name.equals("number"))
                return;

            try {
                i = Integer.parseInt(args[1]);
            } catch (Exception e) {
                ChatUtil.sendMessage(player, OpPrison.PREFIX + "OnlyNumberFormat");
                return;
            }

            ItemStack itemMain = player.getInventory().getItemInMainHand();

            if (itemMain == null) {
                ChatUtil.sendMessage(player, OpPrison.PREFIX + "Возьмите в руку разыгрываемый предмет");
                return;
            }

            if (!itemMain.hasItemMeta()) {
                ChatUtil.sendMessage(player, OpPrison.PREFIX + "Вы можете разыгрывать только предметы режима");
                return;
            }

            if (Items.isSomePickaxe(itemMain, playerName)) {
                ChatUtil.sendMessage(player, OpPrison.PREFIX + "Кирка является разыгрываемым предметом");
                return;
            }


            OpEvent opEvent = new OpEvents();
            var events = opEvent.getEvents();
            int random = new Random().nextInt(i) + 1;

            opEvent.getActiveEvents().remove(name);
            events.remove(name);
            events.put(name, event -> {
                Player target = event.getPlayer();
                String msg = event.getMessage();
                int num;

                if (msg.split("\\s").length > 1) {
                    ChatUtil.sendMessage(target, OpPrison.PREFIX + "Во время события можно писать только цифры §8(без пробелов)");

                    event.setCancelled(true);
                    return;
                }

                try {
                    num = Integer.parseInt(msg.split("\\s")[0]);
                } catch (Exception e) {
                    ChatUtil.sendMessage(target, OpPrison.PREFIX + "Во время события можно писать только цифры §8(без пробелов)");

                    event.setCancelled(true);
                    return;
                }

                if (num > i) {
                    ChatUtil.sendMessage(target, OpPrison.PREFIX + "Ваше число больше заданного диапозона");

                    event.setCancelled(true);
                    return;
                }

                if (num == random) {
                    opEvent.stop(name);

                    ChatUtil.broadcast("");
                    ChatUtil.broadcast("    §fЗагаданное число было: §b%s", random);
                    ChatUtil.broadcast("    §fПобедителем события стал: §b%s", target.getName());
                    ChatUtil.broadcast("");

                    new OpPlayer(target).add(itemMain);
                }
            });

            BaseComponent text = new TextComponent("    §fНаграда: ");
            BaseComponent[] reward = ChatUtil
                    .newBuilder(itemMain.getItemMeta().getDisplayName() + " §fx" + itemMain.getAmount())
                    .setHoverEvent(HoverEvent.Action.SHOW_ITEM, ItemStackUtils.convertItemStackToJsonRegular(itemMain))
                    .build();

            text.addExtra(reward[0]);

            ChatUtil.broadcast("");
            ChatUtil.broadcast("    §b%s §fначал новое событие §b%s", playerName, name.replaceAll("number", "отгадай число"));
            ChatUtil.broadcast("    §fДиапозон: §b1-%s", i);
            ChatUtil.sendMessage(player, "    §8ЗАГАДАННОЕ ЧИСЛО: §o%s", random);
            Bukkit.getOnlinePlayers().forEach(onPlayer -> onPlayer.spigot().sendMessage(text));
            ChatUtil.broadcast("");

            opEvent.start(name);
            return;
        }

        ChatUtil.sendMessage(player, OpPrison.PREFIX + "/event name value");
    }
}
