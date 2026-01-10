package it.univr.diabete.controller;

import it.univr.diabete.MainApp;
import it.univr.diabete.dao.PazienteDAO;
import it.univr.diabete.dao.impl.PazienteDAOImpl;
import it.univr.diabete.model.Paziente;
import it.univr.diabete.ui.ConfirmDialog;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Label;
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
import javafx.scene.control.TextField;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.geometry.Insets;

import java.util.List;

public class AdminPatientsController {

    @FXML private TextField searchField;
    @FXML private ListView<Paziente> patientsListView;

    private final PazienteDAO pazienteDAO = new PazienteDAOImpl();
    private final ObservableList<Paziente> allPatients = FXCollections.observableArrayList();
    private FilteredList<Paziente> filteredPatients;

    @FXML
    private void initialize() {
        loadPatients();
        filteredPatients = new FilteredList<>(allPatients, p -> true);
        patientsListView.setItems(filteredPatients);
        patientsListView.setCellFactory(lv -> createPatientCardCell());

        searchField.textProperty().addListener((obs, oldV, newV) -> {
            String q = newV == null ? "" : newV.toLowerCase().trim();
            filteredPatients.setPredicate(p -> {
                if (q.isEmpty()) return true;
                String fullName = (p.getNome() + " " + p.getCognome()).toLowerCase();
                String email = p.getEmail() != null ? p.getEmail().toLowerCase() : "";
                String cf = p.getCodiceFiscale() != null ? p.getCodiceFiscale().toLowerCase() : "";
                String fk = p.getFkDiabetologo() != null ? p.getFkDiabetologo().toLowerCase() : "";
                return fullName.contains(q) || email.contains(q) || cf.contains(q) || fk.contains(q);
            });
        });
    }

    private void loadPatients() {
        try {
            List<Paziente> list = pazienteDAO.findAll();
            allPatients.setAll(list);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void reloadPatients() {
        loadPatients();
    }

    @FXML
    private void handleCreatePatient() {
        try {
            FXMLLoader loader = new FXMLLoader(MainApp.class.getResource("/fxml/AddPatientView.fxml"));
            Parent root = loader.load();

            AddPatientController ctrl = loader.getController();
            ctrl.initAdminMode(this::reloadPatients);

            Stage popup = new Stage();
            popup.initModality(Modality.WINDOW_MODAL);
            popup.setScene(new Scene(root));
            popup.setTitle("Nuovo paziente");
            popup.showAndWait();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private ListCell<Paziente> createPatientCardCell() {
        return new ListCell<>() {

            private final HBox root = new HBox();
            private final VBox textBox = new VBox(4);
            private final Label nameLbl = new Label();
            private final Label metaLbl = new Label();
            private final HBox actions = new HBox(6);
            private final Button deleteBtn = new Button("Elimina");

            {
                root.getStyleClass().add("patient-card");
                root.setPadding(new Insets(0));

                nameLbl.getStyleClass().add("patient-card-name");
                metaLbl.getStyleClass().add("patient-card-sub");
                textBox.getStyleClass().add("patient-card-main");
                textBox.getChildren().addAll(nameLbl, metaLbl);

                deleteBtn.getStyleClass().add("btn-ghost");
                actions.getStyleClass().add("patient-card-actions");
                actions.getChildren().add(deleteBtn);

                Region spacer = new Region();
                HBox.setHgrow(spacer, Priority.ALWAYS);
                root.getChildren().addAll(textBox, spacer, actions);
                root.setOnMouseClicked(e -> {
                    Paziente p = getItem();
                    if (p != null) {
                        openEditPatient(p);
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
                    String email = p.getEmail() != null ? p.getEmail() : "—";
                    String cf = p.getCodiceFiscale() != null ? p.getCodiceFiscale() : "—";
                    String fk = p.getFkDiabetologo() != null ? p.getFkDiabetologo() : "—";
                    metaLbl.setText(email + "  •  CF: " + cf + "  •  " + fk);
                    deleteBtn.setOnAction(e -> {
                        e.consume();
                        deletePatient(p);
                    });
                    deleteBtn.setOnMouseClicked(e -> e.consume());
                    setGraphic(root);
                    setText(null);
                }
            }
        };
    }

    private void openEditPatient(Paziente paziente) {
        try {
            FXMLLoader loader = new FXMLLoader(MainApp.class.getResource("/fxml/AddPatientView.fxml"));
            Parent root = loader.load();

            AddPatientController ctrl = loader.getController();
            ctrl.initEditAdmin(paziente, this::reloadPatients);

            Stage popup = new Stage();
            popup.initModality(Modality.WINDOW_MODAL);
            popup.setScene(new Scene(root));
            popup.setTitle("Modifica paziente");
            popup.showAndWait();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void deletePatient(Paziente paziente) {
        boolean confirmed = ConfirmDialog.show(
                "Elimina paziente",
                "Confermi eliminazione di " + paziente.getNome() + " " + paziente.getCognome() + "?",
                "Questa azione non può essere annullata."
        );
        if (!confirmed) return;

        try {
            pazienteDAO.deleteById(paziente.getCodiceFiscale());
            reloadPatients();
        } catch (Exception e) {
            e.printStackTrace();
            Alert alert = new Alert(Alert.AlertType.ERROR, "Impossibile eliminare il paziente.", ButtonType.OK);
            alert.setHeaderText("Errore eliminazione");
            alert.showAndWait();
        }
    }
}
