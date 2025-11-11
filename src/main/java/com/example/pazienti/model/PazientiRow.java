package com.example.pazienti.model;

import com.example.pazienti.classi.Paziente;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;

public class PazientiRow {
    private final Paziente paziente;
    private final StringProperty outOfRange;
    private final StringProperty aderenza;
    private final StringProperty ultimaGlicemia;
    private final StringProperty alert;

    public PazientiRow(Paziente paziente, String outOfRange, String aderenza, String ultimaGlicemia, String alert) {
        this.paziente = paziente;
        this.outOfRange = new SimpleStringProperty(outOfRange);
        this.aderenza = new SimpleStringProperty(aderenza);
        this.ultimaGlicemia = new SimpleStringProperty(ultimaGlicemia);
        this.alert = new SimpleStringProperty(alert);
    }

    public Paziente getPaziente() { return paziente; }
//    public void setPaziente(String value) { paziente.set(value); }
    // espone il nome completo del paziente come StringProperty per l'uso nelle TableView
    public StringProperty pazienteProperty() {
        return new SimpleStringProperty(paziente != null ? paziente.getNomeCognome() : "");
    };

//    // opzionali: getter/setter sul nome visualizzato
//    public String getPazienteNome() { return pazienteNome.get(); }
//    public void setPazienteNome(String value) { pazienteNome.set(value); }

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
