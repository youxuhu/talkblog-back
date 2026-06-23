package com.revy.talkblogback.websocket;

import com.revy.talkblogback.auth.JwtTokenService;
import com.revy.talkblogback.pojo.response.UserProfile;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.TextWebSocketHandler;

import java.io.IOException;
import java.net.URI;
import java.util.concurrent.ConcurrentHashMap;

@Component
public class NotificationWebSocketHandler extends TextWebSocketHandler {

    private static final Logger log = LoggerFactory.getLogger(NotificationWebSocketHandler.class);
    private final ConcurrentHashMap<Long, WebSocketSession> sessions = new ConcurrentHashMap<>();
    private final JwtTokenService jwtTokenService;

    public NotificationWebSocketHandler(JwtTokenService jwtTokenService) {
        this.jwtTokenService = jwtTokenService;
    }

    @Override
    public void afterConnectionEstablished(WebSocketSession session) {
        Long userId = getUserIdFromSession(session);
        if (userId == null) {
            try {
                session.close(CloseStatus.POLICY_VIOLATION);
            } catch (IOException e) {
                log.error("Failed to close unauthorized websocket session", e);
            }
            return;
        }
        sessions.put(userId, session);
        log.debug("WebSocket connected: userId={}, sessionId={}", userId, session.getId());
    }

    @Override
    public void afterConnectionClosed(WebSocketSession session, CloseStatus status) {
        sessions.entrySet().removeIf(entry -> entry.getValue().getId().equals(session.getId()));
        log.debug("WebSocket disconnected: sessionId={}", session.getId());
    }

    @Override
    protected void handleTextMessage(WebSocketSession session, TextMessage message) {
        // Client-to-server messages are not expected in this design
    }

    public void sendNotification(Long userId, String messageJson) {
        WebSocketSession session = sessions.get(userId);
        if (session != null && session.isOpen()) {
            try {
                session.sendMessage(new TextMessage(messageJson));
            } catch (IOException e) {
                log.error("Failed to send WebSocket message to userId={}", userId, e);
                sessions.remove(userId);
            }
        }
    }

    private Long getUserIdFromSession(WebSocketSession session) {
        URI uri = session.getUri();
        if (uri == null) return null;
        String query = uri.getQuery();
        if (query == null || !query.contains("token=")) return null;

        String token = null;
        for (String param : query.split("&")) {
            if (param.startsWith("token=")) {
                token = param.substring(6);
                break;
            }
        }
        if (token == null || token.isEmpty()) return null;

        UserProfile userProfile = jwtTokenService.parseToken(token);
        return userProfile != null ? userProfile.getUserId() : null;
    }
}
