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

    @FXML
    private TextField quantitaField;

    @FXML
    private DatePicker dateField;

    private String pazienteId;
    // 👇 adesso memorizziamo l'id della TerapiaFarmaco
    private int terapiaFarmacoId;

    private Runnable onSave;

    private final AssunzioneDAO dao = new AssunzioneDAOImpl();

    /**
     * @param pazienteId        id del paziente
     * @param terapiaFarmacoId  id della riga TerapiaFarmaco
     */
    public void initData(String pazienteId, int terapiaFarmacoId, Runnable onSave) {
        this.pazienteId = pazienteId;
        this.terapiaFarmacoId = terapiaFarmacoId;
        this.onSave = onSave;

        dateField.setValue(LocalDate.now()); // default oggi
    }

    @FXML
    private void handleConfirm() {
        try {
            int q = Integer.parseInt(quantitaField.getText());
            LocalDate d = dateField.getValue();
            if (d == null) {
                // se vuoi puoi mostrare un alert
                return;
            }

            Assunzione a = new Assunzione();
            a.setQuantitaAssunta(q);
            a.setDateStamp(LocalDateTime.of(d, LocalTime.now()));
            a.setFkPaziente(pazienteId);
            a.setFkTerapia(terapiaFarmacoId);

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