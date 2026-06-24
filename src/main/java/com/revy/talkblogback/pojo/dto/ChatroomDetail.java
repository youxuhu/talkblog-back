package com.revy.talkblogback.pojo.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ChatroomDetail {
    private Long chatroomId;
    private String name;
    private String description;
    private Integer memberCount;
    private Integer messageCount;
    private Integer maxMembers;
    private String status;
    private Boolean isPrivate;
    private java.time.LocalDateTime createdAt;
    private java.time.LocalDateTime updatedAt;
    private List<DailyStatRow> dailyStats;
}