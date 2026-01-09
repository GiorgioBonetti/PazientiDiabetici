package it.univr.diabete.model;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class Terapia {

    private int id;
    private String nome;

    private LocalDate dataInizio;
    private LocalDate dataFine;
    private LocalDateTime ultimaModifica;

    private String fkDiabetologo; // VARCHAR nel DB
    private String fkPaziente;    // VARCHAR(16) nel DB

    // Una terapia contiene più farmaci (join table)
    private List<FarmacoTerapia> farmaci = new ArrayList<>();

    public Terapia() {}

    public Terapia(int id, String nome, LocalDate dataInizio, LocalDate dataFine,
                   LocalDateTime ultimaModifica, String fkDiabetologo, String fkPaziente) {
        this.id = id;
        this.nome = nome;
        this.dataInizio = dataInizio;
        this.dataFine = dataFine;
        this.ultimaModifica = ultimaModifica;
        this.fkDiabetologo = fkDiabetologo;
        this.fkPaziente = fkPaziente;
    }

    // Getter & Setter
    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public String getNome() { return nome; }
    public void setNome(String nome) { this.nome = nome; }

    public LocalDate getDataInizio() { return dataInizio; }
    public void setDataInizio(LocalDate dataInizio) { this.dataInizio = dataInizio; }

    public LocalDate getDataFine() { return dataFine; }
    public void setDataFine(LocalDate dataFine) { this.dataFine = dataFine; }

    public LocalDateTime getUltimaModifica() { return ultimaModifica; }
    public void setUltimaModifica(LocalDateTime ultimaModifica) { this.ultimaModifica = ultimaModifica; }

    public String getFkDiabetologo() { return fkDiabetologo; }
    public void setFkDiabetologo(String fkDiabetologo) { this.fkDiabetologo = fkDiabetologo; }

    public String getFkPaziente() { return fkPaziente; }
    public void setFkPaziente(String fkPaziente) { this.fkPaziente = fkPaziente; }

    public List<FarmacoTerapia> getFarmaci() { return farmaci; }
    public void setFarmaci(List<FarmacoTerapia> farmaci) { this.farmaci = farmaci; }
}