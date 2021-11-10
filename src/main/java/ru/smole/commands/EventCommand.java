package ru.smole.commands;

import net.md_5.bungee.api.chat.BaseComponent;
import net.md_5.bungee.api.chat.HoverEvent;
import net.md_5.bungee.api.chat.TextComponent;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.command.ConsoleCommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import org.bukkit.inventory.ItemStack;
import ru.smole.OpPrison;
import ru.smole.data.event.EventManager;
import ru.smole.data.event.data.Event;
import ru.smole.data.event.data.impl.ChatEvent;
import ru.smole.data.items.Items;
import ru.smole.data.player.OpPlayer;
import ru.smole.utils.ItemStackUtils;
import ru.xfenilafs.core.command.BukkitCommand;
import ru.xfenilafs.core.command.annotation.CommandPermission;
import ru.xfenilafs.core.util.ChatUtil;
import ru.xfenilafs.core.util.Schedules;

import java.util.Map;
import java.util.Random;
import java.util.function.Consumer;

@CommandPermission(permission = "opprison.admin")
public class EventCommand extends BukkitCommand<CommandSender> {
    public EventCommand() {
        super("event");
    }

    @Override
    protected void onExecute(CommandSender sender, String[] args) {
        switch (args.length) {
            case 2: {
                String name = args[0].toLowerCase();
                switch (name.toLowerCase()) {
                    case "chat": {
                        chat(sender, args[1].equals("on"));
                        break;
                    }

                    case "giveall": {
                        String[] item = args[1].split(":");
                        ItemStack itemMain = Items.getItem(item[0], Double.parseDouble(item[1]));

                        if (itemMain == null)
                            break;

                        all(sender, itemMain);
                        break;
                    }
                }
                break;
            }

            case 3: {
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
                        if (itemMain == null)
                            break;

                        number(name, i, itemMain);
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

                    number(name, i, itemMain);
                }

                break;
            }
        }
    }
    public void all(CommandSender sender, ItemStack itemMain) {
        ChatEvent.get()
                .id("all")
                .name("&bПодарки")
                .description(String.format("    §b%s §fподарил всем на сервере %s §fx%s", sender.getName(), itemMain.getItemMeta().getDisplayName(), itemMain.getAmount()))
                .consumer(null)
                .runnable(event -> () -> {
                    Bukkit.getOnlinePlayers().forEach(player -> {
                        OpPlayer.add(player, itemMain.clone());
                        ChatUtil.sendMessage(player, OpPrison.PREFIX + "§fВы получили %s §fx%s", itemMain.getItemMeta().getDisplayName(), itemMain.getAmount());
                    });

                    event.stop();
                })
                .start(0);
    }

    public void number(String name, int i, ItemStack itemMain) {
        int random = new Random().nextInt(i) + 1;
        final boolean[] is = {false};

        ChatEvent chatEvent = ChatEvent.get()
                .id("number")
                .name("&eОтгадай Число")
                .description(String.format("    Диапозон: &b1-%s &8&o(%s)", i, random));

        chatEvent.start();

        BaseComponent text = new TextComponent("    §fНаграда: ");
        BaseComponent[] reward = ChatUtil.newBuilder(itemMain.getItemMeta().getDisplayName() + " §fx" + itemMain.getAmount()).setHoverEvent(HoverEvent.Action.SHOW_ITEM, ItemStackUtils.convertItemStackToJsonRegular(itemMain)).build();
        text.addExtra(reward[0]);

        Bukkit.getOnlinePlayers().forEach((onPlayer) -> onPlayer.spigot().sendMessage(text));
        ChatUtil.broadcast("");

        chatEvent.consumer(event -> {
            Player target = event.getPlayer();
            String msg = event.getMessage();
            int num;

            if (event.isCancelled()) {
                event.setMessage("");
                event.setCancelled(true);
                return;
            }

            if (is[0]) {
                event.setCancelled(true);
                return;
            }

            if (msg.contains("-")) {
                ChatUtil.sendMessage(target, OpPrison.PREFIX + "Число не может быть отрицательным");

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

            if (num > i) {
                ChatUtil.sendMessage(target, OpPrison.PREFIX + "Вы превысили диапозон события §8(%s)", i);

                event.setCancelled(true);
                return;
            }

            if (num == random) {
                is[0] = true;

                ChatUtil.broadcast("");
                ChatUtil.broadcast("    §fЗагаданное число было: §b%s", random);
                ChatUtil.broadcast("    §fПобедителем события стал: §b%s", target.getName());
                ChatUtil.broadcast("");

                OpPlayer.add(target, itemMain.clone());

                ChatUtil.broadcast("");
                ChatUtil.broadcast("    Событие %s §fбудет завершено через 3 секунды...", name.replaceAll("number", "§bотгадай число"));
                ChatUtil.broadcast("");

                Schedules.runAsync(() -> {
                    chatEvent.stop();
                    is[0] = false;

                    ChatUtil.broadcast("");
                    ChatUtil.broadcast("    Событие %s §fзавершено. Чат включён.", name.replaceAll("number", "§bотгадай число"));
                    ChatUtil.broadcast("");

                }, 60);
            }
        });
    }

    public void chat(CommandSender sender, boolean is) {
        ChatEvent chatEvent =
                ChatEvent.get()
                        .id("chat")
                        .name("&2Отключение Чата")
                        .description(String.format("&b%s &f%s возможность писать в чат", sender.getName(), !is ? "включил" : "отключил"))
                        .consumer(event -> {
                            Player player = event.getPlayer();
                            if (player.hasPermission("opprison.admin"))
                                return;

                            if (is) {
                                ChatUtil.sendMessage(player, OpPrison.PREFIX + "Чат был временно отключён");
                                event.setCancelled(true);
                            }
                        });

        if (is) {
            chatEvent.start();
            return;
        }

        chatEvent.stop();
    }
}
