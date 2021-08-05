package ru.smole.data.mysql;

import ru.smole.OpPrison;

import java.sql.Array;
import java.sql.ResultSet;
import java.sql.SQLException;

public class GangDataSQL {

    private static DatabaseManager db = OpPrison.getInstance().getBase();

    public static void create(String name, String owner) {
        db.update("INSERT INTO " +
                "OpPrisonGangs(name, members, score) " +
                "VALUES("
                +
                String.format(
                        "'%s', '%s', '%s'",
                        name, owner, 0.0)
                +
                ");");
    }

    public static boolean gangExists(final String name) {
        final ResultSet resultSet = db.getResult("SELECT * FROM OpPrisonGangs WHERE name='" + name + "'");
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

        final ResultSet resultSet = db.getResult("SELECT * FROM OpPrisonGangs WHERE name ='" + name + "'");
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

    public static Object get(String table) {
        Object obj = null;

        final ResultSet resultSet = db.getResult("SELECT * FROM OpPrisonGangs");
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

    public static void save(String name, String members, double score) {
        db.update(String.format("UPDATE OpPrisonGangs SET name='%s', members='%s', score='%s' WHERE name='%s'",
                name, members, score, name));
    }

    public static void set(String name, String table, String input) {
        db.update("UPDATE OpPrisonGangs SET " + table + "=" + input + " WHERE name='" + name + "'");
    }
}
