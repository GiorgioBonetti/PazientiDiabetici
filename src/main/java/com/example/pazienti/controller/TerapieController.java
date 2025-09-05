package com.example.pazienti.controller;

import javafx.fxml.FXML;
import javafx.scene.control.*;

public class TerapieController {
    @FXML private Label titleLabel;
    @FXML private Label subtitleLabel;

    @FXML private Button addButton;
    @FXML private Button editButton;
    @FXML private Button removeButton;

    @FXML private TableView<?> therapiesTable;
    @FXML private TextArea therapyDetailsArea;

    @FXML private void initialize() {
        // inizializzazione terapie
    }
}

