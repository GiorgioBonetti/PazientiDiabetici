package it.univr.diabete.controller;

import it.univr.diabete.dao.PatologiaDAO;
import it.univr.diabete.dao.impl.PatologiaDAOImpl;
import it.univr.diabete.model.Patologia;
import it.univr.diabete.ui.ErrorDialog;
import javafx.fxml.FXML;
import javafx.scene.control.DatePicker;
import javafx.scene.control.TextField;
import javafx.stage.Stage;

import java.time.LocalDate;

public class AddPatologiaController {

    @FXML private TextField nomeField;
    @FXML private DatePicker dataInizioPicker;

    private final PatologiaDAO patologiaDAO = new PatologiaDAOImpl();
    private String fkPaziente;
    private Runnable onSavedCallback;

    public void initData(String fkPaziente, Runnable callback) {
        this.fkPaziente = fkPaziente;
        this.onSavedCallback = callback;
        dataInizioPicker.setValue(LocalDate.now());
    }

    @FXML
    private void handleSave() {
        String nome = nomeField.getText() != null ? nomeField.getText().trim() : "";
        if (nome.isEmpty()) {
            ErrorDialog.show("Nome mancante", "Inserisci il nome della patologia.");
            return;
        }

        try {
            if (patologiaDAO.existsByPazienteAndNome(fkPaziente, nome)) {
                ErrorDialog.show("Patologia duplicata",
                        "Questa patologia è già presente per questo paziente.");
                return;
            }

            if (dataInizioPicker.getValue() == null) {
                ErrorDialog.show("Data mancante",
                        "Seleziona la data di inizio della patologia.");
                return;
            }

            Patologia p = new Patologia();
            p.setNome(nome);
            p.setFkPaziente(fkPaziente);
            p.setDataInizio(dataInizioPicker.getValue());

            patologiaDAO.insert(p);
            if (onSavedCallback != null) {
                onSavedCallback.run();
            }
            close();
        } catch (Exception e) {
            ErrorDialog.show("Errore di salvataggio",
                    "Impossibile salvare la patologia. Riprova.");
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
