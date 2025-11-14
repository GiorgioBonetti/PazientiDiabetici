package com.example.pazienti;

import com.example.pazienti.DB.DbPaziente;
import com.example.pazienti.DB.DbTerapia;
import com.example.pazienti.classi.Paziente;
import com.example.pazienti.classi.Terapia;
import javafx.collections.ObservableList;

public class MainTest {
    public static void main(String[] args) {
        DbPaziente dbPaziente = new DbPaziente();
        DbTerapia dbTerapia = new DbTerapia();

        System.out.println("Connessione al database stabilita con successo.");
        ObservableList<Paziente> p = dbPaziente.getAllUtenti();
        ObservableList<Terapia> t = dbTerapia.getAllTerapie();

        p.forEach(paziente -> {
            System.out.println("Paziente: " + paziente.getNomeCognome());
        });

        t.forEach(terapia -> {
            System.out.println("Terapia: " + terapia.toString());
        });

    }
}
