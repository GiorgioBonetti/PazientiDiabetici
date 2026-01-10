package it.univr.diabete.dao.impl;

import it.univr.diabete.dao.MessageDAO;
import it.univr.diabete.database.Database;
import it.univr.diabete.model.Message;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class MessageDAOImpl implements MessageDAO {

    private Message mapRow(ResultSet rs) throws Exception {
        Message m = new Message();
        m.setId(rs.getInt("id"));
        m.setFkPatient(rs.getString("fkPaziente"));
        m.setFkDiabetologist(rs.getString("fkDiabetologo"));
        m.setSenderRole(rs.getString("mittente"));
        m.setContent(rs.getString("testo"));
        Timestamp sentAt = rs.getTimestamp("timestampInvio");
        if (sentAt != null) {
            m.setSentAt(sentAt.toLocalDateTime());
        }
        m.setRead(rs.getBoolean("letto"));
        Timestamp readAt = rs.getTimestamp("timestampLettura");
        if (readAt != null) {
            m.setReadAt(readAt.toLocalDateTime());
        }
        return m;
    }

    @Override
    public List<Message> getConversation(String patientId, String diabetologistId) throws Exception {
        String sql = """
            SELECT id, fkPaziente, fkDiabetologo, mittente, testo, timestampInvio, letto, timestampLettura
            FROM Messaggi
            WHERE fkPaziente = ? AND fkDiabetologo = ?
            ORDER BY timestampInvio ASC, id ASC
            """;
        List<Message> result = new ArrayList<>();
        try (Connection conn = Database.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, patientId);
            ps.setString(2, diabetologistId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    result.add(mapRow(rs));
                }
            }
        }
        return result;
    }

    @Override
    public Message getLastMessage(String patientId, String diabetologistId) throws Exception {
        String sql = """
            SELECT id, fkPaziente, fkDiabetologo, mittente, testo, timestampInvio, letto, timestampLettura
            FROM Messaggi
            WHERE fkPaziente = ? AND fkDiabetologo = ?
            ORDER BY timestampInvio DESC, id DESC
            LIMIT 1
            """;
        try (Connection conn = Database.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, patientId);
            ps.setString(2, diabetologistId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return mapRow(rs);
                }
            }
        }
        return null;
    }

    @Override
    public void sendMessage(Message message) throws Exception {
        String sql = """
            INSERT INTO Messaggi (fkPaziente, fkDiabetologo, mittente, testo, timestampInvio, letto, timestampLettura)
            VALUES (?, ?, ?, ?, ?, ?, ?)
            """;
        LocalDateTime sentAt = message.getSentAt() != null ? message.getSentAt() : LocalDateTime.now();
        try (Connection conn = Database.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, message.getFkPatient());
            ps.setString(2, message.getFkDiabetologist());
            ps.setString(3, message.getSenderRole());
            ps.setString(4, message.getContent());
            ps.setTimestamp(5, Timestamp.valueOf(sentAt));
            ps.setBoolean(6, false);
            ps.setTimestamp(7, null);
            ps.executeUpdate();
        }
    }

    @Override
    public int countUnreadForDiabetologist(String diabetologistId) throws Exception {
        String sql = """
            SELECT COUNT(*) AS cnt
            FROM Messaggi
            WHERE fkDiabetologo = ? AND mittente = 'PAZIENTE' AND letto = 0
            """;
        try (Connection conn = Database.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, diabetologistId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt("cnt");
                }
            }
        }
        return 0;
    }

    @Override
    public Map<String, Integer> getUnreadByPatient(String diabetologistId) throws Exception {
        String sql = """
            SELECT fkPaziente, COUNT(*) AS cnt
            FROM Messaggi
            WHERE fkDiabetologo = ? AND mittente = 'PAZIENTE' AND letto = 0
            GROUP BY fkPaziente
            """;
        Map<String, Integer> result = new HashMap<>();
        try (Connection conn = Database.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, diabetologistId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    result.put(rs.getString("fkPaziente"), rs.getInt("cnt"));
                }
            }
        }
        return result;
    }

    @Override
    public void markAsRead(String patientId, String diabetologistId, String recipientRole) throws Exception {
        String senderRoleToMark = "PAZIENTE";
        if ("PAZIENTE".equalsIgnoreCase(recipientRole)) {
            senderRoleToMark = "DIABETOLOGO";
        }
        String sql = """
            UPDATE Messaggi
            SET letto = 1, timestampLettura = NOW()
            WHERE fkPaziente = ? AND fkDiabetologo = ? AND mittente = ? AND letto = 0
            """;
        try (Connection conn = Database.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, patientId);
            ps.setString(2, diabetologistId);
            ps.setString(3, senderRoleToMark);
            ps.executeUpdate();
        }
    }
}
