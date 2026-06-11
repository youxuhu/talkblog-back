package com.revy.talkblogback.mapper;

import com.revy.talkblogback.pojo.Chatroom;
import com.revy.talkblogback.pojo.ChatroomMessage;
import com.revy.talkblogback.pojo.dto.ChatroomMemberRow;
import com.revy.talkblogback.pojo.dto.ChatroomRow;
import com.revy.talkblogback.pojo.dto.DailyStatRow;
import com.revy.talkblogback.pojo.dto.MessageRow;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.time.LocalDateTime;
import java.util.List;

@Mapper
public interface ChatroomMapper {

    List<ChatroomRow> pageChatrooms(@Param("keyword") String keyword, @Param("offset") int offset, @Param("limit") int limit);

    List<ChatroomRow> pageMyChatrooms(@Param("userId") Long userId, @Param("offset") int offset, @Param("limit") int limit);

    long countChatrooms(@Param("keyword") String keyword);

    ChatroomRow findChatroomById(@Param("chatroomId") Long chatroomId);

    int insertChatroom(Chatroom chatroom);

    int updateChatroom(Chatroom chatroom);

    int deleteChatroom(@Param("chatroomId") Long chatroomId);

    List<ChatroomMemberRow> pageChatroomMembers(@Param("chatroomId") Long chatroomId, @Param("keyword") String keyword, @Param("offset") int offset, @Param("limit") int limit);

    long countChatroomMembers(@Param("chatroomId") Long chatroomId, @Param("keyword") String keyword);

    int insertMember(@Param("chatroomId") Long chatroomId, @Param("userId") Long userId, @Param("role") String role);

    int deleteMember(@Param("chatroomId") Long chatroomId, @Param("userId") Long userId);

    int updateMemberRole(@Param("chatroomId") Long chatroomId, @Param("userId") Long userId, @Param("role") String role);

    int countMembersByChatroomId(@Param("chatroomId") Long chatroomId);

    int countMemberByChatroomAndUser(@Param("chatroomId") Long chatroomId, @Param("userId") Long userId);

    long countTotalChatrooms();

    long countTotalMembers();

    long countTotalMessages();

    long countActiveChatrooms();

    long countTodayMessages();

    double avgMembersPerChatroom();

    List<DailyStatRow> selectDailyStats(@Param("chatroomId") Long chatroomId, @Param("startDate") String startDate, @Param("endDate") String endDate);

    List<MessageRow> pageMessages(@Param("chatroomId") Long chatroomId, @Param("offset") int offset, @Param("limit") int limit);

    long countMessages(@Param("chatroomId") Long chatroomId);

    int insertMessage(ChatroomMessage message);

    int updateMemberLastActive(@Param("chatroomId") Long chatroomId, @Param("userId") Long userId);

    ChatroomMessage findMessageById(@Param("messageId") Long messageId);

    int recallMessage(@Param("messageId") Long messageId);

    int updatePinStatus(@Param("messageId") Long messageId, @Param("isPinned") boolean isPinned);

    int deleteMessageById(@Param("messageId") Long messageId);

    String findMemberRole(@Param("chatroomId") Long chatroomId, @Param("userId") Long userId);

    int updateMutedUntil(@Param("chatroomId") Long chatroomId, @Param("userId") Long userId, @Param("mutedUntil") LocalDateTime mutedUntil);

    LocalDateTime findMutedUntil(@Param("chatroomId") Long chatroomId, @Param("userId") Long userId);
}