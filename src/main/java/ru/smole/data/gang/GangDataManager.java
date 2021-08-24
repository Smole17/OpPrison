package ru.smole.data.gang;

import lombok.Getter;
import lombok.val;
import org.yaml.snakeyaml.external.biz.base64Coder.Base64Coder;
import ru.smole.commands.GangCommand;
import ru.smole.data.mysql.GangDataSQL;

import java.util.HashMap;
import java.util.Map;

import static ru.smole.data.gang.GangData.GangPlayer.GangPlayerType;

public class GangDataManager {

    private @Getter Map<String, GangData> gangDataMap;

    public GangDataManager() {
        gangDataMap = new HashMap<>();
    }

    public void create(String name, String leader) {
        Map<String, GangData.GangPlayer> gangPlayerMap = new HashMap<>();
        gangPlayerMap.put(leader, new GangData.GangPlayer(leader, GangData.GangPlayer.GangPlayerType.LEADER));

        gangDataMap.put(name.toLowerCase(), new GangData(name, gangPlayerMap, 0.0));
        GangDataSQL.create(name.toLowerCase(), leader);
    }

    public void load() {
        String[] name = (String[]) GangDataSQL.get("name");

        if (name == null)
            return;

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

    public boolean playerHasGang(String playerName) {
        return getGangFromPlayer(playerName) != null;
    }

    public boolean playerInGang(GangData gangData, String playerName) {
        return gangData.getGangPlayerMap().containsKey(playerName);
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

            gangPlayerMap.put(playerName, new GangData.GangPlayer(playerName, type));
        }

        return gangPlayerMap;
    }

    protected String getGangPlayers(Map<String, GangData.GangPlayer> gangPlayerMap) {
        StringBuilder builder = new StringBuilder();

        int i = 1;
        String format = "%s-%s,";

        for (GangData.GangPlayer gangPlayer : gangPlayerMap.values()) {
            if (i == gangPlayerMap.size())
                format = "%s-%s";

            String member = String.format(format,
                    gangPlayer.getPlayerName(), gangPlayer.getType().name());

            builder.append(member);
            i++;
        }

        return builder.toString();
    }
}
