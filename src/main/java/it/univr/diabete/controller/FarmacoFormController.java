package it.univr.diabete.controller;

import it.univr.diabete.model.Farmaco;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.stage.Stage;

public class FarmacoFormController {

    @FXML private Label titleLabel;
    @FXML private TextField nomeField;
    @FXML private TextField marcaField;
    @FXML private Label errorLabel;

    private Farmaco farmaco;      // esistente (edit) o nuovo
    private boolean confirmed = false;

    @FXML
    private void initialize() {
        errorLabel.setVisible(false);
        errorLabel.setManaged(false);
    }

    /** init(null) per "nuovo", init(farmaco) per "modifica". */
    public void init(Farmaco existing, String title) {
        this.farmaco = existing;
        if (title != null && !title.isBlank()) {
            titleLabel.setText(title);
        }

        if (existing != null) {
            nomeField.setText(existing.getNome());
            if (existing.getMarca() != null) {
                marcaField.setText(existing.getMarca());
            }
        }
    }

    @FXML
    private void handleSave() {
        String nome = nomeField.getText() != null ? nomeField.getText().trim() : "";
        String marca = marcaField.getText() != null ? marcaField.getText().trim() : "";

        if (nome.isEmpty()) {
            showError("Inserisci il nome del farmaco.");
            return;
        }

        if (farmaco == null) {
            farmaco = new Farmaco();
        }

        farmaco.setNome(nome);
        farmaco.setMarca(marca.isEmpty() ? null : marca);

        confirmed = true;
        close();
    }

    @FXML
    private void handleCancel() {
        confirmed = false;
        close();
    }

    private void showError(String msg) {
        errorLabel.setText(msg);
        errorLabel.setVisible(true);
        errorLabel.setManaged(true);
    }

    private void close() {
        Stage stage = (Stage) nomeField.getScene().getWindow();
        stage.close();
    }

    public boolean isConfirmed() {
        return confirmed;
    }

    public Farmaco getResult() {
        return farmaco;
    }
}