package it.univr.diabete.controller;

import it.univr.diabete.database.Database;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import javafx.geometry.Insets;

import java.sql.*;
import java.time.LocalDate;

public class AddTherapyController {

    @FXML private DatePicker dataInizioPicker;
    @FXML private DatePicker dataFinePicker;
    @FXML private TextField searchFarmacoField;
    @FXML private ListView<String> farmacoListView;
    @FXML private HBox selectedFarmaciPane;
    @FXML private Label errorLabel;
    @FXML private ScrollPane selectedFarmaciScroll;
    @FXML private TextField nomeTerapiaField;
    private String codiceFiscale;
    private Runnable onSavedCallback;

    // Tutti i nomi farmaco dal DB
    private final ObservableList<String> allFarmaci = FXCollections.observableArrayList();

    // Farmaci selezionati per questa terapia
    private static class FarmacoInTerapia {
        int idFarmaco;
        String nome;
        int assunzioniGiornaliere;
        int quantitaAssunzione;

        FarmacoInTerapia(int idFarmaco, String nome,
                         int assunzioniGiornaliere, int quantitaAssunzione) {
            this.idFarmaco = idFarmaco;
            this.nome = nome;
            this.assunzioniGiornaliere = assunzioniGiornaliere;
            this.quantitaAssunzione = quantitaAssunzione;
        }
    }

    private final ObservableList<FarmacoInTerapia> selectedFarmaci =
            FXCollections.observableArrayList();

    @FXML
    private void initialize() {
        errorLabel.setVisible(false);
        errorLabel.setManaged(false);

        dataInizioPicker.setValue(LocalDate.now());

        // collega ListView ai farmaci
        farmacoListView.setItems(allFarmaci);

        // card stile paziente + spazio tra i box
        farmacoListView.setCellFactory(lv -> createFarmacoCardCell());

        // carica farmaci dal DB
        loadFarmaciFromDb();

        // filtro live sulla search
        searchFarmacoField.textProperty().addListener((obs, oldV, newV) -> filterFarmaciList(newV));

        // click singolo su un farmaco -> popup dosi (continua a funzionare)
        farmacoListView.setOnMouseClicked(event -> {
            if (event.getClickCount() == 1) {
                String nome = farmacoListView.getSelectionModel().getSelectedItem();
                if (nome != null) {
                    onFarmacoSelected(nome);
                }
            }
        });

        selectedFarmaci.addListener((javafx.collections.ListChangeListener<FarmacoInTerapia>) c -> refreshSelectedFarmaciChips());
        selectedFarmaciScroll.vvalueProperty().addListener((obs, oldV, newV) -> {
            if (newV.doubleValue() != 0.0) {
                selectedFarmaciScroll.setVvalue(0.0);
            }
        });

    }
    private ListCell<String> createFarmacoCardCell() {
        return new ListCell<>() {

            private final VBox root = new VBox();
            private final Label nameLbl = new Label();

            {
                // SPAZIO TRA I BOX 👇
                setPadding(new Insets(2, 0, 2, 0)); // sopra/sotto = margine tra una card e l’altra

                // stile della card bianca
                root.setPadding(new Insets(10));
                root.setStyle("""
                -fx-background-color: white;
                -fx-background-radius: 18;
                -fx-border-radius: 18;
                -fx-border-color: #e5e7eb;
                -fx-effect: dropshadow(gaussian, rgba(15,23,42,0.06), 8, 0.2, 0, 2);
                """);

                nameLbl.setStyle("-fx-font-size: 13px; -fx-text-fill: #111827;");

                root.getChildren().add(nameLbl);

                // hover carino
                root.setOnMouseEntered(e -> root.setStyle("""
                -fx-background-color: #f5f3ff;
                -fx-background-radius: 18;
                -fx-border-radius: 18;
                -fx-cursor: hand;
                -fx-border-color: #a855f7;
                -fx-effect: dropshadow(gaussian, rgba(129,140,248,0.35), 12, 0.3, 0, 4);
                """));

                root.setOnMouseExited(e -> root.setStyle("""
                -fx-background-color: white;
                -fx-background-radius: 18;
                -fx-border-radius: 18;
                -fx-border-color: #e5e7eb;
                -fx-effect: dropshadow(gaussian, rgba(15,23,42,0.06), 8, 0.2, 0, 2);
                """));
            }

            @Override
            protected void updateItem(String nome, boolean empty) {
                super.updateItem(nome, empty);

                if (empty || nome == null) {
                    setGraphic(null);
                    setText(null);
                } else {
                    nameLbl.setText(nome);
                    setGraphic(root);
                    setText(null);
                }
            }
        };
    }

