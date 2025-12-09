package it.univr.diabete.controller;

import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.stage.Stage;

public class ConfirmDialogController {

    @FXML private Label titleLbl;
    @FXML private Label messageLbl;
    @FXML private Button okBtn;
    @FXML private Button cancelBtn;

    private boolean confirmed = false;

    @FXML
    private void initialize() {
        okBtn.setOnAction(e -> {
            confirmed = true;
            close();
        });
        cancelBtn.setOnAction(e -> close());
    }

    public void setTexts(String title, String msg) {
        titleLbl.setText(title);
        messageLbl.setText(msg);
    }

    public boolean isConfirmed() {
        return confirmed;
    }

    private void close() {
        Stage stage = (Stage) okBtn.getScene().getWindow();
        stage.close();
    }
}