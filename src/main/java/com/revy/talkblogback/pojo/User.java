package com.revy.talkblogback.pojo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class User {
    private Long userId;
    private String username;
    private String email;
    private String phone;
    private String passwordHash;
    private Long faceVectorId;
    private String loginType;
    private String avatarUrl;
    private Short status;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private LocalDateTime lastLoginTime;

    // 状态常量
    public static final short STATUS_DISABLED = 0;
    public static final short STATUS_NORMAL = 1;
    public static final short STATUS_FROZEN = 2;

    // 登录类型常量
    public static final String LOGIN_TYPE_EMAIL = "EMAIL";
    public static final String LOGIN_TYPE_PHONE = "PHONE";
    public static final String LOGIN_TYPE_FACE = "FACE";

}
