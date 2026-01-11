package it.univr.diabete.controller;

import it.univr.diabete.dao.MessageDAO;
import it.univr.diabete.dao.PazienteDAO;
import it.univr.diabete.dao.impl.MessageDAOImpl;
import it.univr.diabete.dao.impl.PazienteDAOImpl;
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
import javafx.scene.control.OverrunStyle;
import javafx.scene.control.TextField;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class DoctorMessagesController {

    @FXML private ListView<ConversationItem> conversationsListView;
    @FXML private ListView<Message> messagesListView;
    @FXML private Label chatTitleLabel;
    @FXML private Label chatSubtitleLabel;
    @FXML private TextField messageInput;
    @FXML private Button sendButton;

    private final PazienteDAO pazienteDAO = new PazienteDAOImpl();
    private final MessageDAO messageDAO = new MessageDAOImpl();

    private String diabetologistId;
    private Paziente selectedPatient;

    private final ObservableList<ConversationItem> conversations = FXCollections.observableArrayList();
    private final ObservableList<Message> messages = FXCollections.observableArrayList();

    private final DateTimeFormatter timeFormatter = DateTimeFormatter.ofPattern("HH:mm");
    private final DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("dd/MM/yyyy");

    public void setDoctorContext(String doctorId) {
        this.diabetologistId = doctorId;
        loadConversations();
    }

    @FXML
    private void initialize() {
        conversationsListView.setItems(conversations);
        conversationsListView.setCellFactory(lv -> createConversationCell());
        conversationsListView.getSelectionModel().selectedItemProperty().addListener((obs, oldV, newV) -> {
            if (newV != null) {
                openConversation(newV);
            }
        });

        messagesListView.setItems(messages);
        messagesListView.setCellFactory(lv -> createMessageCell("DIABETOLOGO"));
        sendButton.setDefaultButton(true);
    }

    private void loadConversations() {
        if (diabetologistId == null) {
            return;
        }

        try {
            List<Paziente> all = pazienteDAO.findAll();
            Map<String, Integer> unreadMap = messageDAO.getUnreadByPatient(diabetologistId);

            List<ConversationItem> items = new ArrayList<>();

            for (Paziente p : all) {
                if (p.getFkDiabetologo() == null) {
                    continue;
                }

                if (!diabetologistId.equalsIgnoreCase(p.getFkDiabetologo())) {
                    continue;
                }

                Message last = messageDAO.getLastMessage(p.getCodiceFiscale(), diabetologistId);
                int unread = unreadMap.getOrDefault(p.getCodiceFiscale(), 0);

                items.add(new ConversationItem(p, last, unread));
            }

            String selectedId = selectedPatient != null ? selectedPatient.getCodiceFiscale() : null;
            conversations.setAll(items);

            if (selectedId != null) {
                for (ConversationItem item : items) {
                    if (selectedId.equals(item.patient.getCodiceFiscale())) {
                        conversationsListView.getSelectionModel().select(item);
                        break;
                    }
                }
            }

        } catch (Exception e) {
            ErrorDialog.show("Errore caricamento conversazioni",
                    "Impossibile caricare la lista dei pazienti.");
            e.printStackTrace();
        }
    }

    private void openConversation(ConversationItem item) {
        selectedPatient = item.patient;
        chatTitleLabel.setText(selectedPatient.getNome() + " " + selectedPatient.getCognome());
        chatSubtitleLabel.setText(selectedPatient.getEmail() != null ? selectedPatient.getEmail() : "—");
        loadMessages();
        markAsRead();
        item.unreadCount = 0;
        conversationsListView.refresh();
    }

    private void loadMessages() {
        if (selectedPatient == null) {
            return;
        }

        try {
            List<Message> list = messageDAO.getConversation(
                    selectedPatient.getCodiceFiscale(),
                    diabetologistId
            );
            messages.setAll(list);
            scrollToBottom();

        } catch (Exception e) {
            ErrorDialog.show("Errore caricamento messaggi",
                    "Impossibile caricare la conversazione.");
            e.printStackTrace();
        }
    }

    private void markAsRead() {
        if (selectedPatient == null) {
            return;
        }

        try {
            messageDAO.markAsRead(selectedPatient.getCodiceFiscale(), diabetologistId, "DIABETOLOGO");
        } catch (Exception e) {
            ErrorDialog.show("Errore marcatura messaggi",
                    "Impossibile marcare i messaggi come letti.");
            e.printStackTrace();
        }
    }

    @FXML
    private void handleSend() {
        if (selectedPatient == null) {
            ErrorDialog.show("Nessun paziente selezionato",
                    "Seleziona un paziente prima di inviare un messaggio.");
            return;
        }

        String text = messageInput.getText() != null ? messageInput.getText().trim() : "";
        if (text.isEmpty()) {
            ErrorDialog.show("Messaggio vuoto", "Inserisci un messaggio prima di inviare.");
            return;
        }

        try {
            Message m = new Message();
            m.setFkPatient(selectedPatient.getCodiceFiscale());
            m.setFkDiabetologist(diabetologistId);
            m.setSenderRole("DIABETOLOGO");
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

    private ListCell<ConversationItem> createConversationCell() {
        return new ListCell<>() {
            @Override
            protected void updateItem(ConversationItem item, boolean empty) {
                super.updateItem(item, empty);

                if (empty || item == null) {
                    setGraphic(null);
                    setText(null);
                    return;
                }

                HBox root = new HBox(12);
                root.setPadding(new Insets(8));
                root.setStyle("-fx-background-color: white; -fx-border-color: #e5e7eb; -fx-border-width: 0 0 1 0;");

                VBox textBox = new VBox(2);

                Label nameLabel = new Label(item.patient.getNome() + " " + item.patient.getCognome());
                nameLabel.setStyle("-fx-font-weight: bold; -fx-font-size: 13px;");

                Label lastMsgLabel = new Label();
                if (item.lastMessage != null) {
                    String preview = item.lastMessage.getContent().length() > 40
                            ? item.lastMessage.getContent().substring(0, 40) + "..."
                            : item.lastMessage.getContent();
                    lastMsgLabel.setText(preview);
                } else {
                    lastMsgLabel.setText("Nessun messaggio");
                }
                lastMsgLabel.setStyle("-fx-font-size: 12px; -fx-text-fill: #666;");
                lastMsgLabel.setWrapText(true);

                textBox.getChildren().addAll(nameLabel, lastMsgLabel);

                Region spacer = new Region();
                HBox.setHgrow(spacer, Priority.ALWAYS);

                VBox sideBox = new VBox(4);
                sideBox.setAlignment(Pos.TOP_RIGHT);

                if (item.unreadCount > 0) {
                    Label unreadLabel = new Label(String.valueOf(item.unreadCount));
                    unreadLabel.setStyle(
                            "-fx-background-color: #ef4444; " +
                                    "-fx-text-fill: white; " +
                                    "-fx-font-weight: bold; " +
                                    "-fx-padding: 4 8; " +
                                    "-fx-background-radius: 12;"
                    );
                    sideBox.getChildren().add(unreadLabel);
                }

                root.getChildren().addAll(textBox, spacer, sideBox);
                setGraphic(root);
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

    // Inner class per item conversazione
    public static class ConversationItem {
        public Paziente patient;
        public Message lastMessage;
        public int unreadCount;

        public ConversationItem(Paziente patient, Message lastMessage, int unreadCount) {
            this.patient = patient;
            this.lastMessage = lastMessage;
            this.unreadCount = unreadCount;
        }
    }
}
