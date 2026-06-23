package com.revy.talkblogback.service;

import com.revy.talkblogback.auth.JwtTokenService;
import com.revy.talkblogback.config.FaceServiceProperties;
import com.revy.talkblogback.mapper.FaceVectorMapper;
import com.revy.talkblogback.mapper.LoginMapper;
import com.revy.talkblogback.pojo.FaceVector;
import com.revy.talkblogback.pojo.dto.UserPageRow;
import com.revy.talkblogback.pojo.User;
import com.revy.talkblogback.pojo.response.AuthResult;
import com.revy.talkblogback.pojo.response.PageResult;
import com.revy.talkblogback.pojo.response.UserProfile;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

import java.util.HashMap;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.time.LocalDateTime;

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
    private final JwtTokenService jwtTokenService;
    private final FileService fileService;

    public UserService(
            LoginMapper loginMapper,
            FaceVectorMapper faceVectorMapper,
            RestTemplate restTemplate,
            FaceServiceProperties faceServiceProperties,
            JwtTokenService jwtTokenService,
            FileService fileService) {
        this.loginMapper = loginMapper;
        this.faceVectorMapper = faceVectorMapper;
        this.restTemplate = restTemplate;
        this.faceServiceProperties = faceServiceProperties;
        this.jwtTokenService = jwtTokenService;
        this.fileService = fileService;
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
     * 密码登录逻辑。
     *
     * @param email    邮箱
     * @param password 原始密码
     * @return 登录结果，失败时返回 null
     */
    public AuthResult loginByPassword(String email, String password) {
        if (!StringUtils.hasText(email)) {
            throw new IllegalArgumentException("Email is required for login.");
        }
        if (!StringUtils.hasText(password)) {
            throw new IllegalArgumentException("Password is required for login.");
        }

        User user = loginMapper.findUserByEmail(email.trim().toLowerCase());
        if (user == null) {
            return null;
        }
        if (user.getStatus() == null || user.getStatus() != User.STATUS_NORMAL) {
            return null;
        }
        if (!passwordEncoder.matches(password, user.getPasswordHash())) {
            return null;
        }

        loginMapper.updateLastLoginTime(user.getUserId());
        return buildAuthResult(user);
    }

    /**
     * 人脸登录逻辑。
     * 登录时必须提供邮箱与人脸图片（base64）。
     *
     * @param email     用户邮箱
     * @param faceImage 人脸图片 base64
     * @return 登录结果，失败时返回 null
     */
    public AuthResult loginByFace(String email, String faceImage) {
        if (!StringUtils.hasText(email)) {
            throw new IllegalArgumentException("Email is required for login.");
        }
        if (!StringUtils.hasText(faceImage)) {
            throw new IllegalArgumentException("Face image is required for login.");
        }

        User user = loginMapper.findUserByEmail(email.trim().toLowerCase());
        if (user == null) {
            return null;
        }
        if (user.getStatus() == null || user.getStatus() != User.STATUS_NORMAL) {
            return null;
        }

        float[] queryVector = extractFaceVector(faceImage);
        if (queryVector == null || queryVector.length == 0) {
            return null;
        }

        Double distance = loginMapper.computeCosineDistanceByUserId(user.getUserId(), queryVector);
        if (distance == null || distance > FACE_LOGIN_THRESHOLD) {
            return null;
        }

        loginMapper.updateLastLoginTime(user.getUserId());
        user.setLastLoginTime(LocalDateTime.now());
        return buildAuthResult(user);
    }

    /**
     * 兼容旧接口：邮箱 + 人脸登录。
     *
     * @param email     邮箱
     * @param faceImage 人脸图片 base64
     * @return 是否登录成功
     */
    public boolean login(String email, String faceImage) {
        return loginByFace(email, faceImage) != null;
    }

    public PageResult<UserProfile> pageUsers(int page, int size, String keyword) {
        int safePage = Math.max(page, 1);
        int safeSize = Math.max(size, 1);
        int offset = (safePage - 1) * safeSize;

        List<UserPageRow> rows = loginMapper.pageUsers(keyword, offset, safeSize);
        long total = loginMapper.countUsers(keyword);
        List<UserProfile> list = new ArrayList<>();
        for (UserPageRow row : rows) {
            list.add(toUserProfile(row));
        }

        return new PageResult<>(list, total, safePage, safeSize);
    }

    public boolean updateUserStatus(Long userId, Short status) {
        if (userId == null) {
            throw new IllegalArgumentException("User id is required.");
        }
        if (status == null) {
            throw new IllegalArgumentException("Status is required.");
        }

        return loginMapper.updateUserStatus(userId, status) > 0;
    }

    public void updateProfile(Long userId, String username) {
        if (userId == null) {
            throw new IllegalArgumentException("User id is required.");
        }
        if (!StringUtils.hasText(username)) {
            throw new IllegalArgumentException("Username is required.");
        }
        loginMapper.updateUsername(userId, username.trim());
    }

    public String updateAvatar(Long userId, MultipartFile file) {
        if (userId == null) {
            throw new IllegalArgumentException("User id is required.");
        }
        if (file == null || file.isEmpty()) {
            throw new IllegalArgumentException("头像文件不能为空");
        }
        String avatarUrl = fileService.uploadAvatar(file);
        loginMapper.updateAvatarUrl(userId, avatarUrl);
        return avatarUrl;
    }

    public void changePassword(Long userId, String oldPassword, String newPassword) {
        if (userId == null) {
            throw new IllegalArgumentException("User id is required.");
        }
        if (!StringUtils.hasText(oldPassword)) {
            throw new IllegalArgumentException("Old password is required.");
        }
        if (!StringUtils.hasText(newPassword)) {
            throw new IllegalArgumentException("New password is required.");
        }

        User user = loginMapper.findUserById(userId);
        if (user == null) {
            throw new IllegalArgumentException("User not found.");
        }
        if (!passwordEncoder.matches(oldPassword, user.getPasswordHash())) {
            throw new IllegalArgumentException("Old password is incorrect.");
        }

        loginMapper.updatePasswordHash(userId, passwordEncoder.encode(newPassword));
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

    private AuthResult buildAuthResult(User user) {
        UserProfile userProfile = toUserProfile(user, loadRoles(user.getUserId()));
        String token = jwtTokenService.generateToken(userProfile);
        return new AuthResult(jwtTokenService.tokenType(), token, userProfile);
    }

    private UserProfile toUserProfile(User user, List<String> roles) {
        UserProfile userProfile = new UserProfile();
        userProfile.setUserId(user.getUserId());
        userProfile.setUsername(user.getUsername());
        userProfile.setEmail(user.getEmail());
        userProfile.setPhone(user.getPhone());
        userProfile.setAvatarUrl(user.getAvatarUrl());
        userProfile.setStatus(user.getStatus());
        userProfile.setLoginType(user.getLoginType());
        userProfile.setLastLoginTime(user.getLastLoginTime());
        userProfile.setRoles(roles);
        return userProfile;
    }

    private UserProfile toUserProfile(UserPageRow row) {
        UserProfile userProfile = new UserProfile();
        userProfile.setUserId(row.getUserId());
        userProfile.setUsername(row.getUsername());
        userProfile.setEmail(row.getEmail());
        userProfile.setPhone(row.getPhone());
        userProfile.setAvatarUrl(row.getAvatarUrl());
        userProfile.setStatus(row.getStatus());
        userProfile.setLoginType(row.getLoginType());
        userProfile.setLastLoginTime(row.getLastLoginTime());
        userProfile.setRoles(splitRoles(row.getRoleNames()));
        return userProfile;
    }

    private List<String> loadRoles(Long userId) {
        List<String> roles = loginMapper.findRoleNamesByUserId(userId);
        return roles == null ? List.of() : List.copyOf(roles);
    }

    private List<String> splitRoles(String roleNames) {
        if (!StringUtils.hasText(roleNames)) {
            return List.of();
        }

        String[] parts = roleNames.split(",");
        List<String> roles = new ArrayList<>();
        for (String part : parts) {
            if (StringUtils.hasText(part)) {
                roles.add(part.trim());
            }
        }
        return roles;
    }
}