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
     * idTerapiaFarmaco = id della riga in tabella TerapiaFarmaco (null se nuovo).
     */
    private static class FarmacoInTerapia {
        Integer idTerapiaFarmaco;     // può essere null per farmaci nuovi
        int idFarmaco;
        String nome;
        int assunzioniGiornaliere;
        int quantitaAssunzione;

        FarmacoInTerapia(Integer idTerapiaFarmaco,
                         int idFarmaco,
                         String nome,
                         int assunzioniGiornaliere,
                         int quantitaAssunzione) {
            this.idTerapiaFarmaco = idTerapiaFarmaco;
            this.idFarmaco = idFarmaco;
            this.nome = nome;
            this.assunzioniGiornaliere = assunzioniGiornaliere;
            this.quantitaAssunzione = quantitaAssunzione;
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
            updateTerapia(conn, terapiaOriginale.getId(), nomeTerapia, dataInizio, dataFine);

            // 2️⃣ per ogni farmaco visibile nelle pilloline:
            //     - se esiste già (idTerapiaFarmaco != null) → UPDATE
            //     - se nuovo → INSERT
            for (FarmacoInTerapia fit : selectedFarmaci) {
                if (fit.idTerapiaFarmaco != null) {
                    updateTerapiaFarmaco(conn,
                            fit.idTerapiaFarmaco,
                            fit.assunzioniGiornaliere,
                            fit.quantitaAssunzione);
                } else {
                    insertTerapiaFarmaco(conn,
                            terapiaOriginale.getId(),
                            fit.idFarmaco,
                            fit.assunzioniGiornaliere,
                            fit.quantitaAssunzione);
                }
            }

            // 3️⃣ elimino SOLO i farmaci che hai tolto con la X
            for (Integer idTf : farmaciDaCancellare) {
                deleteTerapiaFarmacoForTerapia(conn, idTf);
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
                SELECT tf.Id       AS IdTF,
                       tf.IdFarmaco,
                       f.Nome,
                       tf.AssunzioniGiornaliere,
                       tf.QuantitaAssunzione
                FROM TerapiaFarmaco tf
                JOIN Farmaco f ON tf.IdFarmaco = f.Id
                WHERE tf.IdTerapia = ?
                ORDER BY tf.Id
                """;

        try (Connection conn = Database.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, idTerapia);

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    int idTF     = rs.getInt("IdTF");
                    int idFarm   = rs.getInt("IdFarmaco");
                    String nome  = rs.getString("Nome");
                    int ass      = rs.getInt("AssunzioniGiornaliere");
                    int quant    = rs.getInt("QuantitaAssunzione");

                    selectedFarmaci.add(
                            new FarmacoInTerapia(idTF, idFarm, nome, ass, quant)
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

                // se già presente, lo sostituisco ma mantengo l'eventuale idTerapiaFarmaco
                FarmacoInTerapia esistente = null;
                for (FarmacoInTerapia f : selectedFarmaci) {
                    if (f.idFarmaco == idFarmaco) {
                        esistente = f;
                        break;
                    }
                }
                Integer idTF = (esistente != null) ? esistente.idTerapiaFarmaco : null;
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
                    fit.nome + " (" + fit.assunzioniGiornaliere + "x, " + fit.quantitaAssunzione + " mg)"
            );
            nameLabel.getStyleClass().add("chip-label");

            Button removeBtn = new Button("x");
            removeBtn.getStyleClass().add("btn-ghost");
            removeBtn.setOnAction(e -> {
                // se era un farmaco già esistente in DB, segno che va cancellato
                if (fit.idTerapiaFarmaco != null) {
                    farmaciDaCancellare.add(fit.idTerapiaFarmaco);
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

    private void updateTerapia(Connection conn,
                               int idTerapia,
                               String nomeTerapia,
                               LocalDate dataInizio,
                               LocalDate dataFine) throws SQLException {

        String sql = """
            UPDATE Terapia
            SET Nome = ?,
                DataInizio = ?,
                DataFine = ?
            WHERE Id = ?
            """;

        try (PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, nomeTerapia);
            ps.setDate(2, Date.valueOf(dataInizio));

            if (dataFine != null)
                ps.setDate(3, Date.valueOf(dataFine));
            else
                ps.setNull(3, Types.DATE);

            ps.setInt(4, idTerapia);

            ps.executeUpdate();
        }
    }

    /** Aggiorna dosaggio/frequenza di un record già esistente in TerapiaFarmaco. */
    private void updateTerapiaFarmaco(Connection conn,
                                      int idTerapiaFarmaco,
                                      int assunzioniGiornaliere,
                                      int quantitaAssunzione) throws SQLException {

        String sql = """
                UPDATE TerapiaFarmaco
                SET AssunzioniGiornaliere = ?,
                    QuantitaAssunzione = ?
                WHERE Id = ?
                """;

        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, assunzioniGiornaliere);
            ps.setInt(2, quantitaAssunzione);
            ps.setInt(3, idTerapiaFarmaco);
            ps.executeUpdate();
        }
    }

    /**
     * Elimina un singolo record di TerapiaFarmaco e TUTTE le assunzioni
     * collegate solo a quel farmaco.
     */
    private void deleteTerapiaFarmacoForTerapia(Connection conn,
                                                int idTerapiaFarmaco) throws SQLException {

        // 1️⃣ prima elimino tutte le assunzioni collegate
        String sqlAss = "DELETE FROM AssunzioneTerapia WHERE IdTerapiaFarmaco = ?";
        try (PreparedStatement ps = conn.prepareStatement(sqlAss)) {
            ps.setInt(1, idTerapiaFarmaco);
            ps.executeUpdate();
        }

        // 2️⃣ poi elimino il record di TerapiaFarmaco
        String sqlTf = "DELETE FROM TerapiaFarmaco WHERE Id = ?";
        try (PreparedStatement ps = conn.prepareStatement(sqlTf)) {
            ps.setInt(1, idTerapiaFarmaco);
            ps.executeUpdate();
        }
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