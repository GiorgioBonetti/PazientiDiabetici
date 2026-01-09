package it.univr.diabete.controller;

import it.univr.diabete.database.Database;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.Modality;
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
    private String fkDiabetologo;       // email diabetologo loggato
    private Runnable onSavedCallback;

    private final ObservableList<String> allFarmaci = FXCollections.observableArrayList();

    private static class FarmacoInTerapia {
        int fkFarmaco;
        String nome;
        int assunzioniGiornaliere;
        int quantitaAssunzione;

        FarmacoInTerapia(int fkFarmaco, String nome, int assunzioniGiornaliere, int quantitaAssunzione) {
            this.fkFarmaco = fkFarmaco;
            this.nome = nome;
            this.assunzioniGiornaliere = assunzioniGiornaliere;
            this.quantitaAssunzione = quantitaAssunzione;
        }
    }

    private final ObservableList<FarmacoInTerapia> selectedFarmaci = FXCollections.observableArrayList();

    @FXML
    private void initialize() {
        errorLabel.setVisible(false);
        errorLabel.setManaged(false);

        dataInizioPicker.setValue(LocalDate.now());

        farmacoListView.setItems(allFarmaci);
        farmacoListView.setCellFactory(lv -> createFarmacoCardCell());

        loadFarmaciFromDb();

        searchFarmacoField.textProperty().addListener((obs, oldV, newV) -> filterFarmaciList(newV));

        farmacoListView.setOnMouseClicked(event -> {
            if (event.getClickCount() == 1) {
                String nome = farmacoListView.getSelectionModel().getSelectedItem();
                if (nome != null) onFarmacoSelected(nome);
            }
        });

        selectedFarmaci.addListener((javafx.collections.ListChangeListener<FarmacoInTerapia>) c -> refreshSelectedFarmaciChips());
        selectedFarmaciScroll.vvalueProperty().addListener((obs, oldV, newV) -> {
            if (newV.doubleValue() != 0.0) selectedFarmaciScroll.setVvalue(0.0);
        });
    }

    private ListCell<String> createFarmacoCardCell() {
        return new ListCell<>() {
            private final VBox root = new VBox();
            private final Label nameLbl = new Label();

            {
                setPadding(new Insets(2, 0, 2, 0));

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

    /** Qui arrivano CF paziente + email diabetologo */
    public void initData(String codiceFiscale, String fkDiabetologo, Runnable onSavedCallback) {
        this.codiceFiscale = codiceFiscale;
        this.fkDiabetologo = fkDiabetologo;
        this.onSavedCallback = onSavedCallback;
    }

    @FXML
    private void handleSave() {
        errorLabel.setVisible(false);
        errorLabel.setManaged(false);

        String nomeTerapia = nomeTerapiaField.getText() != null ? nomeTerapiaField.getText().trim() : "";
        if (nomeTerapia.isEmpty()) {
            showError("Inserisci un nome per la terapia.");
            return;
        }

        if (codiceFiscale == null || codiceFiscale.isBlank()) {
            showError("Paziente non valido.");
            return;
        }

        if (fkDiabetologo == null || fkDiabetologo.isBlank()) {
            showError("Diabetologo non valido (fkDiabetologo mancante).");
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

            int fkTerapia = insertTerapia(conn, nomeTerapia, dataInizio, dataFine, fkDiabetologo, codiceFiscale);

            for (FarmacoInTerapia fit : selectedFarmaci) {
                insertFarmacoTerapia(conn,
                        fkTerapia,
                        fit.fkFarmaco,
                        fit.assunzioniGiornaliere,
                        fit.quantitaAssunzione);
            }

            conn.commit();

            if (onSavedCallback != null) onSavedCallback.run();
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

    private void loadFarmaciFromDb() {
        allFarmaci.clear();
        String sql = "SELECT nome FROM Farmaco ORDER BY nome";

        try (Connection conn = Database.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) allFarmaci.add(rs.getString("nome"));

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
        ObservableList<String> filtered = allFarmaci.filtered(name -> name.toLowerCase().contains(lower));
        farmacoListView.setItems(filtered);
    }

    private void onFarmacoSelected(String nomeFarmaco) {
        try {
            javafx.fxml.FXMLLoader loader = new javafx.fxml.FXMLLoader(
                    it.univr.diabete.MainApp.class.getResource("/fxml/FarmacoDoseDialogView.fxml"));
            Parent root = loader.load();

            FarmacoDoseDialogController ctrl = loader.getController();
            ctrl.init(nomeFarmaco);

            Stage popup = new Stage();
            popup.initOwner(searchFarmacoField.getScene().getWindow());
            popup.initModality(Modality.WINDOW_MODAL);
            popup.setTitle("Imposta dosi");
            popup.setScene(new Scene(root));
            popup.showAndWait();

            if (!ctrl.isConfirmed()) return;

            int assunzioni = ctrl.getAssunzioni();
            int quantita = ctrl.getUnita();

            try (Connection conn = Database.getConnection()) {
                int fkFarmaco = ensureFarmaco(conn, nomeFarmaco);

                // se già presente, sovrascrivo dosi
                FarmacoInTerapia esistente = null;
                for (FarmacoInTerapia f : selectedFarmaci) {
                    if (f.fkFarmaco == fkFarmaco) { esistente = f; break; }
                }
                if (esistente != null) selectedFarmaci.remove(esistente);

                selectedFarmaci.add(new FarmacoInTerapia(fkFarmaco, nomeFarmaco, assunzioni, quantita));
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void refreshSelectedFarmaciChips() {
        selectedFarmaciPane.getChildren().clear();

        for (FarmacoInTerapia fit : selectedFarmaci) {
            Label nameLabel = new Label(fit.nome + " (" + fit.assunzioniGiornaliere + "x, " + fit.quantitaAssunzione + " mg.)");
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

    private int ensureFarmaco(Connection conn, String nome) throws SQLException {
        String selectSql = """
                SELECT id
                FROM Farmaco
                WHERE nome = ?
                LIMIT 1
                """;

        try (PreparedStatement ps = conn.prepareStatement(selectSql)) {
            ps.setString(1, nome);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) return rs.getInt("id");
            }
        }

        String insertSql = """
                INSERT INTO Farmaco (nome, marca)
                VALUES (?, NULL)
                """;

        try (PreparedStatement ps = conn.prepareStatement(insertSql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setString(1, nome);
            ps.executeUpdate();

            try (ResultSet rs = ps.getGeneratedKeys()) {
                if (rs.next()) return rs.getInt(1);
            }
        }

        throw new SQLException("Impossibile creare/recuperare il farmaco.");
    }

    // ✅ INSERT Terapia SENZA versione, CON nome, e ultimaModifica = NOW()
    private int insertTerapia(Connection conn,
                              String nome,
                              LocalDate dataInizio,
                              LocalDate dataFine,
                              String fkDiabetologo,
                              String fkPaziente) throws SQLException {

        String sql = """
                INSERT INTO Terapia
                    (nome, dataInizio, dataFine, ultimaModifica, fkDiabetologo, fkPaziente)
                VALUES (?, ?, ?, NOW(), ?, ?)
                """;

        try (PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            ps.setString(1, nome);
            ps.setDate(2, Date.valueOf(dataInizio));

            if (dataFine != null) ps.setDate(3, Date.valueOf(dataFine));
            else ps.setNull(3, Types.DATE);

            ps.setString(4, fkDiabetologo);
            ps.setString(5, fkPaziente);

            ps.executeUpdate();

            try (ResultSet rs = ps.getGeneratedKeys()) {
                if (rs.next()) return rs.getInt(1);
            }
        }

        throw new SQLException("Impossibile ottenere l'id della terapia appena inserita.");
    }

    // ✅ INSERT FarmacoTerapia SENZA fkVersioneTerapia
    private void insertFarmacoTerapia(Connection conn,
                                      int fkTerapia,
                                      int fkFarmaco,
                                      int assunzioniGiornaliere,
                                      int quantita) throws SQLException {

        String sql = """
                INSERT INTO FarmacoTerapia
                    (fkTerapia, fkFarmaco, assunzioniGiornaliere, quantita)
                VALUES (?, ?, ?, ?)
                """;

        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, fkTerapia);
            ps.setInt(2, fkFarmaco);
            ps.setInt(3, assunzioniGiornaliere);
            ps.setInt(4, quantita);
            ps.executeUpdate();
        }
    }
}