package it.univr.diabete.model;

import java.time.LocalDate;

public class Paziente {

    private int id;
    private String nome;
    private String cognome;
    private LocalDate dataNascita;
    private String numeroTelefono;
    private String email;
    private String sesso;
    private String codiceFiscale;
    private String password;
    private int idDiabetologo;

    public Paziente() {}

    public Paziente(int id, String nome, String cognome, LocalDate dataNascita,
                    String numeroTelefono, String email, String sesso,
                    String codiceFiscale, String password, int idDiabetologo) {

        this.id = id;
        this.nome = nome;
        this.cognome = cognome;
        this.dataNascita = dataNascita;
        this.numeroTelefono = numeroTelefono;
        this.email = email;
        this.sesso = sesso;
        this.codiceFiscale = codiceFiscale;
        this.password = password;
        this.idDiabetologo = idDiabetologo;
    }

    // GETTER E SETTER

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getNome() {
        return nome;
    }

    public void setNome(String nome) {
        this.nome = nome;
    }

    public String getCognome() {
        return cognome;
    }

    public void setCognome(String cognome) {
        this.cognome = cognome;
    }

    public LocalDate getDataNascita() {
        return dataNascita;
    }

    public void setDataNascita(LocalDate dataNascita) {
        this.dataNascita = dataNascita;
    }

    public String getNumeroTelefono() {
        return numeroTelefono;
    }

    public void setNumeroTelefono(String numeroTelefono) {
        this.numeroTelefono = numeroTelefono;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getSesso() {
        return sesso;
    }

    public void setSesso(String sesso) {
        this.sesso = sesso;
    }

    public String getCodiceFiscale() {
        return codiceFiscale;
    }

    public void setCodiceFiscale(String codiceFiscale) {
        this.codiceFiscale = codiceFiscale;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public int getIdDiabetologo() {
        return idDiabetologo;
    }

    public void setIdDiabetologo(int idDiabetologo) {
        this.idDiabetologo = idDiabetologo;
    }

    public String getFullName() {
        return nome + " " + cognome;
    }
}