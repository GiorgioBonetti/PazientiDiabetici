package it.univr.diabete.model;

public class FarmacoTerapia {

    private int fkTerapia;
    private int fkFarmaco;
    private int assunzioniGiornaliere;
    private int quantita;

    // opzionale: riferimento diretto al Farmaco
    private Farmaco farmaco;

    public FarmacoTerapia() { }

    public FarmacoTerapia(int fkTerapia, int fkFarmaco,
                          int assunzioniGiornaliere,
                          int quantita, int fkVersioneTerapia) {
        this.fkTerapia = fkTerapia;
        this.fkFarmaco = fkFarmaco;
        this.assunzioniGiornaliere = assunzioniGiornaliere;
        this.quantita = quantita;
    }

    // getter & setter
    public int getFkTerapia() {
        return fkTerapia;
    }
    public void setFkTerapia(int fkTerapia) {
        this.fkTerapia = fkTerapia;
    }

    public int getFkFarmaco() {
        return fkFarmaco;
    }
    public void setFkFarmaco(int fkFarmaco) {
        this.fkFarmaco = fkFarmaco;
    }

    public int getAssunzioniGiornaliere() {
        return assunzioniGiornaliere;
    }
    public void setAssunzioniGiornaliere(int assunzioniGiornaliere) { this.assunzioniGiornaliere = assunzioniGiornaliere; }

    public int getQuantita() {
        return quantita;
    }
    public void setQuantita(int quantita) {
        this.quantita = quantita;
    }

    public Farmaco getFarmaco() { return farmaco; }
    public void setFarmaco(Farmaco farmaco) { this.farmaco = farmaco; }
}