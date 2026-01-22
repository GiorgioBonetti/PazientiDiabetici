package it.univr.diabete.controller;

import it.univr.diabete.dao.AssunzioneDAO;
import it.univr.diabete.dao.impl.AssunzioneDAOImpl;
import it.univr.diabete.model.Assunzione;
import it.univr.diabete.ui.ErrorDialog;
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
    @FXML private TextField timeField;

    private String pazienteId;
    private int fkTerapia;
    private int fkFarmaco;
    private LocalDate dataInizioTerapia;

    private Runnable onSave;

    private final AssunzioneDAO dao = new AssunzioneDAOImpl();

    public void initData(String pazienteId, int fkTerapia, int fkFarmaco, Runnable onSave) {
        this.initData(pazienteId, fkTerapia, fkFarmaco, null, onSave);
    }

    public void initData(String pazienteId, int fkTerapia, int fkFarmaco, LocalDate dataInizioTerapia, Runnable onSave) {
        this.pazienteId = pazienteId;
        this.fkTerapia = fkTerapia;
        this.fkFarmaco = fkFarmaco;
        this.dataInizioTerapia = dataInizioTerapia;
        this.onSave = onSave;

        dateField.setValue(LocalDate.now());

        // Imposta l'orario corrente come default (HH:mm)
        LocalTime now = LocalTime.now();
        timeField.setText(String.format("%02d:%02d", now.getHour(), now.getMinute()));

        // Imposta la data minima se disponibile
        if (dataInizioTerapia != null) {
            dateField.setDayCellFactory(datePicker -> new javafx.scene.control.DateCell() {
                @Override
                public void updateItem(LocalDate item, boolean empty) {
                    super.updateItem(item, empty);
                    setDisable(empty || item.isBefore(dataInizioTerapia));
                }
            });
        }
    }

    @FXML
    private void handleConfirm() {
        try {
            int q = Integer.parseInt(quantitaField.getText());
            LocalDate d = dateField.getValue();
            if (d == null) {
                ErrorDialog.show("Data non valida",
                        "Seleziona una data valida per l'assunzione.");
                return;
            }

            // controllo valore assunzione
            if (q <= 0) {
                ErrorDialog.show("Valore non valido",
                        "Inserisci una quantità positiva per l'assunzione.");
                return;
            }

            // Validazione: la data non può essere prima dell'inizio della terapia
            if (dataInizioTerapia != null && d.isBefore(dataInizioTerapia)) {
                ErrorDialog.show("Data non valida",
                        "La data dell'assunzione non può essere prima dell'inizio della terapia (" + dataInizioTerapia + ").");
                return;
            }

            // Parse e validazione dell'orario
            LocalTime time;
            try {
                String timeStr = timeField.getText().trim();
                if (timeStr.isEmpty()) {
                    ErrorDialog.show("Orario non valido",
                            "Inserisci un orario nel formato HH:mm (es. 14:30).");
                    return;
                }
                time = LocalTime.parse(timeStr);
            } catch (Exception e) {
                ErrorDialog.show("Orario non valido",
                        "Inserisci un orario nel formato HH:mm (es. 14:30).");
                return;
            }

            Assunzione a = new Assunzione();
            a.setQuantitaAssunta(q);
            a.setDateStamp(LocalDateTime.of(d, time));
            a.setFkPaziente(pazienteId);
            a.setFkTerapia(fkTerapia);
            a.setFkFarmaco(fkFarmaco);

            dao.insert(a);

            if (onSave != null) onSave.run();
            close();

        } catch (NumberFormatException e) {
            ErrorDialog.show("Quantità non valida",
                    "Inserisci un numero valido per la quantità.");
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
