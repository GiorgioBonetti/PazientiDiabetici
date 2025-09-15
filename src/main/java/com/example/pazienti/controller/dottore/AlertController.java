package com.example.pazienti.controller.dottore;

import com.example.pazienti.model.AlertRow;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;

public class AlertController {
    @FXML private Label titleLabel;

    @FXML private TableView<AlertRow> tblAlert;
    @FXML private TableColumn<AlertRow, String> colPaziente;
    @FXML private TableColumn<AlertRow, String> colTipoAlert;
    @FXML private TableColumn<AlertRow, String> colDataOra;
    @FXML private TableColumn<AlertRow, String> colDettagli;

    @FXML private void initialize() {
        // Collega le colonne ai campi della classe AlertRow
        colPaziente.setCellValueFactory(cellData -> cellData.getValue().pazienteProperty());
        colTipoAlert.setCellValueFactory(cellData -> cellData.getValue().tipoAlertProperty());
        colDataOra.setCellValueFactory(cellData -> cellData.getValue().dataOraProperty());
        colDettagli.setCellValueFactory(cellData -> cellData.getValue().dettagliProperty());

        // Esempio di popolamento della tabella con dati fittizi (da sostituire con dati reali dal DB)
        // ObservableList è una lista che notifica automaticamente la TableView quando i dati cambiano (necessaria)
        ObservableList<AlertRow> alertList = javafx.collections.FXCollections.observableArrayList(
                new AlertRow("Mario Rossi", "Iperglicemia", "2025-09-09 10:30", "Valore glicemia: 250"),
                new AlertRow("Luca Bianchi", "Ipoglicemia", "2025-09-08 08:15", "Valore glicemia: 60"),
                new AlertRow("Anna Verdi", "Promemoria terapia", "2025-09-07 20:00", "Assumere insulina")
        );
        tblAlert.setItems(alertList);
    }
}