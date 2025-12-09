package it.univr.diabete.model;

public class TerapiaFarmaco {

    private int id;
    private int idTerapia;
    private int idFarmaco;
    private int assunzioniGiornaliere;
    private int quantitaAssunzione;

    // opzionale: riferimento diretto al Farmaco
    private Farmaco farmaco;

    public TerapiaFarmaco() { }

    public TerapiaFarmaco(int id, int idTerapia, int idFarmaco,
                          int assunzioniGiornaliere,
                          int quantitaAssunzione,
                          String note) {
        this.id = id;
        this.idTerapia = idTerapia;
        this.idFarmaco = idFarmaco;
        this.assunzioniGiornaliere = assunzioniGiornaliere;
        this.quantitaAssunzione = quantitaAssunzione;
    }

    // getter & setter
    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getIdTerapia() {
        return idTerapia;
    }

    public void setIdTerapia(int idTerapia) {
        this.idTerapia = idTerapia;
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

    public int getQuantitaAssunzione() {
        return quantitaAssunzione;
    }

    public void setQuantitaAssunzione(int quantitaAssunzione) {
        this.quantitaAssunzione = quantitaAssunzione;
    }

    public Farmaco getFarmaco() {
        return farmaco;
    }

    public void setFarmaco(Farmaco farmaco) {
        this.farmaco = farmaco;
    }
}