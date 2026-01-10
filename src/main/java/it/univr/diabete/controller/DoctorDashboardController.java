package it.univr.diabete.controller;

import it.univr.diabete.MainApp;
import it.univr.diabete.dao.MessageDAO;
import it.univr.diabete.dao.PazienteDAO;
import it.univr.diabete.dao.impl.MessageDAOImpl;
import it.univr.diabete.dao.impl.PazienteDAOImpl;
import it.univr.diabete.database.Database;
import it.univr.diabete.model.Paziente;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Insets;
import javafx.scene.Parent;
import javafx.scene.control.*;
import javafx.scene.layout.VBox;

import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Timestamp;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

public class DoctorDashboardController {

    private String idDiabetologoLoggato;

    @FXML private Label totalPatientsLabel;
    @FXML private Label activeAlertsLabel;
    @FXML private Label todayVisitsLabel;
    @FXML private Label unreadMessagesLabel;
    @FXML private Label alertsCountBadge;
    @FXML private ChoiceBox<String> alertsFilter;

    @FXML private TableView<AlertRow> alertsTable;
    @FXML private TableColumn<AlertRow, String> colAlertPatient;
    @FXML private TableColumn<AlertRow, String> colAlertValue;
    @FXML private TableColumn<AlertRow, String> colAlertDetail;
    @FXML private TableColumn<AlertRow, String> colAlertTime;
    @FXML private TableColumn<AlertRow, String> colAlertPriority;

    @FXML private TableView<InactiveRow> lowActivityTable;
    @FXML private TableColumn<InactiveRow, String> colLowActPatient;
    @FXML private TableColumn<InactiveRow, String> colLowActLastValue;
    @FXML private TableColumn<InactiveRow, String> colLowActLastDate;
    @FXML private TableColumn<InactiveRow, String> colLowActDaysAgo;

    @FXML
    private ListView<Paziente> patientsListView;

    @FXML
    private TextField patientSearchField;

    private final PazienteDAO pazienteDAO = new PazienteDAOImpl();
    private final MessageDAO messageDAO = new MessageDAOImpl();

    private final ObservableList<Paziente> allPatients = FXCollections.observableArrayList();
    private FilteredList<Paziente> filteredPatients;
    private final ObservableList<AlertRow> alertRows = FXCollections.observableArrayList();
    private final ObservableList<InactiveRow> inactiveRows = FXCollections.observableArrayList();

    private static final int INACTIVITY_DAYS = 3;
    private static final DateTimeFormatter ALERT_TIME_FMT = DateTimeFormatter.ofPattern("dd/MM HH:mm");
    private static final DateTimeFormatter INACTIVE_TIME_FMT = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");

    // chiamato dal MainShellController
    public void setDoctorContext(String doctorId) {
        this.idDiabetologoLoggato = doctorId;
        reloadDashboard();
    }

    @FXML
    private void initialize() {
        setupTables();
        setupPatientsList();
        setupAlertsFilter();
    }

    private void reloadDashboard() {
        loadKpis();
        loadAlerts();
        loadInactivePatients();
        loadPatientsList();
    }

    private void setupPatientsList() {
        filteredPatients = new FilteredList<>(allPatients, p -> true);
        patientsListView.setItems(filteredPatients);
        patientsListView.setCellFactory(lv -> createPatientCardCell());

        patientSearchField.textProperty().addListener((obs, oldVal, newVal) -> {
            String q = (newVal == null) ? "" : newVal.toLowerCase().trim();
            filteredPatients.setPredicate(p -> {
                if (q.isEmpty()) return true;
                String fullName = (p.getNome() + " " + p.getCognome()).toLowerCase();
                String email    = p.getEmail() != null ? p.getEmail().toLowerCase() : "";
                String idStr    = String.valueOf(p.getCodiceFiscale());
                return fullName.contains(q) || email.contains(q) || idStr.contains(q);
            });
        });
        loadPatientsList();
    }

