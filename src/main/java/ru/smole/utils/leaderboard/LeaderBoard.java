package ru.smole.utils.leaderboard;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import org.bukkit.ChatColor;
import org.bukkit.Location;
import ru.smole.OpPrison;
import ru.smole.utils.StringUtils;
import ru.smole.utils.hologram.Hologram;
import ru.xfenilafs.core.util.ChatUtil;

public class LeaderBoard {
    public static List<String> holograms = new ArrayList<>();

    private final String criteria;

    public LeaderBoard(String topName, Location location, String criteria) {
        this.criteria = criteria;
        OpPrison.getInstance().getHologramManager().createHologram(
                criteria,
                location,
                hologram -> {
                    hologram.addLine(ChatUtil.color(topName));
                    hologram.addLine(ChatUtil.color(topName));
                    for (int i = 0; i < 10; i++)
                        hologram.addLine(ChatUtil.text("§fЗагрузка..."));
                    hologram.addLine(ChatUtil.color("&8(обновление раз в 5 минут)"));
                }
        );

        OpPrison.getInstance().getServer().getScheduler().runTaskTimer(OpPrison.getInstance(), this::update, 0L, OpPrison.getInstance().getConfig().getInt("leaderBoard.update") * 20L);

        holograms.add(criteria);
    }

    public void update() {
        Hologram hologram = OpPrison.getInstance().getHologramManager().getCachedHologram(criteria);
        ResultSet resultSet = OpPrison.getInstance().getBase().getResult("SELECT * FROM OpPrison ORDER BY " + criteria + " DESC LIMIT 10");
        try {
            for (int i = 1; resultSet.next(); i++) {
                hologram.modifyLine(i, getLine(i, resultSet.getString("name"), StringUtils.fixDouble(0, resultSet.getDouble(criteria))));
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
