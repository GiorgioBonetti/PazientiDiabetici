package it.univr.diabete.controller;
import it.univr.diabete.MainApp;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Modality;
import it.univr.diabete.dao.FarmacoDAO;
import it.univr.diabete.dao.impl.FarmacoDAOImpl;
import it.univr.diabete.model.Farmaco;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.stage.Stage;

public class FarmacoController {

    @FXML private TableView<Farmaco> farmacoTable;
    @FXML private TableColumn<Farmaco, String> colNome;
    @FXML private TableColumn<Farmaco, String> colMarca;
    @FXML private TextField searchField;
    @FXML private Button editButton;
    @FXML private Button deleteButton;

    private final FarmacoDAO farmacoDAO = new FarmacoDAOImpl();

    // lista completa caricata dal DB (base per il filtro)
    private final ObservableList<Farmaco> farmaciCompleti =
            FXCollections.observableArrayList();

    @FXML
    private void initialize() {
        // colonne
        colNome.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().getNome()));
        colMarca.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().getMarca()));

        // tabella appoggiata alla lista completa
        farmacoTable.setItems(farmaciCompleti);

        // bottoni modifica/elimina disabilitati se nulla selezionato
        editButton.disableProperty().bind(
                farmacoTable.getSelectionModel().selectedItemProperty().isNull()
        );
        deleteButton.disableProperty().bind(
                farmacoTable.getSelectionModel().selectedItemProperty().isNull()
        );

        // filtro ricerca live
        searchField.textProperty().addListener((obs, oldV, newV) -> applyFilter(newV));

        // carico i dati iniziali
        loadFarmaci();
    }

    /** Carica tutti i farmaci dal DB nella lista completa. */
    private void loadFarmaci() {
        try {
            farmaciCompleti.setAll(farmacoDAO.findAll());

            // ri-applico l’eventuale filtro che c’era già
            applyFilter(searchField.getText());
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /** Applica il filtro sulla lista completa e aggiorna la tabella. */
    private void applyFilter(String filter) {
        if (filter == null || filter.isBlank()) {
            farmacoTable.setItems(farmaciCompleti);
            return;
        }

        String lower = filter.toLowerCase();

        ObservableList<Farmaco> filtrati = farmaciCompleti.filtered(f ->
                f.getNome().toLowerCase().contains(lower) ||
                        (f.getMarca() != null && f.getMarca().toLowerCase().contains(lower))
        );

        farmacoTable.setItems(filtrati);
    }

    @FXML
    private void handleAdd() {
        try {
            FXMLLoader loader = new FXMLLoader(
                    MainApp.class.getResource("/fxml/FarmacoFormView.fxml"));
            Parent root = loader.load();

            FarmacoFormController formCtrl = loader.getController();
            formCtrl.init(null, "Nuovo farmaco");

            Stage dialog = new Stage();
            dialog.initOwner(farmacoTable.getScene().getWindow());
            dialog.initModality(Modality.WINDOW_MODAL);
            dialog.setTitle("Nuovo farmaco");
            dialog.setScene(new Scene(root));
            dialog.showAndWait();

            if (formCtrl.isConfirmed()) {
                Farmaco nuovo = formCtrl.getResult();
                farmacoDAO.insert(nuovo);
                loadFarmaci();
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @FXML
    private void handleEdit() {
        Farmaco sel = farmacoTable.getSelectionModel().getSelectedItem();
        if (sel == null) return;

        try {
            FXMLLoader loader = new FXMLLoader(
                    MainApp.class.getResource("/fxml/FarmacoFormView.fxml"));
            Parent root = loader.load();

            FarmacoFormController formCtrl = loader.getController();
            formCtrl.init(sel, "Modifica farmaco");

            Stage dialog = new Stage();
            dialog.initOwner(farmacoTable.getScene().getWindow());
            dialog.initModality(Modality.WINDOW_MODAL);
            dialog.setTitle("Modifica farmaco");
            dialog.setScene(new Scene(root));
            dialog.showAndWait();

            if (formCtrl.isConfirmed()) {
                Farmaco aggiornato = formCtrl.getResult();
                farmacoDAO.update(aggiornato);
                loadFarmaci();
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    // ───────────── DELETE ─────────────
    @FXML
    private void handleDelete() {
        Farmaco sel = farmacoTable.getSelectionModel().getSelectedItem();
        if (sel == null) return;

        boolean confermato = it.univr.diabete.ui.ConfirmDialog.show(
                "Elimina farmaco",
                "Vuoi davvero eliminare il farmaco \"" + sel.getNome() + "\"?",
                "L'operazione non è reversibile."
        );

        if (!confermato) {
            return; // ❌ utente ha premuto ANNULLA → non fare nulla
        }

        try {
            farmacoDAO.delete(sel.getId());
            loadFarmaci();  // 🔄 aggiorna la tabella
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}