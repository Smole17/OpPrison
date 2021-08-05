package ru.smole.commands;

import discord.DiscordHandler;
import lombok.val;
import net.md_5.bungee.api.chat.BaseComponent;
import net.md_5.bungee.api.chat.ClickEvent;
import net.md_5.bungee.api.chat.HoverEvent;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import ru.smole.OpPrison;
import ru.smole.data.player.PlayerData;
import ru.smole.utils.StringUtils;
import ru.xfenilafs.core.command.BukkitCommand;
import ru.xfenilafs.core.util.ChatUtil;

import java.util.HashMap;
import java.util.Map;

public class DiscordCommand extends BukkitCommand<Player> {

    public static Map<String, Integer> taskMap = new HashMap<>();
    private String discordServer;

    public DiscordCommand(String discordServer) {
        super("discord");
        this.discordServer = discordServer;
    }

    @Override
    protected void onExecute(Player player, String[] strings) {
        String playerName = player.getName();
        OpPrison main = OpPrison.getInstance();
        DiscordHandler handler = main.getDiscordBot().getDiscordHandler();

        val verified = handler.getVerified();

        if (handler.getVerified().containsKey(playerName)) {
            ChatUtil.sendMessage(player, OpPrison.PREFIX + "Ваш аккаунт привязан к &b%s", verified.get(playerName));
            return;
        }

        val verifyMap = handler.getVerifyMap();
        PlayerData playerData = main.getPlayerDataManager().getPlayerDataMap().get(playerName);

        if (verifyMap.containsValue(playerData)) {
            ChatUtil.sendMessage(player, OpPrison.PREFIX + "Вы уже получили код верификации");
            return;
        }

        String randomCode = StringUtils.generateString(6);

        BaseComponent[] text = ChatUtil
                .newBuilder(String.format(OpPrison.PREFIX + "Ваш код: §b%s§f. Данный код доступен в течение 5 минут.", randomCode))
                .setHoverEvent(HoverEvent.Action.SHOW_TEXT, "§fНажмите, чтобы скопировать")
                .setClickEvent(ClickEvent.Action.SUGGEST_COMMAND, randomCode)
                .build();

        player.spigot().sendMessage(text);
        ChatUtil.sendMessage(player, OpPrison.PREFIX + "Сервер: &b%s", discordServer == null ? "Не установлен" : discordServer);
        ChatUtil.sendMessage(player, OpPrison.PREFIX + "Используйте его в канале &b#%s&f. §8(!verify <ваш код>)", handler.getVerify());

        verifyMap.put(randomCode, playerData);

        taskMap.put(
                playerName,
                Bukkit.getScheduler().runTaskLater(
                        main,
                        () -> {
                            verifyMap.remove(playerName);

                            if (player.isOnline())
                                ChatUtil.sendMessage(player, "Ваш код верификации устарел. Используйте &b/discord&f, чтобы получить новый");

                            Bukkit.getScheduler().cancelTask(taskMap.get(playerName));
                            taskMap.remove(playerName);
                        },
                        20 * 300
                ).getTaskId());
    }
}
