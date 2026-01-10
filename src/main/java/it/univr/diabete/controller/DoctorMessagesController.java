package it.univr.diabete.controller;

import it.univr.diabete.dao.MessageDAO;
import it.univr.diabete.dao.PazienteDAO;
import it.univr.diabete.dao.impl.MessageDAOImpl;
import it.univr.diabete.dao.impl.PazienteDAOImpl;
import it.univr.diabete.model.Message;
import it.univr.diabete.model.Paziente;
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
import javafx.scene.control.OverrunStyle;
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
            e.printStackTrace();
        }
    }

    @FXML
    private void handleSend() {
        if (selectedPatient == null) {
            return;
        }
        String text = messageInput.getText() != null ? messageInput.getText().trim() : "";
        if (text.isEmpty()) {
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
            loadConversations();
        } catch (Exception e) {
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

                VBox textBox = new VBox(2);
                Label name = new Label(item.patient.getNome() + " " + item.patient.getCognome());
                name.getStyleClass().add("thread-title");

                String preview = item.lastMessage != null ? item.lastMessage.getContent() : "Nessun messaggio";
                Label last = new Label(preview);
                last.getStyleClass().add("thread-preview");
                last.setMaxWidth(220);
                last.setTextOverrun(OverrunStyle.ELLIPSIS);

                textBox.getChildren().addAll(name, last);

                HBox root = new HBox(8);
                root.getStyleClass().add("message-thread");
                root.setAlignment(Pos.CENTER_LEFT);

                Region spacer = new Region();
                HBox.setHgrow(spacer, Priority.ALWAYS);

                root.getChildren().addAll(textBox, spacer);

                if (item.unreadCount > 0) {
                    Label badge = new Label(String.valueOf(item.unreadCount));
                    badge.getStyleClass().add("unread-badge");
                    root.getChildren().add(badge);
                }

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

    private static class ConversationItem {
        private final Paziente patient;
        private final Message lastMessage;
        private int unreadCount;

        private ConversationItem(Paziente patient, Message lastMessage, int unreadCount) {
            this.patient = patient;
            this.lastMessage = lastMessage;
            this.unreadCount = unreadCount;
        }
    }
}
