package it.univr.diabete.controller;

import it.univr.diabete.MainApp;
import it.univr.diabete.dao.PazienteDAO;
import it.univr.diabete.dao.impl.PazienteDAOImpl;
import it.univr.diabete.model.Paziente;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Insets;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.Pane;
import javafx.scene.layout.VBox;
import javafx.stage.Modality;
import javafx.stage.Stage;

import java.io.IOException;
import java.util.List;

public class DoctorDashboardController {

    private String idDiabetologoLoggato;

    @FXML
    private ListView<Paziente> patientsListView;

    @FXML
    private TextField patientSearchField;

    private final PazienteDAO pazienteDAO = new PazienteDAOImpl();

    private final ObservableList<Paziente> allPatients = FXCollections.observableArrayList();
    private FilteredList<Paziente> filteredPatients;

    // chiamato dal MainShellController
    public void setDoctorContext(String doctorId) {
        this.idDiabetologoLoggato = doctorId;
        // se vuoi filtrare i pazienti per diabetologo, lo fai qui
        reloadPatients();
    }

    @FXML
    private void initialize() {
        setupPatientsList();
    }

    private void reloadPatients() {
        setupPatientsList();
    }

    private void setupPatientsList() {
        try {
            // carica pazienti
            List<Paziente> lista = pazienteDAO.findAll();
            allPatients.setAll(lista);

            // opzionale: ultimi creati in alto
            //allPatients.sort((a, b) -> Integer.compare(b.getId(), a.getId()));

            // filtered list
            filteredPatients = new FilteredList<>(allPatients, p -> true);
            patientsListView.setItems(filteredPatients);

            // 🔥 card-style cells
            patientsListView.setCellFactory(lv -> createPatientCardCell());

            // filtro ricerca
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

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @FXML
    private void handleAddPatient() {
        try {
            FXMLLoader loader = new FXMLLoader(MainApp.class.getResource("/fxml/AddPatientView.fxml"));
            Parent root = loader.load();

            AddPatientController ctrl = loader.getController();
            ctrl.initData(idDiabetologoLoggato, this::reloadPatients);

            Stage popup = new Stage();
            popup.initModality(Modality.WINDOW_MODAL);
            popup.setScene(new Scene(root));
            popup.setTitle("Nuovo paziente");
            popup.showAndWait();

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
}