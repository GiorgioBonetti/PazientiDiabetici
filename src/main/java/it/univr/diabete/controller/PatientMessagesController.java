package it.univr.diabete.controller;

import it.univr.diabete.dao.DiabetologoDAO;
import it.univr.diabete.dao.MessageDAO;
import it.univr.diabete.dao.PazienteDAO;
import it.univr.diabete.dao.impl.DiabetologoDAOImpl;
import it.univr.diabete.dao.impl.MessageDAOImpl;
import it.univr.diabete.dao.impl.PazienteDAOImpl;
import it.univr.diabete.model.Diabetologo;
import it.univr.diabete.model.Message;
import it.univr.diabete.model.Paziente;
import it.univr.diabete.ui.ErrorDialog;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
import javafx.scene.control.TextField;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.List;

public class PatientMessagesController {

    @FXML private Label diabetologistLabel;
    @FXML private Label infoLabel;
    @FXML private ListView<Message> messagesListView;
    @FXML private TextField messageInput;
    @FXML private Button sendButton;

    private final PazienteDAO pazienteDAO = new PazienteDAOImpl();
    private final DiabetologoDAO diabetologoDAO = new DiabetologoDAOImpl();
    private final MessageDAO messageDAO = new MessageDAOImpl();

    private String patientId;
    private String diabetologistId;

    private final ObservableList<Message> messages = FXCollections.observableArrayList();

    private final DateTimeFormatter timeFormatter = DateTimeFormatter.ofPattern("HH:mm");
    private final DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("dd/MM/yyyy");

    public void setPatientContext(String patientId) {
        this.patientId = patientId;
        loadAssignedDiabetologist();
        loadMessages();
    }

    @FXML
    private void initialize() {
        messagesListView.setItems(messages);
        messagesListView.setCellFactory(lv -> createMessageCell("PAZIENTE"));
        sendButton.setDefaultButton(true);
    }

    private void loadAssignedDiabetologist() {
        try {
            Paziente paziente = pazienteDAO.findById(patientId);

            if (paziente == null || paziente.getFkDiabetologo() == null) {
                infoLabel.setText("Nessun diabetologo assegnato.");
                infoLabel.setVisible(true);
                infoLabel.setManaged(true);
                messageInput.setDisable(true);
                sendButton.setDisable(true);
                return;
            }

            diabetologistId = paziente.getFkDiabetologo();
            infoLabel.setVisible(false);
            infoLabel.setManaged(false);
            messageInput.setDisable(false);
            sendButton.setDisable(false);

            Diabetologo d = diabetologoDAO.findByEmail(diabetologistId);
            if (d != null) {
                diabetologistLabel.setText(d.getNome() + " " + d.getCognome());
            } else {
                diabetologistLabel.setText(diabetologistId);
            }

        } catch (Exception e) {
            ErrorDialog.show("Errore caricamento diabetologo",
                    "Impossibile caricare i dati del diabetologo assegnato.");
            e.printStackTrace();
        }
    }

    private void loadMessages() {
        if (patientId == null || diabetologistId == null) {
            return;
        }

        try {
            List<Message> list = messageDAO.getConversation(patientId, diabetologistId);
            messages.setAll(list);
            messageDAO.markAsRead(patientId, diabetologistId, "PAZIENTE");
            scrollToBottom();

        } catch (Exception e) {
            ErrorDialog.show("Errore caricamento messaggi",
                    "Impossibile caricare la conversazione.");
            e.printStackTrace();
        }
    }

    @FXML
    private void handleSend() {
        if (patientId == null || diabetologistId == null) {
            ErrorDialog.show("Errore", "Paziente o diabetologo non validi.");
            return;
        }

        String text = messageInput.getText() != null ? messageInput.getText().trim() : "";
        if (text.isEmpty()) {
            ErrorDialog.show("Messaggio vuoto", "Inserisci un messaggio prima di inviare.");
            return;
        }

        try {
            Message m = new Message();
            m.setFkPatient(patientId);
            m.setFkDiabetologist(diabetologistId);
            m.setSenderRole("PAZIENTE");
            m.setContent(text);
            m.setSentAt(LocalDateTime.now());

            messageDAO.sendMessage(m);
            messageInput.clear();
            loadMessages();

        } catch (Exception e) {
            ErrorDialog.show("Errore di invio",
                    "Impossibile inviare il messaggio. Riprova.");
            e.printStackTrace();
        }
    }

