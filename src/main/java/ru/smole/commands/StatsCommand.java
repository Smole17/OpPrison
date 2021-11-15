package ru.smole.commands;

import lombok.AllArgsConstructor;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.command.ConsoleCommandSender;
import org.bukkit.entity.Player;
import ru.smole.OpPrison;
import ru.smole.data.player.PlayerData;
import ru.smole.data.group.GroupsManager;
import ru.smole.utils.StringUtils;
import ru.xfenilafs.core.command.BukkitCommand;
import ru.xfenilafs.core.command.annotation.CommandPermission;
import ru.xfenilafs.core.util.ChatUtil;

import java.util.Arrays;

@CommandPermission(permission = "opprison.admin")
public class StatsCommand extends BukkitCommand<CommandSender> {
    public StatsCommand() {
        super("stats", "st", "statistic");
    }

    @Override
    protected void onExecute(CommandSender sender, String[] args) {
        String msg = OpPrison.PREFIX + "/stats name " + Arrays.toString(Stat.values()) + " value";

        if (sender instanceof Player) {
            Player player = (Player) sender;

            if (args.length == 3) {
                Player target = Bukkit.getPlayer(args[0]);
                String targetName = target.getName();
                PlayerData targetData = OpPrison.getInstance().getPlayerDataManager().getPlayerDataMap().get(targetName);
                Stat type = Stat.getTypeFromString(args[1].toUpperCase());

                if (type == null) {
                    ChatUtil.sendMessage(player, msg);
                    return;
                }

                Object value;

                if (type != Stat.GROUP && type != Stat.ACCESS) {
                    try {
                        value = Double.valueOf(args[2]);
                    } catch (Exception e) {
                        ChatUtil.sendMessage(player, OpPrison.PREFIX_N + "Введите целое положительное число");
                        return;
                    }
                } else if (type == Stat.GROUP) {
                    try {
                        value = GroupsManager.Group.getGroupFromString(args[2]);
                    } catch (Exception e) {
                        ChatUtil.sendMessage(player, OpPrison.PREFIX_N + "Группа не найдена. Используйте данный список: %s", Arrays.toString(GroupsManager.Group.values()));
                        return;
                    }
                } else {
                    value = args[2];
                }

                String piece = "null";

                switch (type) {
                    case BLOCKS:
                        targetData.setBlocks((double) value);
                        piece = "было установлено блоков";
                        break;

                    case MONEY:
                        targetData.setMoney((double) value);
                        piece = "было установлено денег";
                        break;

                    case TOKEN:
                        targetData.setToken((double) value);
                        piece = "было установлено токенов";
                        break;

                    case MULTIPLIER:
                        targetData.setMultiplier((double) value);
                        piece = "был установлен множитель";
                        break;

                    case PRESTIGE:
                        targetData.setPrestige((double) value);
                        piece = "было установлено престижей";
                        break;

                    case GROUP:
                        targetData.setGroup(GroupsManager.Group.getGroupFromString(String.valueOf(value)));
                        piece = "была установлена группа";
                        break;

                    case ACCESS:
                        targetData.getAccess().add(String.valueOf(value));
                        piece = "было добавлено право";
                        break;
                }

                if (value != null) {
                    ChatUtil.sendMessage(player, OpPrison.PREFIX + "Игроку &b%s &f%s: §b%s", targetName, piece,
                            value.getClass() == Double.class ? StringUtils._fixDouble(0, (double) value) : value);
                    return;
                }
            }

            ChatUtil.sendMessage(player, msg);
        }

        if (sender instanceof ConsoleCommandSender) {
            if (args.length == 3) {
                Player target = Bukkit.getPlayer(args[0]);
                String targetName = target.getName();
                PlayerData targetData = OpPrison.getInstance().getPlayerDataManager().getPlayerDataMap().get(targetName);
                Stat type = Stat.getTypeFromString(args[1].toUpperCase());

                if (type == null) {
                    ChatUtil.sendMessage(sender, msg);
                    return;
                }

                Object value;

                if (type != Stat.GROUP && type != Stat.ACCESS) {
                    try {
                        value = Double.valueOf(args[2]);
                    } catch (Exception e) {
                        ChatUtil.sendMessage(sender, OpPrison.PREFIX + "Введите целое положительное число");
                        return;
                    }
                } else if (type == Stat.GROUP) {
                    try {
                        value = GroupsManager.Group.getGroupFromString(args[2]);
                    } catch (Exception e) {
                        ChatUtil.sendMessage(sender, OpPrison.PREFIX + "Группа не найдена. Используйте данный список: %s", Arrays.toString(GroupsManager.Group.values()));
                        return;
                    }
                } else {
                    value = args[2];
                }

                String piece = "null";

                switch (type) {
                    case BLOCKS:
                        targetData.setBlocks((double) value);
                        piece = "было установлено блоков";
                        break;

                    case MONEY:
                        targetData.setMoney((double) value);
                        piece = "было установлено денег";
                        break;

                    case TOKEN:
                        targetData.setToken((double) value);
                        piece = "было установлено токенов";
                        break;

                    case MULTIPLIER:
                        targetData.setMultiplier((double) value);
                        piece = "был установлен множитель";
                        break;

                    case PRESTIGE:
                        targetData.setPrestige((double) value);
                        piece = "было установлено престижей";
                        break;

                    case GROUP:
                        targetData.setGroup(GroupsManager.Group.getGroupFromString(String.valueOf(value)));
                        piece = "была установлена группа";
                        break;

                    case ACCESS:
                        targetData.getAccess().add(String.valueOf(value));
                        piece = "было добавлено право";
                        break;
                }

                if (value != null) {
                    ChatUtil.sendMessage(sender, OpPrison.PREFIX + "Игроку &b%s &f%s: §b%s", targetName, piece,
                            value.getClass() == Double.class ? StringUtils._fixDouble(0, (double) value) : value);
                    return;
                }
            }

            ChatUtil.sendMessage(sender, msg);
        }
    }

    @AllArgsConstructor public enum Stat {

        BLOCKS(),
        MONEY(),
        TOKEN(),
        MULTIPLIER(),
        PRESTIGE(),
        GROUP(),
        ACCESS();

        public static Stat getTypeFromString(String stat) {
            for (Stat type : Stat.values())
                if (type == Stat.valueOf(stat))
                    return type;

            return null;
        }
    }
}
