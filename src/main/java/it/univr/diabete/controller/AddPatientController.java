package it.univr.diabete.controller;

import it.univr.diabete.dao.PazienteDAO;
import it.univr.diabete.dao.impl.PazienteDAOImpl;
import it.univr.diabete.model.Paziente;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.stage.Stage;

import java.time.LocalDate;

public class AddPatientController {

    @FXML private TextField nomeField;
    @FXML private TextField cognomeField;
    @FXML private TextField emailField;
    @FXML private TextField telefonoField;
    @FXML private TextField cfField;
    @FXML private ChoiceBox<String> sessoChoice;
    @FXML private DatePicker dataNascitaPicker;
    @FXML private PasswordField passwordField;

    private final PazienteDAO pazienteDAO = new PazienteDAOImpl();

    private String diabetologoId;           // viene passato dal controller del medico
    private Runnable onSavedCallback;    // ricarica lista pazienti

    // --- inizializzazione dati da fuori ---
    public void initData(String diabetologoId, Runnable callback) {
        this.diabetologoId = diabetologoId;
        this.onSavedCallback = callback;
    }

    // --- SALVATAGGIO NUOVO PAZIENTE ---
    @FXML
    private void handleSave() {
        try {
            Paziente p = new Paziente();

            p.setNome(nomeField.getText());
            p.setCognome(cognomeField.getText());
            p.setEmail(emailField.getText());
            p.setNumeroTelefono(telefonoField.getText());
            p.setCodiceFiscale(cfField.getText());
            p.setSesso(sessoChoice.getValue());
            p.setDataNascita(dataNascitaPicker.getValue());
            p.setPassword(passwordField.getText());
            p.setIdDiabetologo(diabetologoId);

            pazienteDAO.insert(p);  // 🔥 CREA IL PAZIENTE

            if (onSavedCallback != null)
                onSavedCallback.run();
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
}