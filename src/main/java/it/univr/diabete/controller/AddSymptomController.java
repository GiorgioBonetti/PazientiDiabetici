package it.univr.diabete.controller;

import it.univr.diabete.dao.SintomoDAO;
import it.univr.diabete.dao.impl.SintomoDAOImpl;
import it.univr.diabete.model.Sintomo;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.stage.Stage;

import java.time.LocalDate;
import java.time.LocalDateTime;

public class AddSymptomController {

    @FXML private TextField descrizioneField;
    @FXML private TextField intensitaField;
    @FXML private TextField frequenzaField;
    @FXML private TextArea noteField;

    private final SintomoDAO sintomoDAO = new SintomoDAOImpl();
    private String fkPaziente;
    private Runnable onSavedCallback;

    public void initData(String fkPaziente, Runnable callback) {
        this.fkPaziente = fkPaziente;
        this.onSavedCallback = callback;
    }

    @FXML
    private void handleSave() {
        String descrizione = descrizioneField.getText() != null ? descrizioneField.getText().trim() : "";
        String intensitaText = intensitaField.getText() != null ? intensitaField.getText().trim() : "";

        if (descrizione.isEmpty() || intensitaText.isEmpty()) {
            showError("Compila descrizione e intensita.");
            return;
        }

        int intensita;
        try {
            intensita = Integer.parseInt(intensitaText);
        } catch (NumberFormatException e) {
            showError("Intensita deve essere un numero.");
            return;
        }

        try {
            Sintomo s = new Sintomo();
            s.setDescrizione(descrizione);
            s.setIntensita(intensita);
            s.setFrequenza(frequenzaField.getText());
            s.setNoteAggiuntive(noteField.getText());
            s.setFkPaziente(fkPaziente);
            s.setDataInizio(LocalDate.now());
            s.setDateStamp(LocalDateTime.now());

            sintomoDAO.insert(s);

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
        Stage stage = (Stage) descrizioneField.getScene().getWindow();
        stage.close();
    }

    private void showError(String message) {
        Alert alert = new Alert(Alert.AlertType.WARNING, message, ButtonType.OK);
        alert.setHeaderText("Dati incompleti");
        alert.showAndWait();
    }
}
