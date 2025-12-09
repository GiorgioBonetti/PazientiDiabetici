package it.univr.diabete.model;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public class Terapia {

    private int id;
    private LocalDate dataInizio;
    private LocalDate dataFine;
    private int idDiabetologo;
    private int idPaziente;
    private String nome;   // nome della terapia

    public String getNome() { return nome; }
    public void setNome(String nome) { this.nome = nome; }

    // nuova parte: una terapia contiene più farmaci
    private List<TerapiaFarmaco> farmaci = new ArrayList<>();

    public Terapia() { }

    public Terapia(int id, LocalDate dataInizio, LocalDate dataFine,
                   int idDiabetologo, int idPaziente) {
        this.id = id;
        this.dataInizio = dataInizio;
        this.dataFine = dataFine;
        this.idDiabetologo = idDiabetologo;
        this.idPaziente = idPaziente;
    }

    // getter & setter base
    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public LocalDate getDataInizio() { return dataInizio; }
    public void setDataInizio(LocalDate dataInizio) { this.dataInizio = dataInizio; }

    public LocalDate getDataFine() { return dataFine; }
    public void setDataFine(LocalDate dataFine) { this.dataFine = dataFine; }

    public int getIdDiabetologo() { return idDiabetologo; }
    public void setIdDiabetologo(int idDiabetologo) { this.idDiabetologo = idDiabetologo; }

    public int getIdPaziente() { return idPaziente; }
    public void setIdPaziente(int idPaziente) { this.idPaziente = idPaziente; }

    public List<TerapiaFarmaco> getFarmaci() {
        return farmaci;
    }

    public void setFarmaci(List<TerapiaFarmaco> farmaci) {
        this.farmaci = farmaci;
    }
}