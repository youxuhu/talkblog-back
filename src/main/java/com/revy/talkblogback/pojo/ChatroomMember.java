package com.revy.talkblogback.pojo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ChatroomMember {
    private Long memberId;
    private Long chatroomId;
    private Long userId;
    private String role;
    private LocalDateTime joinedAt;
    private LocalDateTime lastActiveTime;
    private Integer messageCount;
    private LocalDateTime mutedUntil;

    public static final String ROLE_OWNER = "OWNER";
    public static final String ROLE_ADMIN = "ADMIN";
    public static final String ROLE_MEMBER = "MEMBER";
}