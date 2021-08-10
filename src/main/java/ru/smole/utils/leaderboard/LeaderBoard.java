package ru.smole.utils.leaderboard;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import org.bukkit.ChatColor;
import org.bukkit.Location;
import ru.smole.OpPrison;
import ru.smole.utils.StringUtils;
import ru.xfenilafs.core.ApiManager;
import ru.xfenilafs.core.holographic.impl.SimpleHolographic;
import ru.xfenilafs.core.util.ChatUtil;

public class LeaderBoard {
    public static List<SimpleHolographic> holograms = new ArrayList<>();

    private final String criteria;
    private final SimpleHolographic simpleHolographic;

    public LeaderBoard(String topName, Location location, String criteria) {
        this.criteria = criteria;
        simpleHolographic = new SimpleHolographic(location);

        simpleHolographic.addTextLine(ChatUtil.text(topName));

        for (int i = 0; i < 10; i++)
            simpleHolographic.addTextLine(ChatUtil.text("§fЗагрузка..."));

        simpleHolographic.addTextLine(ChatUtil.text("&8(обновление раз в 5 минут)"));

        holograms.add(simpleHolographic);
        update();
    }

    public void update() {
        OpPrison.getInstance().getBase().getExecuteHandler()
                .executeQuery(true, "SELECT * FROM players ORDER BY " + criteria + " DESC LIMIT 10")
                .thenAccept(resultSet -> {
                    for (int i = 1; resultSet.next(); i++)
                        simpleHolographic.setTextLine(i, getLine(i, resultSet.getString("name"), resultSet.getDouble(criteria)));
                });
    }

    private String getLine(int i, String user, Double count) {
        if (i == 1) {
            String str = ChatColor.translateAlternateColorCodes('&', OpPrison.getInstance().getConfig().getString("leaderBoard.format1"));
            return str.replace("[top]", String.valueOf(i)).replace("[name]", user).replace("[count]",
                    StringUtils.replaceComma(count));
        } else if (i == 2) {
            String str = ChatColor.translateAlternateColorCodes('&', OpPrison.getInstance().getConfig().getString("leaderBoard.format2"));
            return str.replace("[top]", String.valueOf(i)).replace("[name]", user).replace("[count]",
                    StringUtils.replaceComma(count));
        } else if (i == 3) {
            String str = ChatColor.translateAlternateColorCodes('&', OpPrison.getInstance().getConfig().getString("leaderBoard.format3"));
            return str.replace("[top]", String.valueOf(i)).replace("[name]", user).replace("[count]",
                    StringUtils.replaceComma(count));
        } else {
            String line = ChatColor.translateAlternateColorCodes('&', OpPrison.getInstance().getConfig().getString("leaderBoard.format-default"));
            return line.replace("[top]", String.valueOf(i)).replace("[name]", user).replace("[count]",
                    StringUtils.replaceComma(count));
        }
    }
}
