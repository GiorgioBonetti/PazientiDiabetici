package com.example.pazienti.controller.paziente;

import javafx.fxml.FXML;
import javafx.scene.control.*;

public class AddSintomoDialogController {
    @FXML private Label titleLabel;
    @FXML private ChoiceBox<String> sintomoChoiceBox;
    @FXML private DatePicker periodoPicker;
    @FXML private RadioButton continuoRadio;
    @FXML private RadioButton ricorrenteRadio;
    @FXML private RadioButton occasionaleRadio;
    @FXML private Slider intensitaSlider;
    @FXML private TextArea descrizioneArea;
    @FXML private Button saveButton;
    @FXML private Button cancelButton;

    @FXML private void initialize() {
        // inizializzazione dialog aggiungi sintomo
    }
}

