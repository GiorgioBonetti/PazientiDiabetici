package it.univr.diabete.controller;

import it.univr.diabete.MainApp;
import it.univr.diabete.dao.AssunzioneDAO;
import it.univr.diabete.dao.TerapiaDAO;
import it.univr.diabete.dao.FarmacoTerapiaDAO;
import it.univr.diabete.dao.impl.AssunzioneDAOImpl;
import it.univr.diabete.dao.impl.TerapiaDAOImpl;
import it.univr.diabete.dao.impl.FarmacoTerapiaDAOImpl;
import it.univr.diabete.model.Assunzione;
import it.univr.diabete.model.Terapia;
import it.univr.diabete.model.FarmacoTerapia;
import it.univr.diabete.ui.ErrorDialog;
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
    @FXML private Label  patientNameLabel;
    @FXML private VBox   therapyRoot;

    // filtro assunzioni + tabella
    @FXML private ChoiceBox<String> assunzioniFilter;
    @FXML private Button            addAssunzioneButton;
    @FXML private TableView<Assunzione> assunzioniTable;
    @FXML private TableColumn<Assunzione, String>  colData;
    @FXML private TableColumn<Assunzione, Integer> colQuantita;
    private TableColumn<Assunzione, Void> colEdit;
    private TableColumn<Assunzione, Void> colDelete;

    // card terapie (sopra)
    @FXML private HBox therapyCardsContainer;

    // card farmaci della terapia selezionata (sotto)
    @FXML private HBox farmaciCardsContainer;
    private VBox selectedFarmacoCard;

    private final TerapiaDAO terapiaDAO = new TerapiaDAOImpl();
    private final AssunzioneDAO assunzioneDAO = new AssunzioneDAOImpl();
    private final FarmacoTerapiaDAO farmacoTerapiaDAO = new FarmacoTerapiaDAOImpl();

    private Terapia terapiaCorrente;
    private FarmacoTerapia farmacoTerapiaCorrente;

    private String codiceFiscale;

    // ✅ nuova: email del diabetologo loggato (serve per INSERT Terapia)
    private String fkDiabetologoLoggato;

    private final DateTimeFormatter df = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");
    private VBox selectedCard;

    // ─────────────────── NAVIGATION ───────────────────

    @FXML
    private void handleGoToMeasurements() {
        if (codiceFiscale == null || codiceFiscale.isBlank()) return;
        MainShellController shell = MainApp.getMainShellController();
        if (shell != null) {
            shell.openPatientMeasurements(patientNameLabel.getText(), codiceFiscale);
        }
    }

    @FXML
    private void handleGoToReport() {
        if (codiceFiscale == null || codiceFiscale.isBlank()) return;
        MainShellController shell = MainApp.getMainShellController();
        if (shell != null) {
            shell.openPatientReport(patientNameLabel.getText(), codiceFiscale);
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
        addDeleteColumn();

        assunzioniFilter.getItems().addAll(
                "Tutte", "Oggi", "Ultimi 7 giorni", "Ultimi 30 giorni"
        );
        assunzioniFilter.getSelectionModel().selectFirst();
        assunzioniFilter.setOnAction(e -> applyAssunzioniFilter());
    }

    /**
     * Chiamato dal MainShellController.
     * Qui mi salvo anche l'ID del diabetologo loggato (se sono in modalità diabetologo).
     */
    public void setPatientContext(String nomeCompleto, String codiceFiscale) {
        this.codiceFiscale = codiceFiscale;
        patientNameLabel.setText(nomeCompleto);

        // ✅ recupero diabetologo loggato dal MainShell
        // Se l'app è loggata come Diabetologo, loggedUserId = email diabetologo
        // Se l'app è loggata come Paziente, loggedUserId = CF (quindi NON va usato come fkDiabetologo)
        MainShellController shell = MainApp.getMainShellController();
        if (shell != null && "Diabetologo".equalsIgnoreCase(shell.getRole())) {
            this.fkDiabetologoLoggato = shell.getLoggedUserId();
        } else {
            this.fkDiabetologoLoggato = null;
        }

        loadTerapie();
    }

    // ─────────────────── CARICAMENTO TERAPIE ───────────────────

    private void loadTerapie() {
        try {
            List<Terapia> lista = terapiaDAO.findByPazienteId(codiceFiscale);

            therapyCardsContainer.getChildren().clear();
            terapiaCorrente = null;
            farmacoTerapiaCorrente = null;
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
        farmacoTerapiaCorrente = null;
        addAssunzioneButton.setDisable(true);
        addAssunzioneButton.setStyle("-fx-opacity: 0.4;");
    }

    // ─────────────────── CARD TERAPIA ───────────────────

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

        lblNome.setText(t.getNome());

        try {
            List<FarmacoTerapia> farmaci = farmacoTerapiaDAO.findByTerapiaId(t.getId());
            if (farmaci.isEmpty()) {
                lblInfo.setText("Nessun farmaco assegnato");
            } else {
                FarmacoTerapia tf = farmaci.get(0);

                String subtitle = (tf.getFarmaco() != null)
                        ? tf.getFarmaco().getNome()
                        : "Farmaco ID " + tf.getFkFarmaco();
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

        Button deleteBtn = new Button();
        deleteBtn.getStyleClass().add("icon-delete-btn");
        SVGPath deleteIcon = new SVGPath();
        deleteIcon.setContent("M6 7h10l-1 12H7L6 7zm2-3h6l1 2H7l1-2zm-3 2h14v2H5V6z");
        deleteIcon.getStyleClass().add("svg-path");
        deleteBtn.setGraphic(deleteIcon);
        deleteBtn.setId("deleteTherapyButton");
        deleteBtn.setOnAction(e -> handleDeleteTherapy(t));

        HBox bottomRow = new HBox(8, editBtn, deleteBtn);
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

        if (selectedCard != null) {
            selectedCard.getStyleClass().remove("therapy-card-selected");
        }
        selectedCard = card;
        if (!selectedCard.getStyleClass().contains("therapy-card-selected")) {
            selectedCard.getStyleClass().add("therapy-card-selected");
        }

        try {
            List<FarmacoTerapia> farmaci = farmacoTerapiaDAO.findByTerapiaId(t.getId());

            farmaciCardsContainer.getChildren().clear();
            selectedFarmacoCard = null;

            if (farmaci.isEmpty()) {
                clearDettaglioTerapia();
                assunzioniTable.getItems().clear();
                return;
            }

            for (int i = 0; i < farmaci.size(); i++) {
                FarmacoTerapia tf = farmaci.get(i);
                VBox cardFarmaco = creaCardFarmaco(t, tf);
                farmaciCardsContainer.getChildren().add(cardFarmaco);

                if (i == 0) {
                    selezionaFarmaco(t, tf, cardFarmaco);
                }
            }

        } catch (Exception e) {
            e.printStackTrace();
            clearDettaglioTerapia();
        }
    }

    // ─────────────────── CARD FARMACO ───────────────────

    private VBox creaCardFarmaco(Terapia terapia, FarmacoTerapia tf) {
        VBox card = new VBox(4);
        card.getStyleClass().add("drug-card");
        card.setPadding(new Insets(8, 12, 10, 12));

        card.setMinWidth(180);
        card.setPrefWidth(180);
        card.setMaxWidth(180);

        String nomeFarmaco = (tf.getFarmaco() != null)
                ? tf.getFarmaco().getNome()
                : "Farmaco ID " + tf.getFkFarmaco();

        Label lblNome = new Label(nomeFarmaco);
        lblNome.getStyleClass().add("drug-card-title");

        Label lblDose = new Label("Dosaggio: " + tf.getQuantita() + " mg");
        lblDose.getStyleClass().add("drug-card-line");

        Label lblFreq = new Label("Assunzioni: " + tf.getAssunzioniGiornaliere() + " x/giorno");
        lblFreq.getStyleClass().add("drug-card-line");

        if (terapia.getDataInizio() != null) {
            String periodoText;
            if (terapia.getDataFine() != null) {
                periodoText = terapia.getDataInizio() + " al " + terapia.getDataFine();
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

    private void selezionaFarmaco(Terapia terapia, FarmacoTerapia tf, VBox card) {
        this.terapiaCorrente = terapia;
        this.farmacoTerapiaCorrente = tf;

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
        boolean disable    = nonIniziata || scaduta || (farmacoTerapiaCorrente == null);

        addAssunzioneButton.setDisable(disable);
        addAssunzioneButton.setStyle(disable ? "-fx-opacity: 0.4;" : "-fx-opacity: 1;");
    }

    private void loadAssunzioni() {
        if (terapiaCorrente == null || farmacoTerapiaCorrente == null) {
            assunzioniTable.getItems().clear();
            return;
        }
        applyAssunzioniFilter();
    }

    @FXML
    private void handleAddAssunzione() {
        if (terapiaCorrente == null || farmacoTerapiaCorrente == null) return;

        try {
            FXMLLoader loader = new FXMLLoader(MainApp.class.getResource("/fxml/AddAssunzioneView.fxml"));
            Parent root = loader.load();

            AddAssunzioneController controller = loader.getController();
            controller.initData(
                    codiceFiscale,
                    terapiaCorrente.getId(),                 // ✅ fkTerapia vero
                    farmacoTerapiaCorrente.getFkFarmaco(),   // ✅ fkFarmaco
                    terapiaCorrente.getDataInizio(),         // ✅ Data inizio terapia
                    this::loadAssunzioni
            );
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
        if (terapiaCorrente == null || farmacoTerapiaCorrente == null) return;

        String filter = assunzioniFilter.getValue();
        LocalDate today = LocalDate.now();

        try {
            List<Assunzione> lista = assunzioneDAO.findByPazienteAndTerapiaAndFarmaco(
                    codiceFiscale,
                    farmacoTerapiaCorrente.getFkTerapia(),
                    farmacoTerapiaCorrente.getFkFarmaco()
            );

            List<Assunzione> filtrate = lista.stream()
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

    // ─────────────────── COLONNA EDIT ───────────────────

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
                    Assunzione a = getTableView().getItems().get(getIndex());
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

    // ─────────────────── COLONNA DELETE ───────────────────
    private void addDeleteColumn() {
        colDelete = new TableColumn<>("");
        colDelete.setPrefWidth(40);

        colDelete.setCellFactory(column -> new TableCell<>() {

            private final Button deleteBtn;

            {
                deleteBtn = new Button();
                deleteBtn.getStyleClass().add("icon-delete-btn");

                SVGPath icon = new SVGPath();
                // icona "cestino" semplice (puoi cambiarla)
                icon.setContent("M6 7h10l-1 12H7L6 7zm2-3h6l1 2H7l1-2zm-3 2h14v2H5V6z");
                icon.setScaleX(0.9);
                icon.setScaleY(0.9);
                deleteBtn.setGraphic(icon);

                deleteBtn.setOnAction(event -> {
                    Assunzione a = getTableView().getItems().get(getIndex());
                    if (a != null) {
                        handleDeleteAssunzione(a);
                    }
                });
            }

            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                setGraphic(empty ? null : deleteBtn);
            }
        });

        assunzioniTable.getColumns().add(colDelete);
    }

    private void openEditAssunzionePopup(Assunzione a) {
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
            List<Terapia> lista = terapiaDAO.findByPazienteId(codiceFiscale);

            therapyCardsContainer.getChildren().clear();
            terapiaCorrente = null;
            farmacoTerapiaCorrente = null;
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

                if (t.getId() == terapiaIdDaSelezionare) {
                    terapiaDaSelezionare = t;
                    cardDaSelezionare = card;
                }
            }

            if (terapiaDaSelezionare != null) {
                selezionaTerapia(terapiaDaSelezionare, cardDaSelezionare);
            } else {
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
        if (colDelete != null) {
            assunzioniTable.getColumns().remove(colDelete);
        }
    }

    public void hideEditingToolsPat() {
        if (addTherapyButton != null) {
            addTherapyButton.setVisible(false);
            addTherapyButton.setManaged(false);
        }

        for (Node node : therapyCardsContainer.lookupAll("#editTherapyButton")) {
            node.setVisible(false);
            node.setManaged(false);
        }

        for (Node node : therapyCardsContainer.lookupAll("#deleteTherapyButton")) {
            node.setVisible(false);
            node.setManaged(false);
        }
    }

    // ─────────────────── ADD / EDIT TERAPIA ───────────────────

    private void handleDeleteTherapy(Terapia t) {
        try {
            FXMLLoader loader = new FXMLLoader(
                    MainApp.class.getResource("/fxml/ConfirmDialogView.fxml")
            );
            Parent root = loader.load();

            ConfirmDialogController ctrl = loader.getController();
            ctrl.setTexts(
                    "Conferma eliminazione",
                    "Eliminare la terapia \"" + t.getNome() + "\"?\n" +
                    "Verranno eliminate anche tutte le assunzioni registrate."
            );

            Stage dialog = new Stage();
            dialog.initOwner(therapyRoot.getScene().getWindow());
            dialog.initModality(Modality.WINDOW_MODAL);
            dialog.setResizable(false);
            dialog.setScene(new Scene(root));
            dialog.setTitle("Conferma");
            dialog.showAndWait();

            if (ctrl.isConfirmed()) {
                try {
                    terapiaDAO.delete(t.getId());
                    loadTerapie();
                } catch (Exception ex) {
                    ex.printStackTrace();
                    ErrorDialog.show("Errore eliminazione",
                            "Impossibile eliminare la terapia. Verificare che non sia collegata ad altri dati.");
                }
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @FXML
    private void handleAddTherapy() {
        if (codiceFiscale == null || codiceFiscale.isBlank()) return;

        // ✅ qui deve esserci un diabetologo loggato, altrimenti non posso creare terapia
        if (fkDiabetologoLoggato == null || fkDiabetologoLoggato.isBlank()) {
            // se vuoi: mostra alert globale
            MainShellController shell = MainApp.getMainShellController();
            if (shell != null) shell.showGlobalError("Solo il diabetologo può creare una terapia.");
            return;
        }

        try {
            FXMLLoader loader = new FXMLLoader(MainApp.class.getResource("/fxml/AddTherapyView.fxml"));
            Parent root = loader.load();

            AddTherapyController ctrl = loader.getController();

            // ✅ CAMBIO: passiamo anche fkDiabetologo (email del loggato)
            ctrl.initData(codiceFiscale, fkDiabetologoLoggato, this::loadTerapie);

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

    // ─────────────────── DELETE ASSUNZIONE ───────────────────

    private void handleDeleteAssunzione(Assunzione a) {
        try {
            FXMLLoader loader = new FXMLLoader(
                    MainApp.class.getResource("/fxml/ConfirmDialogView.fxml")
            );
            Parent root = loader.load();

            ConfirmDialogController ctrl = loader.getController();

            String dateStr = "data non disponibile";
            String nomeFarmaco = "";
            if (a != null && a.getDateStamp() != null) {
                dateStr = df.format(a.getDateStamp());
            }
            if (farmacoTerapiaCorrente != null && farmacoTerapiaCorrente.getFarmaco() != null) {
                nomeFarmaco = farmacoTerapiaCorrente.getFarmaco().getNome();
            }

            ctrl.setTexts(
                    "Conferma eliminazione",
                    "Eliminare l’assunzione del farmaco " + nomeFarmaco + " aggiunto il " + dateStr + "?\n"
            );

            Stage dialog = new Stage();
            dialog.initOwner(assunzioniTable.getScene().getWindow());
            dialog.initModality(Modality.WINDOW_MODAL);
            dialog.setResizable(false);
            dialog.setScene(new Scene(root));
            dialog.setTitle("Conferma");
            dialog.showAndWait();

            if (ctrl.isConfirmed()) {
                assunzioneDAO.delete(a.getId());
                loadAssunzioni();
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}

