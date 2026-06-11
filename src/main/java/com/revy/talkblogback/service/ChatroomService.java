package com.revy.talkblogback.service;

import com.revy.talkblogback.mapper.ChatroomMapper;
import com.revy.talkblogback.mapper.LoginMapper;
import com.revy.talkblogback.pojo.Chatroom;
import com.revy.talkblogback.pojo.ChatroomMember;
import com.revy.talkblogback.pojo.ChatroomMessage;
import com.revy.talkblogback.pojo.User;
import com.revy.talkblogback.pojo.dto.ChatroomMemberRow;
import com.revy.talkblogback.pojo.dto.ChatroomRow;
import com.revy.talkblogback.pojo.dto.ChatroomStats;
import com.revy.talkblogback.pojo.dto.DailyStatRow;
import com.revy.talkblogback.pojo.dto.MessageRow;
import com.revy.talkblogback.pojo.request.AddMemberRequest;
import com.revy.talkblogback.pojo.request.CreateChatroomRequest;
import com.revy.talkblogback.pojo.request.UpdateChatroomRequest;
import com.revy.talkblogback.pojo.response.PageResult;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Service
public class ChatroomService {

    private final ChatroomMapper chatroomMapper;
    private final LoginMapper loginMapper;

    public ChatroomService(ChatroomMapper chatroomMapper, LoginMapper loginMapper) {
        this.chatroomMapper = chatroomMapper;
        this.loginMapper = loginMapper;
    }

    public PageResult<ChatroomRow> pageChatrooms(int page, int size, String keyword) {
        int safePage = Math.max(page, 1);
        int safeSize = Math.max(size, 1);
        int offset = (safePage - 1) * safeSize;

        List<ChatroomRow> list = chatroomMapper.pageChatrooms(keyword, offset, safeSize);
        long total = chatroomMapper.countChatrooms(keyword);
        return new PageResult<>(list, total, safePage, safeSize);
    }

    public PageResult<ChatroomRow> pageMyChatrooms(Long userId, int page, int size) {
        if (userId == null) {
            throw new IllegalArgumentException("User id is required.");
        }

        int safePage = Math.max(page, 1);
        int safeSize = Math.max(size, 1);
        int offset = (safePage - 1) * safeSize;

        List<ChatroomRow> list = chatroomMapper.pageMyChatrooms(userId, offset, safeSize);
        long total = list.size();
        return new PageResult<>(list, total, safePage, safeSize);
    }

    public ChatroomRow getChatroomById(Long chatroomId) {
        return chatroomMapper.findChatroomById(chatroomId);
    }

    @Transactional(rollbackFor = Exception.class)
    public Long createChatroom(CreateChatroomRequest request, Long creatorId) {
        if (request == null || !StringUtils.hasText(request.getName())) {
            throw new IllegalArgumentException("Chatroom name is required.");
        }

        Chatroom chatroom = new Chatroom();
        chatroom.setName(request.getName().trim());
        chatroom.setDescription(StringUtils.hasText(request.getDescription()) ? request.getDescription().trim() : null);
        chatroom.setMaxMembers(request.getMaxMembers() != null && request.getMaxMembers() > 0 ? request.getMaxMembers() : 100);
        chatroom.setIsPrivate(request.getIsPrivate() != null ? request.getIsPrivate() : false);
        chatroom.setStatus(Chatroom.STATUS_ACTIVE);
        chatroom.setCreatorId(creatorId);
        chatroom.setCreatedAt(LocalDateTime.now());
        chatroom.setUpdatedAt(LocalDateTime.now());

        chatroomMapper.insertChatroom(chatroom);

        if (creatorId != null) {
            chatroomMapper.insertMember(chatroom.getChatroomId(), creatorId, ChatroomMember.ROLE_OWNER);
        }

        return chatroom.getChatroomId();
    }

