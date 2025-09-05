package com.example.pazienti.controller.paziente;

import javafx.fxml.FXML;
import javafx.scene.control.*;

public class PazientiController {
    @FXML private Label titleLabel;
    @FXML private TextField searchField;
    @FXML private Button addButton;
    @FXML private Button editButton;
    @FXML private Button removeButton;

    @FXML private TableView<?> patientsTable;
    @FXML private ListView<?> patientsList;
    @FXML private TextArea detailsArea;

    @FXML private void initialize() {
        // inizializzazione pazienti
    }
}

