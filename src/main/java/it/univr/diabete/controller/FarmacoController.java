package it.univr.diabete.controller;
import it.univr.diabete.MainApp;
import it.univr.diabete.dao.FarmacoDAO;
import it.univr.diabete.dao.impl.FarmacoDAOImpl;
import it.univr.diabete.model.Farmaco;
import it.univr.diabete.ui.ConfirmDialog;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
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

public class FarmacoController {

    @FXML private ListView<Farmaco> farmacoListView;
    @FXML private TextField searchField;
    @FXML private Button newButton;

    private final FarmacoDAO farmacoDAO = new FarmacoDAOImpl();

    // lista completa caricata dal DB (base per il filtro)
    private final ObservableList<Farmaco> farmaciCompleti =
            FXCollections.observableArrayList();

    @FXML
    private void initialize() {
        farmacoListView.setItems(farmaciCompleti);
        farmacoListView.setCellFactory(lv -> createFarmacoCardCell());

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
            farmacoListView.setItems(farmaciCompleti);
            return;
        }

        String lower = filter.toLowerCase();

        ObservableList<Farmaco> filtrati = farmaciCompleti.filtered(f ->
                f.getNome().toLowerCase().contains(lower) ||
                        (f.getMarca() != null && f.getMarca().toLowerCase().contains(lower))
        );

        farmacoListView.setItems(filtrati);
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
            dialog.initOwner(farmacoListView.getScene().getWindow());
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

    private void openEdit(Farmaco farmaco) {
        if (farmaco == null) return;
        try {
            FXMLLoader loader = new FXMLLoader(
                    MainApp.class.getResource("/fxml/FarmacoFormView.fxml"));
            Parent root = loader.load();

            FarmacoFormController formCtrl = loader.getController();
            formCtrl.init(farmaco, "Modifica farmaco");

            Stage dialog = new Stage();
            dialog.initOwner(farmacoListView.getScene().getWindow());
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

    private void deleteFarmaco(Farmaco farmaco) {
        if (farmaco == null) return;
        boolean confermato = ConfirmDialog.show(
                "Elimina farmaco",
                "Vuoi davvero eliminare il farmaco \"" + farmaco.getNome() + "\"?",
                "L'operazione non è reversibile."
        );

        if (!confermato) {
            return;
        }

        try {
            farmacoDAO.delete(farmaco.getId());
            loadFarmaci();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private ListCell<Farmaco> createFarmacoCardCell() {
        return new ListCell<>() {
            private final HBox root = new HBox();
            private final VBox textBox = new VBox(4);
            private final Label nameLbl = new Label();
            private final Label brandLbl = new Label();
            private final HBox actions = new HBox(6);
            private final Button deleteBtn = new Button("Elimina");

            {
                root.getStyleClass().add("patient-card");
                root.setAlignment(Pos.CENTER_LEFT);

                nameLbl.getStyleClass().add("patient-card-name");
                brandLbl.getStyleClass().add("patient-card-sub");
                textBox.getStyleClass().add("patient-card-main");
                textBox.getChildren().addAll(nameLbl, brandLbl);

                deleteBtn.getStyleClass().add("btn-ghost");
                actions.getStyleClass().add("patient-card-actions");
                actions.getChildren().add(deleteBtn);

                Region spacer = new Region();
                HBox.setHgrow(spacer, Priority.ALWAYS);
                root.getChildren().addAll(textBox, spacer, actions);

                root.setOnMouseClicked(e -> {
                    Farmaco f = getItem();
                    if (f != null) {
                        openEdit(f);
                    }
                });
            }

            @Override
            protected void updateItem(Farmaco f, boolean empty) {
                super.updateItem(f, empty);
                if (empty || f == null) {
                    setGraphic(null);
                    setText(null);
                } else {
                    nameLbl.setText(f.getNome());
                    brandLbl.setText(f.getMarca() != null ? f.getMarca() : "—");
                    deleteBtn.setOnAction(e -> {
                        e.consume();
                        deleteFarmaco(f);
                    });
                    deleteBtn.setOnMouseClicked(e -> e.consume());
                    setGraphic(root);
                    setText(null);
                }
            }
        };
    }
}
