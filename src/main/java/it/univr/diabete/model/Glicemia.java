package it.univr.diabete.model;

import java.time.LocalDateTime;

public class Glicemia {
    private int id;
    private String idPaziente;
    private int valore;
    private LocalDateTime dataOra;
    private String momento;   // ⬅ AGGIUNTO

    public Glicemia() {}

    public int getId() {
        return id;
    }
    public void setId(int id) {
        this.id = id;
    }

    public String getIdPaziente() {
        return idPaziente;
    }
    public void setIdPaziente(String idPaziente) {
        this.idPaziente = idPaziente;
    }

    public int getValore() {
        return valore;
    }
    public void setValore(int valore) {
        this.valore = valore;
    }

    public LocalDateTime getDataOra() {
        return dataOra;
    }
    public void setDataOra(LocalDateTime dataOra) {
        this.dataOra = dataOra;
    }

    // ⬅⬅⬅ MOMENTO (NUOVO)
    public String getMomento() {
        return momento;
    }
    public void setMomento(String momento) {
        this.momento = momento;
    }
}