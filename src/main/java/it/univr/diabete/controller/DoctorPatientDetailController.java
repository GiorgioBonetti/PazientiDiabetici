package it.univr.diabete.controller;

import it.univr.diabete.MainApp;
import it.univr.diabete.dao.PazienteDAO;
import it.univr.diabete.dao.impl.PazienteDAOImpl;
import it.univr.diabete.model.Paziente;
import javafx.fxml.FXML;
import javafx.scene.control.*;

import java.time.LocalDate;

public class DoctorPatientDetailController {
    @FXML
    private Label saveMessageLabel;
    @FXML
    private Label patientNameLabel;

    @FXML
    private TextField nomeField;

    @FXML
    private TextField cognomeField;

    @FXML
    private TextField emailField;

    @FXML
    private TextField telefonoField;

    @FXML
    private DatePicker dataNascitaPicker;

    @FXML
    private ChoiceBox<String> sessoChoice;

    @FXML
    private TextField codiceFiscaleField;

    @FXML
    private PasswordField passwordField;

    private final PazienteDAO pazienteDAO = new PazienteDAOImpl();
    private Paziente currentPatient;
    @FXML
    private void initialize() {
        // eventuali valori fissi per il sesso
        sessoChoice.getItems().setAll("M", "F", "Altro");
    }

    /** Chiamato dal MainShellController quando apro la scheda. */
    public void setPatient(Paziente paziente) {
        this.currentPatient = paziente;

        patientNameLabel.setText(paziente.getNome() + " " + paziente.getCognome());

        nomeField.setText(paziente.getNome());
        cognomeField.setText(paziente.getCognome());
        emailField.setText(paziente.getEmail());           // o geteMail()
        telefonoField.setText(paziente.getNumeroTelefono());

        LocalDate dob = paziente.getDataNascita();
        if (dob != null) {
            dataNascitaPicker.setValue(dob);
        } else {
            dataNascitaPicker.setValue(null);
        }

        sessoChoice.setValue(paziente.getSesso());
        codiceFiscaleField.setText(paziente.getCodiceFiscale());
        passwordField.setText(paziente.getPassword());
    }

    @FXML
    private void handleSave() {
        try {
            // aggiorna currentPatient con i campi della form
            currentPatient.setNome(nomeField.getText().trim());
            currentPatient.setCognome(cognomeField.getText().trim());
            currentPatient.setEmail(emailField.getText().trim());
            currentPatient.setNumeroTelefono(telefonoField.getText().trim());
            currentPatient.setDataNascita(dataNascitaPicker.getValue());
            currentPatient.setSesso(sessoChoice.getValue());
            currentPatient.setCodiceFiscale(codiceFiscaleField.getText().trim());
            currentPatient.setPassword(passwordField.getText());

            // salva su DB
            pazienteDAO.update(currentPatient);

            // refresh dei campi (così vedi subito il cambiamento)
            setPatient(currentPatient);

            // 🔔 mostra banner globale nel top bar
            MainShellController shell = MainApp.getMainShellController();
            if (shell != null) {
                shell.showGlobalSuccess("Dati paziente salvati correttamente");
            }

        } catch (Exception e) {
            e.printStackTrace();
            MainShellController shell = MainApp.getMainShellController();
            if (shell != null) {
                shell.showGlobalError("Errore durante il salvataggio");
            }
        }
    }

    @FXML
    private void handleCancelChanges() {
        if (currentPatient != null) {
            setPatient(currentPatient); // rimette i valori originali
        }
    }
}