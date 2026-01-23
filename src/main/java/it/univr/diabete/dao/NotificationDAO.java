package it.univr.diabete.dao;

import it.univr.diabete.model.Notification;

import java.util.List;

public interface NotificationDAO {

    List<Notification> findByTarget(String targetRole, String targetUserId, int limit) throws Exception;

    List<Notification> findByTargetAndType(String targetRole, String targetUserId, String type) throws Exception;

    int countUnread(String targetRole, String targetUserId) throws Exception;

    Notification findByKey(String targetRole, String targetUserId, String type, String actionRefId) throws Exception;

    void insert(Notification notification) throws Exception;

    void update(Notification notification) throws Exception;

    void markRead(int id) throws Exception;

    void markAllRead(String targetRole, String targetUserId) throws Exception;

    void markReadByKey(String targetRole, String targetUserId, String type, String actionRefId) throws Exception;
}
