package it.univr.diabete.dao;

import it.univr.diabete.model.Message;
import java.util.List;
import java.util.Map;

public interface MessageDAO {
    List<Message> getConversation(String patientId, String diabetologistId) throws Exception;

    Message getLastMessage(String patientId, String diabetologistId) throws Exception;

    void sendMessage(Message message) throws Exception;

    int countUnreadForDiabetologist(String diabetologistId) throws Exception;

    Map<String, Integer> getUnreadByPatient(String diabetologistId) throws Exception;

    void markAsRead(String patientId, String diabetologistId, String recipientRole) throws Exception;
}
