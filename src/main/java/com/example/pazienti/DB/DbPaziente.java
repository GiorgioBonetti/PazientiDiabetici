package com.example.pazienti.DB;


import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import com.example.pazienti.model.PazientiRow;
import com.example.pazienti.DB.DbConnection.*;

public class DbPaziente {
    // --- Metodi DAO Pubblici (L'interfaccia per la tua App) ---

    /**
     * Recupera tutti gli utenti dal database.
     * @return Una ObservableList di Utenti (perfetta per JavaFX).
     */
    public ObservableList<PazientiRow> getAllUtenti() {
        ObservableList<PazientiRow> listaUtenti = FXCollections.observableArrayList();
        String sql = "SELECT id, nome, Cognome FROM Paziente";

        // Usiamo try-with-resources per chiudere automaticamente connessione e statement
        try (Connection conn = DbConnection.getInstance().getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql);
             ResultSet rs = pstmt.executeQuery()) {

            while (rs.next()) {
                // Crea un oggetto Utente per ogni riga del risultato
                listaUtenti.add(new PazientiRow(
                        rs.getString("nome") +" " + rs.getString("Cognome"),
                        "", // outOfRange placeholder
                        "", // aderenza placeholder
                        "",  // ultimaGlicemia placeholder
                        "" // alert placeholder
                ));
            }
        } catch (SQLException e) {
            System.err.println("Errore durante il caricamento degli utenti: " + e.getMessage());
            // Qui dovresti gestire l'eccezione in modo appropriato
        }
        return listaUtenti;
    }

/*    *//**
     * Inserisce un nuovo utente nel database.
     * @param nome Il nome del nuovo utente
     * @param email L'email del nuovo utente
     * @return true se l'inserimento ha successo, false altrimenti.
     *//*
    public boolean salvaNuovoUtente(String nome, String email) {
        String sql = "INSERT INTO PazientiRow(nome, email) VALUES(?, ?)";

        try (Connection conn = DbConnection.getInstance().getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, nome);
            pstmt.setString(2, email);

            // executeUpdate() restituisce il numero di righe modificate
            int righeInserite = pstmt.executeUpdate();
            return righeInserite > 0;

        } catch (SQLException e) {
            System.err.println("Errore durante il salvataggio dell'utente: " + e.getMessage());
            return false;
        }
    }*/
}
