package com.revy.talkblogback.mapper;

import com.revy.talkblogback.pojo.ChatMessage;
import com.revy.talkblogback.pojo.response.ChatMessageResponse;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface ChatMessageMapper {

    int insertMessage(ChatMessage message);

    List<ChatMessageResponse> findMessagesByGroupId(@Param("groupId") Long groupId, @Param("userId") Long userId, @Param("offset") int offset, @Param("limit") int limit);

    ChatMessage findMessageById(@Param("messageId") Long messageId);

    int deleteMessage(@Param("messageId") Long messageId);

    int softDeleteMessage(@Param("messageId") Long messageId);

    int recallMessage(@Param("messageId") Long messageId);

    List<ChatMessageResponse> pageAllMessages(@Param("keyword") String keyword, @Param("groupId") Long groupId, @Param("userId") Long userId, @Param("offset") int offset, @Param("limit") int limit);

    long countAllMessages(@Param("keyword") String keyword, @Param("groupId") Long groupId, @Param("userId") Long userId);
}
