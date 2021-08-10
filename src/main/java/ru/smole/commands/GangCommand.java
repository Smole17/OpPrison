package ru.smole.commands;

import lombok.val;
import net.dv8tion.jda.api.entities.Guild;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import ru.smole.OpPrison;
import ru.smole.data.gang.GangData;
import ru.smole.data.gang.GangDataManager;
import ru.smole.utils.StringUtils;
import ru.xfenilafs.core.command.BukkitCommand;
import ru.xfenilafs.core.util.ChatUtil;

import java.util.Map;

import static ru.smole.OpPrison.PREFIX;
import static ru.smole.data.gang.GangData.GangPlayer;
import static ru.smole.data.gang.GangData.GangPlayer.GangPlayerType;

public class GangCommand extends BukkitCommand<Player> {
    public GangCommand() {
        super("gang", "g");
    }

    @Override
    protected void onExecute(Player player, String[] args) {
        String playerName = player.getName();
        GangDataManager gangDataManager = OpPrison.getInstance().getGangDataManager();
        val gangDataMap = gangDataManager.getGangDataMap();

        boolean playerHasGang = gangDataManager.playerHasGang(playerName);

        if (playerHasGang) {
            GangData gangData = gangDataManager.getGangFromPlayer(playerName);
            String gangName = gangData.getName();
            Map<String, GangPlayer> gangPlayersMap = gangData.getGangPlayerMap();

            GangPlayer gangPlayer = gangPlayersMap.get(playerName);

            GangPlayerType gangPlayerType = gangPlayer.getType();
            String typeName = gangPlayerType.getName();

            switch (args.length) {
                case 1:
                    switch (args[0]) {
                        case "info":
                            ChatUtil.sendMessage(player, PREFIX + "Банда %s&f:", gangName);
                            ChatUtil.sendMessage(player, "   &fКоличество очков: &b%s", StringUtils.replaceComma(gangData.getScore()));

                            val gangPlayers = gangData.findGangPlayers(GangPlayerType.LEADER, GangPlayerType.MANAGER, GangPlayerType.OLDEST, GangPlayerType.DEFAULT);
                            StringBuilder builder = new StringBuilder();

                            gangPlayers.forEach(tempGangPlayer -> {
                                GangPlayerType tempType = tempGangPlayer.getType();
                                String format = "%s &7%s &7,";

                                if (gangPlayers.get(gangPlayers.size() - 1).equals(tempGangPlayer))
                                    format = format.replace(",", "");

                                builder.append(String.format(format,
                                        tempType.getName(), gangPlayer.getPlayerName()));
                            });

                            ChatUtil.sendMessage(player, "   &fУчастники:");
                            ChatUtil.sendMessage(player, builder.toString());

                            return;
                        case "disband":
                            if (gangPlayerType != GangPlayerType.LEADER) {
                                ChatUtil.sendMessage(player, PREFIX + "Ваша роль в банде слишком мала для этого действия");
                                return;
                            }

                            gangData.sendMessage("Банда была удалена главой &b" + playerName);
                            gangDataMap.remove(gangName);

                            Guild guild = OpPrison.getInstance().getDiscordBot().getGuild();
                            guild.getTextChannelsByName("gang-" + gangName, true).forEach(textChannel -> {
                                textChannel.delete().complete();
                            });

                            return;

                        case "leave":
                            if (gangPlayerType == GangPlayerType.LEADER) {
                                ChatUtil.sendMessage(player, PREFIX + "Вы не можете выйти из банды.");
                                return;
                            }

                            gangData.sendMessage("%s &7%s вышел из банды", typeName, playerName);
                            gangPlayersMap.remove(playerName);

                            return;
                    }

                    break;
                case 2:
                    switch (args[0]) {
                        case "rename":
                            if (gangPlayerType != GangPlayerType.LEADER) {
                                ChatUtil.sendMessage(player, PREFIX + "Ваша роль в банде слишком мала для этого действия");
                                return;
                            }

                            if (args[1].contains("&k") || args[1].contains("&r")) {
                                ChatUtil.sendMessage(player, PREFIX + "Вы используете запрещённый цветовой символ в название");
                                return;
                            }

                            if (ChatUtil.color(args[1]).length() > 5) {
                                ChatUtil.sendMessage(player, PREFIX + "Название банды не может превышать 5 символов &8&o(& не учитывается)");
                                return;
                            }

                            gangData.setName(args[1]);
                            ChatUtil.sendMessage(player, PREFIX + "Вы переименовали банду %s", ChatUtil.color(args[1]));

                            return;

                        case "kick":
                            if (gangPlayerType.ordinal() < GangPlayerType.MANAGER.ordinal()) {
                                ChatUtil.sendMessage(player, PREFIX + "Ваша роль в банде слишком мала для этого действия");
                                return;
                            }

                            if (!gangDataManager.playerInGang(gangData, args[1].toLowerCase())) {
                                ChatUtil.sendMessage(player, "Игрок &b%s &fне найден в банде", Bukkit.getOfflinePlayer(args[1]).getName());
                                return;
                            }

                            GangPlayer kickedGangPlayer = gangData.getGangPlayer(args[1].toLowerCase());

                            gangData.sendMessage("%s %s &fбыл выгнан из банды %s %s",
                                    kickedGangPlayer.getType().getName(), kickedGangPlayer.getPlayerName(),
                                    gangPlayerType.getName(), gangPlayer.getPlayerName()
                            );

                            gangPlayersMap.remove(playerName);

                            return;

                        case "promote":
                            if (gangPlayerType.ordinal() < GangPlayerType.MANAGER.ordinal()) {
                                ChatUtil.sendMessage(player, PREFIX + "Ваша роль в банде слишком мала для этого действия");
                                return;
                            }

                            GangPlayer promotedGangPlayer = gangData.getGangPlayer(args[1].toLowerCase());

                            if (promotedGangPlayer == null) {
                                ChatUtil.sendMessage(player, PREFIX + "Игрок не состоит в банде");
                                return;
                            }

                            if (promotedGangPlayer.upType()) {
                                gangData.sendMessage("Игрок %s &fбыл повышен до %s", promotedGangPlayer.getPlayerName(), promotedGangPlayer.getType().getName());
                                return;
                            }

                            ChatUtil.sendMessage(player, "Игрок не может быть повышен. Используйте &b/gang setleader <Ник игрока>");

                            return;

                        case "demote":
                            if (gangPlayerType.ordinal() < GangPlayerType.MANAGER.ordinal()) {
                                ChatUtil.sendMessage(player, PREFIX + "Ваша роль в банде слишком мала для этого действия");
                                return;
                            }

                            GangPlayer demotedGangPlayer = gangData.getGangPlayer(args[1].toLowerCase());

                            if (demotedGangPlayer == null) {
                                ChatUtil.sendMessage(player, PREFIX + "Игрок не состоит в банде");
                                return;
                            }

                            if (demotedGangPlayer.deUpType()) {
                                gangData.sendMessage("Игрок %s &fбыл понижен до %s", demotedGangPlayer.getPlayerName(), demotedGangPlayer.getType().getName());
                                return;
                            }

                            ChatUtil.sendMessage(player, "Игрок не может быть понижен.");

                            return;

                        case "info":
                            GangData playerGangData = gangDataManager.getGangDataMap().get(args[1]);

                            if (playerGangData == null) {
                                ChatUtil.sendMessage(player, PREFIX + "Банда не найдена");
                                return;
                            }

                            ChatUtil.sendMessage(player, PREFIX + "Банда %s&f:", args[1]);
                            ChatUtil.sendMessage(player, "   &fКоличество очков: &b%s", StringUtils.replaceComma(playerGangData.getScore()));

                            val gangPlayers = playerGangData.findGangPlayers(GangPlayerType.LEADER, GangPlayerType.MANAGER, GangPlayerType.OLDEST, GangPlayerType.DEFAULT);
                            StringBuilder builder = new StringBuilder();

                            gangPlayers.forEach(tempGangPlayer -> {
                                GangPlayerType tempType = tempGangPlayer.getType();
                                String format = "%s &7%s &7,";

                                if (gangPlayers.get(gangPlayers.size() - 1).equals(tempGangPlayer))
                                    format = format.replace(",", "");

                                builder.append(String.format(format,
                                        tempType.getName(), tempGangPlayer.getPlayerName()));
                            });

                            ChatUtil.sendMessage(player, "   &fУчастники:");
                            ChatUtil.sendMessage(player, builder.toString());

                            return;
                    }

                    break;
            }

            help(player);
            return;
        }

        switch (args.length) {
            case 1:
                break;
            case 2:
                switch (args[0]) {
                    case "create":
                        if (args[1].contains("&k") || args[1].contains("&r")) {
                            ChatUtil.sendMessage(player, PREFIX + "Вы используете запрещённый цветовой символ в название");
                            return;
                        }

                        if (ChatUtil.color(args[1].replace("&", "§")).length() > 5) {
                            ChatUtil.sendMessage(player, PREFIX + "Название банды не может превышать 5 символов &8&o(& не учитывается)");
                            return;
                        }

                        gangDataManager.create(args[1], playerName);
                        ChatUtil.sendMessage(player, PREFIX + "Вы создали новую банду %s", ChatUtil.color(args[1]));

                        return;

                    case "info":
                        GangData playerGangData = gangDataManager.getGangDataMap().get(args[1]);

                        if (playerGangData == null) {
                            ChatUtil.sendMessage(player, PREFIX + "Банда не найдена");
                            return;
                        }

                        ChatUtil.sendMessage(player, PREFIX + "Банда %s&f:", args[1]);
                        ChatUtil.sendMessage(player, "   &fКоличество очков: &b%s", StringUtils.replaceComma(playerGangData.getScore()));

                        val gangPlayers = playerGangData.findGangPlayers(GangPlayerType.LEADER, GangPlayerType.MANAGER, GangPlayerType.OLDEST, GangPlayerType.DEFAULT);
                        StringBuilder builder = new StringBuilder();

                        gangPlayers.forEach(tempGangPlayer -> {
                            GangPlayerType tempType = tempGangPlayer.getType();
                            String format = "%s &7%s &7,";

                            if (gangPlayers.get(gangPlayers.size() - 1).equals(tempGangPlayer))
                                format = format.replace(",", "");

                            builder.append(String.format(format,
                                    tempType.getName(), tempGangPlayer.getPlayerName()));
                        });

                        ChatUtil.sendMessage(player, "   &fУчастники:");
                        ChatUtil.sendMessage(player, builder.toString());

                        return;
                }

                break;
        }

        help(player);
    }

