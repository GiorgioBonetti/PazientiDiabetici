package it.univr.diabete.controller;

import it.univr.diabete.MainApp;
import it.univr.diabete.dao.PazienteDAO;
import it.univr.diabete.dao.impl.PazienteDAOImpl;
import it.univr.diabete.model.Notification;
import it.univr.diabete.model.Paziente;
import it.univr.diabete.service.NotificationService;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.control.Button;
import javafx.scene.control.ContextMenu;
import javafx.scene.control.CustomMenuItem;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.Separator;
import javafx.geometry.Side;
import javafx.geometry.Bounds;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import javafx.scene.Scene;
import javafx.stage.Stage;
import javafx.animation.FadeTransition;
import javafx.animation.PauseTransition;
import javafx.animation.SequentialTransition;
import javafx.util.Duration;

import java.io.IOException;
import java.time.format.DateTimeFormatter;
import java.util.List;

public class MainShellController {

    @FXML private Label globalAlertLabel;

    private SequentialTransition alertAnimation;

    @FXML private Label userInitialsLabel;
    @FXML private Label notificationsBadgeLabel;
    @FXML private Button notificationsButton;
    // role labels removed from UI

    @FXML private Button dashboardButton;
    @FXML private Button FarmacoButton;
    @FXML private Button measurementsButton;
    @FXML private Button therapyButton;
    @FXML private Button messagesButton;
    @FXML private Button reportsButton;

    @FXML private StackPane contentArea;

    /** Ruolo corrente (Paziente / Diabetologo) */
    private String role;

    /** Nome completo (es. "Luigi Bianchi") */
    private String userName;

    /** Se role = Paziente, questo è il CF del paziente loggato. Altrimenti null. */
    private String codiceFiscale;

    /** ID utente loggato: per Paziente = CF, per Diabetologo = email */
    private String loggedUserId;

    private Button activeNavButton;

    private final PazienteDAO pazienteDAO = new PazienteDAOImpl();
    private final NotificationService notificationService = new NotificationService();

    private ContextMenu notificationsMenu;

    private static final DateTimeFormatter NOTIF_TIME_FORMATTER =
            DateTimeFormatter.ofPattern("dd/MM HH:mm");

    public String getRole() { return role; }
    public String getUserName() { return userName; }
    public String getCodiceFiscale() { return codiceFiscale; }
    public String getLoggedUserId() { return loggedUserId; }

    @FXML
    private void initialize() {
        if (notificationsBadgeLabel != null) {
            notificationsBadgeLabel.setVisible(false);
            notificationsBadgeLabel.setManaged(false);
        }
    }

    // compatibilità
    public void setUserData(String role, String userName) {
        setUserData(role, userName, null);
    }

    /**
     * Versione completa:
     * - role = "Paziente"  -> userId = codice fiscale
     * - role = "Diabetologo" -> userId = email
     */
    public void setUserData(String role, String userName, String userId) {
        this.role = role;
        this.userName = userName;
        this.loggedUserId = userId;

        if ("Paziente".equalsIgnoreCase(role)) {
            this.codiceFiscale = userId; // userId è CF
        } else {
            this.codiceFiscale = null;   // diabetologo non ha CF paziente
        }

        String initials = getInitials(userName);
        userInitialsLabel.setText(initials);

        configureSidebarForRole();
        loadDashboard();
        refreshNotifications();
    }

