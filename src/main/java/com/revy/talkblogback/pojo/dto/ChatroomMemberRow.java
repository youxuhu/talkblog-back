package com.revy.talkblogback.pojo.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ChatroomMemberRow {
    private Long memberId;
    private Long userId;
    private String username;
    private String nickname;
    private String avatarUrl;
    private String role;
    private LocalDateTime joinedAt;
    private LocalDateTime lastActiveTime;
    private Integer messageCount;
    private LocalDateTime mutedUntil;
}