package com.revy.talkblogback.service;

import com.revy.talkblogback.auth.AuthContext;
import com.revy.talkblogback.pojo.response.UserProfile;
import com.revy.talkblogback.mapper.ChatGroupMapper;
import com.revy.talkblogback.mapper.ChatMessageMapper;
import com.revy.talkblogback.mapper.GroupJoinRequestMapper;
import com.revy.talkblogback.mapper.LoginMapper;
import com.revy.talkblogback.pojo.ChatGroup;
import com.revy.talkblogback.pojo.ChatMessage;
import com.revy.talkblogback.pojo.GroupJoinRequest;
import com.revy.talkblogback.pojo.GroupMember;
import com.revy.talkblogback.pojo.GroupBanRecord;
import com.revy.talkblogback.pojo.User;
import com.revy.talkblogback.pojo.request.BanUserRequest;
import com.revy.talkblogback.pojo.request.CreateGroupRequest;
import com.revy.talkblogback.pojo.request.SendMessageRequest;
import com.revy.talkblogback.pojo.request.SetMemberRoleRequest;
import com.revy.talkblogback.pojo.response.AdminGroupDetailResponse;
import com.revy.talkblogback.pojo.response.ChatGroupResponse;
import com.revy.talkblogback.pojo.response.ChatMessageResponse;
import com.revy.talkblogback.pojo.response.GroupJoinRequestResponse;
import com.revy.talkblogback.pojo.response.GroupMemberResponse;
import com.revy.talkblogback.pojo.response.PageResult;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;

@Service
public class ChatService {

    private final ChatGroupMapper chatGroupMapper;
    private final ChatMessageMapper chatMessageMapper;
    private final GroupJoinRequestMapper groupJoinRequestMapper;
    private final LoginMapper loginMapper;

    public ChatService(ChatGroupMapper chatGroupMapper, ChatMessageMapper chatMessageMapper, GroupJoinRequestMapper groupJoinRequestMapper, LoginMapper loginMapper) {
        this.chatGroupMapper = chatGroupMapper;
        this.chatMessageMapper = chatMessageMapper;
        this.groupJoinRequestMapper = groupJoinRequestMapper;
        this.loginMapper = loginMapper;
    }

    public Long getCurrentUserId() {
        UserProfile user = AuthContext.get();
        if (user == null) {
            throw new IllegalArgumentException("Authentication required.");
        }
        return user.getUserId();
    }

    @Transactional(rollbackFor = Exception.class)
    public ChatGroupResponse createGroup(CreateGroupRequest request) {
        Long userId = getCurrentUserId();

        boolean isPrivate = "private".equalsIgnoreCase(request.getGroupType());
        int targetMemberCount = 1 + (request.getMemberIds() != null ? request.getMemberIds().size() : 0);
        if (isPrivate && targetMemberCount > 2) {
            throw new IllegalArgumentException("私聊最多只能有2个成员");
        }

        ChatGroup group = new ChatGroup();
        String name = request.getGroupName();
        if (!StringUtils.hasText(name)) {
            name = "未命名群组";
        }
        group.setGroupName(name.trim());
        group.setGroupAvatar(null);
        group.setOwnerId(userId);
        group.setGroupType(isPrivate ? ChatGroup.GROUP_TYPE_PRIVATE : ChatGroup.GROUP_TYPE_GROUP);
        group.setIsPublic("private".equalsIgnoreCase(request.getIsPublic()) ? ChatGroup.PUBLIC_NO : ChatGroup.PUBLIC_YES);

        int inserted = chatGroupMapper.insertGroup(group);
        if (inserted <= 0 || group.getGroupId() == null) {
            throw new IllegalStateException("Failed to create group.");
        }

        GroupMember ownerMember = new GroupMember();
        ownerMember.setGroupId(group.getGroupId());
        ownerMember.setUserId(userId);
        ownerMember.setRole(GroupMember.ROLE_OWNER);
        chatGroupMapper.insertMember(ownerMember);

        if (request.getMemberIds() != null) {
            for (Long memberId : request.getMemberIds()) {
                if (memberId.equals(userId)) continue;
                GroupMember member = new GroupMember();
                member.setGroupId(group.getGroupId());
                member.setUserId(memberId);
                member.setRole(GroupMember.ROLE_MEMBER);
                chatGroupMapper.insertMember(member);
            }
        }

        return chatGroupMapper.findGroupsByUserId(userId).stream()
                .filter(g -> g.getGroupId().equals(group.getGroupId()))
                .findFirst()
                .orElse(null);
    }

