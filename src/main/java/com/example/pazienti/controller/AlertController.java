package com.example.pazienti.controller;

import javafx.fxml.FXML;
import javafx.scene.control.*;

public class AlertController {
    @FXML private Label titleLabel;
    @FXML private Label subtitleLabel;

    @FXML private Button closeButton;
    @FXML private Button confirmButton;

    @FXML private TextField searchField;
    @FXML private TextArea descriptionArea;

    @FXML private TableView<?> tableView;
    @FXML private ListView<?> listView;

    @FXML private void initialize() {
        // inizializzazione controller
    }
}

