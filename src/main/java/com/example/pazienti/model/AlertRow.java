// Classe modello per rappresentare una riga della tabella degli alert pazienti.
// Contiene le proprietà: paziente, tipoAlert, dataOra, dettagli.
package com.example.pazienti.model;

import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;

public class AlertRow {
    private final StringProperty paziente;
    private final StringProperty tipoAlert;
    private final StringProperty dataOra;
    private final StringProperty dettagli;

    public AlertRow(String paziente, String tipoAlert, String dataOra, String dettagli) {
        this.paziente = new SimpleStringProperty(paziente);
        this.tipoAlert = new SimpleStringProperty(tipoAlert);
        this.dataOra = new SimpleStringProperty(dataOra);
        this.dettagli = new SimpleStringProperty(dettagli);
    }

    public String getPaziente() { return paziente.get(); }
    public void setPaziente(String value) { paziente.set(value); }
    public StringProperty pazienteProperty() { return paziente; }

    public String getTipoAlert() { return tipoAlert.get(); }
    public void setTipoAlert(String value) { tipoAlert.set(value); }
    public StringProperty tipoAlertProperty() { return tipoAlert; }

    public String getDataOra() { return dataOra.get(); }
    public void setDataOra(String value) { dataOra.set(value); }
    public StringProperty dataOraProperty() { return dataOra; }

    public String getDettagli() { return dettagli.get(); }
    public void setDettagli(String value) { dettagli.set(value); }
    public StringProperty dettagliProperty() { return dettagli; }
}
