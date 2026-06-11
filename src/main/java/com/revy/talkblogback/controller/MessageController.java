package com.revy.talkblogback.controller;

import com.revy.talkblogback.auth.RequireRoles;
import com.revy.talkblogback.pojo.dto.ChatroomRow;
import com.revy.talkblogback.pojo.dto.MessageRow;
import com.revy.talkblogback.pojo.response.ApiResponse;
import com.revy.talkblogback.pojo.response.PageResult;
import com.revy.talkblogback.pojo.response.UserProfile;
import com.revy.talkblogback.service.ChatroomService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/chatrooms")
@RequireRoles
public class MessageController {

    private final ChatroomService chatroomService;

    public MessageController(ChatroomService chatroomService) {
        this.chatroomService = chatroomService;
    }

    @GetMapping("/mine")
    public ResponseEntity<ApiResponse<PageResult<ChatroomRow>>> getMyChatrooms(
            @RequestAttribute("currentUser") UserProfile currentUser,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "100") int size) {
        return ResponseEntity.ok(
                ApiResponse.success("My chatrooms loaded", chatroomService.pageMyChatrooms(currentUser.getUserId(), page, size))
        );
    }

    @GetMapping("/{chatroomId}/messages")
    public ResponseEntity<ApiResponse<PageResult<MessageRow>>> getMessages(
            @PathVariable Long chatroomId,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "50") int size,
            @RequestAttribute("currentUser") UserProfile currentUser) {
        return ResponseEntity.ok(
                ApiResponse.success("Messages loaded", chatroomService.pageMessages(chatroomId, currentUser.getUserId(), page, size))
        );
    }

    @PostMapping("/{chatroomId}/messages")
    public ResponseEntity<ApiResponse<Map<String, Long>>> sendMessage(
            @PathVariable Long chatroomId,
            @RequestBody Map<String, String> body,
            @RequestAttribute("currentUser") UserProfile currentUser) {
        String content = body.get("content");
        Long messageId = chatroomService.sendMessage(chatroomId, currentUser.getUserId(), content);
        return ResponseEntity.ok(
                ApiResponse.success("Message sent", Map.of("messageId", messageId))
        );
    }

    @PatchMapping("/{chatroomId}/messages/{messageId}/recall")
    public ResponseEntity<ApiResponse<Void>> recallMessage(
            @PathVariable Long chatroomId,
            @PathVariable Long messageId,
            @RequestAttribute("currentUser") UserProfile currentUser) {
        chatroomService.recallMessage(chatroomId, messageId, currentUser.getUserId());
        return ResponseEntity.ok(ApiResponse.success("Message recalled"));
    }

    @PatchMapping("/{chatroomId}/messages/{messageId}/pin")
    public ResponseEntity<ApiResponse<Void>> pinMessage(
            @PathVariable Long chatroomId,
            @PathVariable Long messageId,
            @RequestBody Map<String, Boolean> body,
            @RequestAttribute("currentUser") UserProfile currentUser) {
        boolean isPinned = body.getOrDefault("isPinned", false);
        chatroomService.pinMessage(chatroomId, messageId, currentUser.getUserId(), isPinned);
        return ResponseEntity.ok(ApiResponse.success(isPinned ? "Message pinned" : "Message unpinned"));
    }

    @DeleteMapping("/{chatroomId}/messages/{messageId}")
    public ResponseEntity<ApiResponse<Void>> deleteMessage(
            @PathVariable Long chatroomId,
            @PathVariable Long messageId,
            @RequestAttribute("currentUser") UserProfile currentUser) {
        chatroomService.deleteMessage(chatroomId, messageId, currentUser.getUserId());
        return ResponseEntity.ok(ApiResponse.success("Message deleted"));
    }

    @GetMapping("/{chatroomId}/my-role")
    public ResponseEntity<ApiResponse<Map<String, Object>>> getMyRole(
            @PathVariable Long chatroomId,
            @RequestAttribute("currentUser") UserProfile currentUser) {
        String role = chatroomService.getMemberRole(chatroomId, currentUser.getUserId());
        LocalDateTime mutedUntil = chatroomService.getMutedUntil(chatroomId, currentUser.getUserId());
        Map<String, Object> result = new HashMap<>();
        result.put("role", role != null ? role : "");
        result.put("mutedUntil", mutedUntil);
        return ResponseEntity.ok(
                ApiResponse.success("Role retrieved", result)
        );
    }

    @PatchMapping("/{chatroomId}/members/{userId}/mute")
    public ResponseEntity<ApiResponse<Void>> muteMember(
            @PathVariable Long chatroomId,
            @PathVariable Long userId,
            @RequestBody Map<String, Integer> body,
            @RequestAttribute("currentUser") UserProfile currentUser) {
        Integer durationMinutes = body.get("durationMinutes");
        chatroomService.muteMember(chatroomId, currentUser.getUserId(), userId, durationMinutes);
        return ResponseEntity.ok(ApiResponse.success("Member muted"));
    }

    @PatchMapping("/{chatroomId}/members/{userId}/unmute")
    public ResponseEntity<ApiResponse<Void>> unmuteMember(
            @PathVariable Long chatroomId,
            @PathVariable Long userId,
            @RequestAttribute("currentUser") UserProfile currentUser) {
        chatroomService.unmuteMember(chatroomId, currentUser.getUserId(), userId);
        return ResponseEntity.ok(ApiResponse.success("Member unmuted"));
    }
}
