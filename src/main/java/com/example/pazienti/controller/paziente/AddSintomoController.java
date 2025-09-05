package com.example.pazienti.controller.paziente;

import javafx.fxml.FXML;
import javafx.scene.control.*;

public class AddSintomoController {
    @FXML
    private ChoiceBox<String> nomeSintomoChoiceBox;
    @FXML
    private DatePicker periodoPicker;
    @FXML
    private RadioButton frequenzaContinuoRadioButton;
    @FXML
    private RadioButton frequenzaRicorrenteRadioButton;
    @FXML
    private RadioButton frequenzaOccasionaleRadioButton;
    @FXML
    private Slider intesitaSlider;
    @FXML
    private TextArea sintomoTextArea;

    // buttons
    @FXML
    private Button backButton;
    @FXML
    private Button saveButton;

    @FXML
    private void onSaveButtonClick() {
        String nomeSintomo = nomeSintomoChoiceBox.getValue();
        String periodo = (periodoPicker.getValue() != null) ? periodoPicker.getValue().toString() : "";
        String frequenza = frequenzaContinuoRadioButton.isSelected() ? "Continuo" :
                          frequenzaRicorrenteRadioButton.isSelected() ? "Ricorrente" : "Occasionale";
        double intensita = intesitaSlider.getValue();
        String descrizione = sintomoTextArea.getText();

        System.out.println("Sintomo: " + nomeSintomo);
        System.out.println("Periodo: " + periodo);
        System.out.println("Frequenze: " + frequenza);
        System.out.println("Intensità: " + intensita);
        System.out.println("Descrizione: " + descrizione);
    }
}