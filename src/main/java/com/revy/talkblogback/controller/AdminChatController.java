package com.revy.talkblogback.controller;

import com.revy.talkblogback.auth.RequireRoles;
import com.revy.talkblogback.pojo.request.BanUserRequest;
import com.revy.talkblogback.pojo.request.SetMemberRoleRequest;
import com.revy.talkblogback.pojo.response.AdminGroupDetailResponse;
import com.revy.talkblogback.pojo.response.ApiResponse;
import com.revy.talkblogback.pojo.response.ChatGroupResponse;
import com.revy.talkblogback.pojo.response.ChatMessageResponse;
import com.revy.talkblogback.pojo.response.PageResult;
import com.revy.talkblogback.service.ChatService;
import com.revy.talkblogback.websocket.ChatWebSocketHandler;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/admin/chat")
@RequireRoles({"ADMIN", "SUPER_ADMIN"})
public class AdminChatController {

    private final ChatService chatService;
    private final ChatWebSocketHandler webSocketHandler;

    public AdminChatController(ChatService chatService, ChatWebSocketHandler webSocketHandler) {
        this.chatService = chatService;
        this.webSocketHandler = webSocketHandler;
    }

    @GetMapping("/groups")
    public ResponseEntity<ApiResponse<PageResult<ChatGroupResponse>>> getGroups(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) String keyword) {
        PageResult<ChatGroupResponse> result = chatService.adminGetGroups(page, size, keyword);
        return ResponseEntity.ok(ApiResponse.success("群组列表加载成功", result));
    }

    @GetMapping("/groups/{groupId}")
    public ResponseEntity<ApiResponse<AdminGroupDetailResponse>> getGroupDetail(@PathVariable Long groupId) {
        AdminGroupDetailResponse detail = chatService.adminGetGroupDetail(groupId);
        return ResponseEntity.ok(ApiResponse.success("群组详情加载成功", detail));
    }

    @PostMapping("/groups/{groupId}/dismiss")
    public ResponseEntity<ApiResponse<Void>> dismissGroup(@PathVariable Long groupId) {
        chatService.adminDismissGroup(groupId);
        webSocketHandler.broadcastToGroup(groupId, Map.of(
                "type", "group_dismissed",
                "groupId", groupId
        ));
        return ResponseEntity.ok(ApiResponse.success("群组已解散"));
    }

    @PostMapping("/groups/{groupId}/ban")
    public ResponseEntity<ApiResponse<Void>> banUser(
            @PathVariable Long groupId,
            @RequestBody BanUserRequest request) {
        chatService.adminBanUser(groupId, request);
        webSocketHandler.sendToUser(request.getUserId(), Map.of(
                "type", "banned_from_group",
                "groupId", groupId,
                "reason", request.getReason()
        ));
        return ResponseEntity.ok(ApiResponse.success("用户已被移出并封禁"));
    }

    @PostMapping("/groups/{groupId}/set-admin")
    public ResponseEntity<ApiResponse<Void>> setMemberRole(
            @PathVariable Long groupId,
            @RequestBody SetMemberRoleRequest request) {
        chatService.adminSetMemberRole(groupId, request);
        return ResponseEntity.ok(ApiResponse.success("成员角色更新成功"));
    }

    @GetMapping("/messages")
    public ResponseEntity<ApiResponse<PageResult<ChatMessageResponse>>> getMessages(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) Long groupId,
            @RequestParam(required = false) Long userId) {
        PageResult<ChatMessageResponse> result = chatService.adminGetMessages(page, size, keyword, groupId, userId);
        return ResponseEntity.ok(ApiResponse.success("消息列表加载成功", result));
    }

    @DeleteMapping("/messages/{messageId}")
    public ResponseEntity<ApiResponse<Void>> deleteMessage(@PathVariable Long messageId) {
        chatService.adminDeleteMessage(messageId);
        return ResponseEntity.ok(ApiResponse.success("消息已删除"));
    }
}
