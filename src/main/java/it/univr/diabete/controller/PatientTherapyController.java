package it.univr.diabete.controller;

import it.univr.diabete.MainApp;
import it.univr.diabete.dao.AssunzioneTerapiaDAO;
import it.univr.diabete.dao.TerapiaDAO;
import it.univr.diabete.dao.TerapiaFarmacoDAO;
import it.univr.diabete.dao.impl.AssunzioneTerapiaDAOImpl;
import it.univr.diabete.dao.impl.TerapiaDAOImpl;
import it.univr.diabete.dao.impl.TerapiaFarmacoDAOImpl;
import it.univr.diabete.model.AssunzioneTerapia;
import it.univr.diabete.model.Terapia;
import it.univr.diabete.model.TerapiaFarmaco;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import javafx.scene.shape.SVGPath;
import javafx.stage.Modality;
import javafx.stage.Stage;

import java.io.IOException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;

public class PatientTherapyController {

    @FXML private Button addTherapyButton;
    @FXML private Button editTherapyButton;   // se lo usi altrove, per ora resta
    @FXML private Label  patientNameLabel;
    @FXML private VBox   therapyRoot;

    // filtro assunzioni + tabella
    @FXML private ChoiceBox<String> assunzioniFilter;
    @FXML private Button            addAssunzioneButton;
    @FXML private TableView<AssunzioneTerapia> assunzioniTable;
    @FXML private TableColumn<AssunzioneTerapia, String>  colData;
    @FXML private TableColumn<AssunzioneTerapia, Integer> colQuantita;
    private TableColumn<AssunzioneTerapia, Void> colEdit;

    // card terapie (sopra)
    @FXML private HBox therapyCardsContainer;

    // card farmaci della terapia selezionata (sotto, scroll orizzontale)
    @FXML private ScrollPane farmaciScroll;
    @FXML private HBox       farmaciCardsContainer;
    private VBox             selectedFarmacoCard;

    private final TerapiaDAO          terapiaDAO          = new TerapiaDAOImpl();
    private final AssunzioneTerapiaDAO assunzioneDAO      = new AssunzioneTerapiaDAOImpl();
    private final TerapiaFarmacoDAO   terapiaFarmacoDAO   = new TerapiaFarmacoDAOImpl();

    private Terapia       terapiaCorrente;
    private TerapiaFarmaco terapiaFarmacoCorrente;
    private int pazienteId;

    private final DateTimeFormatter df = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");
    private VBox selectedCard;   // card terapia selezionata

    // ─────────────────── NAVIGATION ───────────────────

    @FXML
    private void handleGoToMeasurements() {
        if (pazienteId <= 0) return;
        MainShellController shell = MainApp.getMainShellController();
        if (shell != null) {
            shell.openPatientMeasurements(patientNameLabel.getText(), pazienteId);
        }
    }

    @FXML
    private void handleGoToReport() {
        if (pazienteId <= 0) return;
        MainShellController shell = MainApp.getMainShellController();
        if (shell != null) {
            shell.openPatientReport(patientNameLabel.getText(), pazienteId);
        }
    }

    // ─────────────────── INITIALIZE ───────────────────

    @FXML
    private void initialize() {
        colData.setCellValueFactory(c ->
                new SimpleStringProperty(c.getValue().getDateStamp().format(df))
        );
        colQuantita.setCellValueFactory(c ->
                new SimpleIntegerProperty(c.getValue().getQuantitaAssunta()).asObject()
        );

        addEditColumn();

        assunzioniFilter.getItems().addAll(
                "Tutte", "Oggi", "Ultimi 7 giorni", "Ultimi 30 giorni"
        );
        assunzioniFilter.getSelectionModel().selectFirst();
        assunzioniFilter.setOnAction(e -> applyAssunzioniFilter());
    }

    // chiamato dal MainShellController
    public void setPatientContext(String nomeCompleto, int patientId) {
        this.pazienteId = patientId;
        patientNameLabel.setText(nomeCompleto);
        loadTerapie();
    }

    // ─────────────────── CARICAMENTO TERAPIE ───────────────────

