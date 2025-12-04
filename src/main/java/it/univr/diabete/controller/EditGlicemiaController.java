package it.univr.diabete.controller;

import it.univr.diabete.dao.GlicemiaDAO;
import it.univr.diabete.dao.impl.GlicemiaDAOImpl;
import it.univr.diabete.model.Glicemia;
import javafx.fxml.FXML;
import javafx.scene.control.*;
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

        dateLabel.setText("Registrata il: " + g.getDataOra().format(df));
        valueField.setText(String.valueOf(g.getValore()));

        momentChoice.getItems().addAll("Mattino", "Pranzo", "Cena");
        momentChoice.setValue(g.getMomento());
    }

    @FXML
    private void handleSave() {
        try {
            glicemia.setValore(Integer.parseInt(valueField.getText()));
            glicemia.setMomento(momentChoice.getValue());

            glicemiaDAO.update(glicemia);  // UPDATE nel DB

            if (refreshCallback != null)
                refreshCallback.run();

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
        Stage stage = (Stage) valueField.getScene().getWindow();
        stage.close();
    }
}