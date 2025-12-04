package it.univr.diabete.controller;

import it.univr.diabete.MainApp;
import it.univr.diabete.dao.AssunzioneTerapiaDAO;
import it.univr.diabete.dao.GlicemiaDAO;
import it.univr.diabete.dao.TerapiaDAO;
import it.univr.diabete.dao.impl.AssunzioneTerapiaDAOImpl;
import it.univr.diabete.dao.impl.GlicemiaDAOImpl;
import it.univr.diabete.dao.impl.TerapiaDAOImpl;
import it.univr.diabete.model.AssunzioneTerapia;
import it.univr.diabete.model.Glicemia;
import it.univr.diabete.model.Terapia;
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
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.stage.Modality;
import javafx.stage.Stage;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;
import javafx.scene.control.CheckBox;
import java.time.LocalDate;
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

    // checkbox "Oggi"
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
    private final AssunzioneTerapiaDAO assunzioneDAO = new AssunzioneTerapiaDAOImpl();
    @FXML
    private ChoiceBox<String> measurementFilter;

    private Integer patientId;

    private final GlicemiaDAO glicemiaDAO = new GlicemiaDAOImpl();

    // tutti i record scaricati dal DB per questo paziente
    private final ObservableList<Glicemia> allMeasurements =
            FXCollections.observableArrayList();

    private boolean isSameDay(LocalDateTime dt, LocalDate day) {
        return dt != null && dt.toLocalDate().isEqual(day);
    }
    @FXML
    private void initialize() {

        // --- colonne tabella ---
        colMeasDate.setCellValueFactory(cell -> {
            Glicemia g = cell.getValue();
            LocalDateTime dt = g.getDataOra();  // <-- se nel model hai getDataOra(), cambia qui
            String text = (dt != null) ? dt.toString() : "";
            return new SimpleStringProperty(text);
        });

        colMeasValue.setCellValueFactory(cell ->
                new SimpleIntegerProperty(cell.getValue().getValore()).asObject()
        );
        colDashMoment.setCellValueFactory(c ->
                new SimpleStringProperty(c.getValue().getMomento())
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
    }

    /**
     * Chiamato dalla MainShell quando logga un paziente.
     */
    public void setPatientData(String fullName, int id) {
        this.patientId = id;
        patientNameLabel.setText(fullName);
        loadMeasurements();   // carica dal DB e applica filtro
    }

    /**
     * Legge tutte le glicemie dal DAO e ricarica la tabella
     */
    private void loadMeasurements() {
        if (patientId == null) return;

        try {
            List<Glicemia> lista = glicemiaDAO.findByPazienteId(patientId);
            allMeasurements.setAll(lista);
            applyFilterAndRefresh();
            updateTodayTasks();
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
                .filter(g -> g.getDataOra() != null) // o getDataOra()
                .filter(g -> {
                    LocalDateTime dt = g.getDataOra();
                    return switch (filterValue) {
                        case "Oggi" -> dt.toLocalDate().isEqual(now.toLocalDate());
                        case "Ultimi 7 giorni" -> !dt.isBefore(now.minusDays(7));
                        case "Ultimi 30 giorni" -> !dt.isBefore(now.minusDays(30));
                        default -> true;  // "Tutte"
                    };
                })
                // opzionale: le più recenti per prime
                .sorted(Comparator.comparing(Glicemia::getDataOra).reversed())
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
            controller.initData(patientId, this::loadMeasurements);

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
    private void handleOpenTerapia() {
        MainShellController shell = MainApp.getMainShellController();
        shell.handleTherapyNav();
    }
    private void updateTodayGlycemiaTasks() {
        LocalDate today = LocalDate.now();

        List<Glicemia> lista = measurementsTable.getItems(); // o allMeasurements

        boolean mattinoDone = lista.stream()
                .anyMatch(g -> isSameDay(g.getDataOra(), today)
                        && "Mattino".equalsIgnoreCase(g.getMomento()));

        boolean pranzoDone = lista.stream()
                .anyMatch(g -> isSameDay(g.getDataOra(), today)
                        && "Pranzo".equalsIgnoreCase(g.getMomento()));

        boolean cenaDone = lista.stream()
                .anyMatch(g -> isSameDay(g.getDataOra(), today)
                        && "Cena".equalsIgnoreCase(g.getMomento()));

        chkMattino.setSelected(mattinoDone);
        chkPranzo.setSelected(pranzoDone);
        chkCena.setSelected(cenaDone);
    }
    private void updateTodayTherapyTask() {
        if (patientId == null) {
            chkTerapiaAssunta.setSelected(false);
            return;
        }

        try {
            // Recupera la/e terapia/e del paziente
            List<Terapia> terapie = terapiaDAO.findByPazienteId(patientId);

            if (terapie.isEmpty()) {
                // nessuna terapia → checkbox disabilitata
                chkTerapiaAssunta.setSelected(false);
                chkTerapiaAssunta.setDisable(true);
                return;
            }

            // per ora uso la prima (o quella "corrente")
            Terapia terapiaCorrente = terapie.get(0);

            // tutte le assunzioni di quella terapia
            List<AssunzioneTerapia> assunzioni =
                    assunzioneDAO.findByPazienteAndTerapia(patientId, terapiaCorrente.getId());

            LocalDate today = LocalDate.now();

            int quantitaAssuntaOggi = assunzioni.stream()
                    .filter(a -> isSameDay(a.getDateStamp(), today))
                    .mapToInt(AssunzioneTerapia::getQuantitaAssunta)
                    .sum();

            // quantità giornaliera prevista = quantita per assunzione * assunzioni al giorno
            int targetGiornaliero =
                    terapiaCorrente.getQuantita() * terapiaCorrente.getAssunzioniGiornaliere();

            boolean terapiaCompletaOggi = quantitaAssuntaOggi >= targetGiornaliero;

            chkTerapiaAssunta.setDisable(false);
            chkTerapiaAssunta.setSelected(terapiaCompletaOggi);

        } catch (Exception e) {
            e.printStackTrace();
            // in caso di errore non blocchiamo la UI
            chkTerapiaAssunta.setSelected(false);
        }
    }
    private void updateTodayTasks() {
        updateTodayGlycemiaTasks();
        updateTodayTherapyTask();
    }
}