    @Transactional(rollbackFor = Exception.class)
    public boolean updateChatroom(Long chatroomId, UpdateChatroomRequest request) {
        if (chatroomId == null) {
            throw new IllegalArgumentException("Chatroom id is required.");
        }

        ChatroomRow existing = chatroomMapper.findChatroomById(chatroomId);
        if (existing == null) {
            return false;
        }

        Chatroom chatroom = new Chatroom();
        chatroom.setChatroomId(chatroomId);
        if (StringUtils.hasText(request.getName())) {
            chatroom.setName(request.getName().trim());
        }
        if (request.getDescription() != null) {
            chatroom.setDescription(request.getDescription().trim());
        }
        if (request.getMaxMembers() != null && request.getMaxMembers() > 0) {
            chatroom.setMaxMembers(request.getMaxMembers());
        }
        if (request.getIsPrivate() != null) {
            chatroom.setIsPrivate(request.getIsPrivate());
        }
        if (StringUtils.hasText(request.getStatus())) {
            chatroom.setStatus(request.getStatus());
        }
        chatroom.setUpdatedAt(LocalDateTime.now());

        return chatroomMapper.updateChatroom(chatroom) > 0;
    }

    @Transactional(rollbackFor = Exception.class)
    public boolean deleteChatroom(Long chatroomId) {
        if (chatroomId == null) {
            throw new IllegalArgumentException("Chatroom id is required.");
        }
        return chatroomMapper.deleteChatroom(chatroomId) > 0;
    }

    public PageResult<ChatroomMemberRow> pageMembers(Long chatroomId, int page, int size, String keyword) {
        if (chatroomId == null) {
            throw new IllegalArgumentException("Chatroom id is required.");
        }

        int safePage = Math.max(page, 1);
        int safeSize = Math.max(size, 1);
        int offset = (safePage - 1) * safeSize;

        List<ChatroomMemberRow> list = chatroomMapper.pageChatroomMembers(chatroomId, keyword, offset, safeSize);
        long total = chatroomMapper.countChatroomMembers(chatroomId, keyword);
        return new PageResult<>(list, total, safePage, safeSize);
    }

    @Transactional(rollbackFor = Exception.class)
    public boolean addMember(Long chatroomId, AddMemberRequest request) {
        if (chatroomId == null) {
            throw new IllegalArgumentException("Chatroom id is required.");
        }
        if (request == null || !StringUtils.hasText(request.getUserId())) {
            throw new IllegalArgumentException("User identifier is required.");
        }

        User user = resolveUser(request.getUserId().trim());
        if (user == null) {
            throw new IllegalArgumentException("User not found: " + request.getUserId());
        }

        if (chatroomMapper.countMemberByChatroomAndUser(chatroomId, user.getUserId()) > 0) {
            throw new IllegalArgumentException("User is already a member of this chatroom.");
        }

        ChatroomRow chatroom = chatroomMapper.findChatroomById(chatroomId);
        if (chatroom == null) {
            throw new IllegalArgumentException("Chatroom not found.");
        }
        if (chatroomMapper.countMembersByChatroomId(chatroomId) >= chatroom.getMaxMembers()) {
            throw new IllegalArgumentException("Chatroom has reached maximum member capacity.");
        }

        String role = StringUtils.hasText(request.getRole()) ? request.getRole() : ChatroomMember.ROLE_MEMBER;
        if (!ChatroomMember.ROLE_OWNER.equals(role) && !ChatroomMember.ROLE_ADMIN.equals(role) && !ChatroomMember.ROLE_MEMBER.equals(role)) {
            role = ChatroomMember.ROLE_MEMBER;
        }

        return chatroomMapper.insertMember(chatroomId, user.getUserId(), role) > 0;
    }

    private User resolveUser(String identifier) {
        if (identifier == null || identifier.isBlank()) return null;

        if (identifier.matches("\\d+")) {
            User user = loginMapper.findUserById(Long.parseLong(identifier));
            if (user != null) return user;
        }

        if (identifier.contains("@")) {
            User user = loginMapper.findUserByEmail(identifier);
            if (user != null) return user;
        }

        return loginMapper.findUserByUsername(identifier);
    }

    @Transactional(rollbackFor = Exception.class)
    public boolean removeMember(Long chatroomId, Long userId) {
        if (chatroomId == null || userId == null) {
            throw new IllegalArgumentException("Chatroom id and user id are required.");
        }
        return chatroomMapper.deleteMember(chatroomId, userId) > 0;
    }

    @Transactional(rollbackFor = Exception.class)
    public boolean updateMemberRole(Long chatroomId, Long userId, String role) {
        if (chatroomId == null || userId == null) {
            throw new IllegalArgumentException("Chatroom id and user id are required.");
        }
        if (!StringUtils.hasText(role)) {
            throw new IllegalArgumentException("Role is required.");
        }
        if (!ChatroomMember.ROLE_OWNER.equals(role) && !ChatroomMember.ROLE_ADMIN.equals(role) && !ChatroomMember.ROLE_MEMBER.equals(role)) {
            throw new IllegalArgumentException("Invalid role.");
        }

        return chatroomMapper.updateMemberRole(chatroomId, userId, role) > 0;
    }

