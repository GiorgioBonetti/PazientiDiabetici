package com.example.pazienti.controller.dottore;

import javafx.fxml.FXML;
import javafx.scene.control.*;

public class DashboardController {
    @FXML private Label titleLabel;
    @FXML private Label subtitleLabel;

    @FXML private Button primaryButton;
    @FXML private Button secondaryButton;

    @FXML private TextField searchField;
    @FXML private TextArea detailsArea;

    @FXML private TableView<?> tableView;
    @FXML private ListView<?> listView;

    @FXML private void initialize() {
        // inizializzazione
    }
}

