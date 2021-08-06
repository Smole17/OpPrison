package ru.smole.data.gang;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.Getter;
import ru.xfenilafs.core.util.ChatUtil;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Data
public class GangData {

    private String name;
    private Map<String, GangPlayer> gangPlayerMap;
    private double score;

    public GangData(String name, Map<String, GangPlayer> gangPlayerMap, double score) {
        this.name = name;
        this.gangPlayerMap = gangPlayerMap;
        this.score = score;
    }



    public void sendMessage(String message) {
        gangPlayerMap.values().forEach(gangPlayer ->
                ChatUtil.sendMessage(gangPlayer.getPlayerName(),"&8[&f%s&8] &f" + message, name)
        );
    }

    public void sendMessage(String message, Object... objects) {
        gangPlayerMap.values().forEach(gangPlayer ->
                ChatUtil.sendMessage(gangPlayer.getPlayerName(), "&8[&f%s&8] &f" + message, name, objects)
        );
    }

    public double addScore(double count) {
        double added = score + count;
        setScore(added);
        return added;
    }

    public List<GangPlayer> findGangPlayers(GangPlayer.GangPlayerType type) {
        return gangPlayerMap.values().stream().filter(gangPlayer -> gangPlayer.getType() == type).collect(Collectors.toList());
    }

    public List<GangPlayer> findGangPlayers(GangPlayer.GangPlayerType... type) {
        List<GangPlayer> gangPlayerList = new ArrayList<>();

        for (GangPlayer.GangPlayerType gangPlayerType : type)
            gangPlayerList.addAll(findGangPlayers(gangPlayerType));

        return gangPlayerList;
    }
}
