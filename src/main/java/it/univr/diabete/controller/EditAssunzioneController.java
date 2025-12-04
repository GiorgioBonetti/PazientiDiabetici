package it.univr.diabete.controller;

import it.univr.diabete.dao.AssunzioneTerapiaDAO;
import it.univr.diabete.dao.impl.AssunzioneTerapiaDAOImpl;
import it.univr.diabete.model.AssunzioneTerapia;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.stage.Stage;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;

public class EditAssunzioneController {

    @FXML private Label dataLabel;
    @FXML private TextField quantitaField;
    @FXML private DatePicker dataPicker;
    @FXML private TextField oraField;

    private AssunzioneTerapia assunzione;
    private Runnable reloadCallback;

    private final AssunzioneTerapiaDAO dao = new AssunzioneTerapiaDAOImpl();

    public void initData(AssunzioneTerapia a, Runnable callback) {
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
            int quantita = Integer.parseInt(quantitaField.getText());

            LocalDate d = dataPicker.getValue();
            LocalTime t = LocalTime.parse(oraField.getText());

            assunzione.setQuantitaAssunta(quantita);
            assunzione.setDateStamp(LocalDateTime.of(d, t));

            dao.update(assunzione);

            if (reloadCallback != null) reloadCallback.run();

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
        Stage s = (Stage) quantitaField.getScene().getWindow();
        s.close();
    }
}