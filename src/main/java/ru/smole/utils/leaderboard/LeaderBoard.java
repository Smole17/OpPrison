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
    private final String criteria;

    public LeaderBoard(Location location, OpPrison plugin, String criteria) {
        this.criteria = criteria;
        hologram = ApiManager.createHolographic(location);
        createLeaderBoard();
        plugin.getServer().getScheduler().runTaskTimer(plugin, this::update, 0L, OpPrison.getInstance().getConfig().getInt("leaderBoard.update") * 20L);
    }

    private void createLeaderBoard() {
        for (int i = 0; i < 10; i++)
            hologram.setTextLine(i, ChatUtil.text("#%s §fЗагрузка", i));
    }

    public void update() {
        ResultSet resultSet = OpPrison.getInstance().getBase().getResult("SELECT * FROM OpPrison ORDER BY " + criteria + " DESC LIMIT 10");
        try {
            for (int i = 0; resultSet.next(); i++) {
                hologram.setTextLine(i + 1, getLine(i + 1, resultSet.getString("name"), resultSet.getDouble(criteria)));
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
