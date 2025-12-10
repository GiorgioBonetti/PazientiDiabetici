package it.univr.diabete.controller;

import it.univr.diabete.MainApp;
import it.univr.diabete.dao.GlicemiaDAO;
import it.univr.diabete.dao.PazienteDAO;
import it.univr.diabete.dao.TerapiaDAO;
import it.univr.diabete.dao.TerapiaFarmacoDAO;
import it.univr.diabete.dao.impl.GlicemiaDAOImpl;
import it.univr.diabete.dao.impl.PazienteDAOImpl;
import it.univr.diabete.dao.impl.TerapiaDAOImpl;
import it.univr.diabete.dao.impl.TerapiaFarmacoDAOImpl;
import it.univr.diabete.model.Glicemia;
import it.univr.diabete.model.Paziente;
import it.univr.diabete.model.Terapia;
import it.univr.diabete.model.TerapiaFarmaco;
import javafx.fxml.FXML;
import javafx.scene.chart.CategoryAxis;
import javafx.scene.chart.LineChart;
import javafx.scene.chart.NumberAxis;
import javafx.scene.chart.XYChart;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

public class PatientReportController {

    @FXML private ComboBox<String> periodFilter;
    @FXML private Label periodLabel;

    @FXML private Label symptomsLabel;
    @FXML private Label risksLabel;
    @FXML private Label pathologiesLabel;

    @FXML private Label therapyDrugLabel;
    @FXML private Label therapyDoseLabel;
    @FXML private Label therapyFreqLabel;
    @FXML private Label therapyPeriodLabel;

    @FXML private Label patientNameLabel;

    @FXML private Label avgLabel;
    @FXML private Label minLabel;
    @FXML private Label maxLabel;
    @FXML private Label countLabel;

    @FXML private Label therapyLabel;        // se non usato puoi anche toglierlo da FXML
    @FXML private Label therapyStatusLabel;

    @FXML private LineChart<String, Number> reportChart;
    @FXML private CategoryAxis xAxis;
    @FXML private NumberAxis yAxis;

    @FXML private Button goMeasurementsButton;
    @FXML private Button goTherapyButton;
    @FXML private Button editPatientButton;

    private final PazienteDAO pazienteDAO = new PazienteDAOImpl();
    private final GlicemiaDAO glicemiaDAO = new GlicemiaDAOImpl();
    private final TerapiaDAO terapiaDAO = new TerapiaDAOImpl();
    private final TerapiaFarmacoDAO terapiaFarmacoDAO = new TerapiaFarmacoDAOImpl();

    private List<Glicemia> allMeasurements = new ArrayList<>();
    private List<Terapia> terapiePaziente = new ArrayList<>();
    private String CodiceFiscale;

    private final DateTimeFormatter chartFormatter =
            DateTimeFormatter.ofPattern("dd/MM");
    private final DateTimeFormatter periodFormatter =
            DateTimeFormatter.ofPattern("dd/MM/yyyy");

    // ───────────────────────────── NAV / AZIONI ─────────────────────────────

