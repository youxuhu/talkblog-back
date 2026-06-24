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
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Component
public class ChatWebSocketHandler extends TextWebSocketHandler {

    private static final Logger log = LoggerFactory.getLogger(ChatWebSocketHandler.class);

    private final JwtTokenService jwtTokenService;
    private final WebSocketSessionManager sessionManager;

    public ChatWebSocketHandler(JwtTokenService jwtTokenService, WebSocketSessionManager sessionManager) {
        this.jwtTokenService = jwtTokenService;
        this.sessionManager = sessionManager;
    }

    @Override
    public void afterConnectionEstablished(WebSocketSession session) {
        String token = extractToken(session);
        if (token == null) {
            closeSession(session, CloseStatus.POLICY_VIOLATION, "Missing token");
            return;
        }

        UserProfile user = jwtTokenService.parseToken(token);
        if (user == null) {
            closeSession(session, CloseStatus.POLICY_VIOLATION, "Invalid or expired token");
            return;
        }

        sessionManager.registerSession(session, user);
        log.info("WebSocket connected: user={} (ID={}), session={}", user.getUsername(), user.getUserId(), session.getId());

        sendMessage(session, Map.of(
                "type", "authenticated",
                "userId", user.getUserId(),
                "username", user.getUsername()
        ));
    }

    @Override
    protected void handleTextMessage(WebSocketSession session, TextMessage message) {
        UserProfile user = sessionManager.getUser(session.getId());
        if (user == null) {
            closeSession(session, CloseStatus.POLICY_VIOLATION, "Not authenticated");
            return;
        }

        try {
            @SuppressWarnings("unchecked")
            Map<String, Object> payload = new com.fasterxml.jackson.databind.ObjectMapper()
                    .readValue(message.getPayload(), Map.class);

            String type = (String) payload.get("type");

            switch (type) {
                case "join_group" -> {
                    Long groupId = Long.valueOf(payload.get("groupId").toString());
                    sessionManager.joinGroup(session, groupId);
                    broadcastToGroup(groupId, Map.of(
                            "type", "user_joined",
                            "groupId", groupId,
                            "userId", user.getUserId(),
                            "username", user.getUsername()
                    ));
                }
                case "leave_group" -> {
                    Long groupId = Long.valueOf(payload.get("groupId").toString());
                    sessionManager.leaveGroup(session.getId(), groupId);
                    broadcastToGroup(groupId, Map.of(
                            "type", "user_left",
                            "groupId", groupId,
                            "userId", user.getUserId(),
                            "username", user.getUsername()
                    ));
                }
                case "typing" -> {
                    Long groupId = Long.valueOf(payload.get("groupId").toString());
                    Boolean isTyping = (Boolean) payload.get("isTyping");
                    broadcastToGroupExceptSender(session.getId(), groupId, Map.of(
                            "type", "user_typing",
                            "groupId", groupId,
                            "userId", user.getUserId(),
                            "username", user.getUsername(),
                            "isTyping", isTyping
                    ));
                }
                default -> log.warn("Unknown message type: {}", type);
            }
        } catch (Exception e) {
            log.error("Error handling WebSocket message: {}", e.getMessage());
        }
    }

    @Override
    public void afterConnectionClosed(WebSocketSession session, CloseStatus status) {
        UserProfile user = sessionManager.getUser(session.getId());
        if (user != null) {
            sessionManager.unregisterSession(session.getId());
            broadcastOnlineStatus(user.getUserId(), false);
            log.info("WebSocket disconnected: user={} (ID={}), session={}", user.getUsername(), user.getUserId(), session.getId());
        }
    }

    public void broadcastToGroup(Long groupId, Object data) {
        Map<String, WebSocketSession> sessions = sessionManager.getGroupSessions(groupId);
        if (sessions == null) return;

        String json = toJson(data);
        if (json == null) return;

        TextMessage message = new TextMessage(json);
        for (WebSocketSession s : sessions.values()) {
            if (s.isOpen()) {
                try {
                    s.sendMessage(message);
                } catch (IOException e) {
                    log.error("Failed to send message to session {}", s.getId());
                }
            }
        }
    }

    public void broadcastToGroupExceptSender(String senderSessionId, Long groupId, Object data) {
        Map<String, WebSocketSession> sessions = sessionManager.getGroupSessions(groupId);
        if (sessions == null) return;

        String json = toJson(data);
        if (json == null) return;

        TextMessage message = new TextMessage(json);
        for (Map.Entry<String, WebSocketSession> entry : sessions.entrySet()) {
            if (!entry.getKey().equals(senderSessionId) && entry.getValue().isOpen()) {
                try {
                    entry.getValue().sendMessage(message);
                } catch (IOException e) {
                    log.error("Failed to send message to session {}", entry.getKey());
                }
            }
        }
    }

    public void sendToUser(Long userId, Object data) {
        WebSocketSession session = sessionManager.getUserSession(userId);
        if (session == null || !session.isOpen()) return;
        sendMessage(session, data);
    }

    private void broadcastOnlineStatus(Long userId, boolean online) {
        for (Map.Entry<Long, Map<String, WebSocketSession>> entry : sessionManager.getAllGroupRooms().entrySet()) {
            broadcastToGroup(entry.getKey(), Map.of(
                    "type", "user_status",
                    "userId", userId,
                    "online", online
            ));
        }
    }

    private String extractToken(WebSocketSession session) {
        URI uri = session.getUri();
        if (uri == null) return null;
        String query = uri.getQuery();
        if (query == null) return null;
        for (String param : query.split("&")) {
            String[] pair = param.split("=", 2);
            if (pair.length == 2 && "token".equals(pair[0])) {
                return pair[1];
            }
        }
        return null;
    }

    private void closeSession(WebSocketSession session, CloseStatus status, String reason) {
        try {
            sendMessage(session, Map.of("type", "error", "message", reason));
            session.close(status);
        } catch (IOException ignored) {}
    }

    private void sendMessage(WebSocketSession session, Object data) {
        String json = toJson(data);
        if (json == null) return;
        try {
            if (session.isOpen()) {
                session.sendMessage(new TextMessage(json));
            }
        } catch (IOException e) {
            log.error("Failed to send message: {}", e.getMessage());
        }
    }

    private String toJson(Object data) {
        try {
            return new com.fasterxml.jackson.databind.ObjectMapper().writeValueAsString(data);
        } catch (Exception e) {
            log.error("Failed to serialize message: {}", e.getMessage());
            return null;
        }
    }
}