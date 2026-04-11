package com.revy.talkblogback.service;

import com.revy.talkblogback.pojo.User;

/**
 * 登录注册服务接口。
 */
public interface LoginService {

    /**
     * 邮箱注册。
     *
     * @param user      用户对象（包含邮箱、用户名、密码）
     * @param faceImage 注册时的人脸图片 base64
     * @return 是否注册成功
     */
    boolean register(User user, String faceImage);

    /**
     * 邮箱 + 人脸登录。
     *
     * @param email     邮箱
     * @param faceImage 人脸图片 base64
     * @return 是否登录成功
     */
    boolean login(String email, String faceImage);
}
