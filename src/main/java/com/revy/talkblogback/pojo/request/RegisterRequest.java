package com.revy.talkblogback.pojo.request;

import com.revy.talkblogback.pojo.User;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 注册请求模型。
 * 前端必须传入邮箱、用户名和密码。
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class RegisterRequest {

    /**
     * 用户邮箱，作为登录唯一标识。
     */
    private String email;

    /**
     * 用户名，用于展示。
     */
    private String username;

    /**
     * 原始密码，服务层会进行加密存储。
     */
    private String password;

    /**
     * 注册时上传的人脸图片 base64。
     */
    private String image;

    /**
     * 转为 User 实体供服务层处理。
     * 复用 passwordHash 字段承载原始密码输入，后续再加密。
     *
     * @return User 实体
     */
    public User toUser() {
        User user = new User();
        user.setEmail(this.email);
        user.setUsername(this.username);
        user.setPasswordHash(this.password);
        return user;
    }
}