package it.univr.diabete.controller;

import it.univr.diabete.dao.GlicemiaDAO;
import it.univr.diabete.dao.TerapiaDAO;
import it.univr.diabete.dao.impl.GlicemiaDAOImpl;
import it.univr.diabete.dao.impl.TerapiaDAOImpl;
import it.univr.diabete.model.Glicemia;
import it.univr.diabete.model.Terapia;
import javafx.fxml.FXML;
import javafx.scene.chart.CategoryAxis;
import javafx.scene.chart.LineChart;
import javafx.scene.chart.NumberAxis;
import javafx.scene.chart.XYChart;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Collections;

import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

public class PatientReportController {

    @FXML private ComboBox<String> periodFilter;
    @FXML private Label periodLabel;

    @FXML
    private Label symptomsLabel;

    @FXML
    private Label risksLabel;

    @FXML
    private Label pathologiesLabel;

    @FXML
    private Label therapyDrugLabel;

    @FXML
    private Label therapyDoseLabel;

    @FXML
    private Label therapyFreqLabel;

    @FXML
    private Label therapyPeriodLabel;

    @FXML
    private Label patientNameLabel;


    @FXML
    private Label avgLabel;

    @FXML
    private Label minLabel;

    @FXML
    private Label maxLabel;

    @FXML
    private Label countLabel;

    @FXML
    private Label therapyLabel;

    @FXML
    private Label therapyStatusLabel;

    @FXML
    private LineChart<String, Number> reportChart;

    @FXML
    private CategoryAxis xAxis;

    @FXML
    private NumberAxis yAxis;

    private final GlicemiaDAO glicemiaDAO = new GlicemiaDAOImpl();
    private final TerapiaDAO terapiaDAO = new TerapiaDAOImpl();
    private List<Glicemia> allMeasurements = new ArrayList<>();
    private List<Terapia> terapiePaziente = new ArrayList<>();
    private Integer patientId;

    private final DateTimeFormatter chartFormatter =
            DateTimeFormatter.ofPattern("dd/MM");

    private final DateTimeFormatter periodFormatter =
            DateTimeFormatter.ofPattern("dd/MM/yyyy");

