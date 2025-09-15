package com.example.pazienti.model;

import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;

public class PazientiRow {
    private final StringProperty paziente;
    private final StringProperty outOfRange;
    private final StringProperty aderenza;
    private final StringProperty ultimaGlicemia;
    private final StringProperty alert;

    public PazientiRow(String paziente, String outOfRange, String aderenza, String ultimaGlicemia, String alert) {
        this.paziente = new SimpleStringProperty(paziente);
        this.outOfRange = new SimpleStringProperty(outOfRange);
        this.aderenza = new SimpleStringProperty(aderenza);
        this.ultimaGlicemia = new SimpleStringProperty(ultimaGlicemia);
        this.alert = new SimpleStringProperty(alert);
    }

    public String getPaziente() { return paziente.get(); }
    public void setPaziente(String value) { paziente.set(value); }
    public StringProperty pazienteProperty() { return paziente; }

    public String getOutOfRange() { return outOfRange.get(); }
    public void setOutOfRange(String value) { outOfRange.set(value); }
    public StringProperty outOfRangeProperty() { return outOfRange; }

    public String getAderenza() { return aderenza.get(); }
    public void setAderenza(String value) { aderenza.set(value); }
    public StringProperty aderenzaProperty() { return aderenza; }

    public String getUltimaGlicemia() { return ultimaGlicemia.get(); }
    public void setUltimaGlicemia(String value) { ultimaGlicemia.set(value); }
    public StringProperty ultimaGlicemiaProperty() { return ultimaGlicemia; }

    public String getAlert() { return alert.get(); }
    public void setAlert(String value) { alert.set(value); }
    public StringProperty alertProperty() { return alert; }
}
