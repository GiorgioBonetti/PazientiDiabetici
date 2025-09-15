package com.example.pazienti.controller.paziente;

import com.example.pazienti.model.PatologieRow;
import com.example.pazienti.model.SintomiRow;
import com.example.pazienti.model.TerapieRow;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.stage.Modality;
import javafx.stage.Stage;

import java.io.IOException;

public class DashboardPazienteController {
    @FXML private Label titleLabel;

    // ALERT
    @FXML private Label alertLabel;
    @FXML private ListView<?> listView;

    // SINTOMI
    @FXML private Label sintomiLabel;
    @FXML private Button addSintomoButton;

    @FXML private TableView<SintomiRow> tblSintomi;
    @FXML private TableColumn<SintomiRow, String> colSintomo;
    @FXML private TableColumn<SintomiRow, String> colDataInizio;
    @FXML private TableColumn<SintomiRow, String> colDataFine;
    @FXML private TableColumn<SintomiRow, String> colPatologiaAssociata;
    @FXML private TableColumn<SintomiRow, String> colNoteSintomi;

    @FXML private void handleAddSintomo() {
        // logica per aggiungere sintomo
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/example/pazienti/paziente/AddSintomoDialog.fxml"));
            Parent root = loader.load();

            Stage dialogStage = new Stage();
            dialogStage.setTitle("Aggiungi sintomo");
            dialogStage.initModality(Modality.WINDOW_MODAL);
            dialogStage.initOwner(tblSintomi.getScene().getWindow());
            dialogStage.setScene(new Scene(root));
            dialogStage.setResizable(false);

            dialogStage.showAndWait();
        } catch (IOException e) {
            e.printStackTrace();
            // Eventuale alert per l'utente
        }
    }

    // PATOLOGIE
    @FXML private Label patologieLabel;
    @FXML private Button addPatologiaButton;
    @FXML private TableView<PatologieRow> tblPatologie;
    @FXML private TableColumn<PatologieRow, String> colNomePatologia;
    @FXML private TableColumn<PatologieRow, String> colDataDiagnosi;
    @FXML private TableColumn<PatologieRow, String> colInCorso;
    @FXML private TableColumn<PatologieRow, String> colNotePatologie;
    @FXML private TableColumn<PatologieRow, String> colAzioni;

    @FXML private void handleAddPatologia() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/example/pazienti/paziente/AddPatologiaDialog.fxml"));
            Parent root = loader.load();

            Stage dialogStage = new Stage();
            dialogStage.setTitle("Aggiungi patologia");
            dialogStage.initModality(Modality.WINDOW_MODAL);
            dialogStage.initOwner(tblPatologie.getScene().getWindow());
            dialogStage.setScene(new Scene(root));
            dialogStage.setResizable(false);

            dialogStage.showAndWait();
        } catch (IOException e) {
            e.printStackTrace();
            // Eventuale alert per l'utente
        }
    }

    // BUTTONS
    @FXML private Button refreshButton;
    @FXML private Button logoutButton;

    @FXML private void handleRefresh() {
        // logica per refresh
    }

    @FXML private void handleLogout() {
        // logica per logout
    }

    // INITIALIZE
    @FXML private void initialize() {
        // inizializzazione dashboard paziente
        // collega le colonne ai campi della classe SintomiRow
        colSintomo.setCellValueFactory(cellData -> cellData.getValue().sintomoProperty());
        colDataInizio.setCellValueFactory(cellData -> cellData.getValue().dataInizioProperty());
        colDataFine.setCellValueFactory(cellData -> cellData.getValue().dataFineProperty());
        colPatologiaAssociata.setCellValueFactory(cellData -> cellData.getValue().patologiaAssociataProperty());
        colNoteSintomi.setCellValueFactory(cellData -> cellData.getValue().noteProperty());

        // collega le colonne ai campi della classe PatologieRow
        colNomePatologia.setCellValueFactory(cellData -> cellData.getValue().nomePatologiaProperty());
        colDataDiagnosi.setCellValueFactory(cellData -> cellData.getValue().dataDiagnosiProperty());
        colInCorso.setCellValueFactory(cellData -> cellData.getValue().inCorsoProperty());
        colNotePatologie.setCellValueFactory(cellData -> cellData.getValue().noteProperty());
        colAzioni.setCellValueFactory(cellData -> cellData.getValue().azioniProperty());

        // Esempio di popolamento delle tabelle con dati fittizi (da sostituire con dati reali dal DB)
        ObservableList<SintomiRow> sintomiList = javafx.collections.FXCollections.observableArrayList(
                new SintomiRow("Cefalea", "2024-01-10", "2024-01-15", "Emicrania", "Nessuna nota"),
                new SintomiRow("Febbre", "2024-02-05", "2024-02-10", "Influenza", "Nessuna nota")
        );
        tblSintomi.setItems(sintomiList);

        ObservableList<PatologieRow> patologieList = javafx.collections.FXCollections.observableArrayList(
                new PatologieRow("Diabete", "2020-03-12", "Sì", "Controllo regolare", "Modifica/Rimuovi"),
                new PatologieRow("Ipertensione", "2019-07-22", "No", "In remissione", "Modifica/Rimuovi")
        );
        tblPatologie.setItems(patologieList);
    }
}

