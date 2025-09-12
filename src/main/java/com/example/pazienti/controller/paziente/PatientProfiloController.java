package com.example.pazienti.controller.paziente;

import javafx.fxml.FXML;
import javafx.scene.control.*;

public class PatientProfiloController {
    @FXML private Label titleLabel;

    @FXML private TextField nomeTextField;
    @FXML private TextField cognomeTextField;
    @FXML private DatePicker dataNascitaDatePicker;
    @FXML private TextField emailTextField;
    @FXML private TextField phoneTextField;

    @FXML private Button saveButton;

    @FXML private void initialize() {
        // inizializzazione profilo paziente
    }

    @FXML private void handleSaveButton() {
        // logica per salvare le modifiche al profilo
    }
}

