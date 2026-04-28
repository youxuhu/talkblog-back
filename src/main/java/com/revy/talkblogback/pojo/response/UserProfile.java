package com.revy.talkblogback.pojo.response;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class UserProfile {

    private Long userId;
    private String username;
    private String email;
    private String phone;
    private String avatarUrl;
    private Short status;
    private String loginType;
    private LocalDateTime lastLoginTime;
    private List<String> roles;
}