    private void loadPatientsList() {
        String doctorId = getDoctorId();
        if (doctorId == null) {
            allPatients.clear();
            return;
        }
        try {
            List<Paziente> lista = pazienteDAO.findAll();
            List<Paziente> filtered = lista.stream()
                    .filter(p -> p.getFkDiabetologo() != null
                            && p.getFkDiabetologo().equalsIgnoreCase(doctorId))
                    .toList();
            allPatients.setAll(filtered);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /** Cella della ListView che sembra una card. */
    private ListCell<Paziente> createPatientCardCell() {
        return new ListCell<>() {

            private final VBox root = new VBox(4);
            private final Label nameLbl  = new Label();
            private final Label emailLbl = new Label();

            {
                root.setPadding(new Insets(10));
                root.setStyle("""
                    -fx-background-color: white;
                    -fx-background-radius: 18;
                    -fx-border-radius: 18;
                    -fx-border-color: #e5e7eb;
                    -fx-effect: dropshadow(gaussian, rgba(15,23,42,0.06), 8, 0.2, 0, 2);
                    """);

                nameLbl.setStyle("-fx-font-size: 13px; -fx-font-weight: bold; -fx-text-fill: #111827;");
                emailLbl.setStyle("-fx-font-size: 11px; -fx-text-fill: #6b7280;");

                root.getChildren().addAll(nameLbl, emailLbl);

                // hover
                root.setOnMouseEntered(e -> root.setStyle("""
                    -fx-background-color: #f5f3ff;
                    -fx-background-radius: 18;
                    -fx-border-radius: 18;
                    -fx-border-color: #a855f7;
                    -fx-cursor: hand;
                    -fx-effect: dropshadow(gaussian, rgba(129,140,248,0.35), 12, 0.3, 0, 4);
                    """));

                root.setOnMouseExited(e -> root.setStyle("""
                    -fx-background-color: white;
                    -fx-background-radius: 18;
                    -fx-border-radius: 18;
                    -fx-border-color: #e5e7eb;
                    -fx-effect: dropshadow(gaussian, rgba(15,23,42,0.06), 8, 0.2, 0, 2);
                    """));

                // click singolo → apri REPORT
                root.setOnMouseClicked(e -> {
                    Paziente p = getItem();
                    if (p != null) {
                        openReportForPatient(p);
                    }
                });
            }

            @Override
            protected void updateItem(Paziente p, boolean empty) {
                super.updateItem(p, empty);

                if (empty || p == null) {
                    setGraphic(null);
                    setText(null);
                } else {
                    nameLbl.setText(p.getNome() + " " + p.getCognome());
                    emailLbl.setText(p.getEmail() != null ? p.getEmail() : "—");
                    setGraphic(root);
                    setText(null);
                }
            }
        };
    }

    private void openReportForPatient(Paziente p) {
        try {
            FXMLLoader loader = new FXMLLoader(
                    MainApp.class.getResource("/fxml/PatientReportView.fxml"));
            Parent view = loader.load();

            PatientReportController ctrl = loader.getController();
            ctrl.setPatientContext(p.getNome() + " " + p.getCognome(), p.getCodiceFiscale());

            MainShellController shell = MainApp.getMainShellController();
            shell.getContentArea().getChildren().setAll(view);

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void setupTables() {
        if (alertsTable != null) {
            alertsTable.setItems(alertRows);
            colAlertPatient.setCellValueFactory(c -> c.getValue().patient);
            colAlertValue.setCellValueFactory(c -> c.getValue().value);
            colAlertDetail.setCellValueFactory(c -> c.getValue().detail);
            colAlertTime.setCellValueFactory(c -> c.getValue().time);
            colAlertPriority.setCellValueFactory(c -> c.getValue().priority);
        }
        if (lowActivityTable != null) {
            lowActivityTable.setItems(inactiveRows);
            colLowActPatient.setCellValueFactory(c -> c.getValue().patient);
            colLowActLastValue.setCellValueFactory(c -> c.getValue().lastValue);
            colLowActLastDate.setCellValueFactory(c -> c.getValue().lastDate);
            colLowActDaysAgo.setCellValueFactory(c -> c.getValue().daysAgo);
        }
    }

    private void loadKpis() {
        String doctorId = getDoctorId();
        if (doctorId == null) {
            return;
        }
        try (Connection conn = Database.getConnection()) {
            totalPatientsLabel.setText(String.valueOf(countPatients(conn, doctorId)));
            todayVisitsLabel.setText(String.valueOf(countMeasurementsToday(conn, doctorId)));
            unreadMessagesLabel.setText(String.valueOf(messageDAO.countUnreadForDiabetologist(doctorId)));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void loadAlerts() {
        String doctorId = getDoctorId();
        if (doctorId == null) {
            return;
        }
        List<AlertRow> rows = new ArrayList<>();
        try (Connection conn = Database.getConnection()) {
            DateRange range = getSelectedRange();
            rows.addAll(loadGlycemicAlerts(conn, doctorId, range));
        } catch (Exception e) {
            e.printStackTrace();
        }

        rows.sort(Comparator
                .comparing(AlertRow::priorityRank).reversed()
                .thenComparing(AlertRow::timeValue, Comparator.nullsLast(Comparator.reverseOrder())));
        alertRows.setAll(rows);
        activeAlertsLabel.setText(String.valueOf(rows.size()));
        if (alertsCountBadge != null) {
            alertsCountBadge.setText(String.valueOf(rows.size()));
        }
    }

    private void loadInactivePatients() {
        String doctorId = getDoctorId();
        if (doctorId == null) {
            return;
        }
        List<InactiveRow> rows = new ArrayList<>();
        String sql = """
            SELECT p.codiceFiscale, p.nome, p.cognome,
                   MAX(g.dateStamp) AS lastDate,
                   (SELECT g2.valore
                    FROM Glicemia g2
                    WHERE g2.fkPaziente = p.codiceFiscale
                    ORDER BY g2.dateStamp DESC LIMIT 1) AS lastValue
            FROM Paziente p
            LEFT JOIN Glicemia g ON g.fkPaziente = p.codiceFiscale
            WHERE p.fkDiabetologo = ?
            GROUP BY p.codiceFiscale, p.nome, p.cognome
            HAVING lastDate IS NULL OR DATEDIFF(CURDATE(), DATE(lastDate)) >= ?
            """;
        try (Connection conn = Database.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, doctorId);
            ps.setInt(2, INACTIVITY_DAYS);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    String nome = rs.getString("nome");
                    String cognome = rs.getString("cognome");
                    Integer lastValue = rs.getObject("lastValue") != null ? rs.getInt("lastValue") : null;
                    Timestamp ts = rs.getTimestamp("lastDate");
                    LocalDateTime lastDate = ts != null ? ts.toLocalDateTime() : null;

                    long days = lastDate != null
                            ? ChronoUnit.DAYS.between(lastDate.toLocalDate(), LocalDate.now())
                            : Long.MAX_VALUE;

                    if (days >= INACTIVITY_DAYS) {
                        rows.add(new InactiveRow(
                                nome + " " + cognome,
                                lastValue != null ? String.valueOf(lastValue) : "—",
                                lastDate != null ? INACTIVE_TIME_FMT.format(lastDate) : "—",
                                days == Long.MAX_VALUE ? "—" : String.valueOf(days),
                                days
                        ));
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        rows.sort(Comparator.comparingLong(r -> -r.daysAgoValue));
        inactiveRows.setAll(rows);
    }

    private int countPatients(Connection conn, String doctorId) throws Exception {
        String sql = "SELECT COUNT(*) AS cnt FROM Paziente WHERE fkDiabetologo = ?";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, doctorId);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? rs.getInt("cnt") : 0;
            }
        }
    }

    private int countMeasurementsToday(Connection conn, String doctorId) throws Exception {
        String sql = """
            SELECT COUNT(*) AS cnt
            FROM Glicemia g
            JOIN Paziente p ON p.codiceFiscale = g.fkPaziente
            WHERE p.fkDiabetologo = ? AND DATE(g.dateStamp) = CURDATE()
            """;
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, doctorId);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? rs.getInt("cnt") : 0;
            }
        }
    }

    private List<AlertRow> loadGlycemicAlerts(Connection conn, String doctorId, DateRange range) throws Exception {
        StringBuilder sql = new StringBuilder("""
            SELECT p.nome, p.cognome, g.valore, g.dateStamp,
                   (SELECT GROUP_CONCAT(s.descrizione SEPARATOR '; ')
                    FROM Sintomo s
                    WHERE s.fkPaziente = g.fkPaziente
                      AND DATE(s.datestamp) = DATE(g.dateStamp)) AS sintomi
            FROM Glicemia g
            JOIN Paziente p ON p.codiceFiscale = g.fkPaziente
            WHERE p.fkDiabetologo = ? AND (g.valore > 130 OR g.valore < 80)
            """);
        if (range != null && range.start != null && range.end != null) {
            sql.append(" AND g.dateStamp BETWEEN ? AND ? ");
        }
        List<AlertRow> rows = new ArrayList<>();
        try (PreparedStatement ps = conn.prepareStatement(sql.toString())) {
            ps.setString(1, doctorId);
            int idx = 2;
            if (range != null && range.start != null && range.end != null) {
                ps.setTimestamp(idx++, Timestamp.valueOf(range.start));
                ps.setTimestamp(idx, Timestamp.valueOf(range.end));
            }
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    int valore = rs.getInt("valore");
                    LocalDateTime ts = rs.getTimestamp("dateStamp").toLocalDateTime();
                    String priority = glycemiaPriority(valore);
                    String value = String.valueOf(valore);
                    String detail = rs.getString("sintomi");
                    if (detail == null || detail.isBlank()) {
                        detail = "-";
                    }
                    String patient = rs.getString("nome") + " " + rs.getString("cognome");
                    rows.add(new AlertRow(patient, value, detail, ALERT_TIME_FMT.format(ts), priority, ts));
                }
            }
        }
        return rows;
    }

    private String glycemiaPriority(int valore) {
        if (valore > 180 || valore < 70) {
            return "ALTA";
        }
        if (valore > 130) {
            return "MEDIA";
        }
        if (valore < 80) {
            return "BASSA";
        }
        return "BASSA";
    }

    private void setupAlertsFilter() {
        if (alertsFilter == null) {
            return;
        }
        alertsFilter.setItems(FXCollections.observableArrayList(
                "Oggi", "Ultimi 3 giorni", "Ultimi 7 giorni", "Ultimi 30 giorni", "Tutto"
        ));
        alertsFilter.getSelectionModel().select("Oggi");
        alertsFilter.getSelectionModel().selectedItemProperty().addListener((obs, oldV, newV) -> loadAlerts());
    }

    private DateRange getSelectedRange() {
        if (alertsFilter == null) {
            return DateRange.today();
        }
        String val = alertsFilter.getSelectionModel().getSelectedItem();
        if (val == null) {
            return DateRange.today();
        }
        return switch (val) {
            case "Ultimi 3 giorni" -> DateRange.lastDays(3);
            case "Ultimi 7 giorni" -> DateRange.lastDays(7);
            case "Ultimi 30 giorni" -> DateRange.lastDays(30);
            case "Tutto" -> DateRange.all();
            default -> DateRange.today();
        };
    }

    private String getDoctorId() {
        if (idDiabetologoLoggato == null) {
            return null;
        }
        String trimmed = idDiabetologoLoggato.trim();
        return trimmed.isEmpty() ? null : trimmed;
    }

    private static class AlertRow {
        private final javafx.beans.property.SimpleStringProperty patient;
        private final javafx.beans.property.SimpleStringProperty value;
        private final javafx.beans.property.SimpleStringProperty time;
        private final javafx.beans.property.SimpleStringProperty priority;
        private final javafx.beans.property.SimpleStringProperty detail;
        private final LocalDateTime timeValue;

        private AlertRow(String patient, String value, String detail, String time, String priority, LocalDateTime timeValue) {
            this.patient = new javafx.beans.property.SimpleStringProperty(patient);
            this.value = new javafx.beans.property.SimpleStringProperty(value);
            this.detail = new javafx.beans.property.SimpleStringProperty(detail);
            this.time = new javafx.beans.property.SimpleStringProperty(time);
            this.priority = new javafx.beans.property.SimpleStringProperty(priority);
            this.timeValue = timeValue;
        }

        private int priorityRank() {
            return switch (priority.get()) {
                case "ALTA" -> 3;
                case "MEDIA" -> 2;
                default -> 1;
            };
        }

        private LocalDateTime timeValue() {
            return timeValue;
        }
    }

    private static class InactiveRow {
        private final javafx.beans.property.SimpleStringProperty patient;
        private final javafx.beans.property.SimpleStringProperty lastValue;
        private final javafx.beans.property.SimpleStringProperty lastDate;
        private final javafx.beans.property.SimpleStringProperty daysAgo;
        private final long daysAgoValue;

        private InactiveRow(String patient, String lastValue, String lastDate, String daysAgo, long daysAgoValue) {
            this.patient = new javafx.beans.property.SimpleStringProperty(patient);
            this.lastValue = new javafx.beans.property.SimpleStringProperty(lastValue);
            this.lastDate = new javafx.beans.property.SimpleStringProperty(lastDate);
            this.daysAgo = new javafx.beans.property.SimpleStringProperty(daysAgo);
            this.daysAgoValue = daysAgoValue;
        }
    }

    private static class DateRange {
        private final LocalDateTime start;
        private final LocalDateTime end;

        private DateRange(LocalDateTime start, LocalDateTime end) {
            this.start = start;
            this.end = end;
        }

        private static DateRange today() {
            LocalDate today = LocalDate.now();
            return new DateRange(today.atStartOfDay(), today.plusDays(1).atStartOfDay().minusSeconds(1));
        }

        private static DateRange lastDays(int days) {
            LocalDate today = LocalDate.now();
            LocalDate start = today.minusDays(days - 1L);
            return new DateRange(start.atStartOfDay(), today.plusDays(1).atStartOfDay().minusSeconds(1));
        }

        private static DateRange all() {
            return new DateRange(null, null);
        }
    }
}
