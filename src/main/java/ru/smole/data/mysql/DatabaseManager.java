package ru.smole.data.mysql;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import lombok.Getter;

import javax.sql.rowset.CachedRowSet;
import javax.sql.rowset.RowSetFactory;
import javax.sql.rowset.RowSetProvider;
import java.sql.*;

@Getter
public class DatabaseManager {
    private final String host;
    private final String dbname;
    private final String user;
    private final String password;
    private final boolean useSsl;
    private final HikariDataSource dataSource;
    private final RowSetFactory rowSetFactory;

    public DatabaseManager(String host, String dbname, String user, String password, boolean useSsl) {
        this.host = host;
        this.dbname = dbname;
        this.user = user;
        this.password = password;
        this.useSsl = useSsl;

        HikariConfig hikariConfig = new HikariConfig();
        hikariConfig.setJdbcUrl("jdbc:mysql://" + host + ":3306/" + dbname + "?useUnicode=true&characterEncoding=utf-8&useSSL=" + useSsl);
        hikariConfig.setUsername(user);
        hikariConfig.setPassword(password);
        dataSource = new HikariDataSource(hikariConfig);
        try {
            dataSource.getConnection();
        } catch (SQLException throwables) {
            throw new RuntimeException("Unable to connect DB.", throwables);
        }

        try {
            rowSetFactory = RowSetProvider.newFactory();
        } catch (SQLException throwables) {
            throw new RuntimeException("Unable to create RowSetFactory", throwables);
        }

        loadTables();
    }

    public void loadTables() {
        update("CREATE TABLE IF NOT EXISTS " +
                "OpPrison(" +
                "name VARCHAR(16), " +
                "blocks DOUBLE, " +
                "money DOUBLE, " +
                "token DOUBLE, " +
                "multiplier DOUBLE, " +
                "prestige DOUBLE, " +
                "rank TEXT, " +
                "fly INT, " +
                "pickaxe TEXT, " +
                "kit TEXT," +
                "access TEXT)");

        update("CREATE TABLE IF NOT EXISTS " +
                "OpPrisonGangs(" +
                "name VARCHAR(5), " +
                "members TEXT, " +
                "score DOUBLE");
    }

    public void close() {
        closeQuietly(dataSource);
    }

    public void update(String query) {
        try {
            Connection connection = dataSource.getConnection();
            Statement statement = connection.createStatement();
            statement.execute(query);
            closeQuietly(statement, connection);
        } catch (SQLException throwables) {
            throwables.printStackTrace();
        }
    }

    public ResultSet getResult(String query) {
        try {
            ResultSet rs = dataSource.getConnection().prepareStatement(query).executeQuery();
            CachedRowSet rowSet = rowSetFactory.createCachedRowSet();
            rowSet.populate(rs);
            Statement statement = rs.getStatement();
            Connection connection = statement.getConnection();
            closeQuietly(rs, statement, connection);
            return rowSet;
        } catch (SQLException throwables) {
            throwables.printStackTrace();
        }
        return null;
    }

    private void closeQuietly(AutoCloseable ... closeables) {
        for (AutoCloseable closeable : closeables) {
            try {
                closeable.close();
            } catch (Exception ignored) {
            }
        }
    }
}
