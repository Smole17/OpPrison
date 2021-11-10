package ru.smole.utils.leaderboard;

import org.bukkit.ChatColor;
import org.bukkit.Location;
import ru.smole.OpPrison;
import ru.smole.utils.StringUtils;
import ru.xfenilafs.core.holographic.impl.SimpleHolographic;
import ru.xfenilafs.core.util.ChatUtil;

import java.util.ArrayList;
import java.util.List;

public class LeaderBoard {
    public static List<SimpleHolographic> holograms = new ArrayList<>();

    private final String criteria;
    private final SimpleHolographic simpleHolographic;
    private final String table;

    public LeaderBoard(String topName, Location location, String criteria, String table) {
        this.criteria = criteria;
        this.table = table;
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
                .executeQuery(true, "SELECT * FROM " + table + " ORDER BY " + criteria + " DESC LIMIT 10")
                .thenAcceptAsync(resultSet -> {
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