    public List<ChatGroupResponse> getMyGroups() {
        Long userId = getCurrentUserId();
        return chatGroupMapper.findGroupsByUserId(userId);
    }

    public List<ChatGroupResponse> getPublicGroups() {
        Long userId = getCurrentUserId();
        return chatGroupMapper.findPublicGroups(userId);
    }

    public List<ChatGroupResponse> searchGroups(String keyword) {
        Long userId = getCurrentUserId();
        if (!StringUtils.hasText(keyword)) {
            return List.of();
        }
        return chatGroupMapper.searchGroups(keyword.trim(), userId);
    }

    public ChatGroupResponse getGroup(Long groupId) {
        Long userId = getCurrentUserId();
        GroupMember member = chatGroupMapper.findMember(groupId, userId);
        if (member == null) {
            throw new IllegalArgumentException("You are not a member of this group.");
        }
        return chatGroupMapper.findGroupsByUserId(userId).stream()
                .filter(g -> g.getGroupId().equals(groupId))
                .findFirst()
                .orElse(null);
    }

    @Transactional(rollbackFor = Exception.class)
    public void joinGroup(Long groupId) {
        Long userId = getCurrentUserId();
        ChatGroup group = chatGroupMapper.findGroupById(groupId);
        if (group == null) {
            throw new IllegalArgumentException("Group not found.");
        }
        if (group.getIsPublic() == ChatGroup.PUBLIC_NO) {
            throw new IllegalArgumentException("This group is private and cannot be joined directly.");
        }
        if (group.getGroupType() == ChatGroup.GROUP_TYPE_PRIVATE) {
            int count = chatGroupMapper.countMembers(groupId);
            if (count >= 2) {
                throw new IllegalArgumentException("私聊人数已满（最多2人）");
            }
        }
        GroupMember exists = chatGroupMapper.findMember(groupId, userId);
        if (exists != null) {
            throw new IllegalArgumentException("You are already a member of this group.");
        }
        int banned = chatGroupMapper.isUserBanned(groupId, userId);
        if (banned > 0) {
            throw new IllegalArgumentException("You are banned from this group.");
        }
        GroupMember member = new GroupMember();
        member.setGroupId(groupId);
        member.setUserId(userId);
        member.setRole(GroupMember.ROLE_MEMBER);
        chatGroupMapper.insertMember(member);
    }

    @Transactional(rollbackFor = Exception.class)
    public void leaveGroup(Long groupId) {
        Long userId = getCurrentUserId();
        ChatGroup group = chatGroupMapper.findGroupById(groupId);
        if (group == null) {
            throw new IllegalArgumentException("Group not found.");
        }
        GroupMember member = chatGroupMapper.findMember(groupId, userId);
        if (member == null) {
            throw new IllegalArgumentException("You are not a member of this group.");
        }
        if (member.getRole() == GroupMember.ROLE_OWNER) {
            throw new IllegalArgumentException("Group owner cannot leave. Transfer ownership first or dismiss the group.");
        }
        chatGroupMapper.removeMember(groupId, userId);
    }

    public List<GroupMemberResponse> getGroupMembers(Long groupId) {
        Long userId = getCurrentUserId();
        GroupMember member = chatGroupMapper.findMember(groupId, userId);
        if (member == null) {
            throw new IllegalArgumentException("You are not a member of this group.");
        }
        return chatGroupMapper.findMembersByGroupId(groupId);
    }

    public void renameGroup(Long groupId, String newName) {
        Long userId = getCurrentUserId();
        ChatGroup group = chatGroupMapper.findGroupById(groupId);
        if (group == null) {
            throw new IllegalArgumentException("Group not found.");
        }
        GroupMember member = chatGroupMapper.findMember(groupId, userId);
        if (member == null) {
            throw new IllegalArgumentException("You are not a member of this group.");
        }
        if (member.getRole() != GroupMember.ROLE_OWNER && member.getRole() != GroupMember.ROLE_ADMIN) {
            throw new IllegalArgumentException("Only group owner or admin can rename the group.");
        }
        if (!StringUtils.hasText(newName)) {
            newName = "未命名群组";
        }
        chatGroupMapper.updateGroupName(groupId, newName.trim());
    }

