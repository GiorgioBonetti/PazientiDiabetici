package it.univr.diabete.controller;

import it.univr.diabete.MainApp;
import it.univr.diabete.dao.GlicemiaDAO;
import it.univr.diabete.dao.impl.GlicemiaDAOImpl;
import it.univr.diabete.model.Glicemia;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.chart.CategoryAxis;
import javafx.scene.chart.LineChart;
import javafx.scene.chart.NumberAxis;
import javafx.scene.chart.XYChart;
import javafx.scene.control.*;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.control.Button;

import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

public class PatientMeasurementsController {

    // 🔹 AGGIUNGI QUESTO: collega al bottone "Nuova misurazione" nel FXML
    @FXML
    private Button addGlycemiaButton;

    @FXML
    private Label patientNameLabel;

    @FXML
    private TableView<Glicemia> measurementsTable;

    @FXML
    private TableColumn<Glicemia, String> colMeasDateTime;

    @FXML
    private TableColumn<Glicemia, Integer> colMeasValue;

    @FXML
    private TableColumn<Glicemia, String> colMeasMoment;

    @FXML
    private ComboBox<String> filterCombo;

    @FXML
    private LineChart<String, Number> glycemiaChart;

    @FXML
    private CategoryAxis xAxis;

    @FXML
    private NumberAxis yAxis;

    private final GlicemiaDAO glicemiaDAO = new GlicemiaDAOImpl();

    private Integer patientId;

    private final DateTimeFormatter formatter =
            DateTimeFormatter.ofPattern("dd/MM HH:mm");

    private final ObservableList<Glicemia> allMeasurements =
            FXCollections.observableArrayList();

    // 🔹 AGGIUNGI QUESTO: teniamo un riferimento alla colonna della matita
    private TableColumn<Glicemia, Void> colEdit;

    @FXML
    private void initialize() {
        // colonne tabella
        colMeasDateTime.setCellValueFactory(cell -> {
            Glicemia g = cell.getValue();
            String text = (g.getDataOra() != null)
                    ? g.getDataOra().format(formatter)
                    : "";
            return new SimpleStringProperty(text);
        });

        colMeasValue.setCellValueFactory(cell ->
                new SimpleIntegerProperty(cell.getValue().getValore()).asObject()
        );

        colMeasMoment.setCellValueFactory(c ->
                new SimpleStringProperty(c.getValue().getMomento())
        );

        // opzioni filtro
        filterCombo.setItems(FXCollections.observableArrayList(
                "Tutte",
                "Oggi",
                "Ultimi 7 giorni",
                "Ultimi 30 giorni"
        ));
        filterCombo.getSelectionModel().select("Tutte");

        filterCombo.getSelectionModel()
                .selectedItemProperty()
                .addListener((obs, oldV, newV) -> applyFilterAndRefresh());

        // setup grafico
        glycemiaChart.setAnimated(false);
        yAxis.setForceZeroInRange(false);

        // colonna matita solo lato paziente (di default)
        addEditColumn();
    }

    public void setPatientContext(String fullName, int id) {
        this.patientId = id;
        patientNameLabel.setText(fullName);
        loadMeasurements();
    }

    private void loadMeasurements() {
        if (patientId == null) return;

        try {
            List<Glicemia> lista = glicemiaDAO.findByPazienteId(patientId);
            allMeasurements.setAll(lista);
            applyFilterAndRefresh();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void applyFilterAndRefresh() {
        if (allMeasurements.isEmpty()) {
            measurementsTable.getItems().clear();
            glycemiaChart.getData().clear();
            return;
        }

        final String filterValue =
                (filterCombo.getSelectionModel().getSelectedItem() == null)
                        ? "Tutte"
                        : filterCombo.getSelectionModel().getSelectedItem();

        LocalDateTime now = LocalDateTime.now();

        List<Glicemia> filtered = allMeasurements.stream()
                .filter(g -> g.getDataOra() != null)
                .filter(g -> {
                    LocalDateTime dt = g.getDataOra();
                    return switch (filterValue) {
                        case "Oggi" -> dt.toLocalDate().isEqual(now.toLocalDate());
                        case "Ultimi 7 giorni" -> !dt.isBefore(now.minusDays(7));
                        case "Ultimi 30 giorni" -> !dt.isBefore(now.minusDays(30));
                        default -> true;
                    };
                })
                .sorted(Comparator.comparing(Glicemia::getDataOra).reversed())
                .collect(Collectors.toList());

        measurementsTable.getItems().setAll(filtered);
        refreshChart(filtered);
    }

    private void refreshChart(List<Glicemia> data) {
        glycemiaChart.getData().clear();

        if (data.isEmpty()) return;

        List<Glicemia> sorted = data.stream()
                .sorted(Comparator.comparing(Glicemia::getDataOra))
                .toList();

        XYChart.Series<String, Number> series = new XYChart.Series<>();

        for (Glicemia g : sorted) {
            String label = g.getDataOra().format(formatter);
            series.getData().add(new XYChart.Data<>(label, g.getValore()));
        }

        glycemiaChart.getData().add(series);

        int min = sorted.stream().mapToInt(Glicemia::getValore).min().orElse(0);
        int max = sorted.stream().mapToInt(Glicemia::getValore).max().orElse(0);

        if (min == max) {
            yAxis.setLowerBound(min - 10);
            yAxis.setUpperBound(max + 10);
        } else {
            yAxis.setLowerBound(min - 10);
            yAxis.setUpperBound(max + 10);
        }

        yAxis.setTickUnit(Math.max(5, (yAxis.getUpperBound() - yAxis.getLowerBound()) / 6));
    }

    @FXML
    private void handleAddGlicemia() {
        if (patientId == null) return;

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

    /** Colonna con la matita ✏️ */
    private void addEditColumn() {
        colEdit = new TableColumn<>("");

        colEdit.setCellFactory(column -> new TableCell<>() {
            private final Button editBtn = new Button("✏️");

            {
                editBtn.getStyleClass().add("edit-btn");
                editBtn.setOnAction(event -> {
                    Glicemia g = getTableView().getItems().get(getIndex());
                    openEditGlicemiaPopup(g);
                });
            }

            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                if (empty) {
                    setGraphic(null);
                } else {
                    setGraphic(editBtn);
                }
            }
        });

        measurementsTable.getColumns().add(colEdit);
    }

    private void openEditGlicemiaPopup(Glicemia g) {
        try {
            FXMLLoader loader = new FXMLLoader(MainApp.class.getResource("/fxml/EditGlicemiaView.fxml"));
            Parent root = loader.load();

            EditGlicemiaController ctrl = loader.getController();
            ctrl.init(g, this::loadMeasurements);

            Stage popup = new Stage();
            popup.initOwner(measurementsTable.getScene().getWindow());
            popup.setTitle("Modifica glicemia");
            popup.setScene(new Scene(root));
            popup.showAndWait();

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    // 🔥 METODO CHIAVE: lo chiameremo SOLO quando la vista è aperta dal diabetologo
    public void hideEditingTools() {
        // nascondi bottone "Nuova misurazione"
        if (addGlycemiaButton != null) {
            addGlycemiaButton.setVisible(false);
            addGlycemiaButton.setManaged(false);
        }
        // rimuovi colonna matita
        if (colEdit != null) {
            measurementsTable.getColumns().remove(colEdit);
        }
        // opzionale: disabilita anche il doppio click, se decidi di aggiungerlo in futuro
        measurementsTable.setOnMouseClicked(null);
    }
}