    private void configureSidebarForRole() {
        boolean isPatient = "Paziente".equalsIgnoreCase(role);
        boolean isAdmin = "Admin".equalsIgnoreCase(role);

        if (isAdmin) {
            dashboardButton.setText("Pazienti");
            measurementsButton.setText("Diabetologi");

            therapyButton.setVisible(false);
            therapyButton.setManaged(false);
            messagesButton.setVisible(false);
            messagesButton.setManaged(false);
            reportsButton.setVisible(false);
            reportsButton.setManaged(false);
            FarmacoButton.setVisible(false);
            FarmacoButton.setManaged(false);
        } else if (isPatient) {
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

    private void setActiveNav(Button btn) {
        if (activeNavButton != null) {
            activeNavButton.getStyleClass().remove("nav-item-selected");
        }
        if (btn != null && !btn.getStyleClass().contains("nav-item-selected")) {
            btn.getStyleClass().add("nav-item-selected");
        }
        activeNavButton = btn;
    }

    @FXML
    private void handleLogout() {
        try {
            Stage stage = (Stage) contentArea.getScene().getWindow();

            FXMLLoader loader = new FXMLLoader(
                    MainApp.class.getResource("/fxml/LoginView.fxml")
            );
            Parent root = loader.load();

            Scene scene = new Scene(root, 1150, 850);
            scene.getStylesheets().add(
                    MainApp.class.getResource("/css/app.css").toExternalForm()
            );

            stage.setScene(scene);
            stage.setWidth(1150);
            stage.setHeight(850);
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

    private void loadDashboard() {
        try {
            FXMLLoader loader;
            String fxml;

            if ("Paziente".equalsIgnoreCase(role)) {
                refreshPatientContext();
                fxml = "/fxml/PatientDashboardView.fxml";
            } else if ("Admin".equalsIgnoreCase(role)) {
                fxml = "/fxml/AdminPatientsView.fxml";
            } else {
                fxml = "/fxml/DoctorDashboardView.fxml";
            }

            loader = new FXMLLoader(MainApp.class.getResource(fxml));
            Parent dashboard = loader.load();

            if ("Paziente".equalsIgnoreCase(role)) {
                PatientDashboardController controller = loader.getController();
                controller.setPatientData(userName, codiceFiscale);
            } else if ("Diabetologo".equalsIgnoreCase(role)) {
                DoctorDashboardController ctrl = loader.getController();
                // loggedUserId per diabetologo = email
                ctrl.setDoctorContext(loggedUserId);
            }

            contentArea.getChildren().setAll(dashboard);
            setActiveNav(dashboardButton);
            refreshNotifications();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @FXML
    private void handleDashboardNav() {
        setActiveNav(dashboardButton);
        loadDashboard();
    }

    @FXML
    private void handlePatientsNav() {
        if ("Paziente".equalsIgnoreCase(role)) return;
        setActiveNav(dashboardButton);
        loadDashboard();
    }

    @FXML
    private void handleMeasurementsNav() {
        try {
            if ("Paziente".equalsIgnoreCase(role)) {
                refreshPatientContext();
                FXMLLoader loader = new FXMLLoader(
                        MainApp.class.getResource("/fxml/PatientMeasurementsView.fxml")
                );
                Parent view = loader.load();

                PatientMeasurementsController controller = loader.getController();
                if (codiceFiscale != null && userName != null) {
                    controller.setPatientContext(userName, codiceFiscale);
                }

                contentArea.getChildren().setAll(view);
                setActiveNav(measurementsButton);


            } else if ("Admin".equalsIgnoreCase(role)) {
                FXMLLoader loader = new FXMLLoader(
                        MainApp.class.getResource("/fxml/AdminDoctorsView.fxml")
                );
                Parent view = loader.load();
                contentArea.getChildren().setAll(view);
                setActiveNav(measurementsButton);

            } else {
                FXMLLoader loader = new FXMLLoader(
                        MainApp.class.getResource("/fxml/DoctorMeasurementsView.fxml")
                );
                Parent view = loader.load();
                DoctorMeasurementsController controller = loader.getController();
                controller.setDoctorContext(loggedUserId);

                contentArea.getChildren().setAll(view);
                setActiveNav(measurementsButton);

            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @FXML
    public void handleTherapyNav() {
        try {
            if ("Paziente".equalsIgnoreCase(role)) {
                refreshPatientContext();
            }
            FXMLLoader loader = new FXMLLoader(
                    MainApp.class.getResource("/fxml/PatientTherapyView.fxml")
            );
            Parent view = loader.load();

            PatientTherapyController controller = loader.getController();
            controller.setPatientContext(userName, codiceFiscale);

            if ("Diabetologo".equalsIgnoreCase(role)) {
                controller.hideEditingTools();
            } else {
                controller.hideEditingToolsPat();
            }

            setActiveNav(therapyButton);
            contentArea.getChildren().setAll(view);

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @FXML
    private void handleFarmacoNav() {
        setActiveNav(FarmacoButton);
        openFarmacoView();
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

    public void openPatientMeasurements(String fullName, String codiceFiscale) {
        try {
            FXMLLoader loader = new FXMLLoader(
                    MainApp.class.getResource("/fxml/PatientMeasurementsView.fxml"));
            Parent view = loader.load();

            PatientMeasurementsController ctrl = loader.getController();
            ctrl.setPatientContext(fullName, codiceFiscale);

            if ("Diabetologo".equalsIgnoreCase(role)) {
                ctrl.hideEditingTools();
            }
            contentArea.getChildren().setAll(view);

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void openPatientReport(String fullName, String codiceFiscale) {
        try {
            FXMLLoader loader = new FXMLLoader(
                    MainApp.class.getResource("/fxml/PatientReportView.fxml"));
            Parent view = loader.load();

            PatientReportController ctrl = loader.getController();
            ctrl.setPatientContext(fullName, codiceFiscale);
            contentArea.getChildren().setAll(view);

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void openPatientTherapy(String fullName, String codiceFiscale) {
        try {
            FXMLLoader loader = new FXMLLoader(
                    MainApp.class.getResource("/fxml/PatientTherapyView.fxml"));
            Parent view = loader.load();

            PatientTherapyController ctrl = loader.getController();
            ctrl.setPatientContext(fullName, codiceFiscale);

            if ("Diabetologo".equalsIgnoreCase(role)) {
                ctrl.hideEditingTools();
            } else {
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
                refreshPatientContext();
                FXMLLoader loader = new FXMLLoader(
                        MainApp.class.getResource("/fxml/PatientReportView.fxml")
                );
                Parent view = loader.load();

                PatientReportController controller = loader.getController();
                controller.setPatientContext(userName, codiceFiscale);

                contentArea.getChildren().setAll(view);
                setActiveNav(reportsButton);

            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @FXML
    private void handleMessagesNav() {
        try {
            if ("Paziente".equalsIgnoreCase(role)) {
                refreshPatientContext();
                FXMLLoader loader = new FXMLLoader(
                        MainApp.class.getResource("/fxml/PatientMessagesView.fxml")
                );
                Parent view = loader.load();
                PatientMessagesController ctrl = loader.getController();
                if (codiceFiscale != null) {
                    ctrl.setPatientContext(codiceFiscale);
                }
                contentArea.getChildren().setAll(view);
                setActiveNav(messagesButton);

            } else if ("Diabetologo".equalsIgnoreCase(role)) {
                FXMLLoader loader = new FXMLLoader(
                        MainApp.class.getResource("/fxml/DoctorMessagesView.fxml")
                );
                Parent view = loader.load();
                DoctorMessagesController ctrl = loader.getController();
                ctrl.setDoctorContext(loggedUserId);
                contentArea.getChildren().setAll(view);
                setActiveNav(messagesButton);
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

            contentArea.getChildren().setAll(view);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public StackPane getContentArea() {
        return contentArea;
    }

    @FXML
    private void handleNotificationsClick() {
        if (notificationsMenu != null && notificationsMenu.isShowing()) {
            notificationsMenu.hide();
            return;
        }
        refreshNotifications();
        showNotificationsMenu();
    }

    public void refreshNotifications() {
        if (role == null || loggedUserId == null) {
            updateNotificationBadge(0);
            return;
        }
        try {
            int unread = notificationService.countUnread(role, loggedUserId);
            updateNotificationBadge(unread);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void updateNotificationBadge(int count) {
        if (notificationsBadgeLabel == null) {
            return;
        }
        if (count <= 0) {
            notificationsBadgeLabel.setVisible(false);
            notificationsBadgeLabel.setManaged(false);
            return;
        }
        notificationsBadgeLabel.setText(String.valueOf(count));
        notificationsBadgeLabel.setVisible(true);
        notificationsBadgeLabel.setManaged(true);
    }

    private void showNotificationsMenu() {
        if (notificationsButton == null) {
            return;
        }
        List<Notification> notifications;
        try {
            notifications = notificationService.fetchLatest(role, loggedUserId, 30);
        } catch (Exception e) {
            e.printStackTrace();
            return;
        }

        VBox panel = new VBox(10);
        panel.getStyleClass().add("notification-panel");

        Label title = new Label("Notifiche");
        title.getStyleClass().add("notification-panel-title");
        panel.getChildren().add(title);

        VBox list = new VBox(8);
        list.getStyleClass().add("notification-list");

        if (notifications.isEmpty()) {
            Label empty = new Label("Nessuna notifica recente.");
            empty.getStyleClass().add("notification-empty");
            list.getChildren().add(empty);
        } else {
            for (Notification n : notifications) {
                list.getChildren().add(buildNotificationItem(n));
            }
        }

        ScrollPane scroll = new ScrollPane(list);
        scroll.setFitToWidth(true);
        scroll.setPrefViewportHeight(320);
        scroll.setHbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);
        scroll.getStyleClass().add("notification-scroll");

        panel.getChildren().addAll(new Separator(), scroll);

        CustomMenuItem panelItem = new CustomMenuItem(panel, false);
        panelItem.setHideOnClick(false);

        notificationsMenu = new ContextMenu();
        notificationsMenu.setAutoHide(true);
        notificationsMenu.getItems().setAll(panelItem);

        Bounds bounds = notificationsButton.localToScreen(notificationsButton.getBoundsInLocal());
        double panelWidth = 340;
        double x = bounds.getMaxX() - panelWidth;
        double y = bounds.getMaxY() + 6;
        notificationsMenu.show(notificationsButton, x, y);
    }

    private VBox buildNotificationItem(Notification n) {
        VBox root = new VBox(6);
        root.getStyleClass().add("notification-item");
        if (!n.isRead()) {
            root.getStyleClass().add("notification-item-unread");
        }

        HBox header = new HBox(8);
        Label title = new Label(n.getTitle() != null ? n.getTitle() : "Notifica");
        title.getStyleClass().add("notification-title");

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        Label severity = new Label(n.getSeverity() != null ? n.getSeverity() : "INFO");
        severity.getStyleClass().add("notification-severity");
        severity.getStyleClass().add("notification-severity-" + severity.getText().toLowerCase());

        header.getChildren().addAll(title, spacer, severity);

        Label body = new Label(n.getBody() != null ? n.getBody() : "");
        body.setWrapText(true);
        body.getStyleClass().add("notification-body");

        String timeText = n.getCreatedAt() != null ? NOTIF_TIME_FORMATTER.format(n.getCreatedAt()) : "";
        Label time = new Label(timeText);
        time.getStyleClass().add("notification-time");

        root.getChildren().addAll(header, body, time);

        root.setOnMouseClicked(e -> {
            try {
                notificationService.markAsRead(n.getId());
            } catch (Exception ex) {
                ex.printStackTrace();
            }
            refreshNotifications();
            if (notificationsMenu != null) {
                notificationsMenu.hide();
            }
            handleNotificationAction(n);
        });

        return root;
    }

    private void handleNotificationAction(Notification n) {
        if (n == null || n.getActionType() == null) {
            return;
        }
        String action = n.getActionType();
        String ref = n.getActionRefId();
        try {
            switch (action) {
                case NotificationService.ACTION_OPEN_MEASUREMENTS -> {
                    if ("Paziente".equalsIgnoreCase(role)) {
                        handleMeasurementsNav();
                    } else if (ref != null) {
                        Paziente p = pazienteDAO.findById(ref);
                        if (p != null) {
                            openPatientMeasurements(p.getNome() + " " + p.getCognome(), p.getCodiceFiscale());
                        }
                    }
                }
                case NotificationService.ACTION_OPEN_THERAPY -> {
                    if ("Paziente".equalsIgnoreCase(role)) {
                        handleTherapyNav();
                    } else if (ref != null) {
                        Paziente p = pazienteDAO.findById(ref);
                        if (p != null) {
                            openPatientTherapy(p.getNome() + " " + p.getCognome(), p.getCodiceFiscale());
                        }
                    }
                }
                case NotificationService.ACTION_OPEN_PATIENT -> {
                    if (ref != null) {
                        Paziente p = pazienteDAO.findById(ref);
                        if (p != null) {
                            openPatientDetail(p);
                        }
                    }
                }
                case NotificationService.ACTION_OPEN_REPORT -> {
                    if ("Paziente".equalsIgnoreCase(role)) {
                        handleReportsNav();
                    } else if (ref != null) {
                        Paziente p = pazienteDAO.findById(ref);
                        if (p != null) {
                            openPatientReport(p.getNome() + " " + p.getCognome(), p.getCodiceFiscale());
                        }
                    }
                }
                case NotificationService.ACTION_OPEN_MESSAGES -> handleMessagesNav();
                default -> {
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void showGlobalSuccess(String msg) {
        playAlert(msg, "#d1fae5", "#065f46");
    }

    public void showGlobalError(String msg) {
        playAlert(msg, "#fee2e2", "#991b1b");
    }

    private void playAlert(String message, String bgColor, String textColor) {
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
        globalAlertLabel.setOpacity(0);

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

    private void refreshPatientContext() {
        if (!"Paziente".equalsIgnoreCase(role) || codiceFiscale == null) {
            return;
        }
        try {
            Paziente p = pazienteDAO.findById(codiceFiscale);
            if (p == null) {
                return;
            }
            this.userName = p.getNome() + " " + p.getCognome();
            userInitialsLabel.setText(getInitials(userName));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
