package it.univr.diabete.controller;

import it.univr.diabete.dao.AssunzioneDAO;
import it.univr.diabete.dao.impl.AssunzioneDAOImpl;
import it.univr.diabete.model.Assunzione;
import javafx.fxml.FXML;
import javafx.scene.control.DatePicker;
import javafx.scene.control.TextField;
import javafx.stage.Stage;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;

public class AddAssunzioneController {

    @FXML private TextField quantitaField;
    @FXML private DatePicker dateField;

    private String pazienteId;
    private int fkTerapia;
    private int fkFarmaco;

    private Runnable onSave;

    private final AssunzioneDAO dao = new AssunzioneDAOImpl();

    public void initData(String pazienteId, int fkTerapia, int fkFarmaco, Runnable onSave) {
        this.pazienteId = pazienteId;
        this.fkTerapia = fkTerapia;
        this.fkFarmaco = fkFarmaco;
        this.onSave = onSave;

        dateField.setValue(LocalDate.now());
    }

    @FXML
    private void handleConfirm() {
        try {
            int q = Integer.parseInt(quantitaField.getText());
            LocalDate d = dateField.getValue();
            if (d == null) return;

            // controllo valore assunzione
            if (q <= 0) return;

            Assunzione a = new Assunzione();
            a.setQuantitaAssunta(q);
            a.setDateStamp(LocalDateTime.of(d, LocalTime.now()));
            a.setFkPaziente(pazienteId);
            a.setFkTerapia(fkTerapia);
            a.setFkFarmaco(fkFarmaco);

            dao.insert(a);

            if (onSave != null) onSave.run();
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
        Stage stage = (Stage) quantitaField.getScene().getWindow();
        stage.close();
    }
}
