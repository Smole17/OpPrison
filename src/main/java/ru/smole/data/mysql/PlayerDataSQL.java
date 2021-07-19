package ru.smole.data.mysql;

import org.bukkit.ChatColor;
import ru.smole.OpPrison;
import ru.smole.data.group.GroupsManager;
import ru.xfenilafs.core.ApiManager;

import java.sql.ResultSet;
import java.sql.SQLException;

public class PlayerDataSQL {

    private static DatabaseManager db = OpPrison.getInstance().getBase();

    public static void create(String name, String pickaxe) {
        db.update("INSERT INTO " +
                "OpPrison(name, blocks, money, token, multiplier, rank, prestige, fly, pickaxe, kit, access) " +
                "VALUES("
                +
                String.format(
                        "'%s', '%f', '%f', '%f', '%f', '%s', '%f', '%d', '%s', '%s', '%s'",
                        name, 0.0, 0.0, 0.0, 0.0, GroupsManager.Group.MANTLE.name(), 0.0, 0, pickaxe, null, null)
                +
                ");");
    }

    public static boolean playerExists(final String name) {
        final ResultSet resultSet = db.getResult("SELECT * FROM OpPrison WHERE name='" + name + "'");
        try {
            if (resultSet.next()) {
                return true;
            }
        }
        catch (SQLException throwables) {
            throwables.printStackTrace();
        }
        return false;
    }

    public static Object get(String name, String table) {
        Object obj = null;

        final ResultSet resultSet = db.getResult("SELECT * FROM OpPrison WHERE name ='" + name + "'");
        try {
            if (resultSet.next()) {
                obj = resultSet.getObject(table);
            }
        }
        catch (SQLException throwables) {
            throwables.printStackTrace();
        }
        return obj;
    }

    public static void save(String name, double blocks, double money, double token, double multiplier, GroupsManager.Group group, double prestige, int fly, String pickaxe, String kits, String access) {
        db.update(String.format("UPDATE OpPrison SET name='%s', blocks=%f, money=%f, token=%f, multiplier=%f, rank='%s', prestige=%f, fly=%d, pickaxe='%s', kit='%s', access='%s' WHERE name='%s'",
                name, blocks, money, token, multiplier, group.name(), prestige, fly, pickaxe, kits, access, name));
    }

    public static void set(String name, String table, String input) {
        db.update("UPDATE OpPrison SET " + table + "=" + input + " WHERE name='" + name + "'");
    }
}
