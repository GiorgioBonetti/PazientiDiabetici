package it.univr.diabete.controller;

import it.univr.diabete.model.Sintomo;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.stage.Stage;

import java.time.format.DateTimeFormatter;

public class SymptomDetailController {

    @FXML private Label descrizioneLabel;
    @FXML private Label intensitaLabel;
    @FXML private Label frequenzaLabel;
    @FXML private Label noteLabel;
    @FXML private Label periodoLabel;
    @FXML private Label timestampLabel;

    private final DateTimeFormatter dateFmt = DateTimeFormatter.ofPattern("dd/MM/yyyy");
    private final DateTimeFormatter timeFmt = DateTimeFormatter.ofPattern("HH:mm");

    public void setSymptom(Sintomo s) {
        descrizioneLabel.setText(valueOrDash(s.getDescrizione()));
        intensitaLabel.setText(valueOrDash(String.valueOf(s.getIntensita())));
        frequenzaLabel.setText(valueOrDash(s.getFrequenza()));
        noteLabel.setText(valueOrDash(s.getNoteAggiuntive()));

        String start = s.getDataInizio() != null ? dateFmt.format(s.getDataInizio()) : "—";
        String end = s.getDataFine() != null ? dateFmt.format(s.getDataFine()) : "—";
        periodoLabel.setText(start + " → " + end);

        String ts = s.getDateStamp() != null
                ? dateFmt.format(s.getDateStamp()) + " " + timeFmt.format(s.getDateStamp())
                : "—";
        timestampLabel.setText(ts);
    }

    @FXML
    private void handleClose() {
        Stage stage = (Stage) descrizioneLabel.getScene().getWindow();
        stage.close();
    }

    private String valueOrDash(String value) {
        return value == null || value.isBlank() ? "—" : value;
    }
}
