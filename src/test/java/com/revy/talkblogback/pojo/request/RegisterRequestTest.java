package com.revy.talkblogback.pojo.request;

import com.revy.talkblogback.pojo.User;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * RegisterRequest 单元测试。
 * 验证请求对象到 User 实体的转换。
 */
class RegisterRequestTest {

    /**
     * toUser 应正确映射邮箱、用户名、密码。
     */
    @Test
    void toUser_shouldMapCoreFields() {
        RegisterRequest request = new RegisterRequest("demo@test.com", "demo", "123456", "img");

        User user = request.toUser();

        assertEquals("demo@test.com", user.getEmail());
        assertEquals("demo", user.getUsername());
        assertEquals("123456", user.getPasswordHash());
    }
}
