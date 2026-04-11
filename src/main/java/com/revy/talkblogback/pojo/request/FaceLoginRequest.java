package com.revy.talkblogback.pojo.request;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 人脸登录请求模型。
 * 前端必须传入邮箱与图片 base64。
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class FaceLoginRequest {

    /**
     * 用户邮箱。
     */
    private String email;

    /**
     * 人脸图片 base64 字符串。
     */
    private String image;
}