    public PageResult<MessageRow> pageMessages(Long chatroomId, Long userId, int page, int size) {
        if (chatroomId == null) {
            throw new IllegalArgumentException("Chatroom id is required.");
        }

        if (userId != null && chatroomMapper.countMemberByChatroomAndUser(chatroomId, userId) == 0) {
            throw new IllegalArgumentException("You are not a member of this chatroom.");
        }

        int safePage = Math.max(page, 1);
        int safeSize = Math.max(size, 1);
        int offset = (safePage - 1) * safeSize;

        List<MessageRow> list = chatroomMapper.pageMessages(chatroomId, offset, safeSize);
        long total = chatroomMapper.countMessages(chatroomId);
        return new PageResult<>(list, total, safePage, safeSize);
    }

    @Transactional(rollbackFor = Exception.class)
    public Long sendMessage(Long chatroomId, Long userId, String content) {
        if (chatroomId == null) {
            throw new IllegalArgumentException("Chatroom id is required.");
        }
        if (userId == null) {
            throw new IllegalArgumentException("User id is required.");
        }
        if (!StringUtils.hasText(content)) {
            throw new IllegalArgumentException("Message content is required.");
        }

        if (chatroomMapper.countMemberByChatroomAndUser(chatroomId, userId) == 0) {
            throw new IllegalArgumentException("You are not a member of this chatroom.");
        }

        LocalDateTime mutedUntil = chatroomMapper.findMutedUntil(chatroomId, userId);
        if (mutedUntil != null && mutedUntil.isAfter(LocalDateTime.now())) {
            throw new IllegalArgumentException("您已被禁言，无法发送消息");
        }

        ChatroomMessage message = new ChatroomMessage();
        message.setChatroomId(chatroomId);
        message.setUserId(userId);
        message.setContent(content.trim());

        chatroomMapper.insertMessage(message);
        chatroomMapper.updateMemberLastActive(chatroomId, userId);

        return message.getMessageId();
    }

    @Transactional(rollbackFor = Exception.class)
    public void recallMessage(Long chatroomId, Long messageId, Long userId) {
        ChatroomMessage message = chatroomMapper.findMessageById(messageId);
        if (message == null) {
            throw new IllegalArgumentException("消息不存在");
        }
        if (!message.getChatroomId().equals(chatroomId)) {
            throw new IllegalArgumentException("消息不属于该聊天室");
        }

        String role = chatroomMapper.findMemberRole(chatroomId, userId);
        if (role == null) {
            throw new IllegalArgumentException("你不是该聊天室成员");
        }

        boolean isManager = ChatroomMember.ROLE_OWNER.equals(role) || ChatroomMember.ROLE_ADMIN.equals(role);

        if (!isManager) {
            if (!message.getUserId().equals(userId)) {
                throw new IllegalArgumentException("你无权撤回其他人的消息");
            }
            if (Duration.between(message.getCreatedAt(), LocalDateTime.now()).toMinutes() > 5) {
                throw new IllegalArgumentException("消息已超过5分钟，无法撤回");
            }
        }

        chatroomMapper.recallMessage(messageId);
    }

    @Transactional(rollbackFor = Exception.class)
    public void pinMessage(Long chatroomId, Long messageId, Long userId, boolean isPinned) {
        ChatroomMessage message = chatroomMapper.findMessageById(messageId);
        if (message == null) {
            throw new IllegalArgumentException("消息不存在");
        }
        if (!message.getChatroomId().equals(chatroomId)) {
            throw new IllegalArgumentException("消息不属于该聊天室");
        }

        String role = chatroomMapper.findMemberRole(chatroomId, userId);
        if (role == null) {
            throw new IllegalArgumentException("你不是该聊天室成员");
        }

        if (!ChatroomMember.ROLE_OWNER.equals(role) && !ChatroomMember.ROLE_ADMIN.equals(role)) {
            throw new IllegalArgumentException("只有群主和管理员可以操作精华消息");
        }

        chatroomMapper.updatePinStatus(messageId, isPinned);
    }

