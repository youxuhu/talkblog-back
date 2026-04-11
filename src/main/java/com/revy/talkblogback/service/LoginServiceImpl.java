package com.revy.talkblogback.service;

import com.revy.talkblogback.pojo.User;
import org.springframework.stereotype.Service;

/**
 * 登录注册服务实现。
 * 当前作为门面层，复用 UserService 中的核心实现。
 */
@Service
public class LoginServiceImpl implements LoginService {

    private final UserService userService;

    public LoginServiceImpl(UserService userService) {
        this.userService = userService;
    }

    @Override
    public boolean register(User user, String faceImage) {
        return userService.register(user, faceImage);
    }

    @Override
    public boolean login(String email, String faceImage) {
        return userService.login(email, faceImage);
    }
}
