package com.revy.talkblogback.pojo.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class MessageRow {
    private Long messageId;
    private Long chatroomId;
    private Long userId;
    private String username;
    private String content;
    private Boolean isRecalled;
    private Boolean isPinned;
    private LocalDateTime createdAt;
}
