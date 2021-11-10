package ru.smole.commands;

import lombok.val;
import net.md_5.bungee.api.chat.BaseComponent;
import net.md_5.bungee.api.chat.ClickEvent;
import net.md_5.bungee.api.chat.HoverEvent;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import ru.smole.OpPrison;
import ru.smole.data.gang.GangData;
import ru.smole.data.gang.GangDataManager;
import ru.smole.data.mysql.GangDataSQL;
import ru.smole.utils.StringUtils;
import ru.xfenilafs.core.command.BukkitCommand;
import ru.xfenilafs.core.command.annotation.CommandPermission;
import ru.xfenilafs.core.util.ChatUtil;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static ru.smole.OpPrison.PREFIX;
import static ru.smole.OpPrison.PREFIX_N;
import static ru.smole.data.gang.GangData.GangPlayer;
import static ru.smole.data.gang.GangData.GangPlayer.GangPlayerType;

public class GangCommand extends BukkitCommand<Player> {
    public GangCommand() {
        super("gang");
    }

    public static final Map<String, List<String>> invitedList = new HashMap<>();

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

            GangPlayer gangPlayer = gangPlayersMap.get(playerName.toLowerCase());

            GangPlayerType gangPlayerType = gangPlayer.getType();
            String typeName = gangPlayerType.getName();