    @Transactional(rollbackFor = Exception.class)
    public void deleteMessage(Long chatroomId, Long messageId, Long userId) {
        ChatroomMessage message = chatroomMapper.findMessageById(messageId);
        if (message == null) {
            throw new IllegalArgumentException("消息不存在");
        }
        if (!message.getChatroomId().equals(chatroomId)) {
            throw new IllegalArgumentException("消息不属于该聊天室");
        }

        String role = chatroomMapper.findMemberRole(chatroomId, userId);
        if (role == null) {
            throw new IllegalArgumentException("你不是该聊天室成员");
        }

        if (!ChatroomMember.ROLE_OWNER.equals(role) && !ChatroomMember.ROLE_ADMIN.equals(role)) {
            throw new IllegalArgumentException("只有群主和管理员可以删除消息");
        }

        chatroomMapper.deleteMessageById(messageId);
    }

    public String getMemberRole(Long chatroomId, Long userId) {
        return chatroomMapper.findMemberRole(chatroomId, userId);
    }

    public LocalDateTime getMutedUntil(Long chatroomId, Long userId) {
        return chatroomMapper.findMutedUntil(chatroomId, userId);
    }

    @Transactional(rollbackFor = Exception.class)
    public void muteMember(Long chatroomId, Long operatorUserId, Long targetUserId, Integer durationMinutes) {
        if (chatroomId == null || operatorUserId == null || targetUserId == null) {
            throw new IllegalArgumentException("参数不能为空");
        }

        String operatorRole = chatroomMapper.findMemberRole(chatroomId, operatorUserId);
        if (operatorRole == null) {
            throw new IllegalArgumentException("操作者不是该聊天室成员");
        }
        if (!ChatroomMember.ROLE_OWNER.equals(operatorRole) && !ChatroomMember.ROLE_ADMIN.equals(operatorRole)) {
            throw new IllegalArgumentException("只有群主和管理员可以禁言");
        }

        String targetRole = chatroomMapper.findMemberRole(chatroomId, targetUserId);
        if (targetRole == null) {
            throw new IllegalArgumentException("目标用户不是该聊天室成员");
        }
        if (ChatroomMember.ROLE_OWNER.equals(targetRole)) {
            throw new IllegalArgumentException("不能禁言群主");
        }
        if (ChatroomMember.ROLE_ADMIN.equals(operatorRole) && ChatroomMember.ROLE_ADMIN.equals(targetRole)) {
            throw new IllegalArgumentException("管理员不能禁言其他管理员");
        }

        LocalDateTime mutedUntil;
        if (durationMinutes == null || durationMinutes <= 0) {
            mutedUntil = LocalDateTime.now().plusYears(100);
        } else {
            mutedUntil = LocalDateTime.now().plusMinutes(durationMinutes);
        }

        chatroomMapper.updateMutedUntil(chatroomId, targetUserId, mutedUntil);
    }

    @Transactional(rollbackFor = Exception.class)
    public void unmuteMember(Long chatroomId, Long operatorUserId, Long targetUserId) {
        if (chatroomId == null || operatorUserId == null || targetUserId == null) {
            throw new IllegalArgumentException("参数不能为空");
        }

        String operatorRole = chatroomMapper.findMemberRole(chatroomId, operatorUserId);
        if (operatorRole == null) {
            throw new IllegalArgumentException("操作者不是该聊天室成员");
        }
        if (!ChatroomMember.ROLE_OWNER.equals(operatorRole) && !ChatroomMember.ROLE_ADMIN.equals(operatorRole)) {
            throw new IllegalArgumentException("只有群主和管理员可以解除禁言");
        }

        chatroomMapper.updateMutedUntil(chatroomId, targetUserId, null);
    }

    public ChatroomStats getStats() {
        ChatroomStats stats = new ChatroomStats();
        stats.setTotalChatrooms(chatroomMapper.countTotalChatrooms());
        stats.setTotalMembers(chatroomMapper.countTotalMembers());
        stats.setTotalMessages(chatroomMapper.countTotalMessages());
        stats.setActiveChatrooms(chatroomMapper.countActiveChatrooms());
        stats.setTodayMessages(chatroomMapper.countTodayMessages());
        stats.setAvgMembersPerChatroom(chatroomMapper.avgMembersPerChatroom());
        return stats;
    }

    public List<DailyStatRow> getDailyStats(Long chatroomId, String startDate, String endDate) {
        if (chatroomId == null) {
            throw new IllegalArgumentException("Chatroom id is required.");
        }
        return chatroomMapper.selectDailyStats(chatroomId, startDate, endDate);
    }
}