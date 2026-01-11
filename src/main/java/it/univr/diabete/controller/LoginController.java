package it.univr.diabete.controller;

import it.univr.diabete.MainApp;
import it.univr.diabete.dao.PazienteDAO;
import it.univr.diabete.dao.impl.PazienteDAOImpl;
import it.univr.diabete.dao.DiabetologoDAO;
import it.univr.diabete.dao.impl.DiabetologoDAOImpl;
import it.univr.diabete.model.Paziente;
import it.univr.diabete.model.Diabetologo;
import it.univr.diabete.ui.ErrorDialog;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.stage.Stage;

public class LoginController {

    private final PazienteDAO pazienteDAO = new PazienteDAOImpl();
    private final DiabetologoDAO diabetologoDAO = new DiabetologoDAOImpl();

    @FXML private TextField emailField;
    @FXML private PasswordField passwordField;
    @FXML private Button loginButton;

    @FXML
    private void initialize() {
        if (loginButton != null) {
            loginButton.setDefaultButton(true);
        }
    }

    @FXML
    private void handleLogin() {
        String email = emailField.getText();
        String password = passwordField.getText();

        if (email == null || password == null || email.isBlank() || password.isBlank()) {
            ErrorDialog.show("Dati mancanti", "Inserisci email e password.");
            return;
        }

        try {
            if ("admin".equalsIgnoreCase(email.trim()) && "admin".equals(password.trim())) {
                openShell("Admin", "Admin", "admin");
                return;
            }

            Paziente paziente = pazienteDAO.findByEmailAndPassword(email, password);
            Diabetologo diabetologo;
            String role;
            String displayName;
            String userId;

            if (paziente != null) {
                role = "Paziente";
                displayName = paziente.getNome() + " " + paziente.getCognome();
                userId = paziente.getCodiceFiscale();
            } else {
                diabetologo = diabetologoDAO.findByEmailAndPassword(email, password);
                if (diabetologo != null) {
                    role = "Diabetologo";
                    displayName = diabetologo.getNome() + " " + diabetologo.getCognome();
                    userId = diabetologo.getEmail();
                } else {
                    ErrorDialog.show("Credenziali errate",
                            "Email o password non validi. Riprova.");
                    return;
                }
            }

            openShell(role, displayName, userId);

        } catch (Exception ex) {
            ErrorDialog.show("Errore di connessione",
                    "Impossibile connettersi al database. Riprova.");
            ex.printStackTrace();
        }
    }

    private void openShell(String role, String displayName, String userId) throws Exception {
        FXMLLoader loader = new FXMLLoader(MainApp.class.getResource("/fxml/MainShell.fxml"));
        Parent root = loader.load();

        MainShellController shellController = loader.getController();
        MainApp.setMainShellController(shellController);
        shellController.setUserData(role, displayName, userId);

        Stage stage = (Stage) emailField.getScene().getWindow();
        Scene scene = new Scene(root);
        scene.getStylesheets().add(
                MainApp.class.getResource("/css/app.css").toExternalForm()
        );
        stage.setScene(scene);
        stage.centerOnScreen();
        stage.show();
    }
}
