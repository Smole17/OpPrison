package ru.smole.commands;

import lombok.val;
import org.bukkit.entity.Player;
import ru.smole.OpPrison;
import ru.smole.data.gang.GangData;
import ru.smole.data.gang.GangDataManager;
import ru.smole.data.gang.GangPlayer;
import ru.smole.utils.StringUtils;
import ru.xfenilafs.core.command.BukkitCommand;
import ru.xfenilafs.core.util.ChatUtil;

import java.util.Arrays;
import static ru.smole.data.gang.GangPlayer.GangPlayerType;

import static ru.smole.OpPrison.PREFIX;
import static ru.smole.OpPrison.getInstance;

public class GangCommand extends BukkitCommand<Player> {
    public GangCommand() {
        super("gang", "g");
    }

    @Override
    protected void onExecute(Player player, String[] args) {
        String playerName = player.getName();
        GangDataManager gangDataManager = getInstance().getGangDataManager();
        val gangDataMap = gangDataManager.getGangDataMap();

        boolean playerHasGuild = gangDataManager.playerHasGuild(playerName);

        switch (args.length) {
            case 1:
                if (!playerHasGuild) {
                    ChatUtil.sendMessage(player, PREFIX + "Вы не состоите в банде");
                    return;
                }

                GangData gangData = gangDataManager.getGangFromPlayer(playerName);
                String gangName = gangData.getName();

                val gangPlayersMap = gangData.getGangPlayerMap();
                GangPlayer gangPlayer = gangPlayersMap.get(playerName);

                GangPlayerType gangPlayerType = gangPlayer.getType();
                String typeName = gangPlayerType.getName();

                switch (args[0]) {
                    case "disband":
                        if (gangPlayerType != GangPlayerType.LEADER) {
                            ChatUtil.sendMessage(player, PREFIX + "Ваша роль в банде слишком мала для этого действия");
                            return;
                        }

                        gangData.sendMessage("Банда была удалена главой &b%s", playerName);
                        gangDataMap.remove(gangName);

                        return;

                    case "leave":
                        gangData.sendMessage("%s &7%s вышел из банды", typeName, playerName);
                        gangPlayersMap.remove(playerName);

                        return;

                    case "info":
                        ChatUtil.sendMessage(player, PREFIX + "Банда %s&f:", gangName);
                        ChatUtil.sendMessage(player, "   &fКоличество очков: &b%s", StringUtils.replaceComma(gangData.getScore()));

                        val gangPlayers = gangData.findGangPlayers(GangPlayerType.LEADER, GangPlayerType.MANAGER, GangPlayerType.OLDEST, GangPlayerType.DEFAULT);
                        StringBuilder builder = new StringBuilder();

                        gangPlayers.forEach(tempGangPlayer -> {
                            GangPlayerType tempType = tempGangPlayer.getType();
                            String format = "%s &7%s &8&o(%s)&7,";

                            if (gangPlayers.get(gangPlayers.size() - 1).equals(tempGangPlayer))
                                format = format.replace(",", "");

                            builder.append(String.format(format,
                                    tempType.getName(), gangPlayer.getPlayerName(), StringUtils.replaceComma(gangPlayer.getScore())));
                        });

                        ChatUtil.sendMessage(player, "   &fУчастники:");
                        ChatUtil.sendMessage(player, builder.toString());

                        return;
                }

                break;
            case 2:
                switch (args[0]) {
                    case "create":
                        return;

                }

                break;
        }

        help(player);
    }

    public void help(Player player) {
        ChatUtil.sendMessage(player, PREFIX + "Помощь:");
        ChatUtil.sendMessage(player, "   /gang create <Имя банды> - создать новую банду");
        ChatUtil.sendMessage(player, "   /gang disband - удалить банду &8&o(от Главы)");
        ChatUtil.sendMessage(player, "   /gang leave - покинуть банду");
        ChatUtil.sendMessage(player, "   /gang info - посмотреть информацию о банде");
        ChatUtil.sendMessage(player, "   /gang rename <Новое имя банды> - сменить название банды");
        ChatUtil.sendMessage(player, "   /gangchat (/gc) <Сообщение> - написать сообщение в чат банды");
        ChatUtil.sendMessage(player, "");
        ChatUtil.sendMessage(player, "   /gang invite <Ник игрока> - отправить игроку запрос в банду &8&o(от Старейшины)");
        ChatUtil.sendMessage(player, "   /gang kick <Ник игрока> -  выгнать игрока из банды &8&o(от Соруководителя)");
        ChatUtil.sendMessage(player, "   /gang promote <Ник игрока> - повысить игрока в банде &8&o(от Соруководителя)");
        ChatUtil.sendMessage(player, "   /gang demote <Ник игрока> - понизить игрока в банде &8&o(от Соруководителя)");
        ChatUtil.sendMessage(player, "   /gang setleader <Ник игрока>> - передать Главу игроку");
    }
}
