// Classe modello per rappresentare una riga della tabella delle terapie.
// Contiene le proprietà: paziente, tipoAlert, dataOra, dettagli.
package com.example.pazienti.model;

import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;

public class TerapieRow {
    private final StringProperty paziente;
    private final StringProperty farmaco;
    private final StringProperty dosaggio;
    private final StringProperty frequenza;

    public TerapieRow(String paziente, String farmaco, String dosaggio, String frequenza) {
        this.paziente = new SimpleStringProperty(paziente);
        this.farmaco = new SimpleStringProperty(farmaco);
        this.dosaggio = new SimpleStringProperty(dosaggio);
        this.frequenza = new SimpleStringProperty(frequenza);
    }

    public String getPaziente() { return paziente.get(); }
    public void setPaziente(String value) { paziente.set(value); }
    public StringProperty pazienteProperty() { return paziente; }

    public String getFarmaco() { return farmaco.get(); }
    public void setFarmaco(String value) { farmaco.set(value); }
    public StringProperty farmacoProperty() { return farmaco; }

    public String getDosaggio() { return dosaggio.get(); }
    public void setDosaggio(String value) { dosaggio.set(value); }
    public StringProperty dosaggioProperty() { return dosaggio; }

    public String getFrequenza() { return frequenza.get(); }
    public void setFrequenza(String value) { frequenza.set(value); }
    public StringProperty frequenzaProperty() { return frequenza; }
}
