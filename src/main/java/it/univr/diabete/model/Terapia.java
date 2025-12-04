package it.univr.diabete.model;

import java.time.LocalDate;

public class Terapia {

    private int id;
    private int idFarmaco;
    private int assunzioniGiornaliere;
    private int quantita;
    private LocalDate dataInizio;
    private LocalDate dataFine;   // può essere null
    private int idDiabetologo;
    private int idPaziente;

    // campi "di comodo" per la UI (join con Farmaco)
    private String farmacoNome;
    private double farmacoDosaggio;

    public Terapia() {
    }

    public Terapia(int id,
                   int idFarmaco,
                   int assunzioniGiornaliere,
                   int quantita,
                   LocalDate dataInizio,
                   LocalDate dataFine,
                   int idDiabetologo,
                   int idPaziente) {
        this.id = id;
        this.idFarmaco = idFarmaco;
        this.assunzioniGiornaliere = assunzioniGiornaliere;
        this.quantita = quantita;
        this.dataInizio = dataInizio;
        this.dataFine = dataFine;
        this.idDiabetologo = idDiabetologo;
        this.idPaziente = idPaziente;
    }

    // ====== GETTER / SETTER ======

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getIdFarmaco() {
        return idFarmaco;
    }

    public void setIdFarmaco(int idFarmaco) {
        this.idFarmaco = idFarmaco;
    }

    public int getAssunzioniGiornaliere() {
        return assunzioniGiornaliere;
    }

    public void setAssunzioniGiornaliere(int assunzioniGiornaliere) {
        this.assunzioniGiornaliere = assunzioniGiornaliere;
    }

    public int getQuantita() {
        return quantita;
    }

    public void setQuantita(int quantita) {
        this.quantita = quantita;
    }

    public LocalDate getDataInizio() {
        return dataInizio;
    }

    public void setDataInizio(LocalDate dataInizio) {
        this.dataInizio = dataInizio;
    }

    public LocalDate getDataFine() {
        return dataFine;
    }

    public void setDataFine(LocalDate dataFine) {
        this.dataFine = dataFine;
    }

    public int getIdDiabetologo() {
        return idDiabetologo;
    }

    public void setIdDiabetologo(int idDiabetologo) {
        this.idDiabetologo = idDiabetologo;
    }

    public int getIdPaziente() {
        return idPaziente;
    }

    public void setIdPaziente(int idPaziente) {
        this.idPaziente = idPaziente;
    }

    public String getFarmacoNome() {
        return farmacoNome;
    }

    public void setFarmacoNome(String farmacoNome) {
        this.farmacoNome = farmacoNome;
    }

    public double getFarmacoDosaggio() {
        return farmacoDosaggio;
    }

    public void setFarmacoDosaggio(double farmacoDosaggio) {
        this.farmacoDosaggio = farmacoDosaggio;
    }
}