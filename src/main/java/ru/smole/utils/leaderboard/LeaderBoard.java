package ru.smole.utils.leaderboard;

import java.sql.ResultSet;
import java.sql.SQLException;

import org.bukkit.ChatColor;
import org.bukkit.Location;
import ru.smole.OpPrison;
import ru.smole.utils.StringUtils;
import ru.xfenilafs.core.ApiManager;
import ru.xfenilafs.core.holographic.ProtocolHolographic;
import ru.xfenilafs.core.util.ChatUtil;

public class LeaderBoard {
    private final ProtocolHolographic hologram;
    private final String topName;
    private final String criteria;

    public LeaderBoard(String topName, Location location, String criteria) {
        this.topName = topName;
        this.criteria = criteria;
        hologram = ApiManager.createHolographic(location);
        createLeaderBoard();
        OpPrison.getInstance().getServer().getScheduler().runTaskTimer(OpPrison.getInstance(), this::update, 0L, OpPrison.getInstance().getConfig().getInt("leaderBoard.update") * 20L);
    }

    private void createLeaderBoard() {
        hologram.setTextLine(0, ChatUtil.color(topName));
        for (int i = 1; i < 10; i++)
            hologram.setTextLine(i, ChatUtil.text("#%s §fЗагрузка", i));
    }

    public void update() {
        ResultSet resultSet = OpPrison.getInstance().getBase().getResult("SELECT * FROM OpPrison ORDER BY " + criteria + " DESC LIMIT 10");
        try {
            for (int i = 1; resultSet.next(); i++) {
                hologram.setTextLine(i, getLine(i, resultSet.getString("name"), resultSet.getDouble(criteria)));
            }
        } catch (SQLException exception) {
            exception.printStackTrace();
        }
    }

    private String getLine(int i, String user, Double count) {
        if (i == 1) {
            String str = ChatColor.translateAlternateColorCodes('&', OpPrison.getInstance().getConfig().getString("leaderBoard.format1"));
            return str.replace("[top]", String.valueOf(i)).replace("[name]", user).replace("[count]", String.valueOf(StringUtils.fixDouble(1, count)));
        } else if (i == 2) {
            String str = ChatColor.translateAlternateColorCodes('&', OpPrison.getInstance().getConfig().getString("leaderBoard.format2"));
            return str.replace("[top]", String.valueOf(i)).replace("[name]", user).replace("[count]", String.valueOf(StringUtils.fixDouble(1, count)));
        } else if (i == 3) {
            String str = ChatColor.translateAlternateColorCodes('&', OpPrison.getInstance().getConfig().getString("leaderBoard.format3"));
            return str.replace("[top]", String.valueOf(i)).replace("[name]", user).replace("[count]", String.valueOf(StringUtils.fixDouble(1, count)));
        } else {
            String line = ChatColor.translateAlternateColorCodes('&', OpPrison.getInstance().getConfig().getString("leaderBoard.format-default"));
            return line.replace("[top]", String.valueOf(i)).replace("[name]", user).replace("[count]", String.valueOf(StringUtils.fixDouble(1, count)));
        }
    }
}
