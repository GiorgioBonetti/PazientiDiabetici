package it.univr.diabete.model;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public class Terapia {

    private int id;
    private int versione;
    private LocalDate dataInizio;
    private LocalDate dataFine;
    private String fkDiabetologo;
    private String fkPaziente;


    // nuova parte: una terapia contiene più farmaci
    private List<FarmacoTerapia> farmaci = new ArrayList<>();

    public Terapia() { }

    public Terapia(int id, LocalDate dataInizio, LocalDate dataFine,
                   String fkDiabetologo, String fkPaziente) {
        this.id = id;
        this.dataInizio = dataInizio;
        this.dataFine = dataFine;
        this.fkDiabetologo = fkDiabetologo;
        this.fkPaziente = fkPaziente;
    }

    public Terapia(LocalDate dataInizio, LocalDate dataFine,
                   String fkDiabetologo, String fkPaziente) {
        this.id = id;
        this.dataInizio = dataInizio;
        this.dataFine = dataFine;
        this.fkDiabetologo = fkDiabetologo;
        this.fkPaziente = fkPaziente;
    }

    // getter & setter base
    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public int getVersione() { return versione; }
    public void setVersione(int versione) { this.versione = versione; }

    public LocalDate getDataInizio() { return dataInizio; }
    public void setDataInizio(LocalDate dataInizio) { this.dataInizio = dataInizio; }

    public LocalDate getDataFine() { return dataFine; }
    public void setDataFine(LocalDate dataFine) { this.dataFine = dataFine; }

    public String getFkDiabetologo() { return fkDiabetologo; }
    public void setFkDiabetologo(String fkDiabetologo) { this.fkDiabetologo = fkDiabetologo; }

    public String getFkPaziente() { return fkPaziente; }
    public void setFkPaziente(String fkPaziente) { this.fkPaziente = fkPaziente; }

    public List<FarmacoTerapia> getFarmaci() {
        return farmaci;
    }
    public void setFarmaci(List<FarmacoTerapia> farmaci) {
        this.farmaci = farmaci;
    }
}