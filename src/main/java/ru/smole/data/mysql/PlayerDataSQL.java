package ru.smole.data.mysql;

import org.bukkit.entity.Player;
import ru.smole.OpPrison;
import ru.smole.data.group.GroupsManager;
import ru.xfenilafs.core.database.query.row.ValueQueryRow;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Consumer;

public class PlayerDataSQL {

    public static void load(String name, String pickaxe, Consumer<String> consumer) {
        OpPrison.getInstance().getBase().getTable("OpPrison")
                .newDatabaseQuery()
                .selectQuery()

                .queryRow(new ValueQueryRow("name", name))

                .executeQueryAsync(OpPrison.getInstance().getBase())
                .thenAccept(result -> {
                    if (!result.next()) {
                        OpPrison.getInstance().getBase().getTable("OpPrison").newDatabaseQuery()
                                .insertQuery()

                                .queryRow(new ValueQueryRow("name", name))
                                .queryRow(new ValueQueryRow("blocks", 0.0))
                                .queryRow(new ValueQueryRow("money", 0.0))
                                .queryRow(new ValueQueryRow("token", 0.0))
                                .queryRow(new ValueQueryRow("multiplier", 0.0))
                                .queryRow(new ValueQueryRow("rank", GroupsManager.Group.MANTLE.name()))
                                .queryRow(new ValueQueryRow("prestige", 0.0))
                                .queryRow(new ValueQueryRow("fly", 0))
                                .queryRow(new ValueQueryRow("pickaxe", pickaxe))
                                .queryRow(new ValueQueryRow("kit", null))
                                .queryRow(new ValueQueryRow("access", null))

                                .executeSync(OpPrison.getInstance().getBase());

                        consumer.accept(name);
                    }
                });
    }

    public static Object get(String name, String table) {
        AtomicReference<Object> obj = null;

        OpPrison.getInstance().getBase().getTable("OpPrison")
                .newDatabaseQuery()
                .selectQuery()

                .queryRow(new ValueQueryRow("name", name))

                .executeQueryAsync(OpPrison.getInstance().getBase())
                .thenAccept(result -> obj.set(result.getObject(table)));

        return obj;
    }

    public static void save(String name, double blocks, double money, double token, double multiplier, GroupsManager.Group group, double prestige, int fly, String pickaxe, String kits, String access) {
        OpPrison.getInstance().getBase().getExecuteHandler().executeUpdate(true,//language=SQL
                "UPDATE OpPrisonGangs SET `name` = ?, `blocks` = ?, `money` = ?, `token` = ?, `multiplier` = ?, `rank` = ?, `prestige` = ?, `fly` = ?, `pickaxe` = ?, `kit` = ?, `access` = ?  WHERE `name` = ?",
                name, blocks, money, token, multiplier, group.name(), prestige, fly, pickaxe, kits, access, name
        );
    }

    public static void set(String name, String table, String input) {
        OpPrison.getInstance().getBase().getExecuteHandler().executeUpdate(true,//language=SQL
                "UPDATE OpPrison SET" + table + " = ?  WHERE `name` = ?",
                input, name
        );
    }
}
