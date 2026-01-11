package it.univr.diabete.controller;

import it.univr.diabete.dao.GlicemiaDAO;
import it.univr.diabete.dao.impl.GlicemiaDAOImpl;
import it.univr.diabete.model.Glicemia;
import it.univr.diabete.ui.ErrorDialog;  // ← IMPORT Aggiunto
import javafx.fxml.FXML;
import javafx.scene.control.ComboBox;
import javafx.scene.control.TextField;
import javafx.stage.Stage;

import java.time.LocalDateTime;

public class AddGlicemiaController {

    @FXML private TextField valoreField;
    @FXML private ComboBox<String> momentoChoice;

    private String codiceFiscale;
    private Runnable callbackRicarica;

    private final GlicemiaDAO glicemiaDAO = new GlicemiaDAOImpl();

    @FXML
    private void initialize() {
        momentoChoice.getItems().setAll(
                "Mattina",
                "Pomeriggio",
                "Sera"
        );
        momentoChoice.getSelectionModel().selectFirst();
    }

    public void initData(String codiceFiscale, Runnable callback) {
        this.codiceFiscale = codiceFiscale;
        this.callbackRicarica = callback;
    }

    @FXML
    private void handleSave() {
        try {
            // --- valida valore numerico ---
            String text = valoreField.getText();
            if (text == null || text.isBlank()) {
                ErrorDialog.show("Campo vuoto", "Inserisci il valore della glicemia.");
                return;
            }

            int valore;
            try {
                valore = Integer.parseInt(text.trim());
            } catch (NumberFormatException ex) {
                ErrorDialog.show("Valore non valido", "Inserisci un numero valido per la glicemia.");
                return;
            }

            // Valore negativo o fuori range
            if (valore < 40 || valore > 400) {
                ErrorDialog.show("Valore fuori range",
                        "Il valore della glicemia deve essere tra 40 e 400 mg/dL.");
                return;
            }

            // --- valida momento ---
            String momento = momentoChoice.getValue();
            if (momento == null || momento.isBlank()) {
                ErrorDialog.show("Momento non selezionato",
                        "Seleziona il momento della giornata.");
                return;
            }

            // --- crea e salva la misurazione ---
            Glicemia g = new Glicemia();
            g.setValore(valore);
            g.setParteGiorno(momento);
            g.setFkPaziente(codiceFiscale);
            g.setDateStamp(LocalDateTime.now());

            glicemiaDAO.insert(g);

            if (callbackRicarica != null) {
                callbackRicarica.run();
            }

            close();

        } catch (Exception e) {
            ErrorDialog.show("Errore di salvataggio",
                    "Impossibile salvare la misurazione. Riprova.");
            e.printStackTrace();
        }
    }

    @FXML
    private void handleCancel() {
        close();
    }

    private void close() {
        Stage stage = (Stage) valoreField.getScene().getWindow();
        stage.close();
    }
}