    @Transactional(rollbackFor = Exception.class)
    public void updateGroupSettings(Long groupId, Short groupType, Short isPublic) {
        Long userId = getCurrentUserId();
        ChatGroup group = chatGroupMapper.findGroupById(groupId);
        if (group == null) {
            throw new IllegalArgumentException("Group not found.");
        }
        GroupMember member = chatGroupMapper.findMember(groupId, userId);
        if (member == null) {
            throw new IllegalArgumentException("You are not a member of this group.");
        }
        if (member.getRole() != GroupMember.ROLE_OWNER) {
            throw new IllegalArgumentException("Only group owner can change group settings.");
        }
        if (groupType != null && groupType == ChatGroup.GROUP_TYPE_PRIVATE) {
            int count = chatGroupMapper.countMembers(groupId);
            if (count > 2) {
                throw new IllegalArgumentException("群组人数超过2人，无法设置为私聊");
            }
        }
        chatGroupMapper.updateGroupSettings(groupId, groupType, isPublic);
    }

    public List<ChatMessageResponse> getGroupMessages(Long groupId, int page, int size) {
        Long userId = getCurrentUserId();
        GroupMember member = chatGroupMapper.findMember(groupId, userId);
        if (member == null) {
            throw new IllegalArgumentException("You are not a member of this group.");
        }
        int safePage = Math.max(page, 1);
        int safeSize = Math.max(size, 1);
        int offset = (safePage - 1) * safeSize;

        List<ChatMessageResponse> list = chatMessageMapper.findMessagesByGroupId(groupId, userId, offset, safeSize);
        Collections.reverse(list);
        return list;
    }

    @Transactional(rollbackFor = Exception.class)
    public ChatMessageResponse sendMessage(Long groupId, SendMessageRequest request) {
        Long userId = getCurrentUserId();
        GroupMember member = chatGroupMapper.findMember(groupId, userId);
        if (member == null) {
            throw new IllegalArgumentException("You are not a member of this group.");
        }
        int banned = chatGroupMapper.isUserBanned(groupId, userId);
        if (banned > 0) {
            throw new IllegalArgumentException("You are banned from this group.");
        }
        Short messageType = request.getMessageType() != null ? request.getMessageType().shortValue() : ChatMessage.TYPE_TEXT;
        if (messageType < 1 || messageType > 3) {
            messageType = ChatMessage.TYPE_TEXT;
        }

        ChatMessage message = new ChatMessage();
        message.setGroupId(groupId);
        message.setSenderId(userId);
        message.setMessageType(messageType);
        message.setContent(request.getContent());
        message.setFileUrl(request.getFileUrl());
        message.setFileName(request.getFileName());
        message.setFileSize(request.getFileSize());
        message.setIsDeleted(ChatMessage.DELETED_NO);

        int inserted = chatMessageMapper.insertMessage(message);
        if (inserted <= 0 || message.getMessageId() == null) {
            throw new IllegalStateException("Failed to send message.");
        }

        return chatMessageMapper.findMessagesByGroupId(groupId, userId, 0, 1).stream().findFirst().orElse(null);
    }

    @Transactional(rollbackFor = Exception.class)
    public void deleteMessage(Long messageId) {
        Long userId = getCurrentUserId();
        ChatMessage message = chatMessageMapper.findMessageById(messageId);
        if (message == null) {
            throw new IllegalArgumentException("Message not found.");
        }
        if (!userId.equals(message.getSenderId())) {
            throw new IllegalArgumentException("You can only delete your own messages.");
        }
        LocalDateTime now = LocalDateTime.now();
        if (message.getCreatedAt() != null && message.getCreatedAt().plusMinutes(1).isAfter(now)) {
            chatMessageMapper.recallMessage(messageId);
        } else {
            chatMessageMapper.softDeleteMessage(messageId);
        }
    }

    @Transactional(rollbackFor = Exception.class)
    public void recallMessage(Long messageId) {
        Long userId = getCurrentUserId();
        ChatMessage message = chatMessageMapper.findMessageById(messageId);
        if (message == null) {
            throw new IllegalArgumentException("Message not found.");
        }
        if (!userId.equals(message.getSenderId())) {
            throw new IllegalArgumentException("You can only recall your own messages.");
        }
        LocalDateTime now = LocalDateTime.now();
        if (message.getCreatedAt() == null || !message.getCreatedAt().plusMinutes(1).isAfter(now)) {
            throw new IllegalArgumentException("Message can only be recalled within 1 minute of sending.");
        }
        chatMessageMapper.recallMessage(messageId);
    }

    public PageResult<ChatGroupResponse> adminGetGroups(int page, int size, String keyword) {
        int safePage = Math.max(page, 1);
        int safeSize = Math.max(size, 1);
        int offset = (safePage - 1) * safeSize;

        List<ChatGroupResponse> list = chatGroupMapper.pageAllGroups(keyword, offset, safeSize);
        long total = chatGroupMapper.countAllGroups(keyword);
        return new PageResult<>(list, total, safePage, safeSize);
    }