    /** Chiamato dal PatientTherapyController quando apre il popup. */
    public void initData(String codiceFiscale, Runnable onSavedCallback) {
        this.codiceFiscale = codiceFiscale;
        this.onSavedCallback = onSavedCallback;
    }

    // ────────────────────── HANDLE SAVE / CANCEL ──────────────────────

    @FXML
    private void handleSave() {
        errorLabel.setVisible(false);
        errorLabel.setManaged(false);
        String nomeTerapia = nomeTerapiaField.getText() != null
                ? nomeTerapiaField.getText().trim()
                : "";

        if (nomeTerapia.isEmpty()) {
            showError("Inserisci un nome per la terapia.");
            return;
        }

        LocalDate dataInizio = dataInizioPicker.getValue();
        LocalDate dataFine = dataFinePicker.getValue();

        if (dataInizio == null) {
            showError("Seleziona una data di inizio.");
            return;
        }

        if (dataFine != null && dataFine.isBefore(dataInizio)) {
            showError("La data di fine non può essere precedente alla data di inizio.");
            return;
        }

        if (selectedFarmaci.isEmpty()) {
            showError("Aggiungi almeno un farmaco alla terapia.");
            return;
        }

        try (Connection conn = Database.getConnection()) {
            conn.setAutoCommit(false);

            // 1️⃣ inserisco la Terapia
            int idTerapia = insertTerapia(conn, nomeTerapia, dataInizio, dataFine, codiceFiscale);

            // 2️⃣ una riga in TerapiaFarmaco per ogni farmaco selezionato
            for (FarmacoInTerapia fit : selectedFarmaci) {
                insertTerapiaFarmaco(conn,
                        idTerapia,
                        fit.idFarmaco,
                        fit.assunzioniGiornaliere,
                        fit.quantitaAssunzione);
            }

            conn.commit();

            if (onSavedCallback != null) {
                onSavedCallback.run();   // ricarica lista terapie nella pagina
            }

            closeWindow();

        } catch (Exception e) {
            e.printStackTrace();
            showError("Errore durante il salvataggio della terapia.");
        }
    }

    @FXML
    private void handleCancel() {
        closeWindow();
    }

    private void closeWindow() {
        Stage stage = (Stage) dataInizioPicker.getScene().getWindow();
        stage.close();
    }

    private void showError(String msg) {
        errorLabel.setText(msg);
        errorLabel.setVisible(true);
        errorLabel.setManaged(true);
    }

    // ────────────────────── GESTIONE FARMACI (lista + search + pilloline) ──────────────────────

