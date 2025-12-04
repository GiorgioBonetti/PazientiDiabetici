package it.univr.diabete.controller;

import it.univr.diabete.MainApp;
import it.univr.diabete.dao.AssunzioneTerapiaDAO;
import it.univr.diabete.dao.TerapiaDAO;
import it.univr.diabete.dao.impl.AssunzioneTerapiaDAOImpl;
import it.univr.diabete.dao.impl.TerapiaDAOImpl;
import it.univr.diabete.model.AssunzioneTerapia;
import it.univr.diabete.model.Glicemia;
import it.univr.diabete.model.Terapia;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import javafx.stage.Modality;
import javafx.stage.Stage;

import java.io.IOException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;

public class PatientTherapyController {
    @FXML
    private Label patientNameLabel;

    // Dettaglio terapia selezionata
    @FXML
    private Label farmacoLabel;

    @FXML
    private Label dosaggioLabel;

    @FXML
    private Label frequenzaLabel;

    @FXML
    private Label quantitaLabel;

    @FXML
    private Label periodoLabel;
    @FXML
    private Button addAssunzioneButton;
    @FXML
    private ChoiceBox<String> assunzioniFilter;

    // Tabella assunzioni
    @FXML
    private TableView<AssunzioneTerapia> assunzioniTable;

    @FXML
    private TableColumn<AssunzioneTerapia, String> colData;

    @FXML
    private TableColumn<AssunzioneTerapia, Integer> colQuantita;
    private TableColumn<AssunzioneTerapia, Void> colEdit; // 🔥 globale

    // Contenitore card terapie
    @FXML
    private HBox therapyCardsContainer;

    private final TerapiaDAO terapiaDAO = new TerapiaDAOImpl();
    private final AssunzioneTerapiaDAO assunzioneDAO = new AssunzioneTerapiaDAOImpl();

    private Terapia terapiaCorrente;
    private int pazienteId;

    private final DateTimeFormatter df = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");
    private VBox selectedCard;   // card selezionata (per applicare stile)

    @FXML
    private void initialize() {
        // Configuro solo una volta le colonne della tabella
        colData.setCellValueFactory(c ->
                new SimpleStringProperty(
                        c.getValue().getDateStamp().format(df)
                )
        );

        colQuantita.setCellValueFactory(c ->
                new SimpleIntegerProperty(c.getValue().getQuantitaAssunta()).asObject()
        );
        addEditColumn();
        assunzioniFilter.getItems().addAll(
                "Tutte",
                "Oggi",
                "Ultimi 7 giorni",
                "Ultimi 30 giorni"
        );
        assunzioniFilter.getSelectionModel().selectFirst();
        assunzioniFilter.setOnAction(e -> applyAssunzioniFilter());

    }

    /**
     * Chiamato dal MainShellController.
     */
    public void setPatientContext(String nomeCompleto, int patientId) {
        this.pazienteId = patientId;
        patientNameLabel.setText(nomeCompleto);

        loadTerapie();
    }

