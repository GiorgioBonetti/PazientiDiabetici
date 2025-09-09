package com.example.pazienti.controller;

import javafx.fxml.FXML;
import javafx.scene.control.*;

public class LoginController {
    @FXML private Label titleLabel;
    @FXML private TextField usernameField;
    @FXML private PasswordField passwordField;
    @FXML private Button loginButton;

    @FXML private void initialize() {
        // inizializzazione login
    }

    @FXML private void handleLogin() {
        String username = usernameField.getText();
        String password = passwordField.getText();
        // gestione login DB
    }
}

