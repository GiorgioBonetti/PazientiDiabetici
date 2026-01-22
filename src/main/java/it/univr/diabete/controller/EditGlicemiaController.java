package it.univr.diabete.controller;

import it.univr.diabete.dao.GlicemiaDAO;
import it.univr.diabete.dao.impl.GlicemiaDAOImpl;
import it.univr.diabete.model.Glicemia;
import it.univr.diabete.ui.ErrorDialog;
import javafx.fxml.FXML;
import javafx.scene.control.ChoiceBox;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.stage.Stage;

import java.time.format.DateTimeFormatter;

public class EditGlicemiaController {

    @FXML private Label dateLabel;
    @FXML private TextField valueField;
    @FXML private ChoiceBox<String> momentChoice;

    private Glicemia glicemia;
    private Runnable refreshCallback;

    private final GlicemiaDAO glicemiaDAO = new GlicemiaDAOImpl();
    private final DateTimeFormatter df = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");

    public void init(Glicemia g, Runnable refresh) {
        this.glicemia = g;
        this.refreshCallback = refresh;

        dateLabel.setText(g.getDateStamp().format(df));
        valueField.setText(String.valueOf(g.getValore()));

        momentChoice.getItems().addAll("Mattina",
                "Pomeriggio",
                "Sera");
        momentChoice.setValue(g.getParteGiorno());
    }

    @FXML
    private void handleSave() {
        try {
            // --- VALIDAZIONI ---
            String text = valueField.getText();
            if (text == null || text.trim().isEmpty()) {
                ErrorDialog.show("Valore mancante",
                        "Inserisci il valore della glicemia.");
                return;
            }

            int valore;
            try {
                valore = Integer.parseInt(text.trim());
            } catch (NumberFormatException ex) {
                ErrorDialog.show("Valore non valido",
                        "Inserisci un numero intero valido.");
                return;
            }

            if (valore < 40 || valore > 400) {
                ErrorDialog.show("Valore fuori range",
                        "Il valore della glicemia deve essere tra 40 e 400 mg/dL.");
                return;
            }

            glicemia.setValore(valore);

            // --- valida momento ---
            String momento = momentChoice.getValue();
            if (momento == null || momento.isBlank()) {
                ErrorDialog.show("Momento mancante",
                        "Seleziona il momento della giornata.");
                return;
            }

            glicemia.setParteGiorno(momento);

            // --- SALVATAGGIO ---
            glicemiaDAO.update(glicemia);

            if (refreshCallback != null)
                refreshCallback.run();

            close();

        } catch (Exception e) {
            ErrorDialog.show("Errore di salvataggio",
                    "Impossibile aggiornare la misurazione. Riprova.");
            e.printStackTrace();
        }
    }

    @FXML
    private void handleCancel() {
        close();
    }

    private void close() {
        Stage stage = (Stage) valueField.getScene().getWindow();
        stage.close();
    }
}
