package com.example.pazienti.model;

import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;

public class PatologieRow {
    private final StringProperty nomePatologia;
    private final StringProperty dataDiagnosi;
    private final StringProperty inCorso;
    private final StringProperty note;
    private final StringProperty azioni;

    public PatologieRow(String nomePatologia, String dataDiagnosi, String inCorso, String note, String azioni) {
        this.nomePatologia = new SimpleStringProperty(nomePatologia);
        this.dataDiagnosi = new SimpleStringProperty(dataDiagnosi);
        this.inCorso = new SimpleStringProperty(inCorso);
        this.note = new SimpleStringProperty(note);
        this.azioni = new SimpleStringProperty(azioni);
    }

    public String getNomePatologia() { return nomePatologia.get(); }
    public void setNomePatologia(String value) { nomePatologia.set(value); }
    public StringProperty nomePatologiaProperty() { return nomePatologia; }

    public String getDataDiagnosi() { return dataDiagnosi.get(); }
    public void setDataDiagnosi(String value) { dataDiagnosi.set(value); }
    public StringProperty dataDiagnosiProperty() { return dataDiagnosi; }

    public String getInCorso() { return inCorso.get(); }
    public void setInCorso(String value) { inCorso.set(value); }
    public StringProperty inCorsoProperty() { return inCorso; }

    public String getNote() { return note.get(); }
    public void setNote(String value) { note.set(value); }
    public StringProperty noteProperty() { return note; }

    public String getAzioni() { return azioni.get(); }
    public void setAzioni(String value) { azioni.set(value); }
    public StringProperty azioniProperty() { return azioni; }
}
