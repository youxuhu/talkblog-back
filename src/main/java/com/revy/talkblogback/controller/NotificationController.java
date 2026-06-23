package com.revy.talkblogback.controller;

import com.revy.talkblogback.auth.AuthContext;
import com.revy.talkblogback.auth.RequireRoles;
import com.revy.talkblogback.pojo.Notification;
import com.revy.talkblogback.pojo.response.ApiResponse;
import com.revy.talkblogback.pojo.response.PageResult;
import com.revy.talkblogback.pojo.response.UnreadCount;
import com.revy.talkblogback.pojo.response.UserProfile;
import com.revy.talkblogback.service.NotificationService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/notifications")
public class NotificationController {

    private final NotificationService notificationService;

    public NotificationController(NotificationService notificationService) {
        this.notificationService = notificationService;
    }

    @GetMapping
    @RequireRoles
    public ResponseEntity<ApiResponse<PageResult<Notification>>> getNotifications(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "20") int size) {
        UserProfile user = AuthContext.get();
        if (user == null) {
            return ResponseEntity.status(401).body(ApiResponse.failure("Unauthorized"));
        }
        PageResult<Notification> result = notificationService.getNotifications(user.getUserId(), page, size);
        return ResponseEntity.ok(ApiResponse.success("success", result));
    }

    @GetMapping("/unread-count")
    @RequireRoles
    public ResponseEntity<ApiResponse<UnreadCount>> getUnreadCount() {
        UserProfile user = AuthContext.get();
        if (user == null) {
            return ResponseEntity.status(401).body(ApiResponse.failure("Unauthorized"));
        }
        int count = notificationService.getUnreadCount(user.getUserId());
        return ResponseEntity.ok(ApiResponse.success("success", new UnreadCount(count)));
    }

    @PatchMapping("/{id}/read")
    @RequireRoles
    public ResponseEntity<ApiResponse<Void>> markAsRead(@PathVariable Long id) {
        UserProfile user = AuthContext.get();
        if (user == null) {
            return ResponseEntity.status(401).body(ApiResponse.failure("Unauthorized"));
        }
        notificationService.markAsRead(id, user.getUserId());
        return ResponseEntity.ok(ApiResponse.success("Marked as read"));
    }

    @PatchMapping("/read-all")
    @RequireRoles
    public ResponseEntity<ApiResponse<Void>> markAllAsRead() {
        UserProfile user = AuthContext.get();
        if (user == null) {
            return ResponseEntity.status(401).body(ApiResponse.failure("Unauthorized"));
        }
        notificationService.markAllAsRead(user.getUserId());
        return ResponseEntity.ok(ApiResponse.success("All marked as read"));
    }
}
