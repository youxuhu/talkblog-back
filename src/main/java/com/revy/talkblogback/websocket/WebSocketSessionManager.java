package com.revy.talkblogback.websocket;

import com.revy.talkblogback.pojo.response.UserProfile;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.WebSocketSession;

import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArraySet;

@Component
public class WebSocketSessionManager {

    private final Map<String, UserProfile> sessionUsers = new ConcurrentHashMap<>();
    private final Map<Long, WebSocketSession> userSessions = new ConcurrentHashMap<>();
    private final Map<Long, Map<String, WebSocketSession>> groupRooms = new ConcurrentHashMap<>();
    private final Map<String, Set<Long>> sessionGroups = new ConcurrentHashMap<>();

    public void registerSession(WebSocketSession session, UserProfile user) {
        sessionUsers.put(session.getId(), user);
        WebSocketSession existing = userSessions.put(user.getUserId(), session);
        if (existing != null && existing.isOpen() && !existing.getId().equals(session.getId())) {
            try { existing.close(); } catch (Exception ignored) {}
        }
    }

    public void unregisterSession(String sessionId) {
        UserProfile user = sessionUsers.remove(sessionId);
        if (user != null) {
            userSessions.remove(user.getUserId());
        }
        Set<Long> groupIds = sessionGroups.remove(sessionId);
        if (groupIds != null) {
            for (Long groupId : groupIds) {
                Map<String, WebSocketSession> room = groupRooms.get(groupId);
                if (room != null) {
                    room.remove(sessionId);
                    if (room.isEmpty()) {
                        groupRooms.remove(groupId);
                    }
                }
            }
        }
    }

    public UserProfile getUser(String sessionId) {
        return sessionUsers.get(sessionId);
    }

    public WebSocketSession getUserSession(Long userId) {
        return userSessions.get(userId);
    }

    public void joinGroup(WebSocketSession session, Long groupId) {
        groupRooms.computeIfAbsent(groupId, k -> new ConcurrentHashMap<>()).put(session.getId(), session);
        sessionGroups.computeIfAbsent(session.getId(), k -> new CopyOnWriteArraySet<>()).add(groupId);
    }

    public void leaveGroup(String sessionId, Long groupId) {
        Map<String, WebSocketSession> room = groupRooms.get(groupId);
        if (room != null) {
            room.remove(sessionId);
            if (room.isEmpty()) {
                groupRooms.remove(groupId);
            }
        }
        Set<Long> groups = sessionGroups.get(sessionId);
        if (groups != null) {
            groups.remove(groupId);
        }
    }

    public Map<String, WebSocketSession> getGroupSessions(Long groupId) {
        return groupRooms.get(groupId);
    }

    public Map<Long, Map<String, WebSocketSession>> getAllGroupRooms() {
        return groupRooms;
    }
}