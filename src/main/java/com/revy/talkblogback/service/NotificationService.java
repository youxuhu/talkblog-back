package com.revy.talkblogback.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.revy.talkblogback.mapper.NotificationMapper;
import com.revy.talkblogback.pojo.Notification;
import com.revy.talkblogback.pojo.response.PageResult;
import com.revy.talkblogback.websocket.NotificationWebSocketHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;

@Service
public class NotificationService {

    private static final Logger log = LoggerFactory.getLogger(NotificationService.class);
    private static final ObjectMapper objectMapper = new ObjectMapper();

    private final NotificationMapper notificationMapper;
    private final NotificationWebSocketHandler webSocketHandler;

    public NotificationService(NotificationMapper notificationMapper,
                               NotificationWebSocketHandler webSocketHandler) {
        this.notificationMapper = notificationMapper;
        this.webSocketHandler = webSocketHandler;
    }

    public PageResult<Notification> getNotifications(Long userId, int page, int size) {
        int safePage = Math.max(page, 1);
        int safeSize = Math.max(size, 1);
        int offset = (safePage - 1) * safeSize;
        List<Notification> list = notificationMapper.findByUserId(userId, offset, safeSize);
        long total = notificationMapper.countByUserId(userId);
        return new PageResult<>(list, total, safePage, safeSize);
    }

    public int getUnreadCount(Long userId) {
        return notificationMapper.countUnreadByUserId(userId);
    }

    @Transactional(rollbackFor = Exception.class)
    public void markAsRead(Long id, Long userId) {
        notificationMapper.markAsRead(id, userId);
    }

    @Transactional(rollbackFor = Exception.class)
    public void markAllAsRead(Long userId) {
        notificationMapper.markAllAsRead(userId);
    }

    @Transactional(rollbackFor = Exception.class)
    public Notification createNotification(Notification notification) {
        notificationMapper.insert(notification);
        pushViaWebSocket(notification);
        return notification;
    }

    private void pushViaWebSocket(Notification notification) {
        try {
            String json = objectMapper.writeValueAsString(Map.of(
                    "type", "new_notification",
                    "notification", notification
            ));
            webSocketHandler.sendNotification(notification.getUserId(), json);
        } catch (Exception e) {
            log.warn("Failed to push notification via WebSocket: {}", e.getMessage());
        }
    }
}
