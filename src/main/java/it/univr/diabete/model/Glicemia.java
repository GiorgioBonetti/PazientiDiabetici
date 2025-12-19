package it.univr.diabete.model;

import java.time.LocalDateTime;

public class Glicemia {
    private int id;
    private String fkPaziente;
    private int valore;
    private LocalDateTime dateStamp;
    private String parteGiorno;

    public Glicemia() {}

    public int getId() {
        return id;
    }
    public void setId(int id) {
        this.id = id;
    }

    public String getFkPaziente() {
        return fkPaziente;
    }
    public void setFkPaziente(String fkPaziente) {
        this.fkPaziente = fkPaziente;
    }

    public int getValore() {
        return valore;
    }
    public void setValore(int valore) {
        this.valore = valore;
    }

    public LocalDateTime getDateStamp() {
        return dateStamp;
    }
    public void setDateStamp(LocalDateTime dateStamp) {
        this.dateStamp = dateStamp;
    }

    public String getParteGiorno() {
        return parteGiorno;
    }
    public void setParteGiorno(String parteGiorno) {
        this.parteGiorno = parteGiorno;
    }
}