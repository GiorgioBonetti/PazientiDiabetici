
package com.example.pazienti.DB;


import com.example.pazienti.classi.Paziente;
import com.example.pazienti.classi.Terapia;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import com.example.pazienti.DB.DbConnection.*;

public class DbTerapia {

    /**
     * Recupera tutte le terapie dal database.
     * @return Una ObservableList di Terapie (perfetta per JavaFX).
     */
    public ObservableList<Terapia> getAllTerapie() {
        ObservableList<Terapia> listaTerapia = FXCollections.observableArrayList();
        String sql = "SELECT * FROM Terapia";

        // Usiamo try-with-resources per chiudere automaticamente connessione e statement
        try (Connection conn = DbConnection.getInstance().getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql);
             ResultSet rs = pstmt.executeQuery()) {

            while (rs.next()) {
                Terapia t = new Terapia(
                        rs.getInt("id"),
                        rs.getString("idFarmaco"),
                        rs.getInt("AssunzioniGiornaliere"),
                        rs.getInt("Quantita"),
                        rs.getDate("DataInizio"),
                        rs.getDate("DataFine"),
                        rs.getInt("idDiabetologo"),
                        rs.getInt("idPaziente")

                );
                // Crea un oggetto Terapia per ogni riga del risultato
                listaTerapia.add(t);
            }
        } catch (SQLException e) {
            System.err.println("Errore durante il caricamento delle Terapie: " + e.getMessage());
            // Qui dovresti gestire l'eccezione in modo appropriato
        }
        return listaTerapia;
    }

}
