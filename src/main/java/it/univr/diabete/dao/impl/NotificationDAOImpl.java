package it.univr.diabete.dao.impl;

import it.univr.diabete.dao.NotificationDAO;
import it.univr.diabete.database.Database;
import it.univr.diabete.model.Notification;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class NotificationDAOImpl implements NotificationDAO {

    private Notification mapRow(ResultSet rs) throws Exception {
        Notification n = new Notification();
        n.setId(rs.getInt("id"));
        n.setTargetRole(rs.getString("targetRole"));
        n.setTargetUserId(rs.getString("targetUserId"));
        n.setType(rs.getString("type"));
        n.setSeverity(rs.getString("severity"));
        n.setTitle(rs.getString("title"));
        n.setBody(rs.getString("body"));
        Timestamp createdAt = rs.getTimestamp("createdAt");
        if (createdAt != null) {
            n.setCreatedAt(createdAt.toLocalDateTime());
        }
        Timestamp readAt = rs.getTimestamp("readAt");
        if (readAt != null) {
            n.setReadAt(readAt.toLocalDateTime());
        }
        n.setActionType(rs.getString("actionType"));
        n.setActionRefId(rs.getString("actionRefId"));
        return n;
    }

    @Override
    public List<Notification> findByTarget(String targetRole, String targetUserId, int limit) throws Exception {
        String sql = """
            SELECT id, targetRole, targetUserId, type, severity, title, body,
                   createdAt, readAt, actionType, actionRefId
            FROM Notification
            WHERE targetRole = ? AND targetUserId = ?
            ORDER BY createdAt DESC, id DESC
            LIMIT ?
            """;
        List<Notification> result = new ArrayList<>();
        try (Connection conn = Database.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, targetRole);
            ps.setString(2, targetUserId);
            ps.setInt(3, limit);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    result.add(mapRow(rs));
                }
            }
        }
        return result;
    }

    @Override
    public List<Notification> findByTargetAndType(String targetRole, String targetUserId, String type) throws Exception {
        String sql = """
            SELECT id, targetRole, targetUserId, type, severity, title, body,
                   createdAt, readAt, actionType, actionRefId
            FROM Notification
            WHERE targetRole = ? AND targetUserId = ? AND type = ?
            ORDER BY createdAt DESC, id DESC
            """;
        List<Notification> result = new ArrayList<>();
        try (Connection conn = Database.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, targetRole);
            ps.setString(2, targetUserId);
            ps.setString(3, type);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    result.add(mapRow(rs));
                }
            }
        }
        return result;
    }

    @Override
    public int countUnread(String targetRole, String targetUserId) throws Exception {
        String sql = """
            SELECT COUNT(*) AS cnt
            FROM Notification
            WHERE targetRole = ? AND targetUserId = ? AND readAt IS NULL
            """;
        try (Connection conn = Database.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, targetRole);
            ps.setString(2, targetUserId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt("cnt");
                }
            }
        }
        return 0;
    }

    @Override
    public Notification findByKey(String targetRole, String targetUserId, String type, String actionRefId) throws Exception {
        String sql = """
            SELECT id, targetRole, targetUserId, type, severity, title, body,
                   createdAt, readAt, actionType, actionRefId
            FROM Notification
            WHERE targetRole = ? AND targetUserId = ? AND type = ?
              AND ((actionRefId = ?) OR (actionRefId IS NULL AND ? IS NULL))
            ORDER BY createdAt DESC, id DESC
            LIMIT 1
            """;
        try (Connection conn = Database.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, targetRole);
            ps.setString(2, targetUserId);
            ps.setString(3, type);
            ps.setString(4, actionRefId);
            ps.setString(5, actionRefId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return mapRow(rs);
                }
            }
        }
        return null;
    }

    @Override
    public void insert(Notification notification) throws Exception {
        String sql = """
            INSERT INTO Notification
                (targetRole, targetUserId, type, severity, title, body, createdAt, readAt, actionType, actionRefId)
            VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?)
            """;
        LocalDateTime createdAt = notification.getCreatedAt() != null
                ? notification.getCreatedAt()
                : LocalDateTime.now();
        try (Connection conn = Database.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, notification.getTargetRole());
            ps.setString(2, notification.getTargetUserId());
            ps.setString(3, notification.getType());
            ps.setString(4, notification.getSeverity());
            ps.setString(5, notification.getTitle());
            ps.setString(6, notification.getBody());
            ps.setTimestamp(7, Timestamp.valueOf(createdAt));
            if (notification.getReadAt() != null) {
                ps.setTimestamp(8, Timestamp.valueOf(notification.getReadAt()));
            } else {
                ps.setTimestamp(8, null);
            }
            ps.setString(9, notification.getActionType());
            ps.setString(10, notification.getActionRefId());
            ps.executeUpdate();
        }
    }

    @Override
    public void update(Notification notification) throws Exception {
        String sql = """
            UPDATE Notification
            SET severity = ?, title = ?, body = ?, createdAt = ?, actionType = ?, actionRefId = ?
            WHERE id = ?
            """;
        LocalDateTime createdAt = notification.getCreatedAt() != null
                ? notification.getCreatedAt()
                : LocalDateTime.now();
        try (Connection conn = Database.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, notification.getSeverity());
            ps.setString(2, notification.getTitle());
            ps.setString(3, notification.getBody());
            ps.setTimestamp(4, Timestamp.valueOf(createdAt));
            ps.setString(5, notification.getActionType());
            ps.setString(6, notification.getActionRefId());
            ps.setInt(7, notification.getId());
            ps.executeUpdate();
        }
    }

    @Override
    public void markRead(int id) throws Exception {
        String sql = """
            UPDATE Notification
            SET readAt = NOW()
            WHERE id = ? AND readAt IS NULL
            """;
        try (Connection conn = Database.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, id);
            ps.executeUpdate();
        }
    }

    @Override
    public void markAllRead(String targetRole, String targetUserId) throws Exception {
        String sql = """
            UPDATE Notification
            SET readAt = NOW()
            WHERE targetRole = ? AND targetUserId = ? AND readAt IS NULL
            """;
        try (Connection conn = Database.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, targetRole);
            ps.setString(2, targetUserId);
            ps.executeUpdate();
        }
    }

    @Override
    public void markReadByKey(String targetRole, String targetUserId, String type, String actionRefId) throws Exception {
        String sql = """
            UPDATE Notification
            SET readAt = NOW()
            WHERE targetRole = ? AND targetUserId = ? AND type = ?
              AND ((actionRefId = ?) OR (actionRefId IS NULL AND ? IS NULL))
              AND readAt IS NULL
            """;
        try (Connection conn = Database.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, targetRole);
            ps.setString(2, targetUserId);
            ps.setString(3, type);
            ps.setString(4, actionRefId);
            ps.setString(5, actionRefId);
            ps.executeUpdate();
        }
    }
}
