package it.univr.diabete.controller;

import it.univr.diabete.dao.DiabetologoDAO;
import it.univr.diabete.dao.impl.DiabetologoDAOImpl;
import it.univr.diabete.model.Diabetologo;
import it.univr.diabete.ui.ErrorDialog;
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
            // --- VALIDAZIONI ---
            if (nomeField.getText() == null || nomeField.getText().trim().isEmpty()) {
                ErrorDialog.show("Nome mancante", "Inserisci il nome del diabetologo.");
                return;
            }

            if (cognomeField.getText() == null || cognomeField.getText().trim().isEmpty()) {
                ErrorDialog.show("Cognome mancante", "Inserisci il cognome del diabetologo.");
                return;
            }

            if (emailField.getText() == null || emailField.getText().trim().isEmpty()) {
                ErrorDialog.show("Email mancante", "Inserisci l'email del diabetologo.");
                return;
            }

            if (telefonoField.getText() == null || telefonoField.getText().trim().isEmpty()) {
                ErrorDialog.show("Telefono mancante", "Inserisci il numero di telefono.");
                return;
            }

            if (sessoChoice.getValue() == null) {
                ErrorDialog.show("Sesso non selezionato", "Seleziona il sesso del diabetologo.");
                return;
            }

            if (laureaField.getText() == null || laureaField.getText().trim().isEmpty()) {
                ErrorDialog.show("Laurea mancante", "Inserisci l'università di laurea.");
                return;
            }

            if (passwordField.getText() == null || passwordField.getText().trim().isEmpty()) {
                ErrorDialog.show("Password mancante", "Inserisci la password.");
                return;
            }

            // --- SALVATAGGIO ---
            Diabetologo d = new Diabetologo();
            d.setNome(nomeField.getText().trim());
            d.setCognome(cognomeField.getText().trim());
            d.setEmail(emailField.getText().trim());
            d.setNumeroTelefono(telefonoField.getText().trim());
            d.setSesso(sessoChoice.getValue());
            d.setLaurea(laureaField.getText().trim());
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
            ErrorDialog.show("Errore di salvataggio",
                    "Impossibile salvare il diabetologo. Riprova.");
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
