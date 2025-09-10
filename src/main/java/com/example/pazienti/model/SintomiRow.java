package com.example.pazienti.model;

import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;

public class SintomiRow {
    private final StringProperty sintomo;
    private final StringProperty dataInizio;
    private final StringProperty dataFine;
    private final StringProperty patologiaAssociata;
    private final StringProperty note;

    public SintomiRow(String sintomo, String dataInizio, String dataFine, String patologiaAssociata, String note) {
        this.sintomo = new SimpleStringProperty(sintomo);
        this.dataInizio = new SimpleStringProperty(dataInizio);
        this.dataFine = new SimpleStringProperty(dataFine);
        this.patologiaAssociata = new SimpleStringProperty(patologiaAssociata);
        this.note = new SimpleStringProperty(note);
    }

    public String getSintomo() { return sintomo.get(); }
    public void setSintomo(String value) { sintomo.set(value); }
    public StringProperty sintomoProperty() { return sintomo; }

    public String getDataInizio() { return dataInizio.get(); }
    public void setDataInizio(String value) { dataInizio.set(value); }
    public StringProperty dataInizioProperty() { return dataInizio; }

    public String getDataFine() { return dataFine.get(); }
    public void setDataFine(String value) { dataFine.set(value); }
    public StringProperty dataFineProperty() { return dataFine; }

    public String getPatologiaAssociata() { return patologiaAssociata.get(); }
    public void setPatologiaAssociata(String value) { patologiaAssociata.set(value); }
    public StringProperty patologiaAssociataProperty() { return patologiaAssociata; }

    public String getNote() { return note.get(); }
    public void setNote(String value) { note.set(value); }
    public StringProperty noteProperty() { return note; }
}
