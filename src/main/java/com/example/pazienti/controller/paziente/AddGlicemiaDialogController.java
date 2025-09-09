package com.example.pazienti.controller.paziente;

import javafx.fxml.FXML;
import javafx.scene.control.*;

public class AddGlicemiaDialogController {
    @FXML private Label headerLabel;

    @FXML private TextField dataOraField;
    @FXML private ComboBox<String> contestoComboBox;
    @FXML private TextField valoreField;
    @FXML private TextArea noteArea;

    @FXML private Button saveButton;
    @FXML private Button cancelButton;

    @FXML private void initialize() {
    }

    @FXML private void handleSave() {
        // gestione salvataggio misurazione glicemia
    }

    @FXML private void handleCancel() {
        // Chiudi la finestra del dialog
        cancelButton.getScene().getWindow().hide();
    }
}
