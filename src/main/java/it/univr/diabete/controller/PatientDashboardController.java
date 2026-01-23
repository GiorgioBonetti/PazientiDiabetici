package it.univr.diabete.controller;

import it.univr.diabete.MainApp;
import it.univr.diabete.dao.AssunzioneDAO;
import it.univr.diabete.dao.GlicemiaDAO;
import it.univr.diabete.dao.PazienteDAO;
import it.univr.diabete.dao.SintomoDAO;
import it.univr.diabete.dao.TerapiaDAO;
import it.univr.diabete.dao.impl.AssunzioneDAOImpl;
import it.univr.diabete.dao.impl.GlicemiaDAOImpl;
import it.univr.diabete.dao.impl.PazienteDAOImpl;
import it.univr.diabete.dao.impl.SintomoDAOImpl;
import it.univr.diabete.dao.impl.TerapiaDAOImpl;
import it.univr.diabete.model.Assunzione;
import it.univr.diabete.model.Glicemia;
import it.univr.diabete.model.Paziente;
import it.univr.diabete.model.Sintomo;
import it.univr.diabete.model.Terapia;
import it.univr.diabete.service.NotificationService;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.ChoiceBox;
import javafx.scene.control.Label;
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.Button;
import javafx.stage.Modality;
import javafx.stage.Stage;
import it.univr.diabete.dao.FarmacoTerapiaDAO;
import it.univr.diabete.dao.impl.FarmacoTerapiaDAOImpl;
import it.univr.diabete.model.FarmacoTerapia;
import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import javafx.scene.control.CheckBox;
import java.time.LocalDate;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import javafx.application.Platform;
public class PatientDashboardController {

    @FXML
    private Label patientNameLabel;

    @FXML
    private TableView<Glicemia> measurementsTable;

    @FXML
    private TableColumn<Glicemia, String> colMeasDate;

    @FXML
    private TableColumn<Glicemia, Integer> colMeasValue;
    @FXML
    private TableColumn<Glicemia, String> colDashMoment;

    @FXML
    private CheckBox chkMattino;

    @FXML
    private CheckBox chkPranzo;

    @FXML
    private CheckBox chkCena;

    @FXML
    private CheckBox chkTerapiaAssunta;

    // DAO per terapia / assunzioni (se non li hai già qui)
    private final TerapiaDAO terapiaDAO = new TerapiaDAOImpl();
    private final AssunzioneDAO assunzioneDAO = new AssunzioneDAOImpl();
    // DAO per terapia / assunzioni
     private final FarmacoTerapiaDAO farmacoTerapiaDAO = new FarmacoTerapiaDAOImpl(); // 👈 nuovo
    @FXML
    private ChoiceBox<String> measurementFilter;

    @FXML
    private ListView<Sintomo> todaySymptomsListView;

    @FXML
    private Button addSymptomButton;

    private String codiceFiscale;

    private final GlicemiaDAO glicemiaDAO = new GlicemiaDAOImpl();
    private final PazienteDAO pazienteDAO = new PazienteDAOImpl();
    private final SintomoDAO sintomoDAO = new SintomoDAOImpl();
    private final NotificationService notificationService = new NotificationService();

    // tutti i record scaricati dal DB per questo paziente
    private final ObservableList<Glicemia> allMeasurements =
            FXCollections.observableArrayList();

    private final ObservableList<Sintomo> todaySymptoms =
            FXCollections.observableArrayList();

    private static final DateTimeFormatter DATE_TIME_FORMATTER =
            DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");

