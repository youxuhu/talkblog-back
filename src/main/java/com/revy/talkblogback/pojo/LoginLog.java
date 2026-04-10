package com.revy.talkblogback.pojo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class LoginLog {
    private Long logId;
    private Long userId;
    private String loginType;
    private String idAddress;
    private String deviceInfo;
    private Short loginResult;
    private LocalDateTime loginTime;

    public static final short LOGIN_FAIL = 0;
    public static final short LOGIN_SUCCESS = 1;
}
