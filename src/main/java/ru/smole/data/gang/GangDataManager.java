package ru.smole.data.gang;

import lombok.Getter;
import lombok.val;
import org.yaml.snakeyaml.external.biz.base64Coder.Base64Coder;
import ru.smole.commands.GangCommand;
import ru.smole.data.mysql.GangDataSQL;

import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.util.*;

import static ru.smole.data.gang.GangData.GangPlayer.GangPlayerType;

public class GangDataManager {

    private final @Getter Map<String, GangData> gangDataMap;

    public GangDataManager() {
        gangDataMap = new HashMap<>();
    }

    public void create(String name, String leader) {
        Map<String, GangData.GangPlayer> gangPlayerMap = new HashMap<>();
        gangPlayerMap.put(leader.toLowerCase(), new GangData.GangPlayer(leader, GangData.GangPlayer.GangPlayerType.LEADER));

        gangDataMap.put(name, new GangData(name, gangPlayerMap, 0.0));
        GangDataSQL.create(
                name,
                getGangPlayers(gangPlayerMap)
        );
    }

    public void load() {
        List<String> name = new ArrayList<>();

        GangDataSQL.get(resultSet -> {
            try {
                if (!resultSet.next())
                    return;

                for (int i = 1; i <= resultSet.getMetaData().getColumnCount(); i++) {
                    name.add(resultSet.getString(i));
                }

                if (name.isEmpty())
                    return;

                for (String s : name) {
                    GangDataSQL.get(s, resultSet1 -> {
                        try {
                        if (!resultSet1.next())
                            return;

                        val gangPlayersList = getGangPlayers(s);

                            gangDataMap.put(s, new GangData(s, gangPlayersList, resultSet1.getDouble("score")));
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
                        Base64.getEncoder().encodeToString(getGangPlayers(gangData.getGangPlayerMap()).getBytes()),
                        gangData.getScore()
                )
        );

        gangDataMap.clear();
    }

    public boolean playerHasGang(String playerName) {
        return getGangFromPlayer(playerName.toLowerCase()) != null;
    }

    public boolean playerInGang(GangData gangData, String playerName) {
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

    protected Map<String, GangData.GangPlayer> getGangPlayers(String name) {
        Map<String, GangData.GangPlayer> gangPlayerMap = new HashMap<>();

        GangDataSQL.get(name, resultSet -> {
            try {
                if (!resultSet.next())
                    return;

                String members = new String(Base64.getDecoder().decode(resultSet.getString("members")));

                for (String s : members.split(",")) {
                    String[] data = s.split("-");

                    String playerName = data[0];
                    GangPlayerType type;

                    try {
                        type = GangPlayerType.valueOf(data[1]);
                    } catch (Exception ex) {
                        throw new IllegalArgumentException("Could not load GangPlayerType with name + " + data[1]);
                    }

                    gangPlayerMap.put(playerName.toLowerCase(), new GangData.GangPlayer(playerName, type));
                }
            } catch (SQLException throwables) {
                throwables.printStackTrace();
            }
        });

        return gangPlayerMap;
    }

    public String getGangPlayers(Map<String, GangData.GangPlayer> gangPlayerMap) {
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
