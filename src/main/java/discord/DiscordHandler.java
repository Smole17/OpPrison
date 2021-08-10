package discord;

import lombok.Getter;
import lombok.val;
import net.dv8tion.jda.api.entities.*;
import net.md_5.bungee.api.chat.BaseComponent;
import net.md_5.bungee.api.chat.ClickEvent;
import net.md_5.bungee.api.chat.HoverEvent;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.json.simple.JSONObject;
import org.json.simple.JSONValue;
import ru.smole.OpPrison;
import ru.smole.commands.DiscordCommand;
import ru.smole.data.player.PlayerData;
import ru.smole.utils.RequestUtils;
import ru.xfenilafs.core.util.ChatUtil;

import java.util.*;

public class DiscordHandler {

    private DiscordBot bot;

    private @Getter Map<String, PlayerData> verifyMap;
    private @Getter Map<String, String> verified;
    private @Getter String verify = "verify";
    
    private Guild guild;
    private Role role;

    public DiscordHandler(DiscordBot bot) {
        this.bot = bot;
        this.guild = bot.getGuild();
        this.role = guild.getRoleById("871495206321664050");

        verifyMap = new HashMap<>() ;
        verified = new HashMap<>();

        start();
    }

    public void start() {
        loadMembers();

        bot.handleMessage(event -> {
            if (event.getChannel().getName().equals("основной")) {
            Member member = Objects.requireNonNull(event.getMember());
            String memberName = member.getNickname();

            if (memberName == null)
                return;

            Message message = event.getMessage();
            String text = message.getContentDisplay();

            if (memberName.equals("OpPrison"))
                return;

            val players = Bukkit.getOnlinePlayers();

            if (players.isEmpty())
                return;

            final String[] returnMessage = {text};
            ChatUtil.MessageBuilder returnComponent =
                    new ChatUtil.MessageBuilder(
                            returnMessage[0]
                    );
            List<Message.Attachment> attachments = message.getAttachments();

            if (!attachments.isEmpty()) {
                attachments.forEach(attachment -> {
                    try {
                        String result = RequestUtils.getImgurContent("af592b92855cbe5", attachment.getUrl());
                        JSONObject jsonObject = (JSONObject) JSONValue.parseWithException(result);
                        JSONObject jsonData = (JSONObject) jsonObject.get("data");
                        String formatPage = "https://imgur.com/" + jsonData.get("id");

                        returnComponent.setText(returnMessage[0] + formatPage);
                        returnComponent.setHoverEvent(HoverEvent.Action.SHOW_TEXT, "§7Нажмите, чтобы посмотреть фотографию");
                        returnComponent.setClickEvent(ClickEvent.Action.OPEN_URL, formatPage);

                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                });
            }

            ChatUtil.MessageBuilder builder =
                    new ChatUtil.MessageBuilder(
                            String.format("§8[§9DISCORD§8] §7%s§8: §f",
                                    memberName)
                    );
            String asTag = event.getAuthor().getAsTag();

            builder.setHoverEvent(HoverEvent.Action.SHOW_TEXT, String.format("§7%s", asTag));
            builder.setClickEvent(ClickEvent.Action.SUGGEST_COMMAND, asTag);

            BaseComponent[] spigotMessage = builder.build();
            spigotMessage[0].addExtra(returnComponent.build()[0]);

            players.forEach(player -> {
                if (text.contains("@all")) {
                    player.sendTitle("", "§fвас упомянули в чате", 20, 20, 20);
                }

                player.spigot().sendMessage(spigotMessage[0]);
            });
            }
        });

        bot.handleMessage(event -> {
            TextChannel channel = event.getChannel();

            if (channel.getName().equals(verify)) {
                Member member = Objects.requireNonNull(event.getMember());
                String memberName = member.getNickname();

                Message message = event.getMessage();
                String text = message.getContentDisplay();

                if (memberName != null && memberName.equals("OpPrison"))
                    return;

                String[] args = text.split("\\s");
                if (!text.startsWith("!verify") && args.length != 2) {
                    bot.sendMessage(verify, "Используйте: !verify <код>");
                    return;
                }

                if (!verifyMap.containsKey(args[1])) {
                    bot.sendMessage(verify, "Неверный код!");
                    return;
                }

                Guild guild = channel.getGuild();

                if (role == null) {
                    bot.sendMessage(verify, "Бот не настроен. Срочно обратитесь к администрации!");
                    return;
                }

                PlayerData playerData = verifyMap.get(args[1]);
                String playerName = playerData.getName();
                String tag = event.getAuthor().getAsTag();

                guild.addRoleToMember(member, role).complete();
                member.modifyNickname(playerName).complete();

                verified.put(playerName, tag);
                verifyMap.remove(args[1]);
                Bukkit.getScheduler().cancelTask(DiscordCommand.taskMap.get(playerName));

                Player player = Bukkit.getPlayer(playerName);
                if (player != null)
                    ChatUtil.sendMessage(player, OpPrison.PREFIX + "Вы успешно привязали свой игровой аккаунт к &b%s&f дискорд аккаунту", tag);
            }
        });
    }

    public void loadMembers() {
        guild.getMembersWithRoles(role).forEach(member -> {
            verified.put(member.getNickname(), member.getUser().getAsTag());
        });
    }

    public boolean contains(String playerName) {
        return verifyMap.containsValue(OpPrison.getInstance().getPlayerDataManager().getPlayerDataMap().get(playerName));
    }
}
