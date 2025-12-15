package it.univr.diabete.controller;

import it.univr.diabete.dao.GlicemiaDAO;
import it.univr.diabete.dao.impl.GlicemiaDAOImpl;
import it.univr.diabete.model.Glicemia;
import javafx.fxml.FXML;
import javafx.scene.control.ComboBox;
import javafx.scene.control.TextField;
import javafx.stage.Stage;

import java.time.LocalDateTime;

public class AddGlicemiaController {

    @FXML
    private TextField valoreField;          // campo testo per il valore

    @FXML
    private ComboBox<String> momentoChoice; // mattino / pranzo / ecc.

    private String codiceFiscale;
    private Runnable callbackRicarica;

    private final GlicemiaDAO glicemiaDAO = new GlicemiaDAOImpl();

    @FXML
    private void initialize() {
        momentoChoice.getItems().setAll(
                "Mattino",
                "Pranzo",
                "Cena"
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
                // qui se vuoi puoi mostrare un label di errore
                return;
            }

            int valore;
            try {
                valore = Integer.parseInt(text.trim());
            } catch (NumberFormatException ex) {
                // valore non numerico
                return;
            }

            // opzionale: limiti come nello spinner (40–400)
            if (valore < 40 || valore > 400) {
                return;
            }

            // --- valida momento ---
            String momento = momentoChoice.getValue();
            if (momento == null || momento.isBlank()) {
                return;
            }

            // --- crea e salva la misurazione ---
            Glicemia g = new Glicemia();
            g.setValore(valore);
            g.setMomento(momento);           // <-- nuovo campo
            g.setIdPaziente(codiceFiscale);
            g.setDataOra(LocalDateTime.now());

            glicemiaDAO.insert(g);

            if (callbackRicarica != null) {
                callbackRicarica.run();      // ricarica tabella + grafico
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
        Stage stage = (Stage) valoreField.getScene().getWindow();
        stage.close();
    }
}