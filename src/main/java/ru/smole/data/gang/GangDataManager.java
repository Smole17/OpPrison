package ru.smole.data.gang;

import com.google.common.io.BaseEncoding;
import com.mysql.jdbc.util.Base64Decoder;
import com.sun.xml.internal.org.jvnet.staxex.Base64EncoderStream;
import lombok.Getter;
import lombok.val;
import org.yaml.snakeyaml.external.biz.base64Coder.Base64Coder;
import ru.smole.data.mysql.GangDataSQL;
import sun.misc.BASE64Encoder;

import java.nio.ByteBuffer;
import java.util.*;

import static ru.smole.data.gang.GangData.GangPlayer.GangPlayerType;

public class GangDataManager {

    private @Getter Map<String, GangData> gangDataMap;

    public GangDataManager() {
        gangDataMap = new HashMap<>();
    }

    public void create(String name, String owner) {
        Map<String, GangData.GangPlayer> gangPlayerMap = new HashMap<>();
        gangPlayerMap.put(owner, new GangData.GangPlayer(owner, GangPlayerType.LEADER, 0.0));

        gangDataMap.put(name, new GangData(name, gangPlayerMap, 0.0));
        GangDataSQL.create(name, owner);
    }

    public void load() {
        String[] name = (String[]) GangDataSQL.get("name");

        for (String s : name) {
            double score = (Double) GangDataSQL.get(s, "score");
            val gangPlayersList = getGangPlayers(s);

            gangDataMap.put(s, new GangData(s, gangPlayersList, score));
        }
    }

    public void unload() {
        if (gangDataMap.isEmpty())
            return;

        gangDataMap.forEach((s, gangData) ->
                GangDataSQL.save(
                        s,
                        Base64Coder.encodeString(getGangPlayers(gangData.getGangPlayerMap())),
                        gangData.getScore()
                )
        );

        gangDataMap.clear();
    }

    public boolean playerHasGuild(String playerName) {
        return getGangFromPlayer(playerName) != null;
    }

    public GangData getGangFromPlayer(String playerName) {
        if (gangDataMap.isEmpty())
            return null;

        GangData gangData = null;

        for (GangData value : gangDataMap.values()) {
            if (value.getGangPlayerMap().containsKey(playerName)) {
                gangData = value;
                break;
            }
        }

        return gangData;
    }

    protected Map<String, GangData.GangPlayer> getGangPlayers(String name) {
        String members = Base64Coder.decodeString(String.valueOf(GangDataSQL.get(name, "members")));
        Map<String, GangData.GangPlayer> gangPlayerMap = new HashMap<>();

        for (String s : members.split(",")) {
            String[] data = s.split("-");

            String playerName = data[0];
            GangPlayerType type;

            try {
                type = GangPlayerType.valueOf(data[1]);
            } catch (Exception ex) {
                throw new IllegalArgumentException("Could not load GangPlayerType with name + " + data[1]);
            }

            double score = Double.parseDouble(data[2]);

            gangPlayerMap.put(playerName, new GangData.GangPlayer(playerName, type, score));
        }

        return gangPlayerMap;
    }

    protected String getGangPlayers(Map<String, GangData.GangPlayer> gangPlayerMap) {
        StringBuilder builder = new StringBuilder();

        int i = 1;
        String format = "%s-%s-%s,";

        for (GangData.GangPlayer gangPlayer : gangPlayerMap.values()) {
            if (i == gangPlayerMap.size())
                format = "%s-%s-%s";

            String member = String.format(format,
                    gangPlayer.getPlayerName(), gangPlayer.getType().name(), gangPlayer.getScore());

            builder.append(member);
            i++;
        }

        return builder.toString();
    }
}