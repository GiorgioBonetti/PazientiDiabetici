package com.example.pazienti.classi;

import java.util.Date;

public class Paziente {
    int id;
    String Nome;
    String Cognome;
    Date DataNascita;
    String eMail;
    enum Sesso {M,F }
    String codifeceFiscale;
    String Password;
    int idDiabetologo;

    public Paziente(int idDiabetologo, String password, String codifeceFiscale, String eMail, Date dataNascita, String cognome, String nome, int id) {
        this.idDiabetologo = idDiabetologo;
        this.Password = password;
        this.codifeceFiscale = codifeceFiscale;
        this.eMail = eMail;
        DataNascita = dataNascita;
        Cognome = cognome;
        Nome = nome;
        this.id = id;
    }

    public String getNomeCognome() {
        return Nome + " " + Cognome;
    }
}
