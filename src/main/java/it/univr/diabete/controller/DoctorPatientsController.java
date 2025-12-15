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
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import javafx.stage.Modality;
import javafx.stage.Stage;
import java.io.IOException;
import java.util.List;

public class DoctorPatientsController {

    @FXML
    private TextField searchField;
    // id del diabetologo attualmente loggato
    private String idDiabetologoLoggato;
    @FXML
    private VBox patientsContainer;

    private final PazienteDAO pazienteDAO = new PazienteDAOImpl();

    private final ObservableList<Paziente> allPatients =
            FXCollections.observableArrayList();

    private FilteredList<Paziente> filteredPatients;

    @FXML
    private void initialize() {
        loadPatients();

        // filtro dinamico sulla ricerca
        filteredPatients = new FilteredList<>(allPatients, p -> true);

        searchField.textProperty().addListener((obs, oldV, newV) -> {
            String text = newV == null ? "" : newV.toLowerCase().trim();
            filteredPatients.setPredicate(p -> {
                if (text.isEmpty()) return true;
                String fullName = (p.getNome() + " " + p.getCognome()).toLowerCase();
                String id = String.valueOf(p.getCodiceFiscale());
                return fullName.contains(text)
                        || id.contains(text)
                        || (p.getEmail() != null && p.getEmail().toLowerCase().contains(text));
            });
            renderCards();
        });

        renderCards();
    }
    public void setDoctorContext(String idDiabetologo) {
        this.idDiabetologoLoggato = idDiabetologo;
        reloadPatients(); // appena entro nella pagina carico i pazienti del medico
    }
    private void loadPatients() {
        try {
            List<Paziente> lista = pazienteDAO.findAll();
            allPatients.setAll(lista);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    private void reloadPatients() {
            loadPatients();
            renderCards();
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

    private void openPatientDetail(Paziente paziente) {
        // Chiedo alla MainShell di aprire la scheda nel centro
        MainShellController shell = MainApp.getMainShellController();
        if (shell != null && paziente != null) {
            shell.openPatientDetail(paziente);
        }
    }
    /** Ricostruisce le card dentro il VBox in base a filteredPatients. */
    private void renderCards() {
        patientsContainer.getChildren().clear();

        for (Paziente p : filteredPatients) {
            patientsContainer.getChildren().add(createPatientCard(p));
        }
    }

    /** Crea la singola card per un paziente. */
    private HBox createPatientCard(Paziente p) {
        HBox root = new HBox();
        root.getStyleClass().add("patient-card");

        // colonna testo principale
        VBox textBox = new VBox();
        textBox.getStyleClass().add("patient-card-main");

        Label nameLbl = new Label(p.getNome() + " " + p.getCognome());
        nameLbl.getStyleClass().add("patient-card-name");

        Label subLbl = new Label(
                p.getEmail() != null ? p.getEmail() : ""
        );
        subLbl.getStyleClass().add("patient-card-sub");

        textBox.getChildren().addAll(nameLbl, subLbl);

        // spazio elastico in mezzo
        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        // colonna bottoni azione
        HBox actions = new HBox();
        actions.getStyleClass().add("patient-card-actions");

        Button btnMis = new Button("Misurazioni");
        btnMis.getStyleClass().add("btn-ghost");
        btnMis.setOnAction(e -> openPatientMeasurements(p));

        Button btnTher = new Button("Terapia");
        btnTher.getStyleClass().add("btn-ghost");
        btnTher.setOnAction(e -> openPatientTherapy(p));

        Button btnReport = new Button("Report");
        btnReport.getStyleClass().add("btn-primary");
        btnReport.setOnAction(e -> openPatientReport(p));

        actions.getChildren().addAll(btnMis, btnTher, btnReport);

        root.getChildren().addAll(textBox, spacer, actions);

        /* CLICK SINGOLO PER APRIRE DETTAGLI */
        root.setOnMouseClicked(e -> openPatientDetail(p));

        /* CAMBIA CURSORE A MANINA QUANDO PASSI SOPRA LA CARD */
        root.setOnMouseEntered(e -> root.setStyle("-fx-cursor: hand;"));
        root.setOnMouseExited(e -> root.setStyle("-fx-cursor: default;"));
        root.setOnMouseEntered(e -> root.setStyle("-fx-cursor: hand; -fx-background-color: #F4F5FA; -fx-border-color: #D0D0D0;"));

        root.setOnMouseExited(e -> root.setStyle("-fx-cursor: default; -fx-background-color: transparent; -fx-border-color: transparent;"));
        return root;
    }


    private void openPatientMeasurements(Paziente p) {
        try {
            FXMLLoader loader = new FXMLLoader(
                    MainApp.class.getResource("/fxml/PatientMeasurementsView.fxml")
            );
            Parent view = loader.load();

            PatientMeasurementsController ctrl = loader.getController();
            // passo nome completo + id del paziente scelto
            String fullName = p.getNome() + " " + p.getCognome();
            ctrl.setPatientContext(fullName, p.getCodiceFiscale());
            ctrl.hideEditingTools();
            // metto la view dentro il contenitore centrale della MainShell
            MainShellController shell = MainApp.getMainShellController();
            shell.getContentArea().getChildren().setAll(view);

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void openPatientTherapy(Paziente p) {
        try {
            FXMLLoader loader = new FXMLLoader(
                    MainApp.class.getResource("/fxml/PatientTherapyView.fxml")
            );
            Parent view = loader.load();

            PatientTherapyController ctrl = loader.getController();
            String fullName = p.getNome() + " " + p.getCognome();
            ctrl.setPatientContext(fullName, p.getCodiceFiscale());
            ctrl.hideEditingTools();
            MainShellController shell = MainApp.getMainShellController();
            shell.getContentArea().getChildren().setAll(view);

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void openPatientReport(Paziente p) {
        try {
            FXMLLoader loader = new FXMLLoader(
                    MainApp.class.getResource("/fxml/PatientReportView.fxml")
            );
            Parent view = loader.load();

            PatientReportController ctrl = loader.getController();
            String fullName = p.getNome() + " " + p.getCognome();
            ctrl.setPatientContext(fullName, p.getCodiceFiscale());

            MainShellController shell = MainApp.getMainShellController();
            shell.getContentArea().getChildren().setAll(view);

        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}