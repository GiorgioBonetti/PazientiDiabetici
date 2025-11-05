package com.example.pazienti.DB;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

/**
 * Singleton per la gestione centralizzata del database.
 */
public class DbConnection {

    private static final String DB_URL = "jdbc:mysql://pazientidiabetici.credaci.shop:3306/u343382213_PazDiabetici";
    private static final String USER = "u343382213_PazDiabetici";
    private static final String PASS = "Diabete123$";

    private static DbConnection instance;

    private Connection connection;

    public static synchronized DbConnection getInstance() {
        if (instance == null) {
            instance = new DbConnection();
        }
        return instance;
    }

    private DbConnection() {
        try {
            this.connection = createConnection();
        } catch (SQLException e) {
            throw new RuntimeException("Errore di connessione db: " + e.getMessage(), e);
        }
    }

    // getter pubblico sicuro: ricrea la connessione se necessario
    public synchronized Connection getConnection() throws SQLException {
        if (connection == null || connection.isClosed()) {
            connection = createConnection();
        }
        return connection;
    }

    private Connection createConnection() throws SQLException {
        return DriverManager.getConnection(DB_URL, USER, PASS);

    }
}
