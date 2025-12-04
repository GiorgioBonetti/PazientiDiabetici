package it.univr.diabete.database;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class Database {

    private static final String URL = "jdbc:mysql://localhost:3306/u343382213_PazDiabetici";
    private static final String USER = "root";  // se hai una password mettila qui
    private static final String PASSWORD = "LvVlroot$02"; // se root NON ha password, lascia vuoto

    private static Connection connection;

    public static Connection getConnection() throws SQLException {
        if (connection == null || connection.isClosed()) {
            connection = DriverManager.getConnection(URL, USER, PASSWORD);
        }
        return connection;
    }
}
