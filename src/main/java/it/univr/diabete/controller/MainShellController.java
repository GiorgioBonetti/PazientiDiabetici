package it.univr.diabete.controller;

import it.univr.diabete.MainApp;
import it.univr.diabete.model.Paziente;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.StackPane;
import javafx.scene.Scene;
import javafx.stage.Stage;
import javafx.animation.FadeTransition;
import javafx.animation.PauseTransition;
import javafx.animation.SequentialTransition;
import javafx.util.Duration;
import java.io.IOException;

public class MainShellController {

    @FXML
    private Label globalAlertLabel;

    private SequentialTransition alertAnimation;
    @FXML
    private Label userInitialsLabel;

    @FXML
    private Label userRoleLabel;

    @FXML
    private Label roleBadge;

    @FXML
    private Label todayLabel;

    @FXML
    private Button dashboardButton;
    @FXML
    private Button FarmacoButton;

    @FXML
    private Button patientsButton;

    @FXML
    private Button measurementsButton;

    @FXML
    private Button therapyButton;

    @FXML
    private Button messagesButton;

    @FXML
    private Button reportsButton;

    @FXML
    private StackPane contentArea;

    /** Ruolo corrente (Paziente / Diabetologo) */
    private String role;

    /** Nome completo (es. "Luigi Bianchi") */
    private String userName;

    /** Id paziente (solo se role = Paziente, altrimenti null) */
    private Integer patientId;
    private Integer loggedUserId;
    /**
     * Versione "vecchia": solo ruolo + nome.
     * La lasciamo per compatibilità, e dentro chiamiamo quella a 3 parametri con patientId = null.
     */

    public void setUserData(String role, String userName) {
        setUserData(role, userName, null);
    }
    /**
     * Versione completa: ruolo + nome + id paziente (se è un paziente).
     */
    public void setUserData(String role, String userName, Integer userId) {
        this.role = role;
        this.userName = userName;
        this.loggedUserId = userId;

        // se è paziente salvo anche l'id paziente
        if ("Paziente".equalsIgnoreCase(role)) {
            this.patientId = userId;
        }

        String initials = getInitials(userName);
        userInitialsLabel.setText(initials);
        userRoleLabel.setText(role);
        roleBadge.setText(role.toUpperCase());

        configureSidebarForRole();
        loadDashboard();
    }


    private void configureSidebarForRole() {
        boolean isPatient = "Paziente".equalsIgnoreCase(role);


        if (isPatient) {
            dashboardButton.setText("Home");
            measurementsButton.setText("Misurazioni");
            therapyButton.setText("Terapia");
            messagesButton.setText("Messaggi");
            reportsButton.setText("Report");
            FarmacoButton.setVisible(false);
            FarmacoButton.setManaged(false);
        } else {
            dashboardButton.setText("Dashboard");
            measurementsButton.setText("Misurazioni");
            FarmacoButton.setText("Farmaco");
            messagesButton.setText("Messaggi");
            reportsButton.setVisible(false);
            reportsButton.setManaged(false);
            therapyButton.setVisible(false);
            therapyButton.setManaged(false);
        }
    }

