package com.example.pazienti;

import com.example.pazienti.DB.DbPaziente;
import com.example.pazienti.model.PazientiRow;
import javafx.collections.ObservableList;

public class MainTest {
    public static void main(String[] args) {
        DbPaziente dbPaziente = new DbPaziente();
        System.out.println("Connessione al database stabilita con successo.");
        ObservableList<PazientiRow> p = dbPaziente.getAllUtenti();

        p.forEach(paziente -> {
            System.out.println("Paziente: " + paziente.getPaziente());
        });

    }
}
