package ru.smole.data.mysql;

import org.bukkit.inventory.ItemStack;
import ru.smole.OpPrison;
import ru.xfenilafs.core.database.query.row.ValueQueryRow;
import ru.xfenilafs.core.util.Base64Util;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Base64;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Consumer;

public class GangDataSQL {

    public static void create(String name, String owner) {
        OpPrison.getInstance().getGangs()
                .newDatabaseQuery()
                .insertQuery()

                .queryRow(new ValueQueryRow("name", name))
                .queryRow(new ValueQueryRow("members", Base64.getEncoder().encodeToString(owner.getBytes())))
                .queryRow(new ValueQueryRow("score", 0.0))
                .queryRow(new ValueQueryRow("vault", null))

                .executeSync(OpPrison.getInstance().getBase());
    }

    public static void get(Consumer<ResultSet> resultSetConsumer) {
        OpPrison.getInstance().getGangs()
                .newDatabaseQuery()
                .selectQuery()
                .setSelectedRows("*")
                .executeQueryAsync(OpPrison.getInstance().getBase())
                .thenAcceptAsync(resultSetConsumer);
    }

    public static void save(String name, String members, double score, String vault) {
        OpPrison.getInstance().getBase().getExecuteHandler().executeUpdate(true,//language=SQL
                "UPDATE gangs SET `members` = ?, `score` = ?, `vault` = ?  WHERE `name` = ?",
                members, score, vault, name
        );
    }

    public static void set(String name, String table, String input) {
        OpPrison.getInstance().getBase().getExecuteHandler().executeUpdate(true,//language=SQL
                "UPDATE gangs SET `" + table + "` = ?  WHERE `name` = ?",
                input, name
        );
    }

    public static void remove(String name) {
        OpPrison.getInstance().getGangs().newDatabaseQuery().deleteQuery()
                .queryRow(new ValueQueryRow("name", name))
                .executeAsync(OpPrison.getInstance().getBase());
    }
}