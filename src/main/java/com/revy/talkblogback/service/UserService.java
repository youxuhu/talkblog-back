package com.revy.talkblogback.service;

import com.revy.talkblogback.config.FaceServiceProperties;
import com.revy.talkblogback.mapper.FaceVectorMapper;
import com.revy.talkblogback.mapper.LoginMapper;
import com.revy.talkblogback.pojo.FaceVector;
import com.revy.talkblogback.pojo.User;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

/**
 * 服务层，用于处理用户相关的业务逻辑。
 */
@Service
public class UserService {

    /**
     * 人脸登录通过阈值，使用余弦距离，值越小越相似。
     */
    private static final double FACE_LOGIN_THRESHOLD = 0.35d;

    private final LoginMapper loginMapper;
    private final FaceVectorMapper faceVectorMapper;
    private final RestTemplate restTemplate;
    private final BCryptPasswordEncoder passwordEncoder;
    private final FaceServiceProperties faceServiceProperties;

    public UserService(
            LoginMapper loginMapper,
            FaceVectorMapper faceVectorMapper,
            RestTemplate restTemplate,
            FaceServiceProperties faceServiceProperties) {
        this.loginMapper = loginMapper;
        this.faceVectorMapper = faceVectorMapper;
        this.restTemplate = restTemplate;
        this.faceServiceProperties = faceServiceProperties;
        this.passwordEncoder = new BCryptPasswordEncoder();
    }

    /**
     * 用户注册逻辑。
     * 注册使用邮箱，必须提供用户名和密码。
     *
     * @param user 用户信息
     * @return 注册是否成功
     */
    @Transactional(rollbackFor = Exception.class)
    public boolean register(User user, String faceImage) {
        if (user == null) {
            throw new IllegalArgumentException("Request body is required.");
        }
        if (!StringUtils.hasText(user.getEmail())) {
            throw new IllegalArgumentException("Email is required for registration.");
        }
        if (!StringUtils.hasText(user.getUsername())) {
            throw new IllegalArgumentException("Username is required for registration.");
        }
        if (!StringUtils.hasText(user.getPasswordHash())) {
            throw new IllegalArgumentException("Password is required for registration.");
        }
        if (!StringUtils.hasText(faceImage)) {
            throw new IllegalArgumentException("Face image is required for registration.");
        }

        User exists = loginMapper.findUserByEmail(user.getEmail());
        if (exists != null) {
            return false;
        }

        User toSave = new User();
        toSave.setEmail(user.getEmail().trim().toLowerCase());
        toSave.setUsername(user.getUsername().trim());
        toSave.setPasswordHash(passwordEncoder.encode(user.getPasswordHash()));
        toSave.setStatus(User.STATUS_NORMAL);
        toSave.setLoginType(User.LOGIN_TYPE_EMAIL);

        int inserted = loginMapper.insertUser(toSave);
        if (inserted <= 0 || toSave.getUserId() == null) {
            return false;
        }

        float[] vector = extractFaceVector(faceImage);
        if (vector == null || vector.length != 512) {
            throw new IllegalArgumentException("Face image is invalid or no single face detected.");
        }

        FaceVector faceVector = new FaceVector();
        faceVector.setUserId(toSave.getUserId());
        faceVector.setFaceVector(vector);
        faceVector.setFaceImageUrl(null);
        faceVectorMapper.insertFaceVector(faceVector);
        if (faceVector.getVectorId() == null) {
            throw new IllegalStateException("Failed to create face vector record.");
        }

        int bound = loginMapper.bindFaceVector(toSave.getUserId(), faceVector.getVectorId());
        if (bound <= 0) {
            throw new IllegalStateException("Failed to bind face vector.");
        }

        loginMapper.assignDefaultUserRole(toSave.getUserId());

        return true;
    }

    /**
     * 人脸登录逻辑。
     * 登录时必须提供邮箱与人脸图片（base64）。
     *
     * @param email     用户邮箱
     * @param faceImage 人脸图片 base64
     * @return 登录是否成功
     */
    public boolean login(String email, String faceImage) {
        if (!StringUtils.hasText(email)) {
            throw new IllegalArgumentException("Email is required for login.");
        }
        if (!StringUtils.hasText(faceImage)) {
            throw new IllegalArgumentException("Face image is required for login.");
        }

        User user = loginMapper.findUserByEmail(email.trim().toLowerCase());
        if (user == null) {
            return false;
        }
        if (user.getStatus() == null || user.getStatus() != User.STATUS_NORMAL) {
            return false;
        }

        float[] queryVector = extractFaceVector(faceImage);
        if (queryVector == null || queryVector.length == 0) {
            return false;
        }

        Double distance = loginMapper.computeCosineDistanceByUserId(user.getUserId(), queryVector);
        if (distance == null || distance > FACE_LOGIN_THRESHOLD) {
            return false;
        }

        loginMapper.updateLastLoginTime(user.getUserId());
        return true;
    }

    /**
     * 调用 Python 人脸向量化服务，提取 512 维向量。
     *
     * @param imageBase64 图片 base64
     * @return 人脸向量
     */
    private float[] extractFaceVector(String imageBase64) {
        String url = faceServiceProperties.getUrl() + faceServiceProperties.getExtractEndpoint();

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);

        Map<String, Object> body = new HashMap<>();
        body.put("image", imageBase64);

        try {
            ResponseEntity<Map<String, Object>> response = restTemplate.exchange(
                    url,
                    Objects.requireNonNull(HttpMethod.POST),
                    new HttpEntity<>(body, headers),
                    new ParameterizedTypeReference<>() {
                    });
            Map<String, Object> responseBody = response.getBody();
            if (responseBody == null) {
                return null;
            }

            Object success = responseBody.get("success");
            if (!(success instanceof Boolean) || !((Boolean) success)) {
                return null;
            }

            Object vectorObj = responseBody.get("face_vector");
            if (!(vectorObj instanceof List<?> vectorList) || vectorList.isEmpty()) {
                return null;
            }

            float[] vector = new float[vectorList.size()];
            for (int i = 0; i < vectorList.size(); i++) {
                Object val = vectorList.get(i);
                if (!(val instanceof Number)) {
                    return null;
                }
                vector[i] = ((Number) val).floatValue();
            }

            return vector;
        } catch (RestClientException ex) {
            return null;
        }
    }
}