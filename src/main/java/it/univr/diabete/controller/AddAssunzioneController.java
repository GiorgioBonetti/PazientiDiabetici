package it.univr.diabete.controller;

import it.univr.diabete.dao.AssunzioneTerapiaDAO;
import it.univr.diabete.dao.impl.AssunzioneTerapiaDAOImpl;
import it.univr.diabete.model.AssunzioneTerapia;
import javafx.fxml.FXML;
import javafx.scene.control.DatePicker;
import javafx.scene.control.TextField;
import javafx.stage.Stage;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;

public class AddAssunzioneController {

    @FXML
    private TextField quantitaField;

    @FXML
    private DatePicker dateField;

    private int pazienteId;
    private int terapiaId;

    private Runnable onSave;

    private final AssunzioneTerapiaDAO dao = new AssunzioneTerapiaDAOImpl();

    public void initData(int pazienteId, int terapiaId, Runnable onSave) {
        this.pazienteId = pazienteId;
        this.terapiaId = terapiaId;
        this.onSave = onSave;

        dateField.setValue(LocalDate.now()); // default oggi
    }

    @FXML
    private void handleConfirm() {
        try {
            int q = Integer.parseInt(quantitaField.getText());
            LocalDate d = dateField.getValue();

            AssunzioneTerapia a = new AssunzioneTerapia();
            a.setQuantitaAssunta(q);
            a.setDateStamp(LocalDateTime.of(d, LocalTime.now()));
            a.setIdPaziente(pazienteId);
            a.setIdTerapia(terapiaId);

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