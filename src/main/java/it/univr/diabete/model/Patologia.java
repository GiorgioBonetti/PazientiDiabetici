package it.univr.diabete.model;

import java.time.LocalDate;

public class Patologia {

    private int id;
    private String nome;
    private LocalDate dataInizio;
    private String fkPaziente;

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

    public LocalDate getDataInizio() {
        return dataInizio;
    }

    public void setDataInizio(LocalDate dataInizio) {
        this.dataInizio = dataInizio;
    }

    public String getFkPaziente() {
        return fkPaziente;
    }

    public void setFkPaziente(String fkPaziente) {
        this.fkPaziente = fkPaziente;
    }
}