    @FXML
    private void handleLogout() {
        try {
            Stage stage = (Stage) contentArea.getScene().getWindow();

            FXMLLoader loader = new FXMLLoader(
                    MainApp.class.getResource("/fxml/LoginView.fxml")
            );
            Parent root = loader.load();

            Scene scene = new Scene(root);
            scene.getStylesheets().add(
                    MainApp.class.getResource("/css/app.css").toExternalForm()
            );

            stage.setScene(scene);
            stage.show();

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private String getInitials(String fullName) {
        if (fullName == null || fullName.isBlank()) {
            return "--";
        }
        String[] parts = fullName.trim().split("\\s+");
        if (parts.length >= 2) {
            char first = Character.toUpperCase(parts[0].charAt(0));
            char last  = Character.toUpperCase(parts[parts.length - 1].charAt(0));
            return "" + first + last;
        } else {
            String p = parts[0].toUpperCase();
            return p.length() >= 2 ? p.substring(0, 2) : p;
        }
    }

    public void setUserRole(String role) {
        this.role = role;
    }

    /**
     * Carica la dashboard corretta in base al ruolo.
     */
    private void loadDashboard() {
        try {
            FXMLLoader loader;
            String fxml;

            if ("Paziente".equalsIgnoreCase(role)) {
                fxml = "/fxml/PatientDashboardView.fxml";
            } else {
                fxml = "/fxml/DoctorDashboardView.fxml";
            }

            loader = new FXMLLoader(MainApp.class.getResource(fxml));
            Parent dashboard = loader.load();

            if ("Paziente".equalsIgnoreCase(role)) {
                PatientDashboardController controller = loader.getController();
                controller.setPatientData(userName, patientId); // 👈 qui passeremo anche l’id
            } else {

                DoctorDashboardController ctrl = loader.getController();
                ctrl.setDoctorContext(loggedUserId);
            }

            contentArea.getChildren().setAll(dashboard);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @FXML
    private void handleDashboardNav() {
        loadDashboard();
    }

    @FXML
    private void handlePatientsNav() {
        if ("Paziente".equalsIgnoreCase(role)) {
            return;
        }
        loadDashboard();
    }
    @FXML
    private void handleMeasurementsNav() {
        try {
            if ("Paziente".equalsIgnoreCase(role)) {
                // 💙 Vista misurazioni lato paziente
                FXMLLoader loader = new FXMLLoader(
                        MainApp.class.getResource("/fxml/PatientMeasurementsView.fxml")
                );
                Parent view = loader.load();

                PatientMeasurementsController controller = loader.getController();
                if (patientId != null && userName != null) {
                    controller.setPatientContext(userName, patientId);
                }

                contentArea.getChildren().setAll(view);

            } else {
                // 💙 Vista misurazioni lato DIABETOLOGO (quella nuova con le card)
                FXMLLoader loader = new FXMLLoader(
                        MainApp.class.getResource("/fxml/DoctorMeasurementsView.fxml")
                );
                Parent view = loader.load();

                // se un domani vuoi filtrare per diabetologo:
                // DoctorMeasurementsController ctrl = loader.getController();
                // ctrl.setDoctorContext(idDiabetologoLoggato);

                contentArea.getChildren().setAll(view);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    @FXML
    public void handleTherapyNav() {
        try {
            FXMLLoader loader = new FXMLLoader(
                    MainApp.class.getResource("/fxml/PatientTherapyView.fxml")
            );
            Parent view = loader.load();

            PatientTherapyController controller = loader.getController();
            controller.setPatientContext(userName, patientId);
            if (role.equals("Diabetologo")) {
                controller.hideEditingTools();
            }else{
                controller.hideEditingToolsPat();
            }
            contentArea.getChildren().setAll(view);

        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    @FXML
    private void handleFarmacoNav() {
        openFarmacoView(); // senza parametri
    }
    private void openFarmacoView() {
        try {
            FXMLLoader loader = new FXMLLoader(MainApp.class.getResource("/fxml/FarmacoView.fxml"));
            Parent root = loader.load();
            contentArea.getChildren().setAll(root);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void openPatientMeasurements(String fullName, int patientId) {
        try {
            FXMLLoader loader = new FXMLLoader(
                    MainApp.class.getResource("/fxml/PatientMeasurementsView.fxml"));
            Parent view = loader.load();

            PatientMeasurementsController ctrl = loader.getController();
            ctrl.setPatientContext(fullName, patientId);
            if (role.equals("Diabetologo")) {
                ctrl.hideEditingTools();
            }
            contentArea.getChildren().setAll(view);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    public void openPatientReport(String fullName, int patientId) {
        try {
            FXMLLoader loader = new FXMLLoader(
                    MainApp.class.getResource("/fxml/PatientReportView.fxml"));
            Parent view = loader.load();

            PatientReportController ctrl = loader.getController();
            ctrl.setPatientContext(fullName, patientId);
            contentArea.getChildren().setAll(view);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void openPatientTherapy(String fullName, int patientId) {
        try {
            FXMLLoader loader = new FXMLLoader(
                    MainApp.class.getResource("/fxml/PatientTherapyView.fxml"));
            Parent view = loader.load();

            PatientTherapyController ctrl = loader.getController();
            ctrl.setPatientContext(fullName, patientId);
            if (role.equals("Diabetologo")) {
                ctrl.hideEditingTools();
            }else{
                ctrl.hideEditingToolsPat();
            }
            contentArea.getChildren().setAll(view);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    @FXML
    private void handleReportsNav() {
        try {
            if ("Paziente".equalsIgnoreCase(role)) {
                FXMLLoader loader = new FXMLLoader(
                        MainApp.class.getResource("/fxml/PatientReportView.fxml")
                );
                Parent view = loader.load();

                PatientReportController controller = loader.getController();
                // suppongo che hai già userName e patientId salvati nel MainShellController
                controller.setPatientContext(userName, patientId);

                contentArea.getChildren().setAll(view);

            } else {
                // in futuro: report per diabetologo
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    public void openPatientDetail(Paziente paziente) {
        try {
            FXMLLoader loader = new FXMLLoader(
                    MainApp.class.getResource("/fxml/DoctorPatientDetailView.fxml"));
            Parent view = loader.load();

            DoctorPatientDetailController controller = loader.getController();
            controller.setPatient(paziente);

            // Sostituisci "contentRoot" con il tuo BorderPane centrale
            contentArea.getChildren().setAll(view);

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public StackPane getContentArea() {
        return contentArea;
    }
    private void clearGlobalAlert() {
        if (globalAlertLabel == null) return;
        globalAlertLabel.setVisible(false);
        globalAlertLabel.setManaged(false);
        globalAlertLabel.setText("");
    }

    /** Alert verde (successo) nel top bar */
    public void showGlobalSuccess(String msg) {
        playAlert(msg, "#d1fae5", "#065f46");
    }

    public void showGlobalError(String msg) {
        playAlert(msg, "#fee2e2", "#991b1b");
    }
    private void playAlert(String message, String bgColor, String textColor) {
        // Se c'era un'animazione vecchia, la fermo
        if (alertAnimation != null) {
            alertAnimation.stop();
        }

        globalAlertLabel.setText(message);
        globalAlertLabel.setStyle(
                "-fx-font-size: 11px;" +
                        "-fx-padding: 6 12;" +
                        "-fx-background-radius: 999;" +
                        "-fx-text-fill: " + textColor + ";" +
                        "-fx-background-color: " + bgColor + ";"
        );
        globalAlertLabel.setVisible(true);
        globalAlertLabel.setManaged(true);
        globalAlertLabel.setOpacity(0);   // parto trasparente

        FadeTransition fadeIn = new FadeTransition(Duration.millis(200), globalAlertLabel);
        fadeIn.setFromValue(0);
        fadeIn.setToValue(1);

        PauseTransition stay = new PauseTransition(Duration.seconds(2.5));

        FadeTransition fadeOut = new FadeTransition(Duration.millis(300), globalAlertLabel);
        fadeOut.setFromValue(1);
        fadeOut.setToValue(0);

        fadeOut.setOnFinished(e -> {
            globalAlertLabel.setVisible(false);
            globalAlertLabel.setManaged(false);
        });

        alertAnimation = new SequentialTransition(fadeIn, stay, fadeOut);
        alertAnimation.play();
    }
}
