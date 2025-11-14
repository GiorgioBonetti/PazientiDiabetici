package com.example.pazienti.classi;

import java.util.Date;

public class Terapia {
    int id;
    String idFarmaco;
    int AssunzioniGiornaliere;
    int Quantita;
    Date DataInizio;
    Date DataFine;
    int idDiabetologo;
    int idPaziente;


    public Terapia(int id, String idFarmaco, int assunzioniGiornaliere, int quantita, Date dataInizio, Date dataFine, int idDiabetologo, int idPaziente) {
        this.id = id;
        this.idFarmaco = idFarmaco;
        AssunzioniGiornaliere = assunzioniGiornaliere;
        Quantita = quantita;
        DataInizio = dataInizio;
        DataFine = dataFine;
        this.idDiabetologo = idDiabetologo;
        this.idPaziente = idPaziente;
    }
}

