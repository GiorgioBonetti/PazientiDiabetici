package it.univr.diabete.controller;

import it.univr.diabete.MainApp;
import it.univr.diabete.dao.PatologiaDAO;
import it.univr.diabete.dao.PazienteDAO;
import it.univr.diabete.dao.impl.PatologiaDAOImpl;
import it.univr.diabete.dao.impl.PazienteDAOImpl;
import it.univr.diabete.model.Paziente;
import it.univr.diabete.model.Patologia;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.HBox;
import javafx.stage.Modality;
import javafx.stage.Stage;

import java.time.LocalDate;
import java.util.List;

public class DoctorPatientDetailController {
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

    @FXML
    private FlowPane patologieContainer;

    @FXML
    private Label patologieEmptyLabel;

    @FXML
    private Button addPatologiaButton;

    private final PazienteDAO pazienteDAO = new PazienteDAOImpl();
    private final PatologiaDAO patologiaDAO = new PatologiaDAOImpl();
    private Paziente currentPatient;
    @FXML
    private void initialize() {
        // eventuali valori fissi per il sesso
        sessoChoice.getItems().setAll("M", "F", "Altro");
    }

    /** Chiamato dal MainShellController quando apro la scheda. */
    public void setPatient(Paziente paziente) {
        this.currentPatient = paziente;
        if (paziente != null) {
            try {
                Paziente fresh = pazienteDAO.findById(paziente.getCodiceFiscale());
                if (fresh != null) {
                    this.currentPatient = fresh;
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        patientNameLabel.setText(currentPatient.getNome() + " " + currentPatient.getCognome());

        nomeField.setText(currentPatient.getNome());
        cognomeField.setText(currentPatient.getCognome());
        emailField.setText(currentPatient.getEmail());           // o geteMail()
        telefonoField.setText(currentPatient.getNumeroTelefono());

        LocalDate dob = currentPatient.getDataNascita();
        dataNascitaPicker.setValue(dob);

        sessoChoice.setValue(currentPatient.getSesso());
        codiceFiscaleField.setText(currentPatient.getCodiceFiscale());
        passwordField.setText(currentPatient.getPassword());

        loadPatologie();
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

    @FXML
    private void handleAddPatologia() {
        if (currentPatient == null) return;

        try {
            FXMLLoader loader = new FXMLLoader(
                    MainApp.class.getResource("/fxml/AddPatologiaView.fxml")
            );
            Parent root = loader.load();

            AddPatologiaController ctrl = loader.getController();
            ctrl.initData(currentPatient.getCodiceFiscale(), this::loadPatologie);

            Stage popup = new Stage();
            popup.initOwner(addPatologiaButton.getScene().getWindow());
            popup.initModality(Modality.WINDOW_MODAL);
            popup.setTitle("Nuova patologia");
            popup.setScene(new Scene(root));
            popup.showAndWait();

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void loadPatologie() {
        if (currentPatient == null) return;
        try {
            List<Patologia> list = patologiaDAO.findByPaziente(currentPatient.getCodiceFiscale());
            patologieContainer.getChildren().clear();

            if (list.isEmpty()) {
                patologieEmptyLabel.setVisible(true);
                patologieEmptyLabel.setManaged(true);
                return;
            }

            patologieEmptyLabel.setVisible(false);
            patologieEmptyLabel.setManaged(false);

            for (Patologia p : list) {
                patologieContainer.getChildren().add(createPatologiaChip(p));
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private HBox createPatologiaChip(Patologia p) {
        Label name = new Label(p.getNome());
        name.getStyleClass().add("patologia-pill-text");

        Button remove = new Button("×");
        remove.getStyleClass().add("patologia-pill-remove");
        remove.setOnAction(e -> removePatologia(p));

        HBox box = new HBox(6, name, remove);
        box.getStyleClass().add("patologia-pill");
        return box;
    }

    private void removePatologia(Patologia p) {
        boolean confirmed = it.univr.diabete.ui.ConfirmDialog.show(
                "Rimuovi patologia",
                "Vuoi rimuovere \"" + p.getNome() + "\" dal paziente?",
                null
        );
        if (!confirmed) return;
        try {
            patologiaDAO.delete(p.getId());
            loadPatologie();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