    @FXML
    private void handleEditPatient() {
        if (CodiceFiscale == null || CodiceFiscale.isEmpty()) return;

        try {
            Paziente p = pazienteDAO.findById(CodiceFiscale);
            if (p == null) return;

            MainShellController shell = MainApp.getMainShellController();
            if (shell != null) {
                shell.openPatientDetail(p);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @FXML
    private void handleGoToMeasurements() {
        if (CodiceFiscale == null || CodiceFiscale.isEmpty()) return;

        MainShellController shell = MainApp.getMainShellController();
        if (shell != null) {
            shell.openPatientMeasurements(patientNameLabel.getText(), CodiceFiscale);
        }
    }

    @FXML
    private void handleGoToTherapy() {
        if (CodiceFiscale == null || CodiceFiscale.isEmpty()) return;

        MainShellController shell = MainApp.getMainShellController();
        if (shell != null) {
            shell.openPatientTherapy(patientNameLabel.getText(), CodiceFiscale);
        }
    }

    // ───────────────────────────── INIT CONTEXT ─────────────────────────────

    /**
     * Chiamato dal MainShellController quando carica il report.
     */
    public void setPatientContext(String nomeCompleto, String CodiceFiscale) {
        this.CodiceFiscale = CodiceFiscale;
        patientNameLabel.setText(nomeCompleto);

        loadAllMeasurements();
        setupPeriodFilter();
        applyPeriodAndRefresh();
        loadTherapyForPeriod();
    }

    // ───────────────────────────── GLICEMIE / GRAFICO ───────────────────────

    private void loadAllMeasurements() {
        try {
            allMeasurements = glicemiaDAO.findByPazienteId(CodiceFiscale);
            allMeasurements.sort(Comparator.comparing(Glicemia::getDataOra));
        } catch (Exception e) {
            e.printStackTrace();
            allMeasurements = new ArrayList<>();
        }
    }

    private void setupPeriodFilter() {
        periodFilter.getItems().setAll(
                "Ultimi 7 giorni",
                "Ultimi 30 giorni",
                "Ultimi 90 giorni",
                "Tutto"
        );

        periodFilter.getSelectionModel().select("Ultimi 30 giorni");

        periodFilter.valueProperty().addListener((obs, oldVal, newVal) -> {
            applyPeriodAndRefresh();
            loadTherapyForPeriod();
        });
    }

    private void applyPeriodAndRefresh() {
        if (allMeasurements.isEmpty()) {
            updateKpi(Collections.emptyList());
            updateChart(Collections.emptyList());
            periodLabel.setText("Nessuna misurazione disponibile");
            return;
        }

        String filter = periodFilter.getValue();
        LocalDate today = LocalDate.now();
        LocalDateTime fromDateTime = null;

        switch (filter) {
            case "Ultimi 7 giorni"  -> fromDateTime = today.minusDays(7).atStartOfDay();
            case "Ultimi 30 giorni" -> fromDateTime = today.minusDays(30).atStartOfDay();
            case "Ultimi 90 giorni" -> fromDateTime = today.minusDays(90).atStartOfDay();
            case "Tutto"            -> fromDateTime = null;
        }

        final LocalDateTime fromBoundary = fromDateTime;

        List<Glicemia> filtered = allMeasurements.stream()
                .filter(g -> fromBoundary == null || !g.getDataOra().isBefore(fromBoundary))
                .sorted(Comparator.comparing(Glicemia::getDataOra))
                .toList();

        if (fromBoundary == null || filtered.isEmpty()) {
            periodLabel.setText(filter);
        } else {
            LocalDate start = filtered.get(0).getDataOra().toLocalDate();
            LocalDate end   = filtered.get(filtered.size() - 1).getDataOra().toLocalDate();
            periodLabel.setText("Periodo: " + start + " - " + end);
        }

        updateKpi(filtered);
        updateChart(filtered);
    }

    private void updateKpi(List<Glicemia> data) {
        if (data.isEmpty()) {
            avgLabel.setText("–");
            minLabel.setText("–");
            maxLabel.setText("–");
            countLabel.setText("0");
            return;
        }

        int count = data.size();
        int min = data.stream().mapToInt(Glicemia::getValore).min().orElse(0);
        int max = data.stream().mapToInt(Glicemia::getValore).max().orElse(0);
        double avg = data.stream().mapToInt(Glicemia::getValore).average().orElse(0.0);

        avgLabel.setText(String.format("%.0f", avg));
        minLabel.setText(String.valueOf(min));
        maxLabel.setText(String.valueOf(max));
        countLabel.setText(String.valueOf(count));
    }

    private void updateChart(List<Glicemia> data) {
        reportChart.getData().clear();

        if (data.isEmpty()) {
            return;
        }

        XYChart.Series<String, Number> series = new XYChart.Series<>();

        data.stream()
                .sorted(Comparator.comparing(Glicemia::getDataOra))
                .forEach(g -> {
                    String label = g.getDataOra().format(chartFormatter);
                    series.getData().add(new XYChart.Data<>(label, g.getValore()));
                });

        reportChart.getData().add(series);

        int min = data.stream().mapToInt(Glicemia::getValore).min().orElse(0);
        int max = data.stream().mapToInt(Glicemia::getValore).max().orElse(0);

        yAxis.setLowerBound(min - 10);
        yAxis.setUpperBound(max + 10);
        yAxis.setTickUnit(
                Math.max(5, (yAxis.getUpperBound() - yAxis.getLowerBound()) / 6.0)
        );
    }

    // ───────────────────────────── TERAPIA / BOX DESTRA ─────────────────────

    /**
     * Carica le terapie del paziente e mostra quella rilevante nel periodo selezionato.
     * (per ora la prima terapia che interseca il periodo).
     */
    private void loadTherapyForPeriod() {
        try {
            if (terapiePaziente.isEmpty()) {
                terapiePaziente = terapiaDAO.findByPazienteId(CodiceFiscale);
            }

            if (terapiePaziente.isEmpty()) {
                showNoTherapy();
                return;
            }

            String filter = periodFilter.getValue();
            LocalDate today = LocalDate.now();
            LocalDate startDate;

            switch (filter) {
                case "Ultimi 7 giorni"  -> startDate = today.minusDays(6);
                case "Ultimi 30 giorni" -> startDate = today.minusDays(29);
                case "Ultimi 90 giorni" -> startDate = today.minusDays(89);
                default                 -> startDate = null; // "Tutto"
            }

            LocalDate endDate = today;

            Terapia selected = terapiePaziente.stream()
                    .filter(t -> intersectsPeriod(t, startDate, endDate))
                    .findFirst()
                    .orElse(null);

            if (selected == null) {
                therapyStatusLabel.setText("Nessuna terapia nel periodo");
                therapyDrugLabel.setText("—");
                therapyDoseLabel.setText("—");
                therapyFreqLabel.setText("—");
                therapyPeriodLabel.setText("—");
                return;
            }

            fillTherapyDetails(selected);

        } catch (Exception e) {
            e.printStackTrace();
            showNoTherapy();
        }
    }

    /**
     * Versione "generale" usata da loadReportData (se la userai ancora).
     * Prende semplicemente la prima terapia del paziente.
     */
    private void loadTherapySummary() {
        try {
            List<Terapia> list = terapiaDAO.findByPazienteId(CodiceFiscale);
            if (list.isEmpty()) {
                showNoTherapy();
                return;
            }

            Terapia t = list.get(0); // per ora la prima / quella attiva
            fillTherapyDetails(t);

        } catch (Exception e) {
            e.printStackTrace();
            showNoTherapy();
        }
    }

    /**
     * Popola i label terapia (farmaco, dose, frequenza, periodo, stato)
     * usando la nuova tabella TerapiaFarmaco.
     */
    private void fillTherapyDetails(Terapia terapia) throws Exception {
        if (terapia == null) {
            showNoTherapy();
            return;
        }

        // Stato terapia in base alle date
        LocalDate oggi = LocalDate.now();
        LocalDate inizio = terapia.getDataInizio();
        LocalDate fine   = terapia.getDataFine();

        if (inizio != null && oggi.isBefore(inizio)) {
            therapyStatusLabel.setText("Non iniziata");
        } else if (fine != null && oggi.isAfter(fine)) {
            therapyStatusLabel.setText("Completata");
        } else {
            therapyStatusLabel.setText("In corso");
        }

        if (fine != null) {
            therapyPeriodLabel.setText("dal " + inizio + " al " + fine);
        } else if (inizio != null) {
            therapyPeriodLabel.setText("dal " + inizio);
        } else {
            therapyPeriodLabel.setText("—");
        }

        // Recupero i farmaci della terapia
        List<TerapiaFarmaco> farmaci = terapiaFarmacoDAO.findByTerapiaId(terapia.getId());

        if (farmaci.isEmpty()) {
            therapyDrugLabel.setText("Terapia senza farmaci");
            therapyDoseLabel.setText("—");
            therapyFreqLabel.setText("—");
            return;
        }

        TerapiaFarmaco tf = farmaci.get(0); // per ora mostriamo il primo
        String nomeFarmaco;
        if (tf.getFarmaco() != null) {
            nomeFarmaco = tf.getFarmaco().getNome();
        } else {
            nomeFarmaco = "Farmaco ID " + tf.getIdFarmaco();
        }

        if (farmaci.size() > 1) {
            therapyDrugLabel.setText(nomeFarmaco + " (+ " + (farmaci.size() - 1) + " altri)");
        } else {
            therapyDrugLabel.setText(nomeFarmaco);
        }

        therapyDoseLabel.setText(tf.getQuantitaAssunzione() + " unità");
        therapyFreqLabel.setText(tf.getAssunzioniGiornaliere() + " volte al giorno");
    }

    private void showNoTherapy() {
        therapyStatusLabel.setText("Nessuna terapia");
        therapyDrugLabel.setText("—");
        therapyDoseLabel.setText("—");
        therapyFreqLabel.setText("—");
        therapyPeriodLabel.setText("—");
    }

    private boolean intersectsPeriod(Terapia t, LocalDate start, LocalDate end) {
        LocalDate inizio = t.getDataInizio();
        LocalDate fine   = t.getDataFine(); // può essere null = ancora in corso

        if (start == null) return true; // "Tutto": qualsiasi terapia va bene

        if (inizio == null && fine == null) {
            // se per qualche motivo sono null entrambi, la consideriamo sempre valida
            return true;
        }

        if (fine == null) {
            // terapia ancora in corso: basta che sia iniziata prima della fine
            return inizio == null || !inizio.isAfter(end);
        }

        // intervallo [inizio, fine] interseca [start, end]
        return !fine.isBefore(start) && !inizio.isAfter(end);
    }

    // ───────────────────────────── LEGACY (se lo usi ancora) ─────────────────

    private void loadReportData() {
        if (CodiceFiscale == null) return;

        LocalDate today = LocalDate.now();
        LocalDate from = today.minusDays(30);

        periodLabel.setText(
                "Periodo: " + from.format(periodFormatter) + " - " + today.format(periodFormatter)
        );

        try {
            List<Glicemia> all = glicemiaDAO.findByPazienteId(CodiceFiscale);

            List<Glicemia> last30 = all.stream()
                    .filter(g -> g.getDataOra() != null &&
                            !g.getDataOra().toLocalDate().isBefore(from))
                    .sorted(Comparator.comparing(Glicemia::getDataOra))
                    .toList();

            updateKpi(last30);
            updateChart(last30);
            loadTherapySummary();

            symptomsLabel.setText("Funzionalità in sviluppo");
            risksLabel.setText("Funzionalità in sviluppo");
            pathologiesLabel.setText("Funzionalità in sviluppo");

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}