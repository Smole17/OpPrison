package ru.smole.data.gang;

import lombok.Getter;
import lombok.val;
import ru.smole.data.mysql.GangDataSQL;

import java.sql.SQLException;
import java.util.Base64;
import java.util.HashMap;
import java.util.Map;

import static ru.smole.data.gang.GangData.GangPlayer.GangPlayerType;

public class GangDataManager {

    private final @Getter Map<String, GangData> gangDataMap;

    public GangDataManager() {
        gangDataMap = new HashMap<>();
    }

    public void create(String name, String leader) {
        Map<String, GangData.GangPlayer> gangPlayerMap = new HashMap<>();
        GangData gangData = new GangData(name, gangPlayerMap, 0.0, null);
        gangData.addGangPlayer(new GangData.GangPlayer(leader, GangData.GangPlayer.GangPlayerType.LEADER, 0.0));

        gangDataMap.put(name, gangData);
        GangDataSQL.create(
                name,
                getGangPlayers(gangPlayerMap)
        );
    }

    public void load() {
        GangDataSQL.get(resultSet -> {
            try {
                while (resultSet.next()) {
                    val gangPlayersList =
                            getGangPlayers(
                                    new String(
                                            Base64.getDecoder().decode(resultSet.getString("members"))
                                    )
                            );

                    String s = resultSet.getString("name");

                    gangDataMap.put(s, new GangData(s, gangPlayersList, resultSet.getDouble("score"), resultSet.getString("vault")));
                }
            } catch (SQLException throwables) {
                throwables.printStackTrace();
            }
        });
    }

    public void unload() {
        if (gangDataMap.isEmpty())
            return;

        gangDataMap.forEach((s, gangData) ->
                GangDataSQL.save(
                        s,
                        Base64.getEncoder().encodeToString(getGangPlayers(gangData.getGangPlayerMap()).getBytes()),
                        gangData.getScore(),
                        gangData.saveVault()
                )
        );

        gangDataMap.clear();
    }

    public boolean playerHasGang(String playerName) {
        return getGangFromPlayer(playerName.toLowerCase()) != null;
    }

    public boolean playerInGang(GangData gangData, String playerName) {
        if (gangData == null)
            return false;

        return gangData.getGangPlayerMap().containsKey(playerName.toLowerCase());
    }

    public GangData getGangFromPlayer(String playerName) {
        if (gangDataMap.isEmpty())
            return null;

        GangData gangData = null;

        for (GangData value : gangDataMap.values()) {
            if (value.getGangPlayerMap().containsKey(playerName.toLowerCase())) {
                gangData = value;
                break;
            }
        }

        return gangData;
    }

    protected Map<String, GangData.GangPlayer> getGangPlayers(String members) {
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

            gangPlayerMap.put(playerName.toLowerCase(), new GangData.GangPlayer(playerName, type, score));
        }

        return gangPlayerMap;
    }

    public String getGangPlayers(Map<String, GangData.GangPlayer> gangPlayerMap) {
        StringBuilder builder = new StringBuilder();

        String format = "%s-%s-%s,";

        for (GangData.GangPlayer gangPlayer : gangPlayerMap.values()) {
            String member = String.format(format,
                    gangPlayer.getPlayerName(), gangPlayer.getType().name(), gangPlayer.getScore());

            builder.append(member);
        }

        return builder.toString();
    }
}
