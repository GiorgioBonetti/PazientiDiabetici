package it.univr.diabete.controller;
import it.univr.diabete.MainApp;
import it.univr.diabete.dao.PazienteDAO;
import it.univr.diabete.dao.impl.PazienteDAOImpl;
import it.univr.diabete.model.Paziente;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.fxml.FXML;
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
import javafx.scene.control.TextField;

import java.util.List;

public class DoctorDashboardController {

    @FXML
    private ListView<Paziente> patientsListView;

    @FXML
    private TextField patientSearchField;

    private final PazienteDAO pazienteDAO = new PazienteDAOImpl();

    private ObservableList<Paziente> allPatients = FXCollections.observableArrayList();
    private FilteredList<Paziente> filteredPatients;

    @FXML
    private void initialize() {
        setupPatientsList();
        patientsListView.setOnMouseClicked(event -> {
            if (event.getClickCount() == 2) {
                Paziente selected = patientsListView
                        .getSelectionModel()
                        .getSelectedItem();
                if (selected != null) {
                    MainShellController shell = MainApp.getMainShellController();
                    if (shell != null) {
                        shell.openPatientDetail(selected);
                    }
                }
            }
        });

    }

    private void setupPatientsList() {
        try {
            // carica tutti i pazienti dal DB
            List<Paziente> lista = pazienteDAO.findAll();
            allPatients.setAll(lista);

            // filtered list per la ricerca
            filteredPatients = new FilteredList<>(allPatients, p -> true);
            patientsListView.setItems(filteredPatients);

            // cell factory carina: Nome Cognome (ID)
            patientsListView.setCellFactory(lv -> new ListCell<>() {
                @Override
                protected void updateItem(Paziente item, boolean empty) {
                    super.updateItem(item, empty);
                    if (empty || item == null) {
                        setText(null);
                    } else {
                        setText(item.getNome() + " " + item.getCognome());
                    }
                }
            });

            // listener per la barra di ricerca
            patientSearchField.textProperty().addListener((obs, oldVal, newVal) -> {
                String query = (newVal == null) ? "" : newVal.toLowerCase().trim();
                filteredPatients.setPredicate(p -> {
                    if (query.isEmpty()) return true;
                    String fullName = (p.getNome() + " " + p.getCognome()).toLowerCase();
                    String idStr = String.valueOf(p.getId());
                    return fullName.contains(query) || idStr.contains(query);
                });
            });

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}