            switch (args.length) {
                case 1:
                    switch (args[0]) {
                        case "info":
                            ChatUtil.sendMessage(player, PREFIX + "&fБанда %s &8&o(%s/%s)&r&f:", gangName, gangPlayersMap.size(), 10);
                            ChatUtil.sendMessage(player, "   &fКоличество очков: &b%s", StringUtils.replaceComma(gangData.getScore()));

                            val gangPlayers = gangData.findGangPlayers(GangPlayerType.LEADER, GangPlayerType.MANAGER, GangPlayerType.OLDEST, GangPlayerType.DEFAULT);
                            StringBuilder builder = new StringBuilder();

                            gangPlayers.forEach(tempGangPlayer -> {
                                GangPlayerType tempType = tempGangPlayer.getType();
                                String format = "%s &7%s &8&o(%s)&7, ";

                                if (gangPlayers.get(gangPlayers.size() - 1).equals(tempGangPlayer))
                                    format = format.replace(",", "");

                                builder.append(String.format(format,
                                        tempType.getName(), tempGangPlayer.getPlayerName(), StringUtils.replaceComma(tempGangPlayer.getScore())));
                            });

                            ChatUtil.sendMessage(player, "   &fУчастники:");
                            ChatUtil.sendMessage(player, builder.toString());

                            return;
                        case "disband":
                            if (gangPlayerType != GangPlayerType.LEADER) {
                                ChatUtil.sendMessage(player, PREFIX_N + "Ваша роль в банде слишком мала для этого действия");
                                return;
                            }

                            gangData.sendMessage("Банда была удалена главой &b" + playerName);
                            gangDataMap.remove(gangName);
                            GangDataSQL.remove(gangName);

                            return;

                        case "leave":
                            if (gangPlayerType == GangPlayerType.LEADER) {
                                ChatUtil.sendMessage(player, PREFIX_N + "Вы не можете выйти из банды.");
                                return;
                            }

                            gangData.sendMessage(String.format("%s &7%s вышел из банды", typeName, playerName));
                            gangData.removeGangPlayer(playerName);

                            return;

                        case "vault":
                            if (gangPlayerType.ordinal() < GangPlayerType.OLDEST.ordinal()) {
                                ChatUtil.sendMessage(player, PREFIX_N + "Ваша роль в банде слишком мала для этого действия");
                                return;
                            }

                            player.openInventory(gangData.getVault());
                            return;
                    }

                    break;
                case 2:
                    switch (args[0]) {
                        case "rename":
                            if (gangPlayerType != GangPlayerType.LEADER) {
                                ChatUtil.sendMessage(player, PREFIX_N + "Ваша роль в банде слишком мала для этого действия");
                                return;
                            }

                            if (args[1].contains("&")) {
                                ChatUtil.sendMessage(player, PREFIX_N + "Цветовые символы запрещены");
                                return;
                            }

                            if (gangDataMap.keySet().stream().parallel().anyMatch(s -> s.equalsIgnoreCase(args[1]))) {
                                ChatUtil.sendMessage(player, PREFIX_N + "Придумайте уникальное название");
                                return;
                            }

                            gangDataMap.remove(gangName);

                            GangDataSQL.set(gangName, "name", args[1]);
                            gangData.setName(args[1]);
                            gangDataMap.put(gangData.getName(), gangData);

                            gangData.sendMessage(
                                    String.format("%s %s &fпереименовал банду в %s",
                                    gangPlayerType.getName(), gangPlayer.getPlayerName(), ChatUtil.color(args[1]))
                            );

                            return;

                        case "kick":
                            if (gangPlayerType.ordinal() < GangPlayerType.MANAGER.ordinal()) {
                                ChatUtil.sendMessage(player, PREFIX_N + "Ваша роль в банде слишком мала для этого действия");
                                return;
                            }

                            if (!gangDataManager.playerInGang(gangData, args[1].toLowerCase())) {
                                ChatUtil.sendMessage(player, PREFIX_N + "Игрок &b%s &fне найден в банде", Bukkit.getOfflinePlayer(args[1]).getName());
                                return;
                            }

                            GangPlayer kickedGangPlayer = gangData.getGangPlayer(args[1].toLowerCase());

                            if (kickedGangPlayer.getType().ordinal() >= gangPlayerType.ordinal()) {
                                ChatUtil.sendMessage(player, PREFIX_N + "Вы не можете исключить данного игрока");
                                return;
                            }

                            gangData.sendMessage(String.format("%s %s &fбыл выгнан из банды %s %s",
                                    kickedGangPlayer.getType().getName(), kickedGangPlayer.getPlayerName(),
                                    gangPlayerType.getName(), gangPlayer.getPlayerName())
                            );

                            gangData.removeGangPlayer(kickedGangPlayer.getPlayerName().toLowerCase());

                            return;

                        case "setleader":
                            if (gangPlayerType.ordinal() < GangPlayerType.LEADER.ordinal()) {
                                ChatUtil.sendMessage(player, PREFIX_N + "Ваша роль в банде слишком мала для этого действия");
                                return;
                            }

                            if (!gangData.getGangPlayerMap().containsKey(args[1].toLowerCase())) {
                                ChatUtil.sendMessage(player, PREFIX_N + "Игрок не найден");
                                return;
                            }

                            if (args[1].equalsIgnoreCase(playerName)) {
                                ChatUtil.sendMessage(playerName, PREFIX_N + "Вы не можете передать главу самому себе");
                                return;
                            }

                            GangPlayer newLeader = gangPlayersMap.get(args[1].toLowerCase());

                            gangData.sendMessage(String.format(
                                    "%s %s &fпередал главу %s %s",
                                    gangPlayerType.getName(), gangPlayer.getPlayerName(),
                                    newLeader.getType().getName(), newLeader.getPlayerName()
                                    ));

                            newLeader.setType(GangPlayerType.LEADER);
                            gangPlayer.setType(GangPlayerType.MANAGER);

                            return;

                        case "promote":
                            if (gangPlayerType.ordinal() < GangPlayerType.MANAGER.ordinal()) {
                                ChatUtil.sendMessage(player, PREFIX_N + "Ваша роль в банде слишком мала для этого действия");
                                return;
                            }

                            if (args[1].equalsIgnoreCase(playerName)) {
                                ChatUtil.sendMessage(player, PREFIX_N + "Вы не можете повысить себя");
                                return;
                            }

                            GangPlayer promotedGangPlayer = gangData.getGangPlayer(args[1].toLowerCase());

                            if (promotedGangPlayer == null) {
                                ChatUtil.sendMessage(player, PREFIX_N + "Игрок не состоит в банде");
                                return;
                            }

                            if (promotedGangPlayer.getType().equals(GangPlayerType.getTypeFromOrdinal(gangPlayerType.ordinal() -1))) {
                                ChatUtil.sendMessage(player, PREFIX_N + "Игрок не может быть повышен");
                                return;
                            }

                            if (promotedGangPlayer.upType()) {
                                gangData.sendMessage(String.format("Игрок %s &fбыл повышен до %s", promotedGangPlayer.getPlayerName(), promotedGangPlayer.getType().getName()));
                                return;
                            }

                            ChatUtil.sendMessage(player, PREFIX_N + "Игрок не может быть повышен");
                            return;

                        case "demote":
                            if (gangPlayerType.ordinal() < GangPlayerType.MANAGER.ordinal()) {
                                ChatUtil.sendMessage(player, PREFIX_N + "Ваша роль в банде слишком мала для этого действия");
                                return;
                            }

                            if (args[1].equalsIgnoreCase(playerName)) {
                                ChatUtil.sendMessage(player, PREFIX_N + "Вы не можете понизить себя");
                                return;
                            }

                            GangPlayer demotedGangPlayer = gangData.getGangPlayer(args[1].toLowerCase());

                            if (demotedGangPlayer == null) {
                                ChatUtil.sendMessage(player, PREFIX_N + "Игрок не состоит в банде");
                                return;
                            }

                            if (demotedGangPlayer.getType().ordinal() >= gangPlayerType.ordinal()) {
                                ChatUtil.sendMessage(player, PREFIX_N + "Игрок не может быть понижен");
                                return;
                            }

                            if (demotedGangPlayer.deUpType()) {
                                gangData.sendMessage(String.format("Игрок %s &fбыл понижен до %s", demotedGangPlayer.getPlayerName(), demotedGangPlayer.getType().getName()));
                                return;
                            }

                            ChatUtil.sendMessage(player, PREFIX_N + "Игрок не может быть понижен");
                            return;

                        case "info":
                            GangData playerGangData = gangDataManager.getGangDataMap().get(args[1]);

                            if (playerGangData == null) {
                                ChatUtil.sendMessage(player, PREFIX_N + "Банда не найдена");
                                return;
                            }

                            ChatUtil.sendMessage(player, PREFIX + "§fБанда %s &8&o(%s/%s)&r&f:", args[1], playerGangData.getGangPlayerMap().size(), 10);
                            ChatUtil.sendMessage(player, "   &fКоличество очков: &b%s", StringUtils.replaceComma(playerGangData.getScore()));

                            val gangPlayers = playerGangData.findGangPlayers(
                                    GangPlayerType.LEADER,
                                    GangPlayerType.MANAGER,
                                    GangPlayerType.OLDEST,
                                    GangPlayerType.DEFAULT
                            );
                            StringBuilder builder = new StringBuilder();

                            gangPlayers.forEach(tempGangPlayer -> {
                                GangPlayerType tempType = tempGangPlayer.getType();
                                String format = "%s &7%s, ";

                                if (gangPlayers.get(gangPlayers.size() - 1).equals(tempGangPlayer))
                                    format = format.replace(",", "");

                                builder.append(String.format(format,
                                        tempType.getName(), tempGangPlayer.getPlayerName()));
                            });

                            ChatUtil.sendMessage(player, "   &fУчастники:");
                            ChatUtil.sendMessage(player, builder.toString());

                            return;

                        case "invite":
                            if (gangPlayerType.ordinal() < GangPlayerType.OLDEST.ordinal()) {
                                ChatUtil.sendMessage(player, PREFIX_N + "Ваша роль в банде слишком мала для этого действия");
                                return;
                            }

                            Player invitedPlayer = Bukkit.getPlayer(args[1]);

                            if (invitedPlayer == null) {
                                ChatUtil.sendMessage(player, PREFIX_N + "Игрок не найден");
                                return;
                            }

                            String invitedName = invitedPlayer.getName();

                            if (gangDataManager.playerHasGang(invitedName)) {
                                ChatUtil.sendMessage(player, PREFIX_N + "Игрок уже состоит в банде");
                                return;
                            }

                            if (invitedList.get(invitedName.toLowerCase()).contains(gangData.getName())) {
                                ChatUtil.sendMessage(player, PREFIX_N + "Этот игрок уже приглашён в банду");
                                return;
                            }

                            invitedList.get(invitedName.toLowerCase()).add(gangData.getName());
                            ChatUtil.sendMessage(player, PREFIX + "Вы пригласили &b%s &aв банду. У него есть 1 минута, чтобы принять", invitedName);

                            BaseComponent[] component = ChatUtil
                                    .newBuilder(
                                            String.format("§aВас пригласили в банду %s§a. У Вас есть 1 минута, чтобы принять §8§o(/gang accept %s)",
                                                    gangData.getName().replace("&", "§"), gangData.getName())
                                    )
                                    .setHoverEvent(HoverEvent.Action.SHOW_TEXT, "§7Нажмите, чтобы вывести команду")
                                    .setClickEvent(ClickEvent.Action.SUGGEST_COMMAND, "/gang accept " + gangData.getName())
                                    .build();

                            invitedPlayer.spigot().sendMessage(component);

                            Bukkit.getScheduler().runTaskLater(
                                    OpPrison.getInstance(),
                                    () -> invitedList.get(invitedName.toLowerCase()).remove(gangData.getName()),
                                    20 * 60);

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
                        if (args[1].length() > 5) {
                            ChatUtil.sendMessage(player, PREFIX + "Название банды не может превышать 5 символов");
                            return;
                        }

                        if (args[1].contains("&")) {
                            ChatUtil.sendMessage(player, PREFIX + "Цветовые символы временно запрещены");
                            return;
                        }

                        if (gangDataMap.keySet().stream().parallel().anyMatch(s -> s.equalsIgnoreCase(args[1]))) {
                            ChatUtil.sendMessage(player, PREFIX + "Придумайте уникальное название");
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
                            String format = "%s &7%s&7,";

                            if (gangPlayers.get(gangPlayers.size() - 1).equals(tempGangPlayer))
                                format = format.replace(",", "");

                            builder.append(String.format(format,
                                    tempType.getName(), tempGangPlayer.getPlayerName()));
                        });

                        ChatUtil.sendMessage(player, "   &fУчастники:");
                        ChatUtil.sendMessage(player, builder.toString());

                        return;

                    case "accept":
                        List<String> invitedGangs = invitedList.get(playerName.toLowerCase());

                        if (invitedGangs.isEmpty()) {
                            ChatUtil.sendMessage(player, PREFIX + "У вас нет активных приглашений");
                            return;
                        }

                        GangData acceptedGang = gangDataMap.get(args[1]);

                        if (acceptedGang == null || !invitedGangs.contains(acceptedGang.getName())) {
                            ChatUtil.sendMessage(player, PREFIX + "Банда не найдена");
                            return;
                        }

                        invitedList.get(playerName.toLowerCase()).remove(acceptedGang.getName());
                        acceptedGang.addGangPlayer(new GangPlayer(playerName, GangPlayerType.DEFAULT, 0.0));
                        acceptedGang.sendMessage(String.format("Игрок &b%s &fвступил в банду", playerName));

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
        ChatUtil.sendMessage(player, "   &b/gangchat (/gac) <Сообщение> &f- написать сообщение в чат банды");
        ChatUtil.sendMessage(player, "   &b/gang vault &f- открыть хранилище банды");
        ChatUtil.sendMessage(player, "");
        ChatUtil.sendMessage(player, "   &b/gang invite <Ник игрока> &f- отправить игроку запрос в банду &8&o(от Старейшины)");
        ChatUtil.sendMessage(player, "   &b/gang kick <Ник игрока> &f- исключить игрока из банды &8&o(от Соруководителя)");
        ChatUtil.sendMessage(player, "   &b/gang promote <Ник игрока> &f- повысить игрока в банде &8&o(от Соруководителя)");
        ChatUtil.sendMessage(player, "   &b/gang demote <Ник игрока> &f- понизить игрока в банде &8&o(от Соруководителя)");
        ChatUtil.sendMessage(player, "   &b/gang setleader <Ник игрока> &f- передать Главу игроку");
        ChatUtil.sendMessage(player, "");
        ChatUtil.sendMessage(player, "   &b/gang accept <Имя банды>");
    }
}
