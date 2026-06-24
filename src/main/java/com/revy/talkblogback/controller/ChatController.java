package com.revy.talkblogback.controller;

import com.revy.talkblogback.auth.RequireRoles;
import com.revy.talkblogback.pojo.request.ApplyJoinRequest;
import com.revy.talkblogback.pojo.request.CreateGroupRequest;
import com.revy.talkblogback.pojo.request.RenameGroupRequest;
import com.revy.talkblogback.pojo.request.SendMessageRequest;
import com.revy.talkblogback.pojo.request.UpdateGroupSettingsRequest;
import com.revy.talkblogback.pojo.response.ApiResponse;
import com.revy.talkblogback.pojo.response.ChatGroupResponse;
import com.revy.talkblogback.pojo.response.ChatMessageResponse;
import com.revy.talkblogback.pojo.response.GroupJoinRequestResponse;
import com.revy.talkblogback.pojo.response.GroupMemberResponse;
import com.revy.talkblogback.service.ChatService;
import com.revy.talkblogback.websocket.ChatWebSocketHandler;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/chat")
@RequireRoles
public class ChatController {

    private final ChatService chatService;
    private final ChatWebSocketHandler webSocketHandler;

    public ChatController(ChatService chatService, ChatWebSocketHandler webSocketHandler) {
        this.chatService = chatService;
        this.webSocketHandler = webSocketHandler;
    }

    @PostMapping("/groups")
    public ResponseEntity<ApiResponse<ChatGroupResponse>> createGroup(@RequestBody CreateGroupRequest request) {
        ChatGroupResponse group = chatService.createGroup(request);
        return ResponseEntity.ok(ApiResponse.success("群组创建成功", group));
    }

    @GetMapping("/groups")
    public ResponseEntity<ApiResponse<List<ChatGroupResponse>>> getGroups() {
        List<ChatGroupResponse> groups = chatService.getMyGroups();
        return ResponseEntity.ok(ApiResponse.success("群组加载成功", groups));
    }

    @GetMapping("/groups/public")
    public ResponseEntity<ApiResponse<List<ChatGroupResponse>>> getPublicGroups() {
        List<ChatGroupResponse> groups = chatService.getPublicGroups();
        return ResponseEntity.ok(ApiResponse.success("公开群组加载成功", groups));
    }

    @GetMapping("/groups/search")
    public ResponseEntity<ApiResponse<List<ChatGroupResponse>>> searchGroups(@RequestParam String keyword) {
        List<ChatGroupResponse> groups = chatService.searchGroups(keyword);
        return ResponseEntity.ok(ApiResponse.success("搜索成功", groups));
    }

    @GetMapping("/groups/{groupId}")
    public ResponseEntity<ApiResponse<ChatGroupResponse>> getGroup(@PathVariable Long groupId) {
        ChatGroupResponse group = chatService.getGroup(groupId);
        return ResponseEntity.ok(ApiResponse.success("群组详情加载成功", group));
    }

