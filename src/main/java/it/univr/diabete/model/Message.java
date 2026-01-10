package it.univr.diabete.model;

import java.time.LocalDateTime;

public class Message {

    private int id;
    private String fkPatient;
    private String fkDiabetologist;
    private String senderRole;
    private String content;
    private LocalDateTime sentAt;
    private boolean read;
    private LocalDateTime readAt;

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getFkPatient() {
        return fkPatient;
    }

    public void setFkPatient(String fkPatient) {
        this.fkPatient = fkPatient;
    }

    public String getFkDiabetologist() {
        return fkDiabetologist;
    }

    public void setFkDiabetologist(String fkDiabetologist) {
        this.fkDiabetologist = fkDiabetologist;
    }

    public String getSenderRole() {
        return senderRole;
    }

    public void setSenderRole(String senderRole) {
        this.senderRole = senderRole;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public LocalDateTime getSentAt() {
        return sentAt;
    }

    public void setSentAt(LocalDateTime sentAt) {
        this.sentAt = sentAt;
    }

    public boolean isRead() {
        return read;
    }

    public void setRead(boolean read) {
        this.read = read;
    }

    public LocalDateTime getReadAt() {
        return readAt;
    }

    public void setReadAt(LocalDateTime readAt) {
        this.readAt = readAt;
    }
}
