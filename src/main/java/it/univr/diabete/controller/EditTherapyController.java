package it.univr.diabete.controller;

import it.univr.diabete.database.Database;
import it.univr.diabete.model.Terapia;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.geometry.Insets;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

import java.sql.*;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public class EditTherapyController {

    @FXML private DatePicker dataInizioPicker;
    @FXML private DatePicker dataFinePicker;
    @FXML private TextField  searchFarmacoField;
    @FXML private ListView<String> farmacoListView;
    @FXML private HBox selectedFarmaciPane;
    @FXML private ScrollPane selectedFarmaciScroll;
    @FXML private Label errorLabel;
    @FXML private TextField nomeTerapiaField;

    private Terapia terapiaOriginale;
    private Runnable onUpdatedCallback;

    // tutti i nomi farmaco (per la lista di ricerca)
    private final ObservableList<String> allFarmaci =
            FXCollections.observableArrayList();

    /**
     * Rappresenta un farmaco nella terapia mentre editiamo.
     * fkTerapia = id della riga in tabella TerapiaFarmaco (null se nuovo).
     */
    private static class FarmacoInTerapia {
        Integer fkTerapia;     // può essere null per farmaci nuovi
        int idFarmaco;
        String nome;
        int assunzioniGiornaliere;
        int quantita;

        FarmacoInTerapia(Integer fkTerapia,
                         int idFarmaco,
                         String nome,
                         int assunzioniGiornaliere,
                         int quantita) {
            this.fkTerapia = fkTerapia;
            this.idFarmaco = idFarmaco;
            this.nome = nome;
            this.assunzioniGiornaliere = assunzioniGiornaliere;
            this.quantita = quantita;
        }
    }

    // lista corrente dei farmaci della terapia (quelli che vedi come "pilloline")
    private final ObservableList<FarmacoInTerapia> selectedFarmaci =
            FXCollections.observableArrayList();

    // id dei TerapiaFarmaco che l’utente ha rimosso con la X
    private final List<Integer> farmaciDaCancellare = new ArrayList<>();

    // ────────────────────── INIT ──────────────────────

    @FXML
    private void initialize() {
        errorLabel.setVisible(false);
        errorLabel.setManaged(false);

        // collega lista → dati
        farmacoListView.setItems(allFarmaci);
        farmacoListView.setCellFactory(lv -> createFarmacoCardCell());

        // blocco lo scroll verticale sui chip (solo orizzontale)
        selectedFarmaciScroll.vvalueProperty().addListener((obs, oldV, newV) -> {
            if (newV.doubleValue() != 0.0) {
                selectedFarmaciScroll.setVvalue(0.0);
            }
        });

        // filtro live
        searchFarmacoField.textProperty().addListener((obs, oldV, newV) -> filterFarmaciList(newV));

        // click singolo sulla card → dialog dosi
        farmacoListView.setOnMouseClicked(event -> {
            if (event.getClickCount() == 1) {
                String nome = farmacoListView.getSelectionModel().getSelectedItem();
                if (nome != null) {
                    onFarmacoSelected(nome);
                }
            }
        });

        // quando cambia la lista dei selezionati, ridisegno le pilloline
        selectedFarmaci.addListener(
                (javafx.collections.ListChangeListener<FarmacoInTerapia>) c ->
                        refreshSelectedFarmaciChips()
        );

        // carico tutti i farmaci dal DB (per la lista di ricerca)
        loadFarmaciFromDb();
    }

    /** Chiamato da PatientTherapyController quando apre il popup. */
    public void init(Terapia terapia, Runnable onUpdatedCallback) {
        this.terapiaOriginale = terapia;
        this.onUpdatedCallback = onUpdatedCallback;

        dataInizioPicker.setValue(terapia.getDataInizio());
        dataFinePicker.setValue(terapia.getDataFine());

        loadFarmaciDellaTerapia(terapia.getId());
    }

    // ────────────────────── HANDLE SAVE / CANCEL ──────────────────────

    @FXML
    private void handleSave() {
        errorLabel.setVisible(false);
        errorLabel.setManaged(false);

        LocalDate dataInizio = dataInizioPicker.getValue();
        LocalDate dataFine   = dataFinePicker.getValue();

        if (dataInizio == null) {
            showError("Seleziona una data di inizio.");
            return;
        }
        if (dataFine != null && dataFine.isBefore(dataInizio)) {
            showError("La data di fine non può essere precedente alla data di inizio.");
            return;
        }

        String nomeTerapia = nomeTerapiaField.getText().trim();
        if (nomeTerapia.isEmpty()) {
            showError("Inserisci il nome della terapia.");
            return;
        }

        if (selectedFarmaci.isEmpty()) {
            showError("Aggiungi almeno un farmaco alla terapia.");
            return;
        }

        try (Connection conn = Database.getConnection()) {
            conn.setAutoCommit(false);

            // 1️⃣ aggiorno la terapia (nome + date)
            updateTerapia(conn, terapiaOriginale.getId(), dataInizio, dataFine);

            // 2️⃣ per ogni farmaco visibile nelle pilloline:
            //     - se esiste già (fkTerapia != null) → UPDATE
            //     - se nuovo → INSERT
            for (FarmacoInTerapia fit : selectedFarmaci) {
                if (fit.fkTerapia != null) {
                    updateTerapiaFarmaco(conn,
                            fit.fkTerapia,
                            fit.idFarmaco,
                            fit.assunzioniGiornaliere,
                            fit.quantita);
                } else {
                    insertTerapiaFarmaco(conn,
                            terapiaOriginale.getId(),
                            fit.fkTerapia,
                            fit.idFarmaco,
                            fit.assunzioniGiornaliere,
                            fit.quantita);
                }
            }

            // 3️⃣ elimino SOLO i farmaci che hai tolto con la X
            for (Integer farmaco : farmaciDaCancellare) {
                deleteTerapiaFarmacoForTerapia(conn, farmaco, terapiaOriginale.getId());
            }

            conn.commit();

            if (onUpdatedCallback != null) {
                onUpdatedCallback.run();   // ricarica lista terapie nella pagina
            }

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

    // ────────────────────── LISTA FARMACI (DB + FILTRO) ──────────────────────

    private void loadFarmaciFromDb() {
        allFarmaci.clear();
        String sql = "SELECT nome FROM Farmaco ORDER BY nome";

        try (Connection conn = Database.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                allFarmaci.add(rs.getString("nome"));
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
        ObservableList<String> filtered =
                allFarmaci.filtered(name -> name.toLowerCase().contains(lower));
        farmacoListView.setItems(filtered);
    }

    private ListCell<String> createFarmacoCardCell() {
        return new ListCell<>() {

            private final VBox root = new VBox();
            private final Label nameLbl = new Label();

            {
                // margine tra una card e l'altra
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

                // hover
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

    // ────────────────────── FARMACI DELLA TERAPIA (LOAD + PILL) ──────────────────────

    /** Carica i farmaci già presenti nella terapia e li mette in selectedFarmaci. */
    private void loadFarmaciDellaTerapia(int idTerapia) {
        selectedFarmaci.clear();
        farmaciDaCancellare.clear(); // reset

        String sql = """
                SELECT tf.fkFarmaco,
                       f.nome,
                       tf.assunzioniGiornaliere,
                       tf.quantita
                FROM FarmacoTerapia tf
                JOIN Farmaco f ON tf.fkFarmaco = f.id
                WHERE tf.fkTerapia = ?
                ORDER BY tf.fkTerapia;
                """;

        try (Connection conn = Database.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, idTerapia);

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    int fkTerapia = rs.getInt("fkTerapia");
                    int fKFarm = rs.getInt("fkFarmaco");
                    String nome  = rs.getString("nome");
                    int ass      = rs.getInt("assunzioniGiornaliere");
                    int quant    = rs.getInt("quantita");

                    selectedFarmaci.add(
                            new FarmacoInTerapia(fkTerapia, fKFarm, nome, ass, quant)
                    );
                }
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    /** Aggiunge/modifica un farmaco dalla lista di ricerca. */
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
                return;
            }

            int assunzioni = ctrl.getAssunzioni();
            int quantita   = ctrl.getUnita();

            try (Connection conn = Database.getConnection()) {
                int idFarmaco = ensureFarmaco(conn, nomeFarmaco);

                // se già presente, lo sostituisco ma mantengo l'eventuale fkTerapia
                FarmacoInTerapia esistente = null;
                for (FarmacoInTerapia f : selectedFarmaci) {
                    if (f.idFarmaco == idFarmaco) {
                        esistente = f;
                        break;
                    }
                }
                Integer idTF = (esistente != null) ? esistente.fkTerapia : null;
                if (esistente != null) {
                    selectedFarmaci.remove(esistente);
                }

                selectedFarmaci.add(
                        new FarmacoInTerapia(idTF, idFarmaco, nomeFarmaco, assunzioni, quantita)
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
                    fit.nome + " (" + fit.assunzioniGiornaliere + "x, " + fit.quantita + " mg)"
            );
            nameLabel.getStyleClass().add("chip-label");

            Button removeBtn = new Button("x");
            removeBtn.getStyleClass().add("btn-ghost");
            removeBtn.setOnAction(e -> {
                // se era un farmaco già esistente in DB, segno che va cancellato
                if (fit.fkTerapia != null) {
                    farmaciDaCancellare.add(fit.fkTerapia);
                }
                selectedFarmaci.remove(fit);
            });

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
                SELECT id
                FROM Farmaco
                WHERE nome = ?
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
                INSERT INTO Farmaco (nome, marca, dosaggio)
                VALUES (?, ?, ?)
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

    private void updateTerapia(Connection conn,
                               int idTerapia,
                               LocalDate dataInizio,
                               LocalDate dataFine) throws SQLException {

        String sql = """
            UPDATE Terapia
            SET dataInizio = ?,
                dataFine = ?
            WHERE id = ?
            """;

        try (PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setDate(1, Date.valueOf(dataInizio));

            if (dataFine != null)
                ps.setDate(2, Date.valueOf(dataFine));
            else
                ps.setNull(2, Types.DATE);

            ps.setInt(3, idTerapia);

            ps.executeUpdate();
        }
    }

    /** Aggiorna dosaggio/frequenza di un record già esistente in TerapiaFarmaco. */
    private void updateTerapiaFarmaco(Connection conn,
                                      int fkTerapia,
                                      int fkFarmaco,
                                      int assunzioniGiornaliere,
                                      int quantita) throws SQLException {

        String sql = """
                UPDATE FarmacoTerapia
                SET assunzioniGiornaliere = ?,
                    quantita = ?
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

    /**
     * Elimina un singolo record di TerapiaFarmaco e TUTTE le assunzioni
     * collegate solo a quel farmaco.
     */
    private void deleteTerapiaFarmacoForTerapia(Connection conn,
                                                int fkTerapia, int fkFarmaco) throws SQLException {

        // 1️⃣ prima elimino tutte le assunzioni collegate
        String sqlAss = "DELETE FROM Assunzione WHERE fkTerapia = ? AND fkFarmaco = ?";
        try (PreparedStatement ps = conn.prepareStatement(sqlAss)) {
            ps.setInt(1, fkTerapia);
            ps.setInt(2, fkFarmaco);
            ps.executeUpdate();
        }

        // 2️⃣ poi elimino il record di TerapiaFarmaco
        String sqlTf = "DELETE FROM FarmacoTerapia WHERE fkTerapia = ? AND fkFarmaco = ?";
        try (PreparedStatement ps = conn.prepareStatement(sqlTf)) {
            ps.setInt(1, fkTerapia);
            ps.setInt(2, fkFarmaco);
            ps.executeUpdate();
        }
    }

    private void insertTerapiaFarmaco(Connection conn,
                                      int fkTerapia,
                                      int fkFarmaco,
                                      int assunzioniGiornaliere,
                                      int quantita,
                                      int fkVersioneTerapia) throws SQLException {

        String sql = """
                INSERT INTO FarmacoTerapia
                (fkTerapia, fkFarmaco, assunzioniGiornaliere, quantita, fkVersioneTerapia)
                VALUES (?, ?, ?, ?, ?)
                """;

        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, fkTerapia);
            ps.setInt(2, fkFarmaco);
            ps.setInt(3, assunzioniGiornaliere);
            ps.setInt(4, quantita);
            ps.setInt(5, fkVersioneTerapia);
            ps.executeUpdate();
        }
    }
}