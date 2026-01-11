package it.univr.diabete.controller;

import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.stage.Stage;

public class ErrorDialogController {

    @FXML private Label titleLbl;
    @FXML private Label messageLbl;
    @FXML private Button okBtn;

    @FXML
    private void initialize() {
        okBtn.setOnAction(e -> close());
    }

    public void setTexts(String title, String msg) {
        titleLbl.setText(title);
        messageLbl.setText(msg);
    }

    private void close() {
        Stage stage = (Stage) okBtn.getScene().getWindow();
        stage.close();
    }
}
