// Classe modello per rappresentare una riga della tabella degli alert pazienti.
// Contiene le proprietà: paziente, tipoAlert, dataOra, dettagli.
package com.example.pazienti.model;

import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;

public class MisurazioniRow {
    private final StringProperty dataOra;
    private final StringProperty contesto;
    private final StringProperty valore;
    private final StringProperty note;
    private final StringProperty azioni;

    public MisurazioniRow(String dataOra, String contesto, String valore, String note, String azioni) {
        this.dataOra = new SimpleStringProperty(dataOra);
        this.contesto = new SimpleStringProperty(contesto);
        this.valore = new SimpleStringProperty(valore);
        this.note = new SimpleStringProperty(note);
        this.azioni = new SimpleStringProperty(azioni);
    }

    public String getDataOra() { return dataOra.get(); }
    public void setDataOra(String value) { dataOra.set(value); }
    public StringProperty dataOraProperty() { return dataOra; }

    public String getContesto() { return contesto.get(); }
    public void setContesto(String value) { contesto.set(value); }
    public StringProperty contestoProperty() { return contesto; }

    public String getValore() { return valore.get(); }
    public void setValore(String value) { valore.set(value); }
    public StringProperty valoreProperty() { return valore; }

    public String getNote() { return note.get(); }
    public void setNote(String value) { note.set(value); }
    public StringProperty noteProperty() { return note; }

    public String getAzioni() { return azioni.get(); }
    public void setAzioni(String value) { azioni.set(value); }
    public StringProperty azioniProperty() { return azioni; }
}