    public AdminGroupDetailResponse adminGetGroupDetail(Long groupId) {
        ChatGroupResponse group = chatGroupMapper.findGroupByIdWithMemberCount(groupId);
        if (group == null) {
            throw new IllegalArgumentException("Group not found.");
        }
        List<GroupMemberResponse> members = chatGroupMapper.findMembersByGroupId(groupId);
        long messageCount = chatGroupMapper.countMessagesByGroupId(groupId);

        AdminGroupDetailResponse detail = new AdminGroupDetailResponse();
        detail.setGroup(group);
        detail.setMembers(members);
        detail.setMessageCount(messageCount);
        return detail;
    }

    @Transactional(rollbackFor = Exception.class)
    public void adminDismissGroup(Long groupId) {
        ChatGroup group = chatGroupMapper.findGroupById(groupId);
        if (group == null) {
            throw new IllegalArgumentException("Group not found.");
        }
        chatGroupMapper.deleteAllBanRecords(groupId);
        chatGroupMapper.deleteAllMessages(groupId);
        chatGroupMapper.deleteAllMembers(groupId);
        chatGroupMapper.deleteGroup(groupId);
    }

    @Transactional(rollbackFor = Exception.class)
    public void adminBanUser(Long groupId, BanUserRequest request) {
        ChatGroup group = chatGroupMapper.findGroupById(groupId);
        if (group == null) {
            throw new IllegalArgumentException("Group not found.");
        }
        Long currentUserId = getCurrentUserId();
        GroupMember targetMember = chatGroupMapper.findMember(groupId, request.getUserId());
        if (targetMember != null) {
            chatGroupMapper.removeMember(groupId, request.getUserId());
        }

        GroupBanRecord record = new GroupBanRecord();
        record.setGroupId(groupId);
        record.setUserId(request.getUserId());
        record.setReason(request.getReason());
        record.setBannedAt(LocalDateTime.now());
        if (request.getDuration() != null && request.getDuration() > 0) {
            record.setExpiredAt(LocalDateTime.now().plusMinutes(request.getDuration()));
        } else {
            record.setExpiredAt(null);
        }
        record.setBannedBy(currentUserId);
        chatGroupMapper.insertBanRecord(record);
    }

    @Transactional(rollbackFor = Exception.class)
    public void adminSetMemberRole(Long groupId, SetMemberRoleRequest request) {
        ChatGroup group = chatGroupMapper.findGroupById(groupId);
        if (group == null) {
            throw new IllegalArgumentException("Group not found.");
        }
        if (request.getRole() == null || request.getRole() < 1 || request.getRole() > 3) {
            throw new IllegalArgumentException("Invalid role. Must be 1 (member), 2 (admin), or 3 (owner).");
        }
        GroupMember member = chatGroupMapper.findMember(groupId, request.getUserId());
        if (member == null) {
            throw new IllegalArgumentException("User is not a member of this group.");
        }
        chatGroupMapper.updateMemberRole(groupId, request.getUserId(), request.getRole());
    }

    public PageResult<ChatMessageResponse> adminGetMessages(int page, int size, String keyword, Long groupId, Long userId) {
        int safePage = Math.max(page, 1);
        int safeSize = Math.max(size, 1);
        int offset = (safePage - 1) * safeSize;

        List<ChatMessageResponse> list = chatMessageMapper.pageAllMessages(keyword, groupId, userId, offset, safeSize);
        long total = chatMessageMapper.countAllMessages(keyword, groupId, userId);
        return new PageResult<>(list, total, safePage, safeSize);
    }

    @Transactional(rollbackFor = Exception.class)
    public void adminDeleteMessage(Long messageId) {
        ChatMessage message = chatMessageMapper.findMessageById(messageId);
        if (message == null) {
            throw new IllegalArgumentException("Message not found.");
        }
        chatMessageMapper.deleteMessage(messageId);
    }

