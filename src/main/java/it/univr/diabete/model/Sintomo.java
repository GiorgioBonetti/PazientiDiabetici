package it.univr.diabete.model;

import java.time.LocalDate;
import java.time.LocalDateTime;

public class Sintomo {

    private int id;
    private String descrizione;
    private LocalDate dataInizio;
    private LocalDate dataFine;
    private int intensita;
    private String frequenza;
    private String noteAggiuntive;
    private String fkPaziente;
    private LocalDateTime dateStamp;

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getDescrizione() {
        return descrizione;
    }

    public void setDescrizione(String descrizione) {
        this.descrizione = descrizione;
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

    public int getIntensita() {
        return intensita;
    }

    public void setIntensita(int intensita) {
        this.intensita = intensita;
    }

    public String getFrequenza() {
        return frequenza;
    }

    public void setFrequenza(String frequenza) {
        this.frequenza = frequenza;
    }

    public String getNoteAggiuntive() {
        return noteAggiuntive;
    }

    public void setNoteAggiuntive(String noteAggiuntive) {
        this.noteAggiuntive = noteAggiuntive;
    }

    public String getFkPaziente() {
        return fkPaziente;
    }

    public void setFkPaziente(String fkPaziente) {
        this.fkPaziente = fkPaziente;
    }

    public LocalDateTime getDateStamp() {
        return dateStamp;
    }

    public void setDateStamp(LocalDateTime dateStamp) {
        this.dateStamp = dateStamp;
    }
}
