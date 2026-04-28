package com.revy.talkblogback.pojo.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class UserPageRow {

    private Long userId;
    private String username;
    private String email;
    private String phone;
    private String avatarUrl;
    private Short status;
    private String loginType;
    private LocalDateTime lastLoginTime;
    private String roleNames;
}