    @Transactional(rollbackFor = Exception.class)
    public void applyJoinGroup(Long groupId, String reason) {
        Long userId = getCurrentUserId();
        ChatGroup group = chatGroupMapper.findGroupById(groupId);
        if (group == null) {
            throw new IllegalArgumentException("Group not found.");
        }
        GroupMember member = chatGroupMapper.findMember(groupId, userId);
        if (member != null) {
            throw new IllegalArgumentException("You are already a member of this group.");
        }
        int banned = chatGroupMapper.isUserBanned(groupId, userId);
        if (banned > 0) {
            throw new IllegalArgumentException("You are banned from this group.");
        }
        GroupJoinRequest pending = groupJoinRequestMapper.findPendingRequest(groupId, userId);
        if (pending != null) {
            throw new IllegalArgumentException("You already have a pending request for this group.");
        }
        GroupJoinRequest existing = groupJoinRequestMapper.findByGroupIdAndUserId(groupId, userId);
        if (existing != null) {
            groupJoinRequestMapper.updateRequest(existing.getId(), GroupJoinRequest.STATUS_PENDING, reason);
            return;
        }
        GroupJoinRequest request = new GroupJoinRequest();
        request.setGroupId(groupId);
        request.setUserId(userId);
        request.setStatus(GroupJoinRequest.STATUS_PENDING);
        request.setReason(reason);
        groupJoinRequestMapper.insertRequest(request);
    }

    public List<GroupJoinRequestResponse> getPendingJoinRequests(Long groupId) {
        Long userId = getCurrentUserId();
        ChatGroup group = chatGroupMapper.findGroupById(groupId);
        if (group == null) {
            throw new IllegalArgumentException("Group not found.");
        }
        GroupMember member = chatGroupMapper.findMember(groupId, userId);
        if (member == null || (member.getRole() != GroupMember.ROLE_OWNER && member.getRole() != GroupMember.ROLE_ADMIN)) {
            throw new IllegalArgumentException("Only group owner or admin can view join requests.");
        }
        return groupJoinRequestMapper.findRequestsByGroupId(groupId);
    }

    @Transactional(rollbackFor = Exception.class)
    public void approveJoinRequest(Long groupId, Long requestId) {
        Long userId = getCurrentUserId();
        ChatGroup group = chatGroupMapper.findGroupById(groupId);
        if (group == null) {
            throw new IllegalArgumentException("Group not found.");
        }
        if (group.getGroupType() == ChatGroup.GROUP_TYPE_PRIVATE) {
            int count = chatGroupMapper.countMembers(groupId);
            if (count >= 2) {
                throw new IllegalArgumentException("私聊人数已满（最多2人）");
            }
        }
        GroupMember member = chatGroupMapper.findMember(groupId, userId);
        if (member == null || (member.getRole() != GroupMember.ROLE_OWNER && member.getRole() != GroupMember.ROLE_ADMIN)) {
            throw new IllegalArgumentException("Only group owner or admin can approve join requests.");
        }
        GroupJoinRequest request = groupJoinRequestMapper.findRequestById(requestId);
        if (request == null || !request.getGroupId().equals(groupId)) {
            throw new IllegalArgumentException("Join request not found.");
        }
        if (request.getStatus() != GroupJoinRequest.STATUS_PENDING) {
            throw new IllegalArgumentException("This request has already been processed.");
        }
        groupJoinRequestMapper.updateRequestStatus(requestId, GroupJoinRequest.STATUS_APPROVED);
        GroupMember newMember = new GroupMember();
        newMember.setGroupId(groupId);
        newMember.setUserId(request.getUserId());
        newMember.setRole(GroupMember.ROLE_MEMBER);
        chatGroupMapper.insertMember(newMember);
    }

    @Transactional(rollbackFor = Exception.class)
    public void rejectJoinRequest(Long groupId, Long requestId) {
        Long userId = getCurrentUserId();
        ChatGroup group = chatGroupMapper.findGroupById(groupId);
        if (group == null) {
            throw new IllegalArgumentException("Group not found.");
        }
        GroupMember member = chatGroupMapper.findMember(groupId, userId);
        if (member == null || (member.getRole() != GroupMember.ROLE_OWNER && member.getRole() != GroupMember.ROLE_ADMIN)) {
            throw new IllegalArgumentException("Only group owner or admin can reject join requests.");
        }
        GroupJoinRequest request = groupJoinRequestMapper.findRequestById(requestId);
        if (request == null || !request.getGroupId().equals(groupId)) {
            throw new IllegalArgumentException("Join request not found.");
        }
        if (request.getStatus() != GroupJoinRequest.STATUS_PENDING) {
            throw new IllegalArgumentException("This request has already been processed.");
        }
        groupJoinRequestMapper.updateRequestStatus(requestId, GroupJoinRequest.STATUS_REJECTED);
    }

    public List<GroupJoinRequestResponse> getMyJoinRequests() {
        Long userId = getCurrentUserId();
        return groupJoinRequestMapper.findRequestsByUserId(userId);
    }
}
