package com.example.pazienti.controller;

import com.example.pazienti.model.TerapieRow;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Modality;
import javafx.stage.Stage;
import java.io.IOException;

public class TerapieController {
    @FXML private Label titleLabel;

    @FXML private Button btnAddTerapia;

    @FXML private TableView<TerapieRow> tblTerapie;
    @FXML private TableColumn<TerapieRow, String> colPaziente;
    @FXML private TableColumn<TerapieRow, String> colFarmaco;
    @FXML private TableColumn<TerapieRow, String> colDosaggio;
    @FXML private TableColumn<TerapieRow, String> colFrequenza;

    @FXML private void initialize() {
        // Collega le colonne ai campi della classe TerapieRow
        colPaziente.setCellValueFactory(cellData -> cellData.getValue().pazienteProperty());
        colFarmaco.setCellValueFactory(cellData -> cellData.getValue().farmacoProperty());
        colDosaggio.setCellValueFactory(cellData -> cellData.getValue().dosaggioProperty());
        colFrequenza.setCellValueFactory(cellData -> cellData.getValue().frequenzaProperty());

        // Esempio di popolamento della tabella con dati fittizi (da sostituire con dati reali dal DB)
        // ObservableList è una lista che notifica automaticamente la TableView quando i dati cambiano (necessaria)
        ObservableList<TerapieRow> terapieList = javafx.collections.FXCollections.observableArrayList(
                new TerapieRow("Mario Rossi", "Moment", "2", "Giornaliera"),
                new TerapieRow("Luca Bianchi", "Aspirina", "1", "Giornaliera")
        );
        tblTerapie.setItems(terapieList);
    }

    @FXML private void handleAddTerapia() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/example/pazienti/dottore/TherapyEditorDialog.fxml"));
            Parent root = loader.load();

            Stage dialogStage = new Stage();
            dialogStage.setTitle("Nuova Terapia");
            dialogStage.initModality(Modality.WINDOW_MODAL);
            dialogStage.initOwner(tblTerapie.getScene().getWindow());
            dialogStage.setScene(new Scene(root));
            dialogStage.setResizable(false);

            dialogStage.showAndWait();
        } catch (IOException e) {
            e.printStackTrace();
            // Eventuale alert per l'utente
        }
    }
}
