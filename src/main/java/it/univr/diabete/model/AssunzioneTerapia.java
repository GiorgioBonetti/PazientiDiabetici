package it.univr.diabete.model;

import java.time.LocalDateTime;

public class AssunzioneTerapia {

    private int id;
    private LocalDateTime dateStamp;
    private int quantitaAssunta;
    private int idPaziente;
    private int idTerapiaFarmaco;

    public AssunzioneTerapia() { }

    public AssunzioneTerapia(int id, LocalDateTime dateStamp,
                             int quantitaAssunta, int idPaziente,
                             int idTerapiaFarmaco) {
        this.id = id;
        this.dateStamp = dateStamp;
        this.quantitaAssunta = quantitaAssunta;
        this.idPaziente = idPaziente;
        this.idTerapiaFarmaco = idTerapiaFarmaco;
    }

    // getter & setter
    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public LocalDateTime getDateStamp() { return dateStamp; }
    public void setDateStamp(LocalDateTime dateStamp) { this.dateStamp = dateStamp; }

    public int getQuantitaAssunta() { return quantitaAssunta; }
    public void setQuantitaAssunta(int quantitaAssunta) { this.quantitaAssunta = quantitaAssunta; }

    public int getIdPaziente() { return idPaziente; }
    public void setIdPaziente(int idPaziente) { this.idPaziente = idPaziente; }

    public int getIdTerapiaFarmaco() { return idTerapiaFarmaco; }
    public void setIdTerapiaFarmaco(int idTerapiaFarmaco) { this.idTerapiaFarmaco = idTerapiaFarmaco; }
}