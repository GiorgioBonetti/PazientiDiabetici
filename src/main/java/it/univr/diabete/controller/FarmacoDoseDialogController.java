package it.univr.diabete.controller;

import it.univr.diabete.ui.ErrorDialog;  // ← IMPORT Aggiunto
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.stage.Stage;

public class FarmacoDoseDialogController {

    @FXML private Label farmacoNameLabel;
    @FXML private TextField assunzioniField;
    @FXML private TextField unitaField;

    private boolean confirmed = false;
    private int assunzioni;
    private int unita;

    public void init(String nomeFarmaco) {
        farmacoNameLabel.setText(nomeFarmaco);
    }

    @FXML
    private void handleConfirm() {
        try {
            String assunzioniText = assunzioniField.getText() != null ? assunzioniField.getText().trim() : "";
            String unitaText = unitaField.getText() != null ? unitaField.getText().trim() : "";

            if (assunzioniText.isEmpty()) {
                ErrorDialog.show("Assunzioni mancanti",
                        "Inserisci il numero di assunzioni giornaliere.");
                return;
            }

            if (unitaText.isEmpty()) {
                ErrorDialog.show("Unità mancanti",
                        "Inserisci la quantità per assunzione.");
                return;
            }

            assunzioni = Integer.parseInt(assunzioniText);
            unita = Integer.parseInt(unitaText);

            if (assunzioni <= 0) {
                ErrorDialog.show("Assunzioni non valide",
                        "Le assunzioni giornaliere devono essere > 0.");
                return;
            }

            if (unita <= 0) {
                ErrorDialog.show("Unità non valide",
                        "La quantità per assunzione deve essere > 0.");
                return;
            }

            confirmed = true;
            close();

        } catch (NumberFormatException e) {
            ErrorDialog.show("Valori non validi",
                    "Inserisci numeri interi positivi per assunzioni e unità.");
        }
    }

    @FXML
    private void handleCancel() {
        confirmed = false;
        close();
    }

    private void close() {
        Stage stage = (Stage) farmacoNameLabel.getScene().getWindow();
        stage.close();
    }

    public boolean isConfirmed() {
        return confirmed;
    }

    public int getAssunzioni() {
        return assunzioni;
    }

    public int getUnita() {
        return unita;
    }
}