    private void loadTerapie() {
        try {
            List<Terapia> lista = terapiaDAO.findByPazienteId(pazienteId);

            therapyCardsContainer.getChildren().clear();
            terapiaCorrente = null;
            selectedCard = null;
            assunzioniTable.getItems().clear();
            clearDetailLabels();

            if (lista.isEmpty()) {
                // Nessuna terapia
                Label empty = new Label("Nessuna terapia assegnata");
                empty.getStyleClass().add("page-subtitle");
                therapyCardsContainer.getChildren().add(empty);
                return;
            }

            // Crea una card per ogni terapia
            for (Terapia t : lista) {
                VBox card = creaCardTerapia(t);
                therapyCardsContainer.getChildren().add(card);

                // Se è la prima, la seleziono di default
                if (terapiaCorrente == null) {
                    selezionaTerapia(t, card);
                }
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void clearDetailLabels() {
        farmacoLabel.setText("-");
        dosaggioLabel.setText("-");
        frequenzaLabel.setText("-");
        quantitaLabel.setText("-");
        periodoLabel.setText("-");
    }

    /**
     * Crea la card visuale per una singola terapia.
     */
    private VBox creaCardTerapia(Terapia t) {
        VBox card = new VBox(4);
        card.getStyleClass().add("therapy-card");
        card.setPadding(new Insets(10, 14, 10, 14));

        // Riga 1: Nome farmaco + badge stato a destra
        Label lblNome = new Label(t.getFarmacoNome());
        lblNome.getStyleClass().add("card-title");

        Label lblBadge = creaBadgeStato(t);

        Region spacer = new Region();
        HBox.setHgrow(spacer, javafx.scene.layout.Priority.ALWAYS);

        HBox row1 = new HBox(8, lblNome, spacer, lblBadge);
        row1.setAlignment(Pos.CENTER_LEFT);

        // Riga 2 e 3: dosaggio e frequenza
        Label lblDose = new Label(t.getFarmacoDosaggio() + " mg");
        Label lblFreq = new Label(t.getAssunzioniGiornaliere() + "x/giorno");

        lblDose.getStyleClass().add("page-subtitle");
        lblFreq.getStyleClass().add("page-subtitle");

        card.getChildren().addAll(row1, lblDose, lblFreq);

        // Click sulla card -> seleziona terapia
        card.addEventHandler(MouseEvent.MOUSE_CLICKED, e -> selezionaTerapia(t, card));

        return card;
    }

    /**
     * Crea il badge di stato (IN CORSO, SCADUTA, NON INIZIATA).
     */
    private Label creaBadgeStato(Terapia t) {
        Label badge = new Label();
        badge.getStyleClass().add("therapy-badge");

        LocalDate today = LocalDate.now();
        LocalDate inizio = t.getDataInizio();
        LocalDate fine = t.getDataFine();

        if (inizio != null && today.isBefore(inizio)) {
            badge.setText("NON INIZIATA");
            badge.getStyleClass().add("therapy-upcoming");
        } else if (fine != null && today.isAfter(fine)) {
            badge.setText("COMPLETATA");
            badge.getStyleClass().add("therapy-expired");
        } else {
            badge.setText("IN CORSO");
            badge.getStyleClass().add("therapy-active");
        }

        return badge;
    }

    /**
     * Seleziona una terapia (click sulla card).
     */
    private void selezionaTerapia(Terapia t, VBox card) {
        this.terapiaCorrente = t;

        // Aggiorno stile della card selezionata
        if (selectedCard != null) {
            selectedCard.getStyleClass().remove("therapy-card-selected");
        }
        selectedCard = card;
        if (!selectedCard.getStyleClass().contains("therapy-card-selected")) {
            selectedCard.getStyleClass().add("therapy-card-selected");
        }

        // Aggiorno dettagli descrittivi
        farmacoLabel.setText(t.getFarmacoNome());
        dosaggioLabel.setText(t.getFarmacoDosaggio() + " mg");
        frequenzaLabel.setText(t.getAssunzioniGiornaliere() + " volte al giorno");
        quantitaLabel.setText(t.getQuantita() + " unità");

        if (t.getDataFine() != null) {
            periodoLabel.setText("dal " + t.getDataInizio() + " al " + t.getDataFine());
        } else {
            periodoLabel.setText("dal " + t.getDataInizio());
        }

        disableAssunzioneButtonIfNeeded(t);

        loadAssunzioni();
    }
    private void disableAssunzioneButtonIfNeeded(Terapia t) {
        LocalDate today = LocalDate.now();

        boolean nonIniziata = t.getDataInizio() != null && today.isBefore(t.getDataInizio());
        boolean scaduta = t.getDataFine() != null && today.isAfter(t.getDataFine());

        boolean disable = nonIniziata || scaduta;

        // bottone fx:id="addAssunzioneButton"
        // (aggiungilo nel tuo controller)
        addAssunzioneButton.setDisable(disable);

        if (disable)
            addAssunzioneButton.setStyle("-fx-opacity: 0.4;");
        else
            addAssunzioneButton.setStyle("-fx-opacity: 1;");
    }
    private void loadAssunzioni() {
        if (terapiaCorrente == null) {
            assunzioniTable.getItems().clear();
            return;
        }

        applyAssunzioniFilter(); // 👉 Usa il filtro sempre
    }

    /**
     * Apre popup per registrare l'assunzione.
     */
    @FXML
    private void handleAddAssunzione() {
        if (terapiaCorrente == null) {
            // Se vuoi puoi mostrare un alert qui
            return;
        }

        try {
            FXMLLoader loader = new FXMLLoader(MainApp.class.getResource("/fxml/AddAssunzioneView.fxml"));
            Parent root = loader.load();

            AddAssunzioneController controller = loader.getController();
            controller.initData(pazienteId, terapiaCorrente.getId(), this::loadAssunzioni);

            Stage popup = new Stage();
            popup.initOwner(assunzioniTable.getScene().getWindow());
            popup.initModality(Modality.WINDOW_MODAL);
            popup.setScene(new Scene(root));
            popup.setTitle("Registra assunzione");
            popup.showAndWait();

        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    private void applyAssunzioniFilter() {
        if (terapiaCorrente == null) return;

        String filter = assunzioniFilter.getValue();
        LocalDate today = LocalDate.now();

        try {
            List<AssunzioneTerapia> lista =
                    assunzioneDAO.findByPazienteAndTerapia(pazienteId, terapiaCorrente.getId());

            List<AssunzioneTerapia> filtrate = lista.stream()
                    .filter(a -> {
                        LocalDate data = a.getDateStamp().toLocalDate();
                        return switch (filter) {
                            case "Oggi" -> data.isEqual(today);
                            case "Ultimi 7 giorni" -> data.isAfter(today.minusDays(7));
                            case "Ultimi 30 giorni" -> data.isAfter(today.minusDays(30));
                            default -> true;
                        };
                    })
                    .toList();

            assunzioniTable.getItems().setAll(filtrate);

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void addEditColumn() {

        colEdit = new TableColumn<>(""); // 🔥 fondamentale farlo qui

        colEdit.setCellFactory(column -> new TableCell<>() {
            private final Button editBtn = new Button("✏️");

            {
                editBtn.getStyleClass().add("edit-btn");
                editBtn.setOnAction(event -> {
                    AssunzioneTerapia a = getTableView().getItems().get(getIndex());
                    openEditAssunzionePopup(a);
                });
            }

            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                setGraphic(empty ? null : editBtn);
            }
        });

        assunzioniTable.getColumns().add(colEdit);
    }
    private void openEditAssunzionePopup(AssunzioneTerapia a) {
        try {
            FXMLLoader loader = new FXMLLoader(MainApp.class.getResource("/fxml/EditAssunzioneView.fxml"));
            Parent root = loader.load();

            EditAssunzioneController ctrl = loader.getController();
            ctrl.initData(a, this::loadAssunzioni);

            Stage popup = new Stage();
            popup.initOwner(assunzioniTable.getScene().getWindow());
            popup.setScene(new Scene(root));
            popup.setTitle("Modifica assunzione");
            popup.showAndWait();

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    public void hideEditingTools() {
        // nascondi bottone "Nuova misurazione"
        if (addAssunzioneButton != null) {
            addAssunzioneButton.setVisible(false);
            addAssunzioneButton.setManaged(false);
        }
        // rimuovi colonna matita
        if (colEdit != null) {
            assunzioniTable.getColumns().remove(colEdit);
        }
        // opzionale: disabilita anche il doppio click, se decidi di aggiungerlo in futuro
        assunzioniTable.setOnMouseClicked(null);
    }
}