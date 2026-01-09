package it.univr.diabete.controller;

import it.univr.diabete.MainApp;
import it.univr.diabete.dao.AssunzioneDAO;
import it.univr.diabete.dao.GlicemiaDAO;
import it.univr.diabete.dao.TerapiaDAO;
import it.univr.diabete.dao.impl.AssunzioneDAOImpl;
import it.univr.diabete.dao.impl.GlicemiaDAOImpl;
import it.univr.diabete.dao.impl.TerapiaDAOImpl;
import it.univr.diabete.model.Assunzione;
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
import it.univr.diabete.dao.FarmacoTerapiaDAO;
import it.univr.diabete.dao.impl.FarmacoTerapiaDAOImpl;
import it.univr.diabete.model.FarmacoTerapia;
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
    private final AssunzioneDAO assunzioneDAO = new AssunzioneDAOImpl();
    // DAO per terapia / assunzioni
     private final FarmacoTerapiaDAO farmacoTerapiaDAO = new FarmacoTerapiaDAOImpl(); // 👈 nuovo
    @FXML
    private ChoiceBox<String> measurementFilter;

    private String codiceFiscale;

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
            LocalDateTime dt = g.getDateStamp();  // <-- se nel model hai getDateStamp(), cambia qui
            String text = (dt != null) ? dt.toString() : "";
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
    }

    /**
     * Chiamato dalla MainShell quando un paziente effettua il login.
     */
    public void setPatientData(String fullName, String codiceFiscale) {
        this.codiceFiscale = codiceFiscale;
        patientNameLabel.setText(fullName);
        loadMeasurements();   // carica dal DB e applica filtro
    }

    /**
     * Legge tutte le glicemie dal DAO e ricarica la tabella
     */
    private void loadMeasurements() {
        if (codiceFiscale == null) return;

        try {
            List<Glicemia> lista = glicemiaDAO.findByPazienteId(codiceFiscale);
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

            // 4) Target giornaliero atteso: somma di (quantità per assunzione * assunzioni/giorno) per ogni farmaco
            int targetGiornalieroTotale = farmaci.stream()
                    .mapToInt(tf -> tf.getQuantita() * tf.getAssunzioniGiornaliere())
                    .sum();

            // 5) Quantità effettivamente assunta oggi su tutti i farmaci
            int quantitaAssuntaOggiTotale = 0;

            for (FarmacoTerapia tf : farmaci) {
                List<Assunzione> assunzioni =
                        assunzioneDAO.findByPazienteAndTerapiaAndFarmaco(codiceFiscale, tf.getFkFarmaco(), terapiaCorrente.getId());

                int qFarmacoOggi = assunzioni.stream()
                        .filter(a -> isSameDay(a.getDateStamp(), today))
                        .mapToInt(Assunzione::getQuantitaAssunta)
                        .sum();

                quantitaAssuntaOggiTotale += qFarmacoOggi;
            }

            boolean terapiaCompletaOggi = quantitaAssuntaOggiTotale >= targetGiornalieroTotale;

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
}