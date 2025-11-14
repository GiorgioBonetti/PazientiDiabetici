package com.example.pazienti.controller.dottore;

import com.example.pazienti.DB.DbPaziente;
import com.example.pazienti.classi.Paziente;
import com.example.pazienti.model.PazientiRow;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.stage.Modality;
import javafx.stage.Stage;

import java.io.IOException;

public class DashboardController {
    @FXML private Label titleLabel;

    DbPaziente dbPaziente = new DbPaziente();


    @FXML private TextField searchBar;
    @FXML private Button searchButton;
    @FXML private MenuItem profileButton;
    @FXML private MenuItem logoutButton;

    @FXML private ToggleButton pazientiButton;
    @FXML private ToggleButton terapieButton;
    @FXML private ToggleButton alertButton;
    @FXML private ToggleButton impostazioniButton;

    @FXML private void handlePazienti() {



        // apertura pazienti
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/example/pazienti/dottore/DoctorPatientList.fxml"));
            Parent root = loader.load();

            Stage dialogStage = new Stage();
            dialogStage.setTitle("Pazienti");
            dialogStage.initModality(Modality.WINDOW_MODAL);
            dialogStage.initOwner(pazientiButton.getScene().getWindow());
            dialogStage.setScene(new Scene(root));
            dialogStage.setResizable(false);

            dialogStage.showAndWait();
        } catch (IOException e) {
            e.printStackTrace();
            // Eventuale alert per l'utente
        }
    }

    @FXML private void handleTerapie() {
        // apertura terapie
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/example/pazienti/dottore/Terapie.fxml"));
            Parent root = loader.load();

            Stage dialogStage = new Stage();
            dialogStage.setTitle("Terapie");
            dialogStage.initModality(Modality.WINDOW_MODAL);
            dialogStage.initOwner(terapieButton.getScene().getWindow());
            dialogStage.setScene(new Scene(root));
            dialogStage.setResizable(false);

            dialogStage.showAndWait();
        } catch (IOException e) {
            e.printStackTrace();
            // Eventuale alert per l'utente
        }
    }

    @FXML private void handleAlert() {
        // apertura alert
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/example/pazienti/dottore/Alert.fxml"));
            Parent root = loader.load();

            Stage dialogStage = new Stage();
            dialogStage.setTitle("Alert");
            dialogStage.initModality(Modality.WINDOW_MODAL);
            dialogStage.initOwner(alertButton.getScene().getWindow());
            dialogStage.setScene(new Scene(root));
            dialogStage.setResizable(false);

            dialogStage.showAndWait();
        } catch (IOException e) {
            e.printStackTrace();
            // Eventuale alert per l'utente
        }
    }

    @FXML private TableView<PazientiRow> tblPazienti;
    @FXML private TableColumn<PazientiRow, String> colPaziente;
    @FXML private TableColumn<PazientiRow, String> colOutOfRange;
    @FXML private TableColumn<PazientiRow, String> colAderenza;
    @FXML private TableColumn<PazientiRow, String> colUltimaGlicemia;
    @FXML private TableColumn<PazientiRow, String> colAlert;

    @FXML private void initialize() {
        // inizializzazione
//        colPaziente.setCellValueFactory(cellData -> cellData.getValue().pazienteProperty());
//        colOutOfRange.setCellValueFactory(cellData -> cellData.getValue().outOfRangeProperty());
//        colAderenza.setCellValueFactory(cellData -> cellData.getValue().aderenzaProperty());
//        colUltimaGlicemia.setCellValueFactory(cellData -> cellData.getValue().ultimaGlicemiaProperty());
//        colAlert.setCellValueFactory(cellData -> cellData.getValue().alertProperty());

        // Esempio di popolamento della tabella con dati fittizi (da sostituire con dati reali dal DB)
        ObservableList<Paziente> pazientiList = FXCollections.observableArrayList();

        for (Paziente p : dbPaziente.getAllUtenti()) {
            pazientiList.add(p);
        }

       // tblPazienti.setItems(pazientiList);
    }
}

