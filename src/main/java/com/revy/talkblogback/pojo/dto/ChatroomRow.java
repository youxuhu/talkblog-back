package com.revy.talkblogback.pojo.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ChatroomRow {
    private Long chatroomId;
    private String name;
    private String description;
    private Integer memberCount;
    private Integer messageCount;
    private Integer maxMembers;
    private String status;
    private Boolean isPrivate;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}