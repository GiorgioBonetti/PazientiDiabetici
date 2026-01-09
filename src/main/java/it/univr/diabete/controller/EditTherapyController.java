package it.univr.diabete.controller;

import it.univr.diabete.database.Database;
import it.univr.diabete.model.Terapia;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.geometry.Insets;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.Modality;
import javafx.stage.Stage;

import java.sql.*;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public class EditTherapyController {

    @FXML private DatePicker dataInizioPicker;
    @FXML private DatePicker dataFinePicker;

    @FXML private TextField searchFarmacoField;
    @FXML private ListView<String> farmacoListView;

    @FXML private HBox selectedFarmaciPane;
    @FXML private ScrollPane selectedFarmaciScroll;

    @FXML private Label errorLabel;
    @FXML private TextField nomeTerapiaField;

    private Terapia terapiaOriginale;
    private Runnable onUpdatedCallback;

    private final ObservableList<String> allFarmaci = FXCollections.observableArrayList();

    private static class FarmacoInTerapia {
        int fkFarmaco;
        String nome;
        int assunzioniGiornaliere;
        int quantita;

        FarmacoInTerapia(int fkFarmaco, String nome, int assunzioniGiornaliere, int quantita) {
            this.fkFarmaco = fkFarmaco;
            this.nome = nome;
            this.assunzioniGiornaliere = assunzioniGiornaliere;
            this.quantita = quantita;
        }
    }

    private final ObservableList<FarmacoInTerapia> selectedFarmaci = FXCollections.observableArrayList();

    // elenco farmaci rimossi (per fare delete)
    private final List<FarmacoInTerapia> farmaciDaCancellare = new ArrayList<>();

    @FXML
    private void initialize() {
        errorLabel.setVisible(false);
        errorLabel.setManaged(false);

        farmacoListView.setItems(allFarmaci);
        farmacoListView.setCellFactory(lv -> createFarmacoCardCell());

        selectedFarmaciScroll.vvalueProperty().addListener((obs, oldV, newV) -> {
            if (newV.doubleValue() != 0.0) selectedFarmaciScroll.setVvalue(0.0);
        });

        searchFarmacoField.textProperty().addListener((obs, oldV, newV) -> filterFarmaciList(newV));

        farmacoListView.setOnMouseClicked(event -> {
            if (event.getClickCount() == 1) {
                String nome = farmacoListView.getSelectionModel().getSelectedItem();
                if (nome != null) onFarmacoSelected(nome);
            }
        });

        selectedFarmaci.addListener((javafx.collections.ListChangeListener<FarmacoInTerapia>) c -> refreshSelectedFarmaciChips());

        loadFarmaciFromDb();
    }

    public void init(Terapia terapia, Runnable onUpdatedCallback) {
        this.terapiaOriginale = terapia;
        this.onUpdatedCallback = onUpdatedCallback;

        dataInizioPicker.setValue(terapia.getDataInizio());
        dataFinePicker.setValue(terapia.getDataFine());

        // ✅ nome terapia in edit
        nomeTerapiaField.setText(terapia.getNome() != null ? terapia.getNome() : "");

        // ✅ carico farmaci della terapia (senza versione)
        loadFarmaciDellaTerapia(terapia.getId());
    }

    @FXML
    private void handleSave() {
        hideError();

        LocalDate dataInizio = dataInizioPicker.getValue();
        LocalDate dataFine   = dataFinePicker.getValue();

        if (dataInizio == null) { showError("Seleziona una data di inizio."); return; }
        if (dataFine != null && dataFine.isBefore(dataInizio)) { showError("La data di fine non può essere precedente."); return; }

        String nomeTerapia = (nomeTerapiaField.getText() != null) ? nomeTerapiaField.getText().trim() : "";
        if (nomeTerapia.isEmpty()) { showError("Inserisci il nome della terapia."); return; }

        if (selectedFarmaci.isEmpty()) { showError("Aggiungi almeno un farmaco alla terapia."); return; }

        try (Connection conn = Database.getConnection()) {
            conn.setAutoCommit(false);

            // ✅ update terapia (nome + date + ultimaModifica)
            updateTerapia(conn, terapiaOriginale.getId(), nomeTerapia, dataInizio, dataFine);

            // ✅ upsert farmaci (update se esiste, altrimenti insert)
            for (FarmacoInTerapia fit : selectedFarmaci) {
                if (existsFarmacoTerapia(conn, terapiaOriginale.getId(), fit.fkFarmaco)) {
                    updateFarmacoTerapia(conn, terapiaOriginale.getId(), fit.fkFarmaco,
                            fit.assunzioniGiornaliere, fit.quantita);
                } else {
                    insertFarmacoTerapia(conn, terapiaOriginale.getId(), fit.fkFarmaco,
                            fit.assunzioniGiornaliere, fit.quantita);
                }
            }

            // ✅ delete farmaci rimossi (e assunzioni collegate se presenti)
            for (FarmacoInTerapia del : farmaciDaCancellare) {
                deleteFarmacoTerapiaAndAssunzioni(conn, terapiaOriginale.getId(), del.fkFarmaco);
            }

            conn.commit();

            // aggiorna oggetto in memoria (utile per UI)
            terapiaOriginale.setNome(nomeTerapia);
            terapiaOriginale.setDataInizio(dataInizio);
            terapiaOriginale.setDataFine(dataFine);

            if (onUpdatedCallback != null) onUpdatedCallback.run();
            closeWindow();

        } catch (Exception e) {
            e.printStackTrace();
            showError("Errore durante il salvataggio delle modifiche.");
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

    private void hideError() {
        errorLabel.setVisible(false);
        errorLabel.setManaged(false);
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
        farmacoListView.setItems(allFarmaci.filtered(name -> name.toLowerCase().contains(lower)));
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

    // ✅ senza versione
    private void loadFarmaciDellaTerapia(int idTerapia) {
        selectedFarmaci.clear();
        farmaciDaCancellare.clear();

        String sql = """
            SELECT tf.fkFarmaco, f.nome, tf.assunzioniGiornaliere, tf.quantita
            FROM FarmacoTerapia tf
            JOIN Farmaco f ON tf.fkFarmaco = f.id
            WHERE tf.fkTerapia = ?
            """;

        try (Connection conn = Database.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, idTerapia);

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    int fkFarmaco = rs.getInt("fkFarmaco");
                    String nome = rs.getString("nome");
                    int ass = rs.getInt("assunzioniGiornaliere");
                    int quant = rs.getInt("quantita");

                    selectedFarmaci.add(new FarmacoInTerapia(fkFarmaco, nome, ass, quant));
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private void onFarmacoSelected(String nomeFarmaco) {
        try {
            javafx.fxml.FXMLLoader loader =
                    new javafx.fxml.FXMLLoader(it.univr.diabete.MainApp.class.getResource("/fxml/FarmacoDoseDialogView.fxml"));
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
                int idFarmaco = ensureFarmaco(conn, nomeFarmaco);

                // se già presente, sovrascrivo dosi
                FarmacoInTerapia esistente = null;
                for (FarmacoInTerapia f : selectedFarmaci) {
                    if (f.fkFarmaco == idFarmaco) { esistente = f; break; }
                }
                if (esistente != null) selectedFarmaci.remove(esistente);

                selectedFarmaci.add(new FarmacoInTerapia(idFarmaco, nomeFarmaco, assunzioni, quantita));
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void refreshSelectedFarmaciChips() {
        selectedFarmaciPane.getChildren().clear();

        for (FarmacoInTerapia fit : selectedFarmaci) {
            Label nameLabel = new Label(fit.nome + " (" + fit.assunzioniGiornaliere + "x, " + fit.quantita + " mg)");
            nameLabel.getStyleClass().add("chip-label");

            Button removeBtn = new Button("x");
            removeBtn.getStyleClass().add("btn-ghost");
            removeBtn.setOnAction(e -> {
                farmaciDaCancellare.add(fit);
                selectedFarmaci.remove(fit);
            });

            HBox chip = new HBox(4, nameLabel, removeBtn);
            chip.setPadding(new Insets(4, 8, 4, 8));
            chip.setAlignment(javafx.geometry.Pos.CENTER);
            chip.getStyleClass().add("chip");

            selectedFarmaciPane.getChildren().add(chip);
        }
    }

    private int ensureFarmaco(Connection conn, String nome) throws SQLException {
        String selectSql = "SELECT id FROM Farmaco WHERE nome = ? LIMIT 1";
        try (PreparedStatement ps = conn.prepareStatement(selectSql)) {
            ps.setString(1, nome);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) return rs.getInt("id");
            }
        }

        String insertSql = "INSERT INTO Farmaco (nome, marca) VALUES (?, NULL, 0)";
        try (PreparedStatement ps = conn.prepareStatement(insertSql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setString(1, nome);
            ps.executeUpdate();
            try (ResultSet rs = ps.getGeneratedKeys()) {
                if (rs.next()) return rs.getInt(1);
            }
        }

        throw new SQLException("Impossibile creare/recuperare il farmaco.");
    }

    // ✅ aggiorna anche nome + ultimaModifica
    private void updateTerapia(Connection conn, int idTerapia, String nome, LocalDate dataInizio, LocalDate dataFine) throws SQLException {
        String sql = """
            UPDATE Terapia
            SET nome = ?,
                dataInizio = ?,
                dataFine = ?,
                ultimaModifica = NOW()
            WHERE id = ?
            """;

        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, nome);
            ps.setDate(2, Date.valueOf(dataInizio));
            if (dataFine != null) ps.setDate(3, Date.valueOf(dataFine));
            else ps.setNull(3, Types.DATE);
            ps.setInt(4, idTerapia);
            ps.executeUpdate();
        }
    }

    private boolean existsFarmacoTerapia(Connection conn, int fkTerapia, int fkFarmaco) throws SQLException {
        String sql = "SELECT 1 FROM FarmacoTerapia WHERE fkTerapia = ? AND fkFarmaco = ? LIMIT 1";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, fkTerapia);
            ps.setInt(2, fkFarmaco);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next();
            }
        }
    }

    private void updateFarmacoTerapia(Connection conn, int fkTerapia, int fkFarmaco,
                                      int assunzioniGiornaliere, int quantita) throws SQLException {
        String sql = """
            UPDATE FarmacoTerapia
            SET assunzioniGiornaliere = ?, quantita = ?
            WHERE fkTerapia = ? AND fkFarmaco = ?
            """;

        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, assunzioniGiornaliere);
            ps.setInt(2, quantita);
            ps.setInt(3, fkTerapia);
            ps.setInt(4, fkFarmaco);
            ps.executeUpdate();
        }
    }

    private void insertFarmacoTerapia(Connection conn, int fkTerapia, int fkFarmaco,
                                      int assunzioniGiornaliere, int quantita) throws SQLException {
        String sql = """
            INSERT INTO FarmacoTerapia (fkTerapia, fkFarmaco, assunzioniGiornaliere, quantita)
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

    private void deleteFarmacoTerapiaAndAssunzioni(Connection conn, int fkTerapia, int fkFarmaco) throws SQLException {
        // Se vuoi cancellare anche le assunzioni collegate:
        // (se la tua tabella si chiama AssunzioneTerapia invece di Assunzione, cambia qui)
        String sqlAss = "DELETE FROM Assunzione WHERE fkTerapia = ? AND fkFarmaco = ?";
        try (PreparedStatement ps = conn.prepareStatement(sqlAss)) {
            ps.setInt(1, fkTerapia);
            ps.setInt(2, fkFarmaco);
            ps.executeUpdate();
        }

        String sqlTf = "DELETE FROM FarmacoTerapia WHERE fkTerapia = ? AND fkFarmaco = ?";
        try (PreparedStatement ps = conn.prepareStatement(sqlTf)) {
            ps.setInt(1, fkTerapia);
            ps.setInt(2, fkFarmaco);
            ps.executeUpdate();
        }
    }
}