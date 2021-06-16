package ru.smole.utils;

import com.google.common.collect.Lists;
import com.google.common.io.ByteArrayDataInput;
import com.google.common.io.ByteArrayDataOutput;
import com.google.common.io.ByteStreams;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import org.bukkit.entity.Player;
import org.bukkit.plugin.messaging.PluginMessageListener;
import ru.smole.OpPrison;

import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;

@Data
@RequiredArgsConstructor
public class BungeeUtils implements PluginMessageListener {

    private static final List<String> SERVERS = Lists.newArrayList(
            "hub1",
            "hub2",
            "MyLittleFarm",
            "hard",
            "evo",
            "naruto",
            "AzerusLobby",
            "azerus1-1",
            "azerus1-2",
            "azerus2-1",
            "azerus2-2"
    );
    public static Map<String, Integer> serversOnline = new ConcurrentHashMap<>();

    private static long lastUpdate = 0L;

    public static void sendToServer(final Player player, final String serverName) {
        ByteArrayDataOutput out = ByteStreams.newDataOutput();
        out.writeUTF("Connect");
        out.writeUTF(serverName);
        player.sendPluginMessage(OpPrison.getInstance(), "BungeeCord", out.toByteArray());
    }

    public static int getAllOnline(Player player) {
        if (System.currentTimeMillis() - lastUpdate >= 10_000L) {
            SERVERS.forEach(s -> requestPlayersCount(s, player));
            lastUpdate = System.currentTimeMillis();
        }
        int online = 0;
        for (String server : SERVERS) {
            online += getServerOnline(server);
        }
        return online;
    }

    public static int getServerOnline(String serverName) {
        return serversOnline.getOrDefault(serverName.toLowerCase(), 0);
    }

    public static void requestPlayersCount(String serverName, final Player player) {
        final ByteArrayDataOutput out = ByteStreams.newDataOutput();
        out.writeUTF("PlayerCount");
        out.writeUTF(serverName);
        player.sendPluginMessage(OpPrison.getInstance(), "BungeeCord", out.toByteArray());
    }

    @Override
    public void onPluginMessageReceived(final String string, final Player player, final byte[] message) {
        if (!string.equals("BungeeCord")) {
            return;
        }
        ByteArrayDataInput in = ByteStreams.newDataInput(message);
        String subchannel = in.readUTF();
        if (subchannel.equals("PlayerCount")) {
            String server = in.readUTF();
            int playersCount = in.readInt();
            serversOnline.put(server.toLowerCase(), playersCount);
        }
    }
}
