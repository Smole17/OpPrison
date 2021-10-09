package ru.smole.commands;

import net.md_5.bungee.api.chat.BaseComponent;
import net.md_5.bungee.api.chat.HoverEvent;
import net.md_5.bungee.api.chat.TextComponent;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.command.ConsoleCommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import ru.smole.OpPrison;
import ru.smole.data.event.OpEvent;
import ru.smole.data.event.OpEvents;
import ru.smole.data.items.Items;
import ru.smole.data.player.OpPlayer;
import ru.smole.utils.ItemStackUtils;
import ru.xfenilafs.core.command.BukkitCommand;
import ru.xfenilafs.core.command.annotation.CommandPermission;
import ru.xfenilafs.core.util.ChatUtil;

import java.util.Random;

@CommandPermission(permission = "opprison.admin")
public class EventCommand extends BukkitCommand<CommandSender> {
    public EventCommand() {
        super("event");
    }

    @Override
    protected void onExecute(CommandSender sender, String[] args) {
        if (args.length == 2) {
            String name = args[0].toLowerCase();
            if (name.equalsIgnoreCase("chat"))
                chat(sender, name, args[1].equals("on"));
        }

        if (args.length == 3) {
            String name = args[0].toLowerCase();

            if (name.equalsIgnoreCase("number")) {
                String[] item = args[1].split(":");
                ItemStack itemMain = Items.getItem(item[0], Double.parseDouble(item[1]));

                int i;

                try {
                    i = Integer.parseInt(args[2]);
                } catch (Exception e) {
                    throw new IllegalArgumentException("Could not load range of number event");
                }

                if (sender instanceof ConsoleCommandSender) {
                    number(sender, name, i, itemMain);
                    return;
                }

                Player player = (Player) sender;
                String playerName = player.getName();

                if (itemMain == null) {
                    return;
                }

                if (!itemMain.hasItemMeta()) {
                    return;
                }

                if (Items.isSomePickaxe(itemMain, playerName)) {
                    return;
                }


                number(sender, name, i, itemMain);
            }
        }
    }

    public void number(CommandSender sender, String name, int i, ItemStack itemMain) {
        OpEvent opEvent = new OpEvents();
        var events = opEvent.getChatEvents();

        int random = new Random().nextInt(i) + 1;
        final boolean[][] is = {{false}};

        opEvent.getActiveEvents().remove(name);
        events.remove(name);
        events.put(name, event -> {
            Player target = event.getPlayer();
            String msg = event.getMessage();
            int num;

            if (is[0][0]) {
                event.setCancelled(true);
                return;
            }

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

            if (num == random) {
                is[0][0] = true;

                ChatUtil.broadcast("");
                ChatUtil.broadcast("    §fЗагаданное число было: §b%s", random);
                ChatUtil.broadcast("    §fПобедителем события стал: §b%s", target.getName());
                ChatUtil.broadcast("");

                new OpPlayer(target).add(itemMain);

                ChatUtil.broadcast("");
                ChatUtil.broadcast("    Событие %s §fбудет завершено через 3 секунды...", name.replaceAll("number", "§bотгадай число"));
                ChatUtil.broadcast("");

                Bukkit.getScheduler().runTaskLater(OpPrison.getInstance(), () -> {
                    opEvent.stop(name);
                    is[0][0] = false;

                    ChatUtil.broadcast("");
                    ChatUtil.broadcast("    Событие %s §fзавершено. Чат включён.", name.replaceAll("number", "§bотгадай число"));
                    ChatUtil.broadcast("");

                }, 60);
            }
        });

        opEvent.start(name);

        BaseComponent text = new TextComponent("    §fНаграда: ");
        BaseComponent[] reward = ChatUtil.newBuilder(itemMain.getItemMeta().getDisplayName() + " §fx" + itemMain.getAmount()).setHoverEvent(HoverEvent.Action.SHOW_ITEM, ItemStackUtils.convertItemStackToJsonRegular(itemMain)).build();
        text.addExtra(reward[0]);

        ChatUtil.broadcast("");
        ChatUtil.broadcast("    &b%s &fначал новое событие &b%s",
                sender.getName(),
                name.replaceAll("number", "§bотгадай число"));

        ChatUtil.broadcast("    Диапозон: &b1-%s", i);

        Bukkit.getOnlinePlayers().forEach((onPlayer) -> onPlayer.spigot().sendMessage(text));

        ChatUtil.broadcast("");

        if (sender instanceof Player) {
            ChatUtil.sendMessage((Player) sender, "    §8ЗАГАДАННОЕ ЧИСЛО: §o%s", random);
        }

        ChatUtil.broadcast("");


    }

    public void chat(CommandSender sender, String name, boolean is) {
        OpEvent opEvent = new OpEvents();
        var events = opEvent.getChatEvents();

        opEvent.getActiveEvents().remove(name);
        events.remove(name);
        events.put(name, event -> {
            Player player = event.getPlayer();
            if (player.hasPermission("opprison.admin"))
                return;

            if (is) {
                ChatUtil.sendMessage(player, OpPrison.PREFIX + "Чат был временно отключён");
                event.setCancelled(true);
            }
        });

        opEvent.start(name);

        ChatUtil.broadcast("");
        ChatUtil.broadcast(OpPrison.PREFIX + "&b%s &f%s возможность писать в чат", sender.getName(), !is ? "включил" : "отключил");
        ChatUtil.broadcast("");
    }
}