    private boolean isSameDay(LocalDateTime dt, LocalDate day) {
        return dt != null && dt.toLocalDate().isEqual(day);
    }
    @FXML
    private void initialize() {

        // --- colonne tabella ---
        colMeasDate.setCellValueFactory(cell -> {
            Glicemia g = cell.getValue();
            LocalDateTime dt = g.getDateStamp();
            String text = (dt != null) ? DATE_TIME_FORMATTER.format(dt) : "";
            return new SimpleStringProperty(text);
        });

        colMeasValue.setCellValueFactory(cell ->
                new SimpleIntegerProperty(cell.getValue().getValore()).asObject()
        );
        colDashMoment.setCellValueFactory(c ->
                new SimpleStringProperty(c.getValue().getParteGiorno())
        );

        // collega la lista alla tabella
        measurementsTable.setItems(FXCollections.observableArrayList());

        // --- filtro periodi ---
        if (measurementFilter != null) {
            measurementFilter.setItems(FXCollections.observableArrayList(
                    "Tutte",
                    "Oggi",
                    "Ultimi 7 giorni",
                    "Ultimi 30 giorni"
            ));
            measurementFilter.getSelectionModel().selectFirst(); // "Tutte"

            // se cambi filtro dal menu, ricalcola
            measurementFilter.getSelectionModel().selectedItemProperty()
                    .addListener((obs, oldV, newV) -> applyFilterAndRefresh());
        }

        if (todaySymptomsListView != null) {
            todaySymptomsListView.setItems(todaySymptoms);
            todaySymptomsListView.setCellFactory(lv -> createSymptomCell());
            todaySymptomsListView.setPlaceholder(new Label("Nessun sintomo registrato oggi."));
        }
    }

    /**
     * Chiamato dalla MainShell quando un paziente effettua il login.
     */
    public void setPatientData(String fullName, String codiceFiscale) {
        this.codiceFiscale = codiceFiscale;
        patientNameLabel.setText(fullName);
        loadDashboardData();
        runNotificationsAsync();
    }

