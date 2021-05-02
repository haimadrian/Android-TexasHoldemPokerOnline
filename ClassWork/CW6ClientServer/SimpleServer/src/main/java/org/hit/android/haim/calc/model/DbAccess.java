package org.hit.android.haim.calc.model;

import lombok.extern.log4j.Log4j2;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.SQLException;

/**
 * @author Haim Adrian
 * @since 02-May-21
 */
@Log4j2
public class DbAccess {
    private static final DbAccess instance = new DbAccess();

    private Connection connection;

    private DbAccess() {
        try {
            Class.forName("com.mysql.jdbc.Driver");
            log.info("MySQL jdbc class was loaded");
        } catch (ClassNotFoundException e) {
            log.error("Unable to find MySQL jdbc driver.", e);
        }
    }

    public static DbAccess getInstance() {
        return instance;
    }

    public void connect() {
        String host = "jdbc:mysql://localhost:3306/android";
        String username = "root";
        String password = System.getenv("MAP_STORIES_DB_PASSWORD");

        try {
            connection = DriverManager.getConnection(host, username, password);
            log.info("MySQL jdbc connection is open");
        } catch (SQLException e) {
            log.error("Unable to find connect to DB.", e);
        }
    }

    public void close() {
        if (connection != null) {
            try {
                connection.close();
            } catch (SQLException e) {
                log.error("Error has occurred while closing connection", e);
            }
        }
    }

    public PreparedStatement preparedStatement(String sql) throws SQLException {
        return connection.prepareStatement(sql);
    }
}

