package it.univr.diabete.controller;

import it.univr.diabete.dao.DiabetologoDAO;
import it.univr.diabete.dao.PazienteDAO;
import it.univr.diabete.dao.impl.DiabetologoDAOImpl;
import it.univr.diabete.dao.impl.PazienteDAOImpl;
import it.univr.diabete.model.Diabetologo;
import it.univr.diabete.model.Paziente;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import javafx.scene.control.ChoiceBox;
import javafx.scene.control.ComboBox;
import javafx.scene.control.DatePicker;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.scene.control.Button;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

public class AddPatientController {

    @FXML private Label titleLabel;
    @FXML private Label subtitleLabel;
    @FXML private TextField nomeField;
    @FXML private TextField cognomeField;
    @FXML private TextField emailField;
    @FXML private TextField telefonoField;
    @FXML private TextField cfField;
    @FXML private ChoiceBox<String> sessoChoice;
    @FXML private DatePicker dataNascitaPicker;
    @FXML private PasswordField passwordField;
    @FXML private Button saveButton;
    @FXML private VBox diabetologoContainer;
    @FXML private ComboBox<Diabetologo> diabetologoCombo;

    private final PazienteDAO pazienteDAO = new PazienteDAOImpl();
    private final DiabetologoDAO diabetologoDAO = new DiabetologoDAOImpl();

    private String diabetologoId;           // viene passato dal controller del medico
    private Runnable onSavedCallback;    // ricarica lista pazienti
    private boolean adminMode;
    private boolean editMode;

    // --- inizializzazione dati da fuori ---
    public void initData(String diabetologoId, Runnable callback) {
        this.diabetologoId = diabetologoId;
        this.onSavedCallback = callback;
        this.adminMode = false;
        this.editMode = false;
        setDiabetologoPickerVisible(false);
        cfField.setDisable(false);
    }

    public void initAdminMode(Runnable callback) {
        this.onSavedCallback = callback;
        this.adminMode = true;
        this.editMode = false;
        setDiabetologoPickerVisible(true);
        loadDiabetologists(null);
        cfField.setDisable(false);
        setDialogTexts("Nuovo paziente", "Inserisci i dati del nuovo paziente", "Crea paziente");
    }

    public void initEditAdmin(Paziente paziente, Runnable callback) {
        this.onSavedCallback = callback;
        this.adminMode = true;
        this.editMode = true;
        setDiabetologoPickerVisible(true);

        nomeField.setText(paziente.getNome());
        cognomeField.setText(paziente.getCognome());
        emailField.setText(paziente.getEmail());
        telefonoField.setText(paziente.getNumeroTelefono());
        cfField.setText(paziente.getCodiceFiscale());
        cfField.setDisable(true);
        sessoChoice.setValue(paziente.getSesso());
        dataNascitaPicker.setValue(paziente.getDataNascita());
        passwordField.setText(paziente.getPassword());

        loadDiabetologists(paziente.getFkDiabetologo());
        setDialogTexts("Modifica paziente", "Aggiorna i dati del paziente", "Salva modifiche");
    }

    @FXML
    private void initialize() {
        setDiabetologoPickerVisible(false);
    }

    private void setDiabetologoPickerVisible(boolean visible) {
        if (diabetologoContainer != null) {
            diabetologoContainer.setVisible(visible);
            diabetologoContainer.setManaged(visible);
        }
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
            if (adminMode) {
                Diabetologo selected = diabetologoCombo.getValue();
                if (selected == null) {
                    showError("Seleziona un diabetologo.");
                    return;
                }
                p.setFkDiabetologo(selected.getEmail());
            } else {
                p.setFkDiabetologo(diabetologoId);
            }

            if (editMode) {
                pazienteDAO.update(p);
            } else {
                pazienteDAO.insert(p);  // 🔥 CREA IL PAZIENTE
            }

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

    private void loadDiabetologists(String selectedEmail) {
        try {
            diabetologoCombo.getItems().setAll(diabetologoDAO.findAll());
            diabetologoCombo.setCellFactory(list -> new javafx.scene.control.ListCell<>() {
                @Override
                protected void updateItem(Diabetologo item, boolean empty) {
                    super.updateItem(item, empty);
                    if (empty || item == null) {
                        setText(null);
                    } else {
                        setText(item.getNome() + " " + item.getCognome() + " (" + item.getEmail() + ")");
                    }
                }
            });
            diabetologoCombo.setButtonCell(new javafx.scene.control.ListCell<>() {
                @Override
                protected void updateItem(Diabetologo item, boolean empty) {
                    super.updateItem(item, empty);
                    if (empty || item == null) {
                        setText(null);
                    } else {
                        setText(item.getNome() + " " + item.getCognome());
                    }
                }
            });
            if (selectedEmail != null) {
                for (Diabetologo d : diabetologoCombo.getItems()) {
                    if (selectedEmail.equalsIgnoreCase(d.getEmail())) {
                        diabetologoCombo.getSelectionModel().select(d);
                        break;
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void showError(String message) {
        Alert alert = new Alert(Alert.AlertType.WARNING, message, ButtonType.OK);
        alert.setHeaderText("Dati incompleti");
        alert.showAndWait();
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
