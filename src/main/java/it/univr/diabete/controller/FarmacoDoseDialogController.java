package it.univr.diabete.controller;

import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.stage.Stage;

public class FarmacoDoseDialogController {

    @FXML private Label farmacoNameLabel;
    @FXML private TextField assunzioniField;
    @FXML private TextField unitaField;
    @FXML private Label errorLabel;

    private boolean confirmed = false;
    private int assunzioni;
    private int unita;

    public void init(String nomeFarmaco) {
        farmacoNameLabel.setText(nomeFarmaco);
        errorLabel.setVisible(false);
        errorLabel.setManaged(false);
    }

    @FXML
    private void handleConfirm() {
        errorLabel.setVisible(false);
        errorLabel.setManaged(false);

        try {
            assunzioni = Integer.parseInt(assunzioniField.getText().trim());
            unita = Integer.parseInt(unitaField.getText().trim());

            if (assunzioni <= 0 || unita <= 0) {
                throw new NumberFormatException();
            }

            confirmed = true;
            close();

        } catch (NumberFormatException e) {
            errorLabel.setText("Inserisci numeri interi positivi.");
            errorLabel.setVisible(true);
            errorLabel.setManaged(true);
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