    private void loadFarmaciFromDb() {
        allFarmaci.clear();
        String sql = "SELECT Nome FROM Farmaco ORDER BY Nome";

        try (Connection conn = Database.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                allFarmaci.add(rs.getString("Nome"));
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private void filterFarmaciList(String filter) {
        if (filter == null || filter.isBlank()) {
            farmacoListView.setItems(allFarmaci);
            return;
        }

        String lower = filter.toLowerCase();
        ObservableList<String> filtered = allFarmaci.filtered(
                name -> name.toLowerCase().contains(lower)
        );
        farmacoListView.setItems(filtered);
    }

    private void onFarmacoSelected(String nomeFarmaco) {
        try {
            javafx.fxml.FXMLLoader loader =
                    new javafx.fxml.FXMLLoader(
                            it.univr.diabete.MainApp.class.getResource("/fxml/FarmacoDoseDialogView.fxml"));
            javafx.scene.Parent root = loader.load();

            FarmacoDoseDialogController ctrl = loader.getController();
            ctrl.init(nomeFarmaco);

            Stage popup = new Stage();
            popup.initOwner(searchFarmacoField.getScene().getWindow());
            popup.initModality(javafx.stage.Modality.WINDOW_MODAL);
            popup.setTitle("Imposta dosi");
            popup.setScene(new javafx.scene.Scene(root));
            popup.showAndWait();

            if (!ctrl.isConfirmed()) {
                return; // utente ha annullato
            }

            int assunzioni = ctrl.getAssunzioni();
            int quantita = ctrl.getUnita();

            try (Connection conn = Database.getConnection()) {
                int idFarmaco = ensureFarmaco(conn, nomeFarmaco);

                selectedFarmaci.add(
                        new FarmacoInTerapia(idFarmaco, nomeFarmaco, assunzioni, quantita)
                );

            } catch (SQLException e) {
                e.printStackTrace();
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void refreshSelectedFarmaciChips() {
        selectedFarmaciPane.getChildren().clear();

        for (FarmacoInTerapia fit : selectedFarmaci) {
            Label nameLabel = new Label(
                    fit.nome + " (" + fit.assunzioniGiornaliere + "x, " + fit.quantitaAssunzione + " mg.)"
            );
            nameLabel.getStyleClass().add("chip-label");

            Button removeBtn = new Button("x");
            removeBtn.getStyleClass().add("btn-ghost");
            removeBtn.setOnAction(e -> selectedFarmaci.remove(fit));

            HBox chip = new HBox(4, nameLabel, removeBtn);
            chip.setPadding(new Insets(4, 8, 4, 8));
            chip.setAlignment(javafx.geometry.Pos.CENTER);
            chip.getStyleClass().add("chip");

            selectedFarmaciPane.getChildren().add(chip);
        }
    }

    // ────────────────────── SQL HELPER ──────────────────────

    private int ensureFarmaco(Connection conn, String nome) throws SQLException {
        String selectSql = """
                SELECT Id
                FROM Farmaco
                WHERE Nome = ?
                LIMIT 1
                """;

        try (PreparedStatement ps = conn.prepareStatement(selectSql)) {
            ps.setString(1, nome);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt("Id");
                }
            }
        }

        String insertSql = """
                INSERT INTO Farmaco (Nome, Marca)
                VALUES (?, NULL)
                """;

        try (PreparedStatement ps = conn.prepareStatement(insertSql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setString(1, nome);
            ps.executeUpdate();

            try (ResultSet rs = ps.getGeneratedKeys()) {
                if (rs.next()) {
                    return rs.getInt(1);
                }
            }
        }

        throw new SQLException("Impossibile creare/recuperare il farmaco.");
    }

    private int insertTerapia(Connection conn,
                              String nomeTerapia,
                              LocalDate dataInizio,
                              LocalDate dataFine,
                              String codiceFiscale) throws SQLException {

        String sql = """
            INSERT INTO Terapia (Nome, DataInizio, DataFine, IdDiabetologo, fkPaziente)
            VALUES (?, ?, ?, ?, ?)
            """;

        try (PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            ps.setString(1, nomeTerapia);

            if (dataInizio != null) {
                ps.setDate(2, Date.valueOf(dataInizio));
            } else {
                ps.setNull(2, Types.DATE);
            }

            if (dataFine != null) {
                ps.setDate(3, Date.valueOf(dataFine));
            } else {
                ps.setNull(3, Types.DATE);
            }

            ps.setInt(4, 1);          // TODO: Id diabetologo loggato
            ps.setString(5,  codiceFiscale);

            ps.executeUpdate();

            try (ResultSet rs = ps.getGeneratedKeys()) {
                if (rs.next()) {
                    return rs.getInt(1);
                }
            }
        }

        throw new SQLException("Impossibile ottenere l'Id della terapia appena inserita.");
    }

    private void insertTerapiaFarmaco(Connection conn,
                                      int idTerapia,
                                      int idFarmaco,
                                      int assunzioniGiornaliere,
                                      int quantitaAssunzione) throws SQLException {

        String sql = """
                INSERT INTO TerapiaFarmaco
                (IdTerapia, IdFarmaco, AssunzioniGiornaliere, QuantitaAssunzione)
                VALUES (?, ?, ?, ?)
                """;

        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, idTerapia);
            ps.setInt(2, idFarmaco);
            ps.setInt(3, assunzioniGiornaliere);
            ps.setInt(4, quantitaAssunzione);
            ps.executeUpdate();
        }
    }
}