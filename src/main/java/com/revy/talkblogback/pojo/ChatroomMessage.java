package com.revy.talkblogback.pojo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ChatroomMessage {
    private Long messageId;
    private Long chatroomId;
    private Long userId;
    private String content;
    private String messageType;
    private Boolean isRecalled;
    private Boolean isPinned;
    private LocalDateTime createdAt;
}