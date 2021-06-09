package ru.smole.data.mysql;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;

import lombok.Getter;
import ru.smole.OpPrison;

import javax.sql.rowset.CachedRowSet;
import javax.sql.rowset.RowSetFactory;
import javax.sql.rowset.RowSetProvider;
import java.sql.*;
import java.util.LinkedHashMap;
import java.util.Map;

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
    }

    public Map<String, Double> getTopFromCriteria(String criteria, int amount) {
        Map<String, Double> top = new LinkedHashMap<String, Double>();
        ResultSet resultSet = getResult("SELECT * FROM `" + dbname + "` ORDER BY " + criteria + " DESC LIMIT " + amount);
        try {
            while (resultSet.next()) {
                top.put(resultSet.getString("name"), resultSet.getDouble(criteria));

            }
        } catch (SQLException ex) {
            ex.printStackTrace();
        }
        return top;
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

    public Connection getConnection() throws SQLException {
        return dataSource.getConnection();
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