    @PostMapping("/groups/{groupId}/join")
    public ResponseEntity<ApiResponse<Void>> joinGroup(@PathVariable Long groupId) {
        try {
            chatService.joinGroup(groupId);
            return ResponseEntity.ok(ApiResponse.success("加入群组成功"));
        } catch (IllegalArgumentException ex) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(ApiResponse.failure(ex.getMessage()));
        }
    }

    @PostMapping("/groups/{groupId}/leave")
    public ResponseEntity<ApiResponse<Void>> leaveGroup(@PathVariable Long groupId) {
        chatService.leaveGroup(groupId);
        return ResponseEntity.ok(ApiResponse.success("离开群组成功"));
    }

    @GetMapping("/groups/{groupId}/members")
    public ResponseEntity<ApiResponse<List<GroupMemberResponse>>> getGroupMembers(@PathVariable Long groupId) {
        List<GroupMemberResponse> members = chatService.getGroupMembers(groupId);
        return ResponseEntity.ok(ApiResponse.success("成员列表加载成功", members));
    }

    @GetMapping("/messages/{groupId}")
    public ResponseEntity<ApiResponse<List<ChatMessageResponse>>> getGroupMessages(
            @PathVariable Long groupId,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "50") int size) {
        List<ChatMessageResponse> messages = chatService.getGroupMessages(groupId, page, size);
        return ResponseEntity.ok(ApiResponse.success("消息加载成功", messages));
    }

    @PostMapping("/messages/{groupId}")
    public ResponseEntity<ApiResponse<ChatMessageResponse>> sendMessage(
            @PathVariable Long groupId,
            @RequestBody SendMessageRequest request) {
        ChatMessageResponse message = chatService.sendMessage(groupId, request);
        webSocketHandler.broadcastToGroup(groupId, Map.of(
                "type", "new_message",
                "message", message
        ));
        return ResponseEntity.ok(ApiResponse.success("消息发送成功", message));
    }

    @PutMapping("/groups/{groupId}")
    public ResponseEntity<ApiResponse<Void>> renameGroup(
            @PathVariable Long groupId,
            @RequestBody RenameGroupRequest request) {
        chatService.renameGroup(groupId, request.getGroupName());
        return ResponseEntity.ok(ApiResponse.success("群组名称已更新"));
    }

    @PutMapping("/groups/{groupId}/settings")
    public ResponseEntity<ApiResponse<Void>> updateGroupSettings(
            @PathVariable Long groupId,
            @RequestBody UpdateGroupSettingsRequest request) {
        chatService.updateGroupSettings(groupId, request.getGroupType(), request.getIsPublic());
        return ResponseEntity.ok(ApiResponse.success("群组设置已更新"));
    }

    @DeleteMapping("/messages/{messageId}")
    public ResponseEntity<ApiResponse<Void>> deleteMessage(@PathVariable Long messageId) {
        chatService.deleteMessage(messageId);
        return ResponseEntity.ok(ApiResponse.success("消息删除成功"));
    }

    @PostMapping("/messages/{messageId}/recall")
    public ResponseEntity<ApiResponse<Void>> recallMessage(@PathVariable Long messageId) {
        chatService.recallMessage(messageId);
        return ResponseEntity.ok(ApiResponse.success("消息已撤回"));
    }

    @PostMapping("/groups/{groupId}/apply")
    public ResponseEntity<ApiResponse<Void>> applyJoinGroup(
            @PathVariable Long groupId,
            @RequestBody(required = false) ApplyJoinRequest request) {
        String reason = request != null ? request.getReason() : null;
        chatService.applyJoinGroup(groupId, reason);
        return ResponseEntity.ok(ApiResponse.success("申请已提交，等待群主审核"));
    }

    @GetMapping("/groups/{groupId}/requests")
    public ResponseEntity<ApiResponse<List<GroupJoinRequestResponse>>> getPendingRequests(
            @PathVariable Long groupId) {
        List<GroupJoinRequestResponse> requests = chatService.getPendingJoinRequests(groupId);
        return ResponseEntity.ok(ApiResponse.success("加载成功", requests));
    }

    @PostMapping("/groups/{groupId}/requests/{requestId}/approve")
    public ResponseEntity<ApiResponse<Void>> approveRequest(
            @PathVariable Long groupId,
            @PathVariable Long requestId) {
        chatService.approveJoinRequest(groupId, requestId);
        return ResponseEntity.ok(ApiResponse.success("已通过申请"));
    }

    @PostMapping("/groups/{groupId}/requests/{requestId}/reject")
    public ResponseEntity<ApiResponse<Void>> rejectRequest(
            @PathVariable Long groupId,
            @PathVariable Long requestId) {
        chatService.rejectJoinRequest(groupId, requestId);
        return ResponseEntity.ok(ApiResponse.success("已拒绝申请"));
    }

    @GetMapping("/requests")
    public ResponseEntity<ApiResponse<List<GroupJoinRequestResponse>>> getMyRequests() {
        List<GroupJoinRequestResponse> requests = chatService.getMyJoinRequests();
        return ResponseEntity.ok(ApiResponse.success("加载成功", requests));
    }
}
