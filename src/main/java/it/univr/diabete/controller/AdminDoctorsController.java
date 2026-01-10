package it.univr.diabete.controller;

import it.univr.diabete.MainApp;
import it.univr.diabete.dao.DiabetologoDAO;
import it.univr.diabete.dao.impl.DiabetologoDAOImpl;
import it.univr.diabete.model.Diabetologo;
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

public class AdminDoctorsController {

    @FXML private TextField searchField;
    @FXML private ListView<Diabetologo> doctorsListView;

    private final DiabetologoDAO diabetologoDAO = new DiabetologoDAOImpl();
    private final ObservableList<Diabetologo> allDoctors = FXCollections.observableArrayList();
    private FilteredList<Diabetologo> filteredDoctors;

    @FXML
    private void initialize() {
        loadDoctors();
        filteredDoctors = new FilteredList<>(allDoctors, d -> true);
        doctorsListView.setItems(filteredDoctors);
        doctorsListView.setCellFactory(lv -> createDoctorCardCell());

        searchField.textProperty().addListener((obs, oldV, newV) -> {
            String q = newV == null ? "" : newV.toLowerCase().trim();
            filteredDoctors.setPredicate(d -> {
                if (q.isEmpty()) return true;
                String fullName = (d.getNome() + " " + d.getCognome()).toLowerCase();
                String email = d.getEmail() != null ? d.getEmail().toLowerCase() : "";
                String phone = d.getNumeroTelefono() != null ? d.getNumeroTelefono().toLowerCase() : "";
                return fullName.contains(q) || email.contains(q) || phone.contains(q);
            });
        });
    }

    private void loadDoctors() {
        try {
            List<Diabetologo> list = diabetologoDAO.findAll();
            allDoctors.setAll(list);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void reloadDoctors() {
        loadDoctors();
    }

    @FXML
    private void handleCreateDoctor() {
        try {
            FXMLLoader loader = new FXMLLoader(MainApp.class.getResource("/fxml/AddDoctorView.fxml"));
            Parent root = loader.load();

            AddDoctorController ctrl = loader.getController();
            ctrl.initData(this::reloadDoctors);

            Stage popup = new Stage();
            popup.initModality(Modality.WINDOW_MODAL);
            popup.setScene(new Scene(root));
            popup.setTitle("Nuovo diabetologo");
            popup.showAndWait();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private ListCell<Diabetologo> createDoctorCardCell() {
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
                    Diabetologo d = getItem();
                    if (d != null) {
                        openEditDoctor(d);
                    }
                });
            }

            @Override
            protected void updateItem(Diabetologo d, boolean empty) {
                super.updateItem(d, empty);
                if (empty || d == null) {
                    setGraphic(null);
                    setText(null);
                } else {
                    nameLbl.setText(d.getNome() + " " + d.getCognome());
                    String email = d.getEmail() != null ? d.getEmail() : "—";
                    String phone = d.getNumeroTelefono() != null ? d.getNumeroTelefono() : "—";
                    String degree = d.getLaurea() != null ? d.getLaurea() : "—";
                    metaLbl.setText(email + "  •  " + phone + "  •  " + degree);
                    deleteBtn.setOnAction(e -> {
                        e.consume();
                        deleteDoctor(d);
                    });
                    deleteBtn.setOnMouseClicked(e -> e.consume());
                    setGraphic(root);
                    setText(null);
                }
            }
        };
    }

    private void openEditDoctor(Diabetologo d) {
        try {
            FXMLLoader loader = new FXMLLoader(MainApp.class.getResource("/fxml/AddDoctorView.fxml"));
            Parent root = loader.load();

            AddDoctorController ctrl = loader.getController();
            ctrl.initEdit(d, this::reloadDoctors);

            Stage popup = new Stage();
            popup.initModality(Modality.WINDOW_MODAL);
            popup.setScene(new Scene(root));
            popup.setTitle("Modifica diabetologo");
            popup.showAndWait();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void deleteDoctor(Diabetologo d) {
        boolean confirmed = ConfirmDialog.show(
                "Elimina diabetologo",
                "Confermi eliminazione di " + d.getNome() + " " + d.getCognome() + "?",
                "Questa azione non può essere annullata."
        );
        if (!confirmed) return;

        try {
            diabetologoDAO.deleteByEmail(d.getEmail());
            reloadDoctors();
        } catch (Exception e) {
            e.printStackTrace();
            Alert alert = new Alert(Alert.AlertType.ERROR, "Impossibile eliminare il diabetologo.", ButtonType.OK);
            alert.setHeaderText("Errore eliminazione");
            alert.showAndWait();
        }
    }
}
