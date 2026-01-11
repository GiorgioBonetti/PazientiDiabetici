package it.univr.diabete.controller;

import it.univr.diabete.dao.SintomoDAO;
import it.univr.diabete.dao.impl.SintomoDAOImpl;
import it.univr.diabete.model.Sintomo;
import it.univr.diabete.ui.ErrorDialog;
import javafx.fxml.FXML;
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

        if (descrizione.isEmpty()) {
            ErrorDialog.show("Descrizione mancante", "Inserisci la descrizione del sintomo.");
            return;
        }

        if (intensitaText.isEmpty()) {
            ErrorDialog.show("Intensità mancante", "Inserisci l'intensità del sintomo.");
            return;
        }

        int intensita;
        try {
            intensita = Integer.parseInt(intensitaText);
            if (intensita < 1 || intensita > 10) {
                ErrorDialog.show("Intensità non valida",
                        "L'intensità deve essere un numero tra 1 e 10.");
                return;
            }
        } catch (NumberFormatException e) {
            ErrorDialog.show("Intensità non valida",
                    "L'intensità deve essere un numero valido (1-10).");
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
            ErrorDialog.show("Errore di salvataggio",
                    "Impossibile salvare il sintomo. Riprova.");
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
}
