package com.revy.talkblogback.service;

import com.revy.talkblogback.config.FaceServiceProperties;
import com.revy.talkblogback.mapper.FaceVectorMapper;
import com.revy.talkblogback.mapper.LoginMapper;
import com.revy.talkblogback.pojo.FaceVector;
import com.revy.talkblogback.pojo.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.web.client.RestTemplate;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

/**
 * UserService 单元测试。
 * 目标：验证注册与邮箱+人脸登录的核心分支逻辑。
 */
class UserServiceTest {

    private LoginMapper loginMapper;
    private FaceVectorMapper faceVectorMapper;
    private RestTemplate restTemplate;
    private UserService userService;
    private FaceServiceProperties faceServiceProperties;

    @BeforeEach
    void setUp() {
        loginMapper = mock(LoginMapper.class);
        faceVectorMapper = mock(FaceVectorMapper.class);
        restTemplate = mock(RestTemplate.class);
        faceServiceProperties = new FaceServiceProperties();
        faceServiceProperties.setUrl("http://localhost:5000");
        faceServiceProperties.setExtractEndpoint("/extract");
        userService = new UserService(loginMapper, faceVectorMapper, restTemplate, faceServiceProperties);
    }

    /**
     * 校验：当邮箱已存在时，注册应失败。
     */
    @Test
    void register_shouldReturnFalse_whenEmailExists() {
        User req = new User();
        req.setEmail("demo@test.com");
        req.setUsername("demo");
        req.setPasswordHash("123456");

        when(loginMapper.findUserByEmail("demo@test.com")).thenReturn(new User());

        boolean ok = userService.register(req, "base64-image");

        assertFalse(ok);
        verify(loginMapper, never()).insertUser(any(User.class));
    }

    /**
     * 校验：注册成功时应写入加密后的密码。
     */
    @Test
    @SuppressWarnings({ "unchecked", "null" })
    void register_shouldSaveHashedPassword_whenInputValid() {
        User req = new User();
        req.setEmail("new@test.com");
        req.setUsername("new_user");
        req.setPasswordHash("plain_password");

        when(loginMapper.findUserByEmail("new@test.com")).thenReturn(null);
        when(loginMapper.insertUser(any(User.class))).thenReturn(1);

        when(loginMapper.insertUser(any(User.class))).thenAnswer(invocation -> {
            User user = invocation.getArgument(0);
            user.setUserId(3003L);
            return 1;
        });

        Map<String, Object> body = new HashMap<>();
        body.put("success", true);
        body.put("face_vector", buildVector512());
        ResponseEntity<Map<String, Object>> response = new ResponseEntity<>(body, HttpStatus.OK);
        when(restTemplate.exchange(anyString(), any(HttpMethod.class), any(HttpEntity.class),
                any(ParameterizedTypeReference.class)))
                .thenReturn(response);

        doAnswer(invocation -> {
            FaceVector fv = invocation.getArgument(0);
            fv.setVectorId(9001L);
            return null;
        }).when(faceVectorMapper).insertFaceVector(any(FaceVector.class));

        when(loginMapper.bindFaceVector(3003L, 9001L)).thenReturn(1);
        when(loginMapper.assignDefaultUserRole(3003L)).thenReturn(1);

        boolean ok = userService.register(req, "base64-image");

        assertTrue(ok);
        ArgumentCaptor<User> captor = ArgumentCaptor.forClass(User.class);
        verify(loginMapper).insertUser(captor.capture());
        User saved = captor.getValue();
        assertNotNull(saved.getPasswordHash());
        assertNotEquals("plain_password", saved.getPasswordHash());
        assertEquals(User.STATUS_NORMAL, saved.getStatus());
        verify(faceVectorMapper).insertFaceVector(any(FaceVector.class));
        verify(loginMapper).bindFaceVector(3003L, 9001L);
        verify(loginMapper).assignDefaultUserRole(3003L);
    }

    /**
     * 校验：邮箱+人脸匹配成功时应返回 true，并更新最后登录时间。
     */
    @Test
    @SuppressWarnings({ "unchecked", "null" })
    void login_shouldReturnTrue_whenEmailAndFaceMatched() {
        User dbUser = new User();
        dbUser.setUserId(1001L);
        dbUser.setEmail("face@test.com");
        dbUser.setStatus(User.STATUS_NORMAL);

        when(loginMapper.findUserByEmail("face@test.com")).thenReturn(dbUser);
        when(loginMapper.computeCosineDistanceByUserId(eq(1001L), any(float[].class))).thenReturn(0.12d);
        when(loginMapper.updateLastLoginTime(1001L)).thenReturn(1);

        Map<String, Object> body = new HashMap<>();
        body.put("success", true);
        body.put("face_vector", List.of(0.1, 0.2, 0.3));

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        ResponseEntity<Map<String, Object>> response = new ResponseEntity<>(body, headers, HttpStatus.OK);
        when(restTemplate.exchange(
                anyString(),
                any(HttpMethod.class),
                any(HttpEntity.class),
                any(ParameterizedTypeReference.class)))
                .thenReturn(response);

        boolean ok = userService.login("face@test.com", "base64-image");

        assertTrue(ok);
        verify(loginMapper).updateLastLoginTime(1001L);
    }

    /**
     * 校验：当人脸距离超过阈值时登录失败。
     */
    @Test
    @SuppressWarnings({ "unchecked", "null" })
    void login_shouldReturnFalse_whenFaceDistanceTooLarge() {
        User dbUser = new User();
        dbUser.setUserId(2002L);
        dbUser.setEmail("far@test.com");
        dbUser.setStatus(User.STATUS_NORMAL);

        when(loginMapper.findUserByEmail("far@test.com")).thenReturn(dbUser);
        when(loginMapper.computeCosineDistanceByUserId(eq(2002L), any(float[].class))).thenReturn(0.88d);

        Map<String, Object> body = new HashMap<>();
        body.put("success", true);
        body.put("face_vector", List.of(0.1, 0.2, 0.3));

        ResponseEntity<Map<String, Object>> response = new ResponseEntity<>(body, HttpStatus.OK);
        when(restTemplate.exchange(
                anyString(),
                any(HttpMethod.class),
                any(HttpEntity.class),
                any(ParameterizedTypeReference.class)))
                .thenReturn(response);

        boolean ok = userService.login("far@test.com", "base64-image");

        assertFalse(ok);
        verify(loginMapper, never()).updateLastLoginTime(anyLong());
    }

    /**
     * 生成一个 512 维测试向量。
     */
    private List<Double> buildVector512() {
        Double[] values = new Double[512];
        for (int i = 0; i < values.length; i++) {
            values[i] = 0.001d * i;
        }
        return List.of(values);
    }

}
