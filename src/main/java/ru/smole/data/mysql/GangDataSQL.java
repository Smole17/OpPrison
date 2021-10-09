package ru.smole.data.mysql;

import ru.smole.OpPrison;
import ru.xfenilafs.core.database.query.row.ValueQueryRow;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Consumer;

public class GangDataSQL {

    public static void create(String name, String owner) {
        OpPrison.getInstance().getGangs()
                .newDatabaseQuery()
                .insertQuery()

                .queryRow(new ValueQueryRow("name", name))
                .queryRow(new ValueQueryRow("members", owner))
                .queryRow(new ValueQueryRow("score", 0.0))

                .executeSync(OpPrison.getInstance().getBase());
    }

    public static void get(String name, Consumer<ResultSet> resultSetConsumer) {
        OpPrison.getInstance().getGangs()
                .newDatabaseQuery()
                .selectQuery()

                .queryRow(new ValueQueryRow("name", name))

                .executeQueryAsync(OpPrison.getInstance().getBase())
                .thenAccept(resultSetConsumer);
    }

    public static void get(Consumer<ResultSet> resultSetConsumer) {
        OpPrison.getInstance().getGangs()
                .newDatabaseQuery()
                .selectQuery()

                .executeQueryAsync(OpPrison.getInstance().getBase())
                .thenAccept(resultSetConsumer);
    }

    public static void save(String name, String members, double score) {
        OpPrison.getInstance().getBase().getExecuteHandler().executeUpdate(true,//language=SQL
                "UPDATE gangs SET `name` = ?, `members` = ?, `income` = ?, `score` = ?  WHERE `name` = ?",
                name, members, score, name
        );
    }

    public static void set(String name, String table, String input) {
        OpPrison.getInstance().getBase().getExecuteHandler().executeUpdate(true,//language=SQL
                "UPDATE gangs SET" + table + " = ?  WHERE `name` = ?",
                input, name
        );
    }
}