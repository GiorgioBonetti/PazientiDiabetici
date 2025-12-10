package it.univr.diabete.database;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class Database {

    private static final String URL = "jdbc:mysql://92.113.22.2:3306/u941369887_diabete";
    private static final String USER = "u941369887_diabete";  // se hai una password mettila qui
    private static final String PASSWORD = "Diabete123$"; // se root NON ha password, lascia vuoto

    private static Connection connection;

    public static Connection getConnection() throws SQLException {
        if (connection == null || connection.isClosed()) {
            connection = DriverManager.getConnection(URL, USER, PASSWORD);
        }
        return connection;
    }
}
