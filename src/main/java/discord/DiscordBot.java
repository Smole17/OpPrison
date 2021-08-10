package discord;

import lombok.Getter;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.JDABuilder;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.events.ReadyEvent;
import net.dv8tion.jda.api.events.message.guild.GuildMessageReceivedEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import net.dv8tion.jda.api.requests.GatewayIntent;
import org.apache.commons.lang3.RandomUtils;
import org.bukkit.event.player.PlayerAchievementAwardedEvent;
import org.jetbrains.annotations.NotNull;
import ru.smole.utils.StringUtils;

import javax.security.auth.login.LoginException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;
import java.util.function.Consumer;

@Getter
public class DiscordBot extends ListenerAdapter {

    private String token;
    private JDA jda;
    private Guild guild;
    private DiscordHandler discordHandler;

    protected Map<String, Consumer<GuildMessageReceivedEvent>> events = new HashMap<>();

    public DiscordBot(String token) throws LoginException {
        this.token = token;

        jda = JDABuilder.create(token, GatewayIntent.GUILD_MEMBERS, GatewayIntent.GUILD_MESSAGES)
                .addEventListeners(this)
                .build();
    }

    /**
    * Handler/s
    **/

    public void sendMessage(String channel, String message) {
        jda.getGuilds().forEach(guild ->
                guild.getTextChannels().forEach(textChannel -> {
                    if (textChannel.getName().equals(channel)) {
                        textChannel
                                .sendMessage(message)
                                .queue();
                    }
                })
        );
    }

    public void sendMessage(String channel, String... message) {
        jda.getGuilds().forEach(guild ->
                guild.getTextChannels().forEach(textChannel -> {
                    if (textChannel.getName().equals(channel)) {
                        Arrays.stream(message).forEach(s ->
                                textChannel
                                        .sendMessage(s)
                                        .queue());
                    }
                })
        );
    }

    public void handleMessage(Consumer<GuildMessageReceivedEvent> consumer) {
        events.put(StringUtils.generateString(5), consumer);
    }

    /**
     * Help methods
     **/

    @Override
    public void onGuildMessageReceived(@NotNull GuildMessageReceivedEvent event) {
        if (!events.isEmpty()) {
            events.forEach((s, guildMessageReceivedEventConsumer) -> {
                if (events.containsKey(s))
                    guildMessageReceivedEventConsumer.accept(event);
            });
        }
    }

    @Override
    public void onReady(@NotNull ReadyEvent event) {
        guild = jda.getGuildById("870780539374796892");

        discordHandler = new DiscordHandler(this);
    }
}
