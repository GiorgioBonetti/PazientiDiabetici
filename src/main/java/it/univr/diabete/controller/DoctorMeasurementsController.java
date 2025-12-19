package it.univr.diabete.controller;

import it.univr.diabete.MainApp;
import it.univr.diabete.dao.GlicemiaDAO;
import it.univr.diabete.dao.PazienteDAO;
import it.univr.diabete.dao.impl.GlicemiaDAOImpl;
import it.univr.diabete.dao.impl.PazienteDAOImpl;
import it.univr.diabete.model.Glicemia;
import it.univr.diabete.model.Paziente;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Insets;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.layout.*;
import javafx.scene.shape.Polyline;

import java.io.IOException;
import java.time.LocalDate;
import java.util.*;
import java.util.stream.Collectors;

public class DoctorMeasurementsController {

    // dimensioni card
    private static final double CARD_WIDTH  = 210;
    private static final double CARD_HEIGHT = 200;

    // dimensioni mini-card Mattina / Pomeriggio / Sera
    private static final double MOMENT_WIDTH  = 60;
    private static final double MOMENT_HEIGHT = 42;

    @FXML private TextField searchField;
    @FXML private FlowPane cardsContainer;

    private final PazienteDAO pazienteDAO = new PazienteDAOImpl();
    private final GlicemiaDAO glicemiaDAO = new GlicemiaDAOImpl();

    private final ObservableList<Paziente> allPatients = FXCollections.observableArrayList();
    private FilteredList<Paziente> filtered;

    // cache: tutte le glicemie raggruppate per paziente
    private Map<String, List<Glicemia>> glicemiePerPaziente = new HashMap<>();

    private final LocalDate today = LocalDate.now();

    @FXML
    private void initialize() {
        // layout del FlowPane (margini simmetrici e spaziatura)
        cardsContainer.setHgap(8);
        cardsContainer.setVgap(20);
        cardsContainer.setPadding(new Insets(12, 14, 14, 14));

        // 1) carico pazienti + misurazioni
        loadPatientsAndMeasurements();

        // 2) filtro per ricerca
        filtered = new FilteredList<>(allPatients, p -> true);

        searchField.textProperty().addListener((obs, oldV, newV) -> {
            String t = (newV == null) ? "" : newV.toLowerCase().trim();
            filtered.setPredicate(p -> {
                if (t.isEmpty()) return true;
                String fullName = (p.getNome() + " " + p.getCognome()).toLowerCase();
                String email = (p.getEmail() != null) ? p.getEmail().toLowerCase() : "";
                String id = String.valueOf(p.getCodiceFiscale());
                return fullName.contains(t) || email.contains(t) || id.contains(t);
            });
            renderCards();
        });

        // 3) prima renderizzazione
        renderCards();
    }