    private void scrollToBottom() {
        Platform.runLater(() -> {
            if (!messages.isEmpty()) {
                messagesListView.scrollTo(messages.size() - 1);
            }
        });
    }

    private ListCell<Message> createMessageCell(String currentRole) {
        return new ListCell<>() {
            @Override
            protected void updateItem(Message msg, boolean empty) {
                super.updateItem(msg, empty);

                if (empty || msg == null) {
                    setGraphic(null);
                    setText(null);
                    return;
                }

                boolean isMe = currentRole.equalsIgnoreCase(msg.getSenderRole());

                VBox wrapper = new VBox(6);

                int idx = getIndex();
                if (shouldShowDateHeader(idx, msg)) {
                    Label dateLabel = new Label(formatDateLabel(msg.getSentAt()));
                    dateLabel.getStyleClass().add("message-date");
                    HBox dateRow = new HBox(dateLabel);
                    dateRow.setAlignment(Pos.CENTER);
                    wrapper.getChildren().add(dateRow);
                }

                HBox row = new HBox();
                row.getStyleClass().add("message-row");
                row.setAlignment(isMe ? Pos.CENTER_RIGHT : Pos.CENTER_LEFT);

                VBox bubble = new VBox(2);
                bubble.getStyleClass().add("message-bubble");
                if (isMe) {
                    bubble.getStyleClass().add("message-bubble-me");
                }

                Label content = new Label(msg.getContent());
                content.getStyleClass().add("message-text");
                if (isMe) {
                    content.getStyleClass().add("message-text-me");
                }
                content.setWrapText(true);

                HBox meta = new HBox(6);
                Label time = new Label(formatTime(msg.getSentAt()));
                time.getStyleClass().add("message-time");
                if (isMe) {
                    time.getStyleClass().add("message-time-me");
                }

                meta.getChildren().add(time);

                if (isMe && msg.isRead()) {
                    Label read = new Label("Letto");
                    read.getStyleClass().add("message-read");
                    meta.getChildren().add(read);
                }

                bubble.getChildren().addAll(content, meta);
                row.getChildren().add(bubble);
                HBox.setMargin(bubble, new Insets(2, 8, 2, 8));

                wrapper.getChildren().add(row);
                setGraphic(wrapper);
                setText(null);
            }
        };
    }

    private String formatTime(LocalDateTime time) {
        if (time == null) {
            return "--:--";
        }
        return timeFormatter.format(time);
    }

    private boolean shouldShowDateHeader(int index, Message msg) {
        if (index <= 0) {
            return true;
        }

        List<Message> items = getMessages();
        if (index >= items.size()) {
            return false;
        }

        Message prev = items.get(index - 1);
        if (prev == null || prev.getSentAt() == null || msg.getSentAt() == null) {
            return true;
        }

        LocalDate prevDate = prev.getSentAt().toLocalDate();
        LocalDate curDate = msg.getSentAt().toLocalDate();
        return !prevDate.equals(curDate);
    }

    private List<Message> getMessages() {
        return messagesListView != null ? messagesListView.getItems() : messages;
    }

    private String formatDateLabel(LocalDateTime sentAt) {
        if (sentAt == null) {
            return "";
        }

        LocalDate date = sentAt.toLocalDate();
        LocalDate today = LocalDate.now();
        long diff = ChronoUnit.DAYS.between(date, today);

        if (diff == 0) {
            return "Oggi";
        }
        if (diff == 1) {
            return "Ieri";
        }
        return dateFormatter.format(date);
    }
}
