package com.revy.talkblogback.controller;

import com.revy.talkblogback.auth.RequireRoles;
import com.revy.talkblogback.pojo.dto.ChatroomRow;
import com.revy.talkblogback.pojo.dto.ChatroomStats;
import com.revy.talkblogback.pojo.dto.ChatroomMemberRow;
import com.revy.talkblogback.pojo.dto.DailyStatRow;
import com.revy.talkblogback.pojo.request.AddMemberRequest;
import com.revy.talkblogback.pojo.request.CreateChatroomRequest;
import com.revy.talkblogback.pojo.request.UpdateChatroomRequest;
import com.revy.talkblogback.pojo.request.UpdateMemberRoleRequest;
import com.revy.talkblogback.pojo.response.ApiResponse;
import com.revy.talkblogback.pojo.response.PageResult;
import com.revy.talkblogback.service.ChatroomService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/admin/chatrooms")
@RequireRoles({"ADMIN", "SUPER_ADMIN"})
public class AdminChatroomController {

    private final ChatroomService chatroomService;

    public AdminChatroomController(ChatroomService chatroomService) {
        this.chatroomService = chatroomService;
    }

    @GetMapping
    public ResponseEntity<ApiResponse<PageResult<ChatroomRow>>> pageChatrooms(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) String keyword) {
        return ResponseEntity.ok(ApiResponse.success("Chatrooms loaded", chatroomService.pageChatrooms(page, size, keyword)));
    }

    @GetMapping("/{chatroomId}")
    public ResponseEntity<ApiResponse<ChatroomRow>> getChatroom(@PathVariable Long chatroomId) {
        ChatroomRow chatroom = chatroomService.getChatroomById(chatroomId);
        if (chatroom == null) {
            return ResponseEntity.ok(ApiResponse.failure("Chatroom not found"));
        }
        return ResponseEntity.ok(ApiResponse.success("Chatroom loaded", chatroom));
    }

    @PostMapping
    public ResponseEntity<ApiResponse<Map<String, Long>>> createChatroom(
            @RequestBody CreateChatroomRequest request,
            @RequestHeader(value = "X-User-Id", required = false) Long userId) {
        Long chatroomId = chatroomService.createChatroom(request, userId);
        return ResponseEntity.ok(ApiResponse.success("Chatroom created", Map.of("chatroomId", chatroomId)));
    }

    @PutMapping("/{chatroomId}")
    public ResponseEntity<ApiResponse<Void>> updateChatroom(
            @PathVariable Long chatroomId,
            @RequestBody UpdateChatroomRequest request) {
        boolean updated = chatroomService.updateChatroom(chatroomId, request);
        return ResponseEntity.ok(updated
                ? ApiResponse.success("Chatroom updated")
                : ApiResponse.failure("Failed to update chatroom"));
    }

    @DeleteMapping("/{chatroomId}")
    public ResponseEntity<ApiResponse<Void>> deleteChatroom(@PathVariable Long chatroomId) {
        boolean deleted = chatroomService.deleteChatroom(chatroomId);
        return ResponseEntity.ok(deleted
                ? ApiResponse.success("Chatroom deleted")
                : ApiResponse.failure("Failed to delete chatroom"));
    }

    @GetMapping("/{chatroomId}/members")
    public ResponseEntity<ApiResponse<PageResult<ChatroomMemberRow>>> pageMembers(
            @PathVariable Long chatroomId,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "50") int size,
            @RequestParam(required = false) String keyword) {
        return ResponseEntity.ok(ApiResponse.success("Members loaded", chatroomService.pageMembers(chatroomId, page, size, keyword)));
    }

    @PostMapping("/{chatroomId}/members")
    public ResponseEntity<ApiResponse<Void>> addMember(
            @PathVariable Long chatroomId,
            @RequestBody AddMemberRequest request) {
        boolean added = chatroomService.addMember(chatroomId, request);
        return ResponseEntity.ok(added
                ? ApiResponse.success("Member added")
                : ApiResponse.failure("Failed to add member"));
    }

    @DeleteMapping("/{chatroomId}/members/{userId}")
    public ResponseEntity<ApiResponse<Void>> removeMember(
            @PathVariable Long chatroomId,
            @PathVariable Long userId) {
        boolean removed = chatroomService.removeMember(chatroomId, userId);
        return ResponseEntity.ok(removed
                ? ApiResponse.success("Member removed")
                : ApiResponse.failure("Failed to remove member"));
    }

    @PatchMapping("/{chatroomId}/members/{userId}")
    public ResponseEntity<ApiResponse<Void>> updateMemberRole(
            @PathVariable Long chatroomId,
            @PathVariable Long userId,
            @RequestBody UpdateMemberRoleRequest request) {
        boolean updated = chatroomService.updateMemberRole(chatroomId, userId, request.getRole());
        return ResponseEntity.ok(updated
                ? ApiResponse.success("Member role updated")
                : ApiResponse.failure("Failed to update member role"));
    }

    @GetMapping("/stats")
    public ResponseEntity<ApiResponse<ChatroomStats>> getStats() {
        return ResponseEntity.ok(ApiResponse.success("Stats loaded", chatroomService.getStats()));
    }

    @GetMapping("/{chatroomId}/stats/daily")
    public ResponseEntity<ApiResponse<List<DailyStatRow>>> getDailyStats(
            @PathVariable Long chatroomId,
            @RequestParam(required = false) String startDate,
            @RequestParam(required = false) String endDate) {
        return ResponseEntity.ok(ApiResponse.success("Daily stats loaded", chatroomService.getDailyStats(chatroomId, startDate, endDate)));
    }
}