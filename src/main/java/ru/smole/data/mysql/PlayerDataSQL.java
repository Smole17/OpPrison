package ru.smole.data.mysql;

import ru.smole.OpPrison;

import java.sql.ResultSet;
import java.sql.SQLException;

public class PlayerDataSQL {

    private static DatabaseManager db = OpPrison.getInstance().getBase();

    public static void create(String name) {
        db.update("INSERT INTO " +
                "OpPrison(name, blocks, money, token, multiplier, level, prestige) " +
                "VALUES('" + name + "', '" + 0.0 + "', '" + 0.0 + "', '" + 0.0 + "', '" + 0.0 + "', '" + "A" + "', '" + 0.0 + "')");
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

    public static void save(String name, double blocks, double money, double token, double multiplier, String level, double prestige) {
        db.update(String.format("UPDATE OpPrison SET name=%s, blocks=%f, money=%f, token=%f, multiplier=%f, level=%s, prestige=%s",
                name, blocks, money, token, multiplier, level, prestige));
    }

    public static void set(String name, String table, String input) {
        db.update("UPDATE OpPrison SET " + table + "=" + input + " WHERE name='" + name + "'");
    }
}