    /**
     * Chiamato dal MainShellController quando carica il report.
     */
    public void setPatientContext(String nomeCompleto, int patientId) {
        this.patientId = patientId;
        patientNameLabel.setText(nomeCompleto);

        loadAllMeasurements();   // tutte dal DB
        setupPeriodFilter();     // inizializza combo
        applyPeriodAndRefresh(); // filtro di default (Ultimi 30 giorni)
        loadTherapyForPeriod();  // terapia nel periodo (vedi sotto)
    }
    private void loadAllMeasurements() {
        try {
            allMeasurements = glicemiaDAO.findByPazienteId(patientId);
            // Ordinate per data/ora crescente
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

        // default
        periodFilter.getSelectionModel().select("Ultimi 30 giorni");

        // quando cambia il valore, ricalcola il report
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

        // 👉 variabile final da usare nella lambda
        final LocalDateTime fromBoundary = fromDateTime;

        List<Glicemia> filtered = allMeasurements.stream()
                .filter(g -> fromBoundary == null || !g.getDataOra().isBefore(fromBoundary))
                .sorted(Comparator.comparing(Glicemia::getDataOra))
                .toList();

        // testo sotto al filtro
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
    private void loadTherapyForPeriod() {
        try {
            // se non l'hai ancora caricata dal DAO, fallo qui una sola volta
            if (terapiePaziente.isEmpty()) {
                terapiePaziente = terapiaDAO.findByPazienteId(patientId);
            }

            if (terapiePaziente.isEmpty()) {
                therapyStatusLabel.setText("Nessuna terapia");
                therapyDrugLabel.setText("—");
                therapyDoseLabel.setText("—");
                therapyFreqLabel.setText("—");
                therapyPeriodLabel.setText("—");
                return;
            }

            // calcolo range attuale dal testo del label oppure dal filtro
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

            // scegli una terapia che interseca il periodo (la prima che trovi)
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

            // status
            LocalDate oggi = LocalDate.now();
            if (selected.getDataInizio().isAfter(oggi)) {
                therapyStatusLabel.setText("Non iniziata");
            } else if (selected.getDataFine() != null && selected.getDataFine().isBefore(oggi)) {
                therapyStatusLabel.setText("Completata");
            } else {
                therapyStatusLabel.setText("In corso");
            }

            therapyDrugLabel.setText(selected.getFarmacoNome());
            therapyDoseLabel.setText(selected.getFarmacoDosaggio() + " mg");
            therapyFreqLabel.setText(selected.getAssunzioniGiornaliere() + " volte al giorno");

            if (selected.getDataFine() != null) {
                therapyPeriodLabel.setText("dal " + selected.getDataInizio() +
                        " al " + selected.getDataFine());
            } else {
                therapyPeriodLabel.setText("dal " + selected.getDataInizio());
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private boolean intersectsPeriod(Terapia t, LocalDate start, LocalDate end) {
        LocalDate inizio = t.getDataInizio();
        LocalDate fine   = t.getDataFine(); // può essere null = ancora in corso

        if (start == null) return true; // "Tutto": qualsiasi terapia va bene

        if (fine == null) {
            // terapia ancora in corso: basta che sia iniziata prima della fine
            return !inizio.isAfter(end);
        }

        // intervallo [inizio, fine] interseca [start, end]
        return !fine.isBefore(start) && !inizio.isAfter(end);
    }
    private void loadReportData() {
        if (patientId == null) return;

        LocalDate today = LocalDate.now();
        LocalDate from = today.minusDays(30);

        periodLabel.setText(
                "Periodo: " + from.format(periodFormatter) + " - " + today.format(periodFormatter)
        );

        try {
            List<Glicemia> all = glicemiaDAO.findByPazienteId(patientId);

            List<Glicemia> last30 = all.stream()
                    .filter(g -> g.getDataOra() != null &&
                            !g.getDataOra().toLocalDate().isBefore(from))
                    .sorted(Comparator.comparing(Glicemia::getDataOra))
                    .toList();

            updateKpi(last30);
            updateChart(last30);
            loadTherapySummary();

            // TODO: quando avrai i DAO per sintomi / fattori / patologie,
            // qui li carichi dal DB.
            symptomsLabel.setText("Funzionalità in sviluppo");
            risksLabel.setText("Funzionalità in sviluppo");
            pathologiesLabel.setText("Funzionalità in sviluppo");

        } catch (Exception e) {
            e.printStackTrace();
        }
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

        // sistema range asse Y
        int min = data.stream().mapToInt(Glicemia::getValore).min().orElse(0);
        int max = data.stream().mapToInt(Glicemia::getValore).max().orElse(0);

        if (min == max) {
            yAxis.setLowerBound(min - 10);
            yAxis.setUpperBound(max + 10);
        } else {
            yAxis.setLowerBound(min - 10);
            yAxis.setUpperBound(max + 10);
        }
        yAxis.setTickUnit(Math.max(5, (yAxis.getUpperBound() - yAxis.getLowerBound()) / 6.0));
    }

    private void loadTherapySummary() {
        try {
            List<Terapia> list = terapiaDAO.findByPazienteId(patientId);
            if (list.isEmpty()) {
                therapyDrugLabel.setText("Nessuna terapia");
                therapyDoseLabel.setText("—");
                therapyFreqLabel.setText("—");
                therapyPeriodLabel.setText("—");
                therapyStatusLabel.setText("Nessuna");
                return;
            }

            Terapia t = list.get(0); // per ora la prima / quella attiva

            therapyDrugLabel.setText(t.getFarmacoNome());
            therapyDoseLabel.setText(t.getFarmacoDosaggio() + " mg");
            therapyFreqLabel.setText(t.getAssunzioniGiornaliere() + " volte al giorno");

            if (t.getDataFine() != null) {
                therapyPeriodLabel.setText(
                        "dal " + t.getDataInizio() + " al " + t.getDataFine()
                );
            } else {
                therapyPeriodLabel.setText("dal " + t.getDataInizio());
            }

            // stato semplice in base alle date
            LocalDate oggi = LocalDate.now();
            LocalDate inizio = t.getDataInizio();
            LocalDate fine = t.getDataFine();

            String stato;
            if (inizio != null && oggi.isBefore(inizio)) {
                stato = "Non iniziata";
            } else if (fine != null && oggi.isAfter(fine)) {
                stato = "Conclusa";
            } else {
                stato = "In corso";
            }
            therapyStatusLabel.setText(stato);

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}