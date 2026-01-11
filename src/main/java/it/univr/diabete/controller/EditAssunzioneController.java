package it.univr.diabete.controller;

import it.univr.diabete.dao.AssunzioneDAO;
import it.univr.diabete.dao.impl.AssunzioneDAOImpl;
import it.univr.diabete.model.Assunzione;
import it.univr.diabete.ui.ErrorDialog;
import javafx.fxml.FXML;
import javafx.scene.control.DatePicker;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.stage.Stage;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;

public class EditAssunzioneController {

    @FXML private Label dataLabel;
    @FXML private TextField quantitaField;
    @FXML private DatePicker dataPicker;
    @FXML private TextField oraField;

    private Assunzione assunzione;
    private Runnable reloadCallback;

    private final AssunzioneDAO dao = new AssunzioneDAOImpl();

    public void initData(Assunzione a, Runnable callback) {
        this.assunzione = a;
        this.reloadCallback = callback;

        dataLabel.setText("Registrata il: " + a.getDateStamp().toString());

        quantitaField.setText(String.valueOf(a.getQuantitaAssunta()));
        dataPicker.setValue(a.getDateStamp().toLocalDate());
        oraField.setText(a.getDateStamp().toLocalTime().toString());
    }

    @FXML
    private void handleSave() {
        try {
            // --- VALIDAZIONI ---
            String quantitaText = quantitaField.getText() != null ? quantitaField.getText().trim() : "";
            if (quantitaText.isEmpty()) {
                ErrorDialog.show("Quantità mancante",
                        "Inserisci la quantità assunta.");
                return;
            }

            int quantita;
            try {
                quantita = Integer.parseInt(quantitaText);
                if (quantita <= 0) {
                    ErrorDialog.show("Quantità non valida",
                            "La quantità assunta deve essere > 0.");
                    return;
                }
            } catch (NumberFormatException e) {
                ErrorDialog.show("Quantità non valida",
                        "Inserisci un numero intero valido per la quantità.");
                return;
            }

            LocalDate data = dataPicker.getValue();
            if (data == null) {
                ErrorDialog.show("Data mancante",
                        "Seleziona la data dell'assunzione.");
                return;
            }

            String oraText = oraField.getText() != null ? oraField.getText().trim() : "";
            if (oraText.isEmpty()) {
                ErrorDialog.show("Ora mancante",
                        "Inserisci l'orario dell'assunzione.");
                return;
            }

            LocalTime ora;
            try {
                ora = LocalTime.parse(oraText);
            } catch (Exception e) {
                ErrorDialog.show("Ora non valida",
                        "Inserisci un orario valido (es: 14:30).");
                return;
            }

            // --- SALVATAGGIO ---
            assunzione.setQuantitaAssunta(quantita);
            assunzione.setDateStamp(LocalDateTime.of(data, ora));

            dao.update(assunzione);

            if (reloadCallback != null) reloadCallback.run();
            close();

        } catch (Exception e) {
            ErrorDialog.show("Errore di salvataggio",
                    "Impossibile aggiornare l'assunzione. Riprova.");
            e.printStackTrace();
        }
    }

    @FXML
    private void handleCancel() {
        close();
    }

    private void close() {
        Stage s = (Stage) quantitaField.getScene().getWindow();
        s.close();
    }
}
