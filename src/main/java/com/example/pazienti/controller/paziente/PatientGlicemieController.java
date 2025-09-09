package com.example.pazienti.controller.paziente;

import com.example.pazienti.model.MisurazioniRow;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.stage.Modality;
import javafx.stage.Stage;

import java.io.IOException;

public class PatientGlicemieController {
    @FXML private Label titleLabel;

    @FXML private Button addMisurazioneButton;
    @FXML private ComboBox<String> periodoComboBox;
    @FXML private Button refreshButton;

    @FXML private TableView<MisurazioniRow> tblGlicemie;
    @FXML private TableColumn<MisurazioniRow, String> colDataOra;
    @FXML private TableColumn<MisurazioniRow, String> colContesto;
    @FXML private TableColumn<MisurazioniRow, String> colValore;
    @FXML private TableColumn<MisurazioniRow, String> colNote;
    @FXML private TableColumn<MisurazioniRow, String> colAzioni;

    @FXML private void initialize() {
        // inizializzazione PatientGlicemie
        colDataOra.setCellValueFactory(cellData -> cellData.getValue().dataOraProperty());
        colContesto.setCellValueFactory(cellData -> cellData.getValue().contestoProperty());
        colValore.setCellValueFactory(cellData -> cellData.getValue().valoreProperty());
        colNote.setCellValueFactory(cellData -> cellData.getValue().noteProperty());
        colAzioni.setCellValueFactory(cellData -> cellData.getValue().azioniProperty());

        // Esempio di popolamento della tabella con dati fittizi (da sostituire con dati reali dal DB)
        // ObservableList è una lista che notifica automaticamente la TableView quando i dati cambiano (necessaria)
        ObservableList<MisurazioniRow> terapieList = javafx.collections.FXCollections.observableArrayList(
                new MisurazioniRow("2024-06-01 08:00", "A digiuno", "90 mg/dL", "Nessuna", "Modifica | Elimina"),
                new MisurazioniRow("2024-06-01 12:00", "Post-prandiale", "130 mg/dL", "Nessuna", "Modifica | Elimina")
        );
        tblGlicemie.setItems(terapieList);
    }

    @FXML private void handleAddMisurazione() {
        // gestione aggiunta misurazione
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/example/pazienti/paziente/AddGlicemiaDialog.fxml"));
            Parent root = loader.load();

            Stage dialogStage = new Stage();
            dialogStage.setTitle("Nuova misurazione");
            dialogStage.initModality(Modality.WINDOW_MODAL);
            dialogStage.initOwner(tblGlicemie.getScene().getWindow());
            dialogStage.setScene(new Scene(root));
            dialogStage.setResizable(false);

            dialogStage.showAndWait();
        } catch (IOException e) {
            e.printStackTrace();
            // Eventuale alert per l'utente
        }
    }

    @FXML private void handleRefresh() {
        // gestione refresh tabella
        // Ricarica i dati dalla fonte dati (es. database) e aggiorna la tabella
    }
}

