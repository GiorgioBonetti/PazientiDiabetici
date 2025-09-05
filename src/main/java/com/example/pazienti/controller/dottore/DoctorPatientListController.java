package com.example.pazienti.controller.dottore;

import javafx.fxml.FXML;
import javafx.scene.control.*;

public class DoctorPatientListController {
    @FXML private Label titleLabel;
    @FXML private TextField searchField;
    @FXML private TableView<?> patientsTable;
    @FXML private Button viewDetailsButton;
    @FXML private Button messageButton;

    @FXML private void initialize() {
        // inizializzazione lista pazienti dottore
    }
}

