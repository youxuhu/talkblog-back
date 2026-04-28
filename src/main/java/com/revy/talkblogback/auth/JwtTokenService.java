package com.revy.talkblogback.auth;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.revy.talkblogback.pojo.response.UserProfile;
import org.springframework.stereotype.Service;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.Base64;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class JwtTokenService {

    private static final String HMAC_ALGORITHM = "HmacSHA256";
    private static final String TOKEN_TYPE = "Bearer";

    private final ObjectMapper objectMapper;
    private final AuthProperties authProperties;

    public JwtTokenService(ObjectMapper objectMapper, AuthProperties authProperties) {
        this.objectMapper = objectMapper;
        this.authProperties = authProperties;
    }

    public String tokenType() {
        return TOKEN_TYPE;
    }

    public String generateToken(UserProfile userProfile) {
        long issuedAt = Instant.now().getEpochSecond();
        long expiresAt = issuedAt + authProperties.getExpirationMinutes() * 60;

        Map<String, Object> payload = new HashMap<>();
        payload.put("iss", authProperties.getIssuer());
        payload.put("sub", String.valueOf(userProfile.getUserId()));
        payload.put("iat", issuedAt);
        payload.put("exp", expiresAt);
        payload.put("userId", userProfile.getUserId());
        payload.put("username", userProfile.getUsername());
        payload.put("email", userProfile.getEmail());
        payload.put("status", userProfile.getStatus());
        payload.put("loginType", userProfile.getLoginType());
        payload.put("roles", userProfile.getRoles());

        try {
            String header = base64UrlEncode(objectMapper.writeValueAsBytes(Map.of(
                    "alg", "HS256",
                    "typ", "JWT"
            )));
            String payloadPart = base64UrlEncode(objectMapper.writeValueAsBytes(payload));
            String signature = sign(header + "." + payloadPart);
            return header + "." + payloadPart + "." + signature;
        } catch (Exception ex) {
            throw new IllegalStateException("Failed to generate JWT token", ex);
        }
    }

    public UserProfile parseToken(String token) {
        if (token == null || token.isBlank()) {
            return null;
        }

        try {
            String[] parts = token.split("\\.");
            if (parts.length != 3) {
                return null;
            }

            String signedContent = parts[0] + "." + parts[1];
            if (!sign(signedContent).equals(parts[2])) {
                return null;
            }

            Map<String, Object> payload = objectMapper.readValue(
                    Base64.getUrlDecoder().decode(parts[1]),
                    new TypeReference<>() {
                    });

            Object expValue = payload.get("exp");
            if (expValue instanceof Number number && Instant.now().getEpochSecond() >= number.longValue()) {
                return null;
            }

            UserProfile userProfile = new UserProfile();
            Object userId = payload.get("userId");
            if (userId instanceof Number number) {
                userProfile.setUserId(number.longValue());
            }
            userProfile.setUsername(asString(payload.get("username")));
            userProfile.setEmail(asString(payload.get("email")));
            Object status = payload.get("status");
            if (status instanceof Number number) {
                userProfile.setStatus(number.shortValue());
            }
            userProfile.setLoginType(asString(payload.get("loginType")));
            Object roles = payload.get("roles");
            if (roles instanceof List<?> list) {
                userProfile.setRoles(list.stream().map(String::valueOf).toList());
            }
            return userProfile;
        } catch (Exception ex) {
            return null;
        }
    }

    private String sign(String content) throws Exception {
        Mac mac = Mac.getInstance(HMAC_ALGORITHM);
        mac.init(new SecretKeySpec(authProperties.getSecret().getBytes(StandardCharsets.UTF_8), HMAC_ALGORITHM));
        return base64UrlEncode(mac.doFinal(content.getBytes(StandardCharsets.UTF_8)));
    }

    private String base64UrlEncode(byte[] bytes) {
        return Base64.getUrlEncoder().withoutPadding().encodeToString(bytes);
    }

    private String asString(Object value) {
        return value == null ? null : String.valueOf(value);
    }
}
