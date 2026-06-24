package com.revy.talkblogback.pojo.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ChatroomStats {
    private long totalChatrooms;
    private long totalMembers;
    private long totalMessages;
    private long activeChatrooms;
    private long todayMessages;
    private double avgMembersPerChatroom;
}