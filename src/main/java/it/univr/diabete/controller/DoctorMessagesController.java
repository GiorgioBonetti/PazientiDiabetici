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

    private boolean autoOpenedOnce = false;

    /**
     * Quando apriamo una conversazione automaticamente, vogliamo mantenere il badge/unread nella colonna
     * finché l'utente non cambia conversazione (o esce dalla pagina). Questo flag tiene traccia del fatto
     * che la conversazione corrente ha un "unread UI" da pulire al prossimo cambio.
     */
    private boolean pendingClearUnreadOnConversationChange = false;

    /**
     * In UI, dopo l'auto-open vogliamo mantenere il badge finché l'utente non fa una delle azioni che
     * consideriamo "ho visto":
     * - cambia conversazione
     * - riclicca la conversazione corrente a sinistra
     * - invia un messaggio di risposta
     */
    private void clearPendingUnreadBadgeIfAny() {
        if (selectedPatient == null || !pendingClearUnreadOnConversationChange) {
            return;
        }

        String prevId = selectedPatient.getCodiceFiscale();
        for (ConversationItem ci : conversations) {
            if (ci.patient != null && prevId.equals(ci.patient.getCodiceFiscale())) {
                ci.unreadCount = 0;
                break;
            }
        }
        pendingClearUnreadOnConversationChange = false;
        conversationsListView.refresh();
    }

    public void setDoctorContext(String doctorId) {
        this.diabetologistId = doctorId;
        loadConversations();
    }

    @FXML
    private void initialize() {
        conversationsListView.setItems(conversations);
        conversationsListView.setCellFactory(lv -> createConversationCell());

        // Click sulla conversazione corrente: non cambia la selection, quindi il listener non scatta.
        // In quel caso, se avevamo un badge "in sospeso" dall'auto-open, lo puliamo.
        conversationsListView.setOnMouseClicked(evt -> {
            ConversationItem selected = conversationsListView.getSelectionModel().getSelectedItem();
            if (selected != null && selectedPatient != null
                    && selected.patient != null
                    && selectedPatient.getCodiceFiscale().equals(selected.patient.getCodiceFiscale())) {
                clearPendingUnreadBadgeIfAny();
            }
        });

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

            // Ordina: prima le conversazioni con messaggi, in ordine di ultimo messaggio decrescente;
            // poi quelle senza messaggi in fondo. Tie-breaker stabile per non "saltare" tra refresh.
            items.sort((a, b) -> {
                boolean aHas = a.lastMessage != null && a.lastMessage.getSentAt() != null;
                boolean bHas = b.lastMessage != null && b.lastMessage.getSentAt() != null;

                if (aHas && bHas) {
                    int cmp = b.lastMessage.getSentAt().compareTo(a.lastMessage.getSentAt());
                    if (cmp != 0) return cmp;
                } else if (aHas) {
                    return -1;
                } else if (bHas) {
                    return 1;
                }

                // entrambi senza timestamp: ordina per CF per stabilità
                String aId = a.patient != null ? a.patient.getCodiceFiscale() : "";
                String bId = b.patient != null ? b.patient.getCodiceFiscale() : "";
                return aId.compareToIgnoreCase(bId);
            });

            String selectedId = selectedPatient != null ? selectedPatient.getCodiceFiscale() : null;
            conversations.setAll(items);

            // Se l'utente aveva già selezionato un paziente, mantieni la selezione.
            if (selectedId != null) {
                for (ConversationItem item : items) {
                    if (selectedId.equals(item.patient.getCodiceFiscale())) {
                        conversationsListView.getSelectionModel().select(item);
                        return;
                    }
                }
            }

            // Al primo caricamento, apri automaticamente la conversazione più recente CON messaggi.
            // Se non ce ne sono, apri la prima (che sarà una vuota).
            if (!autoOpenedOnce && !items.isEmpty()) {
                autoOpenedOnce = true;

                int indexToSelect = -1;
                for (int i = 0; i < items.size(); i++) {
                    if (items.get(i).lastMessage != null) {
                        indexToSelect = i;
                        break;
                    }
                }
                if (indexToSelect < 0) {
                    indexToSelect = 0;
                }

                // Segnala che la prima conversazione viene aperta automaticamente:
                // manteniamo il badge finché l'utente non cambia conversazione o esce dalla pagina.
                pendingClearUnreadOnConversationChange = true;
                conversationsListView.getSelectionModel().select(indexToSelect);
            }

        } catch (Exception e) {
            ErrorDialog.show("Errore caricamento conversazioni",
                    "Impossibile caricare la lista dei pazienti.");
            e.printStackTrace();
        }
    }

    private void openConversation(ConversationItem item) {
        // Se l'utente sta cambiando conversazione, ora possiamo rimuovere il badge della chat precedente
        // (se era stata aperta automaticamente e avevamo deciso di mantenerlo visibile).
        if (selectedPatient != null && pendingClearUnreadOnConversationChange) {
            String prevId = selectedPatient.getCodiceFiscale();
            String newId = item.patient != null ? item.patient.getCodiceFiscale() : null;

            // Pulisci solo se stiamo davvero andando su un'altra conversazione.
            if (newId != null && !prevId.equals(newId)) {
                for (ConversationItem ci : conversations) {
                    if (ci.patient != null && prevId.equals(ci.patient.getCodiceFiscale())) {
                        ci.unreadCount = 0;
                        break;
                    }
                }
                pendingClearUnreadOnConversationChange = false;
            }
        }

        selectedPatient = item.patient;
        chatTitleLabel.setText(selectedPatient.getNome() + " " + selectedPatient.getCognome());
        chatSubtitleLabel.setText(selectedPatient.getEmail() != null ? selectedPatient.getEmail() : "—");
        loadMessages();
        markAsRead();

        // Qui NON azzeriamo più subito item.unreadCount.
        // Lo azzeriamo solo se la chat è stata aperta manualmente (click utente).
        // Se è stata aperta automaticamente, lo lasciamo visibile finché l'utente non cambia conversazione.
        if (!pendingClearUnreadOnConversationChange) {
            item.unreadCount = 0;
        }

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
        // Se l'utente risponde, consideriamo la notifica "vista" e puliamo il badge UI (solo UI, DB invariato).
        clearPendingUnreadBadgeIfAny();

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
            loadConversations();

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

    private static final int CONVERSATION_PREVIEW_MAX_CHARS = 30;

    private String buildConversationPreviewText(Message lastMessage) {
        if (lastMessage == null || lastMessage.getContent() == null) {
            return "Nessun messaggio";
        }

        String content = lastMessage.getContent().trim().replaceAll("\\s+", " ");
        if (content.isEmpty()) {
            return "(messaggio vuoto)";
        }

        if (content.length() <= CONVERSATION_PREVIEW_MAX_CHARS) {
            return content;
        }

        return content.substring(0, CONVERSATION_PREVIEW_MAX_CHARS) + "...";
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

                // Wrapper che viene stilizzato dal CSS (app.css: .message-thread)
                HBox root = new HBox(10);
                root.getStyleClass().add("message-thread");

                VBox textBox = new VBox(3);

                // Riga 1: Nome paziente + timestamp (allineati sulla stessa baseline)
                HBox topRow = new HBox(8);
                topRow.setAlignment(Pos.BASELINE_LEFT);

                Label nameLabel = new Label(item.patient.getNome() + " " + item.patient.getCognome());
                nameLabel.getStyleClass().add("thread-title");
                nameLabel.setMaxWidth(Double.MAX_VALUE);
                nameLabel.setTextOverrun(OverrunStyle.ELLIPSIS);
                HBox.setHgrow(nameLabel, Priority.ALWAYS);

                Label timeLabel = new Label();
                timeLabel.getStyleClass().add("thread-preview");
                if (item.lastMessage != null && item.lastMessage.getSentAt() != null) {
                    LocalDate msgDate = item.lastMessage.getSentAt().toLocalDate();
                    LocalDate today = LocalDate.now();
                    timeLabel.setText(msgDate.equals(today)
                            ? formatTime(item.lastMessage.getSentAt())
                            : dateFormatter.format(msgDate));
                } else {
                    timeLabel.setText("");
                }

                topRow.getChildren().addAll(nameLabel, timeLabel);

                // Riga 2: preview ultimo messaggio
                Label lastMsgLabel = new Label();
                lastMsgLabel.getStyleClass().add("thread-preview");

                String preview = buildConversationPreviewText(item.lastMessage);
                lastMsgLabel.setText(preview);

                // Evita che il testo "spinga" fuori badge/orario: una riga, ellissi, larghezza limitata allo spazio disponibile
                lastMsgLabel.setWrapText(false);
                lastMsgLabel.setMinWidth(0);
                lastMsgLabel.setMaxWidth(Double.MAX_VALUE);
                lastMsgLabel.setTextOverrun(OverrunStyle.ELLIPSIS);

                textBox.getChildren().addAll(topRow, lastMsgLabel);
                HBox.setHgrow(textBox, Priority.ALWAYS);

                VBox sideBox = new VBox(4);
                sideBox.setAlignment(Pos.CENTER_RIGHT);

                if (item.unreadCount > 0) {
                    Label unreadLabel = new Label(String.valueOf(item.unreadCount));
                    unreadLabel.getStyleClass().add("unread-badge");
                    sideBox.getChildren().add(unreadLabel);
                }

                root.getChildren().addAll(textBox, sideBox);
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
