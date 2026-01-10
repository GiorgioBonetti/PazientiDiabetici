package it.univr.diabete.controller;

import it.univr.diabete.dao.DiabetologoDAO;
import it.univr.diabete.dao.impl.DiabetologoDAOImpl;
import it.univr.diabete.model.Diabetologo;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.ChoiceBox;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.stage.Stage;

public class AddDoctorController {

    @FXML private Label titleLabel;
    @FXML private Label subtitleLabel;
    @FXML private TextField nomeField;
    @FXML private TextField cognomeField;
    @FXML private TextField emailField;
    @FXML private TextField telefonoField;
    @FXML private ChoiceBox<String> sessoChoice;
    @FXML private TextField laureaField;
    @FXML private PasswordField passwordField;
    @FXML private Button saveButton;

    private final DiabetologoDAO diabetologoDAO = new DiabetologoDAOImpl();
    private Runnable onSavedCallback;
    private boolean editMode;

    public void initData(Runnable callback) {
        this.onSavedCallback = callback;
        this.editMode = false;
        emailField.setDisable(false);
        setDialogTexts("Nuovo diabetologo", "Inserisci i dati del nuovo diabetologo", "Crea diabetologo");
    }

    public void initEdit(Diabetologo d, Runnable callback) {
        this.onSavedCallback = callback;
        this.editMode = true;

        nomeField.setText(d.getNome());
        cognomeField.setText(d.getCognome());
        emailField.setText(d.getEmail());
        emailField.setDisable(true);
        telefonoField.setText(d.getNumeroTelefono());
        sessoChoice.setValue(d.getSesso());
        laureaField.setText(d.getLaurea());
        passwordField.setText(d.getPassword());

        setDialogTexts("Modifica diabetologo", "Aggiorna i dati del diabetologo", "Salva modifiche");
    }

    @FXML
    private void handleSave() {
        try {
            Diabetologo d = new Diabetologo();
            d.setNome(nomeField.getText());
            d.setCognome(cognomeField.getText());
            d.setEmail(emailField.getText());
            d.setNumeroTelefono(telefonoField.getText());
            d.setSesso(sessoChoice.getValue());
            d.setLaurea(laureaField.getText());
            d.setPassword(passwordField.getText());

            if (editMode) {
                diabetologoDAO.update(d);
            } else {
                diabetologoDAO.insert(d);
            }

            if (onSavedCallback != null) {
                onSavedCallback.run();
            }
            close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @FXML
    private void handleCancel() {
        close();
    }

    private void close() {
        Stage stage = (Stage) nomeField.getScene().getWindow();
        stage.close();
    }

    private void setDialogTexts(String title, String subtitle, String actionText) {
        if (titleLabel != null) {
            titleLabel.setText(title);
        }
        if (subtitleLabel != null) {
            subtitleLabel.setText(subtitle);
        }
        if (saveButton != null) {
            saveButton.setText(actionText);
        }
    }
}
