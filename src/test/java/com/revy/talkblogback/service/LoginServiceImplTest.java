package com.revy.talkblogback.service;

import com.revy.talkblogback.pojo.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * LoginServiceImpl 单元测试。
 * 验证门面层是否正确委托到 UserService。
 */
class LoginServiceImplTest {

    private UserService userService;
    private LoginServiceImpl loginService;

    @BeforeEach
    void setUp() {
        userService = mock(UserService.class);
        loginService = new LoginServiceImpl(userService);
    }

    /**
     * 注册委托测试。
     */
    @Test
    void register_shouldDelegateToUserService() {
        User user = new User();
        when(userService.register(user, "img")).thenReturn(true);

        boolean ok = loginService.register(user, "img");

        assertTrue(ok);
        verify(userService).register(user, "img");
    }

    /**
     * 登录委托测试。
     */
    @Test
    void login_shouldDelegateToUserService() {
        when(userService.login("demo@test.com", "img")).thenReturn(false);

        boolean ok = loginService.login("demo@test.com", "img");

        assertFalse(ok);
        verify(userService).login("demo@test.com", "img");
    }
}
