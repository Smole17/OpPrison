package ru.smole.data.mysql;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import ru.smole.OpPrison;
import ru.smole.data.group.GroupsManager;
import ru.smole.data.items.Items;
import ru.smole.data.items.pickaxe.PickaxeManager;
import ru.smole.data.player.OpPlayer;
import ru.smole.data.player.PlayerData;
import ru.smole.scoreboard.ScoreboardManager;
import ru.xfenilafs.core.database.RemoteDatabaseConnectionHandler;
import ru.xfenilafs.core.database.query.row.ValueQueryRow;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Consumer;

public class PlayerDataSQL {

    private static final RemoteDatabaseConnectionHandler base = OpPrison.getInstance().getBase();

    public static void tryLoad(String name, PickaxeManager pickaxe) {
        OpPrison.getInstance().getPlayers()
                .newDatabaseQuery()
                .selectQuery()

                .queryRow(new ValueQueryRow("name", name))

                .executeQueryAsync(base)
                .thenAccept(result -> {
                    if (!result.next()) {
                        pickaxe.create();

                        OpPrison.getInstance().getPlayers().newDatabaseQuery()
                                .insertQuery()

                                .queryRow(new ValueQueryRow("name", name))
                                .queryRow(new ValueQueryRow("blocks", 0.0))
                                .queryRow(new ValueQueryRow("money", 0.0))
                                .queryRow(new ValueQueryRow("token", 0.0))
                                .queryRow(new ValueQueryRow("multiplier", 0.0))
                                .queryRow(new ValueQueryRow("rank", GroupsManager.Group.MANTLE.name()))
                                .queryRow(new ValueQueryRow("prestige", 0.0))
                                .queryRow(new ValueQueryRow("fly", 0))
                                .queryRow(new ValueQueryRow("pickaxe", pickaxe.getStats()))
                                .queryRow(new ValueQueryRow("kit", null))
                                .queryRow(new ValueQueryRow("access", null))
                                .queryRow(new ValueQueryRow("questions", null))

                                .executeSync(base);
                    }
                });
    }

    public static void get(String name, Consumer<ResultSet> resultSetConsumer) {
        OpPrison.getInstance().getPlayers()
                .newDatabaseQuery()
                .selectQuery()

                .queryRow(new ValueQueryRow("name", name))

                .executeQueryAsync(base)
                .thenAccept(resultSetConsumer);
    }

    public static void save(String name, double blocks, double money, double token, double multiplier, GroupsManager.Group group, double prestige, int fly, String pickaxe, String kits, String access, String questions) {
        base.getExecuteHandler().executeUpdate(true,
                "UPDATE players SET `name` = ?, `blocks` = ?, `money` = ?, `token` = ?, `multiplier` = ?, `rank` = ?, `prestige` = ?, `fly` = ?, `pickaxe` = ?, `kit` = ?, `access` = ?, `questions` = ?  WHERE `name` = ?",
                name, blocks, money, token, multiplier, group.name(), prestige, fly, pickaxe, kits, access, questions, name
        );
    }

    public static void set(String name, String table, double input) {
        base.getExecuteHandler().executeUpdate(true,//language=SQL
                "UPDATE players SET `" + table + "` = ?  WHERE `name` = ?",
                input, name
        );
    }
}