    /**
     * Carica tutti i pazienti e tutte le misurazioni in memoria (UNA query per tipo).
     */
    private void loadPatientsAndMeasurements() {
        try {
            // pazienti
            allPatients.setAll(pazienteDAO.findAll());
            // ultimo creato in alto (id più grande prima)
            //allPatients.sort((a, b) -> Integer.compare(b.getCodiceFiscale(), a.getCodiceFiscale()));

            // glicemie (una sola query)
            List<Glicemia> tutte = glicemiaDAO.findAll();

            // raggruppo per IdPaziente
            glicemiePerPaziente = tutte.stream()
                    .collect(Collectors.groupingBy(Glicemia::getFkPaziente));

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void renderCards() {
        cardsContainer.getChildren().clear();
        for (Paziente p : filtered) {
            cardsContainer.getChildren().add(createMeasurementCard(p));
        }
    }

    /** Crea la singola card “dashboard-style” per un paziente. */
    private VBox createMeasurementCard(Paziente p) {
        VBox root = new VBox(6);
        root.getStyleClass().add("measure-card");
        root.setPadding(new Insets(12));

        // dimensioni card → 4 per riga
        root.setPrefWidth(CARD_WIDTH);
        root.setMinWidth(CARD_WIDTH);
        root.setMaxWidth(CARD_WIDTH);
        root.setPrefHeight(CARD_HEIGHT);
        root.setMinHeight(CARD_HEIGHT);
        root.setMaxHeight(CARD_HEIGHT);

        root.setStyle("""
        -fx-background-color: white;
        -fx-background-radius: 20;
        -fx-border-radius: 20;
        -fx-border-color: #e5e7eb;
        -fx-effect: dropshadow(gaussian, rgba(15,23,42,0.06), 8, 0.2, 0, 2);
        """);

        // ---- MISURAZIONI DI OGGI (usiamo la cache) ----
        List<Glicemia> mis = glicemiePerPaziente.getOrDefault(p.getCodiceFiscale(), List.of());

        List<Glicemia> todayList = mis.stream()
                .filter(g -> g.getDateStamp().toLocalDate().equals(today))
                .sorted(Comparator.comparing(Glicemia::getDateStamp))
                .toList();

        // mappa MomentoNormalizzato -> ultima glicemia di quel momento
        Map<String, Glicemia> lastByMoment = new HashMap<>();
        for (Glicemia g : todayList) {
            String norm = normalizeMoment(g.getParteGiorno());
            if (norm != null) {
                lastByMoment.put(norm, g); // siccome sono in ordine, l’ultima vince
            }
        }

        Glicemia gMattina    = lastByMoment.get("Mattina");
        Glicemia gPomeriggio = lastByMoment.get("Pomeriggio");
        Glicemia gSera       = lastByMoment.get("Sera");

        boolean hasAnyToday = (gMattina != null || gPomeriggio != null || gSera != null);

        // ======================
        // HEADER: NOME + PILL STATO
        // ======================
        HBox header = new HBox(8);

        Label nameLbl = new Label(p.getNome() + " " + p.getCognome());
        nameLbl.setStyle("-fx-font-size: 13px; -fx-font-weight: bold; -fx-text-fill: #111827;");

        // stato molto semplice per ora
        String statusText;
        String statusStyle;

        if (!hasAnyToday) {
            statusText = "NESSUN DATO";
            statusStyle = """
            -fx-background-color: #e5e7eb;
            -fx-text-fill: #4b5563;
            """;
        } else {
            statusText = "OK";
            statusStyle = """
            -fx-background-color: #dcfce7;
            -fx-text-fill: #166534;
            """;
        }

        Label statusPill = new Label(statusText);
        statusPill.setStyle("""
        -fx-font-size: 10px;
        -fx-font-weight: bold;
        -fx-padding: 3 10 3 10;
        -fx-background-radius: 999;
        """ + statusStyle);
        statusPill.setMinHeight(18);

        Region headerSpacer = new Region();
        HBox.setHgrow(headerSpacer, Priority.ALWAYS);

        header.getChildren().addAll(nameLbl, headerSpacer, statusPill);

        // ======================
        // RESTO DELLA CARD
        // ======================

        // "Oggi"
        Label todayTitle = new Label("Oggi");
        todayTitle.setStyle("-fx-font-size: 10px; -fx-text-fill: #9ca3af;");
        todayTitle.setMaxWidth(CARD_WIDTH - 20);

        // riga con le 3 mini-card Mattina / Pomeriggio / Sera
        HBox momentsRow = new HBox(3.5);
        momentsRow.getChildren().addAll(
                buildMomentBox("Mattina", gMattina),
                buildMomentBox("Pomeriggio", gPomeriggio),
                buildMomentBox("Sera", gSera)
        );

        Label noTodayLabel = null;
        if (!hasAnyToday) {
            noTodayLabel = new Label("Nessuna misurazione registrata oggi");
            noTodayLabel.setStyle("-fx-font-size: 8px; -fx-text-fill: #9ca3af;");
            noTodayLabel.setMaxWidth(CARD_WIDTH - 20);
            noTodayLabel.setWrapText(true);
        }

        Region spacer = new Region();
        VBox.setVgrow(spacer, Priority.ALWAYS);

        Node sparkline = createSparkline(mis);
        VBox.setMargin(sparkline, new Insets(4, 0, 0, 0));

        // composizione finale
        root.getChildren().addAll(header, todayTitle, momentsRow);
        if (noTodayLabel != null) {
            root.getChildren().add(noTodayLabel);
        }
        root.getChildren().addAll(spacer, sparkline);

        // hover + click (uguale a prima)
        root.setOnMouseEntered(e ->
                root.setStyle("""
            -fx-background-color: #f5f3ff;
            -fx-background-radius: 22;
            -fx-border-radius: 22;
            -fx-border-color: #a855f7;
            -fx-effect: dropshadow(gaussian, rgba(129,140,248,0.35), 12, 0.3, 0, 4);
            -fx-cursor: hand;
            """)
        );

        root.setOnMouseExited(e ->
                root.setStyle("""
            -fx-background-color: white;
            -fx-background-radius: 22;
            -fx-border-radius: 22;
            -fx-border-color: #e5e7eb;
            -fx-effect: dropshadow(gaussian, rgba(15,23,42,0.06), 10, 0.2, 0, 3);
            """)
        );

        root.setOnMouseClicked(e -> openDetail(p));

        return root;
    }

    // Normalizza il valore di Momento che arriva dal DB
    private String normalizeMoment(String raw) {
        if (raw == null) return null;
        String m = raw.trim().toLowerCase();

        if (m.startsWith("matt")) {                     // "Mattina", "Mattino"
            return "Mattina";
        }
        if (m.startsWith("pran") || m.startsWith("pom")) { // "Pranzo", "Pomeriggio"
            return "Pomeriggio";
        }
        if (m.startsWith("cen") || m.startsWith("sera")) { // "Cena", "Sera"
            return "Sera";
        }
        return null;
    }

    /** Mini-card interna: valore o "Manca registrazione". */
    private VBox buildMomentBox(String titolo, Glicemia g) {
        VBox box = new VBox(2);
        box.setPadding(new Insets(4, 4, 4, 4));

        box.setPrefSize(MOMENT_WIDTH, MOMENT_HEIGHT);
        box.setMinSize(MOMENT_WIDTH, MOMENT_HEIGHT);
        box.setMaxSize(MOMENT_WIDTH, MOMENT_HEIGHT);

        box.setStyle("""
            -fx-background-color: #f9fafb;
            -fx-background-radius: 14;
            -fx-border-radius: 14;
            -fx-border-color: #e5e7eb;
            """);

        Label titleLbl = new Label(titolo);
        titleLbl.setStyle("-fx-font-size: 10px; -fx-text-fill: #6b7280;");

        Label valueLbl;
        if (g == null) {
            valueLbl = new Label("Manca\nregistrazione");
            valueLbl.setStyle("-fx-font-size: 9px; -fx-text-fill: #9ca3af;");
        } else {
            valueLbl = new Label(g.getValore() + " mg/dL");
            valueLbl.setStyle("-fx-font-size: 9px; -fx-font-weight: bold; -fx-text-fill: #111827;");
        }
        valueLbl.setWrapText(true);

        box.getChildren().addAll(titleLbl, valueLbl);
        return box;
    }

    /** MINI-GRAFICO DI ANDAMENTO ULTIME N MISURAZIONI */
    private Node createSparkline(List<Glicemia> data) {
        if (data == null || data.isEmpty()) {
            Region empty = new Region();
            empty.setPrefHeight(24);
            return empty;
        }

        List<Glicemia> last = data.stream()
                .sorted(Comparator.comparing(Glicemia::getDateStamp))
                .skip(Math.max(0, data.size() - 10))
                .toList();

        double width = CARD_WIDTH - 35;   // leggermente più largo
        double height = 35;               // altezza grafico

        int min = last.stream().mapToInt(Glicemia::getValore).min().orElse(0);
        int max = last.stream().mapToInt(Glicemia::getValore).max().orElse(1);
        int range = Math.max(1, max - min);

        Polyline line = new Polyline();
        line.setStyle("-fx-stroke: #f97316; -fx-stroke-width: 2;");

        for (int i = 0; i < last.size(); i++) {
            double x = (width / Math.max(1, last.size() - 1)) * i;
            double norm = (last.get(i).getValore() - min) / (double) range;
            double y = height - (norm * height);
            line.getPoints().addAll(x, y);
        }

        Pane wrapper = new Pane(line);
        wrapper.setPrefSize(width, height);

        // 🔥 spostamento fine-tuning
        wrapper.setTranslateX(5);   // ← sposta un po' a sinistra
        wrapper.setTranslateY(-5);    // ↓ sposta leggermente verso il basso

        return wrapper;
    }

    /** APRE LA VISTA COMPLETA DELLE MISURAZIONI DEL PAZIENTE */
    private void openDetail(Paziente p) {
        try {
            FXMLLoader loader =
                    new FXMLLoader(MainApp.class.getResource("/fxml/PatientMeasurementsView.fxml"));
            Parent view = loader.load();

            PatientMeasurementsController ctrl = loader.getController();
            ctrl.setPatientContext(p.getNome() + " " + p.getCognome(), p.getCodiceFiscale());
            ctrl.hideEditingTools();
            MainShellController shell = MainApp.getMainShellController();
            shell.getContentArea().getChildren().setAll(view);

        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}