    private void loadDashboardData() {
        if (codiceFiscale == null) {
            return;
        }
        LocalDate today = LocalDate.now();
        LocalDate start = today.minusDays(30);
        LocalDateTime startTs = start.atStartOfDay();
        LocalDateTime endTs = today.plusDays(1).atStartOfDay().minusSeconds(1);

        try (java.sql.Connection conn = it.univr.diabete.database.Database.getConnection()) {
            // nome paziente
            String sqlPatient = "SELECT nome, cognome FROM Paziente WHERE codiceFiscale = ?";
            try (java.sql.PreparedStatement ps = conn.prepareStatement(sqlPatient)) {
                ps.setString(1, codiceFiscale);
                try (java.sql.ResultSet rs = ps.executeQuery()) {
                    if (rs.next()) {
                        patientNameLabel.setText(rs.getString("nome") + " " + rs.getString("cognome"));
                    }
                }
            }

            // glicemie ultimi 30 giorni
            String sqlGlicemia = """
                SELECT id, fkPaziente, valore, dateStamp, parteGiorno
                FROM Glicemia
                WHERE fkPaziente = ? AND dateStamp BETWEEN ? AND ?
                ORDER BY dateStamp
                """;
            try (java.sql.PreparedStatement ps = conn.prepareStatement(sqlGlicemia)) {
                ps.setString(1, codiceFiscale);
                ps.setTimestamp(2, java.sql.Timestamp.valueOf(startTs));
                ps.setTimestamp(3, java.sql.Timestamp.valueOf(endTs));
                try (java.sql.ResultSet rs = ps.executeQuery()) {
                    List<Glicemia> list = new java.util.ArrayList<>();
                    while (rs.next()) {
                        Glicemia g = new Glicemia();
                        g.setId(rs.getInt("id"));
                        g.setFkPaziente(rs.getString("fkPaziente"));
                        g.setValore(rs.getInt("valore"));
                        g.setParteGiorno(rs.getString("parteGiorno"));
                        java.sql.Timestamp ts = rs.getTimestamp("dateStamp");
                        if (ts != null) {
                            g.setDateStamp(ts.toLocalDateTime());
                        }
                        list.add(g);
                    }
                    allMeasurements.setAll(list);
                    applyFilterAndRefresh();
                    updateTodayGlycemiaTasks();
                }
            }

            // sintomi oggi
            String sqlSintomi = """
                SELECT id, descrizione, dataInizio, dataFine, `intensità`, frequenza,
                       noteAggiuntive, fkPaziente, datestamp
                FROM Sintomo
                WHERE fkPaziente = ? AND DATE(datestamp) = ?
                ORDER BY datestamp DESC
                """;
            try (java.sql.PreparedStatement ps = conn.prepareStatement(sqlSintomi)) {
                ps.setString(1, codiceFiscale);
                ps.setDate(2, java.sql.Date.valueOf(today));
                try (java.sql.ResultSet rs = ps.executeQuery()) {
                    List<Sintomo> list = new java.util.ArrayList<>();
                    while (rs.next()) {
                        Sintomo s = new Sintomo();
                        s.setId(rs.getInt("id"));
                        s.setDescrizione(rs.getString("descrizione"));
                        java.sql.Date di = rs.getDate("dataInizio");
                        if (di != null) {
                            s.setDataInizio(di.toLocalDate());
                        }
                        java.sql.Date df = rs.getDate("dataFine");
                        if (df != null) {
                            s.setDataFine(df.toLocalDate());
                        }
                        s.setIntensita(rs.getInt("intensità"));
                        s.setFrequenza(rs.getString("frequenza"));
                        s.setNoteAggiuntive(rs.getString("noteAggiuntive"));
                        s.setFkPaziente(rs.getString("fkPaziente"));
                        java.sql.Timestamp ts = rs.getTimestamp("datestamp");
                        if (ts != null) {
                            s.setDateStamp(ts.toLocalDateTime());
                        }
                        list.add(s);
                    }
                    todaySymptoms.setAll(list);
                }
            }

            // stato terapia oggi (una terapia principale)
            String sqlTerapia = """
                SELECT id
                FROM Terapia
                WHERE fkPaziente = ?
                ORDER BY dataInizio DESC
                LIMIT 1
                """;
            Integer terapiaId = null;
            try (java.sql.PreparedStatement ps = conn.prepareStatement(sqlTerapia)) {
                ps.setString(1, codiceFiscale);
                try (java.sql.ResultSet rs = ps.executeQuery()) {
                    if (rs.next()) {
                        terapiaId = rs.getInt("id");
                    }
                }
            }

            if (terapiaId == null) {
                chkTerapiaAssunta.setSelected(false);
                chkTerapiaAssunta.setDisable(true);
                return;
            }

            String sqlFarmaci = """
                SELECT fkFarmaco, assunzioniGiornaliere, quantita
                FROM FarmacoTerapia
                WHERE fkTerapia = ?
                """;
            List<FarmacoTerapia> farmaci = new java.util.ArrayList<>();
            try (java.sql.PreparedStatement ps = conn.prepareStatement(sqlFarmaci)) {
                ps.setInt(1, terapiaId);
                try (java.sql.ResultSet rs = ps.executeQuery()) {
                    while (rs.next()) {
                        FarmacoTerapia tf = new FarmacoTerapia();
                        tf.setFkTerapia(terapiaId);
                        tf.setFkFarmaco(rs.getInt("fkFarmaco"));
                        tf.setAssunzioniGiornaliere(rs.getInt("assunzioniGiornaliere"));
                        tf.setQuantita(rs.getInt("quantita"));
                        farmaci.add(tf);
                    }
                }
            }

            if (farmaci.isEmpty()) {
                chkTerapiaAssunta.setSelected(false);
                chkTerapiaAssunta.setDisable(true);
                return;
            }

            String sqlAssunzioni = """
                SELECT fkFarmaco, SUM(quantitaAssunta) AS qty
                FROM Assunzione
                WHERE fkPaziente = ? AND fkTerapia = ? AND dateStamp BETWEEN ? AND ?
                GROUP BY fkFarmaco
                """;
            Map<Integer, Integer> qtyByFarmaco = new java.util.HashMap<>();
            try (java.sql.PreparedStatement ps = conn.prepareStatement(sqlAssunzioni)) {
                ps.setString(1, codiceFiscale);
                ps.setInt(2, terapiaId);
                ps.setTimestamp(3, java.sql.Timestamp.valueOf(today.atStartOfDay()));
                ps.setTimestamp(4, java.sql.Timestamp.valueOf(today.plusDays(1).atStartOfDay().minusSeconds(1)));
                try (java.sql.ResultSet rs = ps.executeQuery()) {
                    while (rs.next()) {
                        qtyByFarmaco.put(rs.getInt("fkFarmaco"), rs.getInt("qty"));
                    }
                }
            }

            int targetDoses = farmaci.stream()
                    .mapToInt(FarmacoTerapia::getAssunzioniGiornaliere)
                    .sum();
            int takenDoses = 0;
            for (FarmacoTerapia tf : farmaci) {
                int perDose = tf.getQuantita();
                if (perDose <= 0) {
                    continue;
                }
                int qty = qtyByFarmaco.getOrDefault(tf.getFkFarmaco(), 0);
                takenDoses += (qty / perDose);
            }
            chkTerapiaAssunta.setDisable(false);
            chkTerapiaAssunta.setSelected(takenDoses >= targetDoses);

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void runNotificationsAsync() {
        if (codiceFiscale == null) {
            return;
        }
        Thread t = new Thread(() -> {
            try {
                notificationService.generatePatientDashboardNotifications(codiceFiscale);
                Platform.runLater(() -> {
                    MainShellController shell = MainApp.getMainShellController();
                    if (shell != null) {
                        shell.refreshNotifications();
                    }
                });
            } catch (Exception e) {
                e.printStackTrace();
            }
        });
        t.setDaemon(true);
        t.start();
    }

    private void refreshPatientName() {
        if (codiceFiscale == null) {
            return;
        }
        try {
            Paziente p = pazienteDAO.findById(codiceFiscale);
            if (p != null) {
                patientNameLabel.setText(p.getNome() + " " + p.getCognome());
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * Legge tutte le glicemie dal DAO e ricarica la tabella
     */
    private void loadMeasurements() {
        if (codiceFiscale == null) return;

        try {
            LocalDate end = LocalDate.now();
            LocalDate start = end.minusDays(30);
            List<Glicemia> lista = glicemiaDAO.findByPazienteIdAndDateRange(codiceFiscale, start, end);
            allMeasurements.setAll(lista);
            applyFilterAndRefresh();
            updateTodayTasks();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void loadTodaySymptoms() {
        if (codiceFiscale == null) {
            return;
        }
        try {
            List<Sintomo> list = sintomoDAO.findByPazienteAndDate(codiceFiscale, LocalDate.now());
            todaySymptoms.setAll(list);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * Applicazione del filtro (Tutte/Oggi/7gg/30gg) e push sulla tabella.
     */
    private void applyFilterAndRefresh() {
        // niente dati -> tabella vuota
        if (allMeasurements.isEmpty()) {
            measurementsTable.getItems().setAll(allMeasurements);
            return;
        }

        final String filterValue =
                (measurementFilter == null ||
                        measurementFilter.getSelectionModel().getSelectedItem() == null)
                        ? "Tutte"
                        : measurementFilter.getSelectionModel().getSelectedItem();

        LocalDateTime now = LocalDateTime.now();

        List<Glicemia> filtered = allMeasurements.stream()
                .filter(g -> g.getDateStamp() != null) // o getDateStamp()
                .filter(g -> {
                    LocalDateTime dt = g.getDateStamp();
                    return switch (filterValue) {
                        case "Oggi" -> dt.toLocalDate().isEqual(now.toLocalDate());
                        case "Ultimi 7 giorni" -> !dt.isBefore(now.minusDays(7));
                        case "Ultimi 30 giorni" -> !dt.isBefore(now.minusDays(30));
                        default -> true;  // "Tutte"
                    };
                })
                // opzionale: le più recenti per prime
                .sorted(Comparator.comparing(Glicemia::getDateStamp).reversed())
                .collect(Collectors.toList());

        measurementsTable.getItems().setAll(filtered);
    }

    /**
     * Richiamato dal ChoiceBox in FXML (onAction).
     */
    @FXML
    private void handleMeasurementFilterChange() {
        applyFilterAndRefresh();
    }

    /**
     * Pulsante "Registra glicemia" nella dashboard (popup).
     */
    @FXML
    private void handleAddGlicemia() {
        try {
            FXMLLoader loader = new FXMLLoader(
                    MainApp.class.getResource("/fxml/AddGlicemiaView.fxml")
            );
            Parent root = loader.load();

            AddGlicemiaController controller = loader.getController();
            controller.initData(codiceFiscale, this::loadMeasurements);

            Stage popup = new Stage();
            popup.initOwner(measurementsTable.getScene().getWindow());
            popup.initModality(Modality.WINDOW_MODAL);
            popup.setTitle("Registra glicemia");
            popup.setScene(new Scene(root));
            popup.showAndWait();

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @FXML
    private void handleAddSymptom() {
        if (codiceFiscale == null) {
            return;
        }
        try {
            FXMLLoader loader = new FXMLLoader(
                    MainApp.class.getResource("/fxml/AddSymptomView.fxml")
            );
            Parent root = loader.load();

            AddSymptomController controller = loader.getController();
            controller.initData(codiceFiscale, this::loadTodaySymptoms);

            Stage popup = new Stage();
            popup.initOwner(addSymptomButton.getScene().getWindow());
            popup.initModality(Modality.WINDOW_MODAL);
            popup.setTitle("Nuovo sintomo");
            popup.setScene(new Scene(root));
            popup.showAndWait();

        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    @FXML
    private void handleOpenTerapia() {
        MainShellController shell = MainApp.getMainShellController();
        shell.handleTherapyNav();
    }
    private void updateTodayGlycemiaTasks() {
        LocalDate today = LocalDate.now();

        List<Glicemia> lista = measurementsTable.getItems(); // o allMeasurements

        boolean mattinoDone = lista.stream()
                .anyMatch(g -> isSameDay(g.getDateStamp(), today)
                        && "Mattina".equalsIgnoreCase(g.getParteGiorno()));

        boolean pranzoDone = lista.stream()
                .anyMatch(g -> isSameDay(g.getDateStamp(), today)
                        && "Pomeriggio".equalsIgnoreCase(g.getParteGiorno()));

        boolean cenaDone = lista.stream()
                .anyMatch(g -> isSameDay(g.getDateStamp(), today)
                        && "Sera".equalsIgnoreCase(g.getParteGiorno()));

        chkMattino.setSelected(mattinoDone);
        chkPranzo.setSelected(pranzoDone);
        chkCena.setSelected(cenaDone);
    }
    private void updateTodayTherapyTask() {
        if (codiceFiscale == null) {
            chkTerapiaAssunta.setSelected(false);
            chkTerapiaAssunta.setDisable(true);
            return;
        }

        try {
            // 1) Recupera le terapie del paziente
            List<Terapia> terapie = terapiaDAO.findByPazienteId(codiceFiscale);

            if (terapie.isEmpty()) {
                // nessuna terapia → checkbox disabilitata
                chkTerapiaAssunta.setSelected(false);
                chkTerapiaAssunta.setDisable(true);
                return;
            }

            // 2) per ora consideriamo la prima terapia come "corrente"
            Terapia terapiaCorrente = terapie.get(0);

            // 3) Recupero i farmaci associati a questa terapia
            List<FarmacoTerapia> farmaci = farmacoTerapiaDAO.findByTerapiaId(terapiaCorrente.getId());

            if (farmaci.isEmpty()) {
                // terapia senza farmaci → non ha senso parlare di "assunzione completata"
                chkTerapiaAssunta.setSelected(false);
                chkTerapiaAssunta.setDisable(true);
                return;
            }

            LocalDate today = LocalDate.now();

            // 4) Target giornaliero atteso: assunzioni/giorno per ogni farmaco
            int targetGiornalieroTotale = farmaci.stream()
                    .mapToInt(FarmacoTerapia::getAssunzioniGiornaliere)
                    .sum();

            // 5) Assunzioni odierne aggregate in un'unica query
            List<Assunzione> assunzioniOggi =
                    assunzioneDAO.findByPazienteAndDateRange(codiceFiscale, today, today);
            int dosiAssunteOggiTotale = 0;
            for (FarmacoTerapia tf : farmaci) {
                int quantitaPerDose = tf.getQuantita();
                if (quantitaPerDose <= 0) {
                    continue;
                }
                int qFarmacoOggi = assunzioniOggi.stream()
                        .filter(a -> a.getFkTerapia() == terapiaCorrente.getId()
                                && a.getFkFarmaco() == tf.getFkFarmaco())
                        .mapToInt(Assunzione::getQuantitaAssunta)
                        .sum();
                dosiAssunteOggiTotale += (qFarmacoOggi / quantitaPerDose);
            }

            boolean terapiaCompletaOggi = dosiAssunteOggiTotale >= targetGiornalieroTotale;

            chkTerapiaAssunta.setDisable(false);
            chkTerapiaAssunta.setSelected(terapiaCompletaOggi);

        } catch (Exception e) {
            e.printStackTrace();
            chkTerapiaAssunta.setSelected(false);
            chkTerapiaAssunta.setDisable(true);
        }
    }
    private void updateTodayTasks() {
        updateTodayGlycemiaTasks();
        updateTodayTherapyTask();
    }

    private ListCell<Sintomo> createSymptomCell() {
        return new ListCell<>() {
            private final HBox root = new HBox(8);
            private final VBox textBox = new VBox(2);
            private final Label title = new Label();
            private final Label badge = new Label();

            {
                root.getStyleClass().add("symptom-chip");
                title.getStyleClass().add("symptom-title");
                badge.getStyleClass().add("symptom-badge");

                textBox.getChildren().addAll(title);

                Region spacer = new Region();
                HBox.setHgrow(spacer, Priority.ALWAYS);
                root.getChildren().addAll(textBox, spacer, badge);
                root.setAlignment(Pos.CENTER_LEFT);
                root.setPadding(new Insets(8));

                root.setOnMouseClicked(e -> {
                    Sintomo s = getItem();
                    if (s != null) {
                        openSymptomDetails(s);
                    }
                });
            }

            @Override
            protected void updateItem(Sintomo s, boolean empty) {
                super.updateItem(s, empty);
                if (empty || s == null) {
                    setGraphic(null);
                    setText(null);
                } else {
                    title.setText(s.getDescrizione());
                    badge.setText("Int " + s.getIntensita());
                    setGraphic(root);
                    setText(null);
                }
            }
        };
    }

    private void openSymptomDetails(Sintomo s) {
        if (s == null) {
            return;
        }
        try {
            FXMLLoader loader = new FXMLLoader(
                    MainApp.class.getResource("/fxml/SymptomDetailView.fxml")
            );
            Parent root = loader.load();

            SymptomDetailController controller = loader.getController();
            controller.setSymptom(s);

            Stage popup = new Stage();
            popup.initOwner(todaySymptomsListView.getScene().getWindow());
            popup.initModality(Modality.WINDOW_MODAL);
            popup.setTitle("Dettaglio sintomo");
            popup.setScene(new Scene(root));
            popup.showAndWait();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

}