    private void loadTerapie() {
        try {
            List<Terapia> lista = terapiaDAO.findByPazienteId(pazienteId);

            therapyCardsContainer.getChildren().clear();
            terapiaCorrente = null;
            terapiaFarmacoCorrente = null;
            selectedCard = null;
            selectedFarmacoCard = null;
            assunzioniTable.getItems().clear();
            clearDettaglioTerapia();

            if (lista.isEmpty()) {
                Label empty = new Label("Nessuna terapia assegnata");
                empty.getStyleClass().add("page-subtitle");
                therapyCardsContainer.getChildren().add(empty);
                return;
            }

            for (Terapia t : lista) {
                VBox card = creaCardTerapia(t);
                therapyCardsContainer.getChildren().add(card);

                // prima terapia selezionata di default
                if (terapiaCorrente == null) {
                    selezionaTerapia(t, card);
                }
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void clearDettaglioTerapia() {
        farmaciCardsContainer.getChildren().clear();
        terapiaFarmacoCorrente = null;
        addAssunzioneButton.setDisable(true);
        addAssunzioneButton.setStyle("-fx-opacity: 0.4;");
    }

    // ─────────────────── CARD TERAPIA (sopra) ───────────────────

    private VBox creaCardTerapia(Terapia t) {
        VBox card = new VBox(4);
        card.getStyleClass().add("therapy-card");
        card.setPadding(new Insets(8, 14, 10, 14));
        card.setMinHeight(70);
        card.setPrefHeight(70);

        card.setPrefWidth(200);
        card.setMinWidth(200);
        card.setMaxWidth(200);

        Label lblNome = new Label();
        lblNome.getStyleClass().add("card-title");

        Label lblInfo = new Label();
        lblInfo.getStyleClass().add("page-subtitle");

        if (t.getNome() != null && !t.getNome().isBlank()) {
            lblNome.setText(t.getNome());
        } else {
            lblNome.setText("Terapia senza nome");
        }

        try {
            List<TerapiaFarmaco> farmaci = terapiaFarmacoDAO.findByTerapiaId(t.getId());
            if (farmaci.isEmpty()) {
                lblInfo.setText("Nessun farmaco assegnato");
            } else {
                TerapiaFarmaco tf = farmaci.get(0);
                String nomeFarmaco = (tf.getFarmaco() != null)
                        ? tf.getFarmaco().getNome()
                        : "Farmaco ID " + tf.getIdFarmaco();

                String subtitle = nomeFarmaco;
                if (farmaci.size() > 1) {
                    subtitle += " (+ " + (farmaci.size() - 1) + " altri)";
                }
                lblInfo.setText(subtitle);
            }
        } catch (Exception e) {
            e.printStackTrace();
            lblInfo.setText("");
        }

        Label lblBadge = creaBadgeStato(t);

        Region hSpacer = new Region();
        HBox.setHgrow(hSpacer, Priority.ALWAYS);

        HBox row1 = new HBox(8, lblNome, hSpacer, lblBadge);
        row1.setAlignment(Pos.CENTER_LEFT);

        Button editBtn = new Button();
        editBtn.getStyleClass().add("icon-edit-btn");
        SVGPath icon = new SVGPath();
        icon.setContent("M12.3 2.3 L3 11.6 L2 15.9 L6.3 14.9 L15.6 5.6 Z");
        icon.getStyleClass().add("svg-path");
        editBtn.setGraphic(icon);
        editBtn.setId("editTherapyButton");
        editBtn.setOnAction(e -> handleEditTherapy(t));

        HBox bottomRow = new HBox(editBtn);
        bottomRow.setAlignment(Pos.BOTTOM_RIGHT);

        card.getChildren().addAll(row1, lblInfo, bottomRow);

        card.addEventHandler(MouseEvent.MOUSE_CLICKED, e -> selezionaTerapia(t, card));

        return card;
    }

    private Label creaBadgeStato(Terapia t) {
        Label badge = new Label();
        badge.getStyleClass().add("therapy-badge");

        LocalDate today = LocalDate.now();
        LocalDate inizio = t.getDataInizio();
        LocalDate fine   = t.getDataFine();

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

    private void selezionaTerapia(Terapia t, VBox card) {
        this.terapiaCorrente = t;

        // stile card terapia selezionata
        if (selectedCard != null) {
            selectedCard.getStyleClass().remove("therapy-card-selected");
        }
        selectedCard = card;
        if (!selectedCard.getStyleClass().contains("therapy-card-selected")) {
            selectedCard.getStyleClass().add("therapy-card-selected");
        }

        try {
            List<TerapiaFarmaco> farmaci = terapiaFarmacoDAO.findByTerapiaId(t.getId());

            farmaciCardsContainer.getChildren().clear();
            selectedFarmacoCard = null;

            if (farmaci.isEmpty()) {
                clearDettaglioTerapia();
                assunzioniTable.getItems().clear();
                return;
            }

            // crea card per ogni farmaco
            for (int i = 0; i < farmaci.size(); i++) {
                TerapiaFarmaco tf = farmaci.get(i);
                VBox cardFarmaco = creaCardFarmaco(t, tf);
                farmaciCardsContainer.getChildren().add(cardFarmaco);

                // seleziono il primo farmaco di default
                if (i == 0) {
                    selezionaFarmaco(t, tf, cardFarmaco);
                }
            }

        } catch (Exception e) {
            e.printStackTrace();
            clearDettaglioTerapia();
        }
    }

    // ─────────────────── CARD FARMACO (sotto) ───────────────────

    private VBox creaCardFarmaco(Terapia terapia, TerapiaFarmaco tf) {
        VBox card = new VBox(4);
        card.getStyleClass().add("drug-card");
        card.setPadding(new Insets(8, 12, 10, 12));

        card.setMinWidth(180);
        card.setPrefWidth(180);
        card.setMaxWidth(180);

        String nomeFarmaco = (tf.getFarmaco() != null)
                ? tf.getFarmaco().getNome()
                : "Farmaco ID " + tf.getIdFarmaco();

        Label lblNome = new Label(nomeFarmaco);
        lblNome.getStyleClass().add("drug-card-title");

        Label lblDose = new Label("Dosaggio: " + tf.getQuantitaAssunzione() + " mg");
        lblDose.getStyleClass().add("drug-card-line");

        Label lblFreq = new Label("Assunzioni: " + tf.getAssunzioniGiornaliere() + " x/giorno");
        lblFreq.getStyleClass().add("drug-card-line");


        // (opzionale) periodo terapia, uguale per tutti i farmaci della terapia
        if (terapia.getDataInizio() != null) {
            String periodoText;
            if (terapia.getDataFine() != null) {
                periodoText = "Periodo: dal " + terapia.getDataInizio() +
                        " al " + terapia.getDataFine();
            } else {
                periodoText = "Periodo: dal " + terapia.getDataInizio();
            }
            Label lblPeriodo = new Label(periodoText);
            lblPeriodo.getStyleClass().add("drug-card-line-small");
            card.getChildren().addAll(lblNome, lblDose, lblFreq, lblPeriodo);
        } else {
            card.getChildren().addAll(lblNome, lblDose, lblFreq);
        }

        card.setOnMouseClicked(e -> selezionaFarmaco(terapia, tf, card));

        return card;
    }

    private void selezionaFarmaco(Terapia terapia,
                                  TerapiaFarmaco tf,
                                  VBox card) {

        this.terapiaCorrente = terapia;
        this.terapiaFarmacoCorrente = tf;

        // stile card farmaco selezionata
        if (selectedFarmacoCard != null) {
            selectedFarmacoCard.getStyleClass().remove("drug-card-selected");
        }
        selectedFarmacoCard = card;
        if (!selectedFarmacoCard.getStyleClass().contains("drug-card-selected")) {
            selectedFarmacoCard.getStyleClass().add("drug-card-selected");
        }

        disableAssunzioneButtonIfNeeded(terapia);
        loadAssunzioni();
    }

    // ─────────────────── ASSUNZIONI ───────────────────

    private void disableAssunzioneButtonIfNeeded(Terapia t) {
        LocalDate today = LocalDate.now();

        boolean nonIniziata = t.getDataInizio() != null && today.isBefore(t.getDataInizio());
        boolean scaduta    = t.getDataFine()   != null && today.isAfter(t.getDataFine());
        boolean disable    = nonIniziata || scaduta || (terapiaFarmacoCorrente == null);

        addAssunzioneButton.setDisable(disable);
        addAssunzioneButton.setStyle(disable ? "-fx-opacity: 0.4;" : "-fx-opacity: 1;");
    }

    private void loadAssunzioni() {
        if (terapiaCorrente == null || terapiaFarmacoCorrente == null) {
            assunzioniTable.getItems().clear();
            return;
        }
        applyAssunzioniFilter();
    }

    @FXML
    private void handleAddAssunzione() {
        if (terapiaCorrente == null || terapiaFarmacoCorrente == null) return;

        try {
            FXMLLoader loader = new FXMLLoader(MainApp.class.getResource("/fxml/AddAssunzioneView.fxml"));
            Parent root = loader.load();

            AddAssunzioneController controller = loader.getController();
            controller.initData(pazienteId, terapiaFarmacoCorrente.getId(), this::loadAssunzioni);

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
        if (terapiaCorrente == null || terapiaFarmacoCorrente == null) return;

        String    filter = assunzioniFilter.getValue();
        LocalDate today  = LocalDate.now();

        try {
            List<AssunzioneTerapia> lista =
                    assunzioneDAO.findByPazienteAndTerapiaFarmaco(pazienteId, terapiaFarmacoCorrente.getId());

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

    // ─────────────────── COLONNA EDIT ASSUNZIONI ───────────────────

    private void addEditColumn() {
        colEdit = new TableColumn<>("");
        colEdit.setPrefWidth(40);
        colEdit.setCellFactory(column -> new TableCell<>() {

            private final Button editBtn;

            {
                editBtn = new Button();
                editBtn.getStyleClass().add("icon-edit-btn");

                SVGPath icon = new SVGPath();
                icon.setContent("M12.3 2.3L2 12.6V17h4.4L16.7 6.7 12.3 2.3z");
                icon.setScaleX(0.9);
                icon.setScaleY(0.9);
                editBtn.setGraphic(icon);

                editBtn.setOnAction(event -> {
                    AssunzioneTerapia a = getTableView().getItems().get(getIndex());
                    if (a != null) {
                        openEditAssunzionePopup(a);
                    }
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
    private void reloadTerapieAndSelect(int terapiaIdDaSelezionare) {
        try {
            List<Terapia> lista = terapiaDAO.findByPazienteId(pazienteId);

            therapyCardsContainer.getChildren().clear();
            terapiaCorrente = null;
            terapiaFarmacoCorrente = null;
            selectedCard = null;
            assunzioniTable.getItems().clear();


            if (lista.isEmpty()) {
                Label empty = new Label("Nessuna terapia assegnata");
                empty.getStyleClass().add("page-subtitle");
                therapyCardsContainer.getChildren().add(empty);
                return;
            }

            Terapia terapiaDaSelezionare = null;
            VBox cardDaSelezionare = null;

            for (Terapia t : lista) {
                VBox card = creaCardTerapia(t);
                therapyCardsContainer.getChildren().add(card);

                // se è la terapia che abbiamo appena modificato, la segno
                if (t.getId() == terapiaIdDaSelezionare) {
                    terapiaDaSelezionare = t;
                    cardDaSelezionare = card;
                }
            }

            if (terapiaDaSelezionare != null && cardDaSelezionare != null) {
                // seleziono la terapia modificata
                selezionaTerapia(terapiaDaSelezionare, cardDaSelezionare);
            } else {
                // fallback: seleziono la prima
                Terapia first = lista.get(0);
                VBox firstCard = (VBox) therapyCardsContainer.getChildren().get(0);
                selezionaTerapia(first, firstCard);
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    // ─────────────────── VISIBILITÀ TOOL EDIT ───────────────────

    public void hideEditingTools() {
        if (addAssunzioneButton != null) {
            addAssunzioneButton.setVisible(false);
            addAssunzioneButton.setManaged(false);
        }
        if (colEdit != null) {
            assunzioniTable.getColumns().remove(colEdit);
        }
    }

    public void hideEditingToolsPat() {
        // nascondo il pulsante "Nuova terapia"
        if (addTherapyButton != null) {
            addTherapyButton.setVisible(false);
            addTherapyButton.setManaged(false);
        }

        // nascondo TUTTE le matite nelle card terapia
        for (Node node : therapyCardsContainer.lookupAll("#editTherapyButton")) {
            node.setVisible(false);
            node.setManaged(false);
        }
    }

    // ─────────────────── ADD / EDIT TERAPIA ───────────────────

    @FXML
    private void handleAddTherapy() {
        if (pazienteId <= 0) return;

        try {
            FXMLLoader loader = new FXMLLoader(MainApp.class.getResource("/fxml/AddTherapyView.fxml"));
            Parent root = loader.load();

            AddTherapyController ctrl = loader.getController();
            ctrl.initData(pazienteId, this::loadTerapie);

            Stage popup = new Stage();
            popup.initOwner(therapyRoot.getScene().getWindow());
            popup.setTitle("Nuova terapia");
            popup.setScene(new Scene(root));
            popup.showAndWait();

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void handleEditTherapy(Terapia t) {
        try {
            FXMLLoader loader = new FXMLLoader(
                    MainApp.class.getResource("/fxml/EditTherapyView.fxml"));
            Parent root = loader.load();

            EditTherapyController ctrl = loader.getController();

            // 👇 callback: dopo il salvataggio ricarico e seleziono QUESTA terapia
            ctrl.init(t, () -> reloadTerapieAndSelect(t.getId()));

            Stage popup = new Stage();
            popup.initOwner(therapyRoot.getScene().getWindow());
            popup.setTitle("Modifica terapia");
            popup.setScene(new Scene(root));
            popup.showAndWait();

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}