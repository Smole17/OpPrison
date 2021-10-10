package ru.smole.data.gang;

import lombok.Getter;
import lombok.val;
import org.yaml.snakeyaml.external.biz.base64Coder.Base64Coder;
import ru.smole.commands.GangCommand;
import ru.smole.data.mysql.GangDataSQL;

import java.sql.SQLException;
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

        gangDataMap.put(name, new GangData(name, gangPlayerMap, 0.0));
        GangDataSQL.create(name, leader);
    }

    public void load() {
        GangDataSQL.get(resultSet -> {
            try {
                if (!resultSet.next())
                    return;

            String[] name = (String[]) resultSet.getObject("name");

            if (name == null)
                return;

            for (String s : name) {
                GangDataSQL.get(s, resultSet1 -> {
                    val gangPlayersList = getGangPlayers(s);

                    try {
                        gangDataMap.put(s, new GangData(s, gangPlayersList, resultSet1.getDouble("")));
                    } catch (SQLException throwables) {
                        throwables.printStackTrace();
                    }
                });
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
        Map<String, GangData.GangPlayer> gangPlayerMap = new HashMap<>();

       GangDataSQL.get(name, resultSet -> {
           String members = null;
           try {
               members = Base64Coder.decodeString(resultSet.getString("members"));
           } catch (SQLException throwables) {
               throwables.printStackTrace();
           }

           if (members == null)
               return;

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
       });

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