    public void help(Player player) {
        ChatUtil.sendMessage(player, PREFIX + "Помощь:");
        ChatUtil.sendMessage(player, "   &b/gang create <Имя банды> &f- создать новую банду");
        ChatUtil.sendMessage(player, "   &b/gang disband &f- удалить банду &8&o(от Главы)");
        ChatUtil.sendMessage(player, "   &b/gang leave &f- покинуть банду");
        ChatUtil.sendMessage(player, "   &b/gang info <Имя банды> &f- посмотреть информацию о банде");
        ChatUtil.sendMessage(player, "   &b/gang rename <Новое имя банды> &f- сменить название банды");
        ChatUtil.sendMessage(player, "   &b/gangchat (/gc) <Сообщение> &f- написать сообщение в чат банды");
        ChatUtil.sendMessage(player, "");
        ChatUtil.sendMessage(player, "   &b/gang invite <Ник игрока> &f- отправить игроку запрос в банду &8&o(от Старейшины)");
        ChatUtil.sendMessage(player, "   &b/gang kick <Ник игрока> &f-  выгнать игрока из банды &8&o(от Соруководителя)");
        ChatUtil.sendMessage(player, "   &b/gang promote <Ник игрока> &f- повысить игрока в банде &8&o(от Соруководителя)");
        ChatUtil.sendMessage(player, "   &b/gang demote <Ник игрока> &f- понизить игрока в банде &8&o(от Соруководителя)");
        ChatUtil.sendMessage(player, "   &b/gang setleader <Ник игрока>> &f- передать Главу игроку");
        ChatUtil.sendMessage(player, "");
        ChatUtil.sendMessage(player, "   &b/gang accept <Имя банды>");
    }
}
