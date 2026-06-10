package com.revy.talkblogback.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.revy.talkblogback.auth.AuthContext;
import com.revy.talkblogback.auth.JwtTokenService;
import com.revy.talkblogback.auth.RequireRoles;
import com.revy.talkblogback.pojo.response.UserProfile;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.context.annotation.Configuration;
import org.springframework.lang.NonNull;
import org.springframework.lang.Nullable;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

@Configuration
public class WebMvcConfig implements WebMvcConfigurer {

    private final JwtTokenService jwtTokenService;
    private final ObjectMapper objectMapper;

    public WebMvcConfig(JwtTokenService jwtTokenService, ObjectMapper objectMapper) {
        this.jwtTokenService = jwtTokenService;
        this.objectMapper = objectMapper;
    }

    @Override
    public void addInterceptors(@NonNull InterceptorRegistry registry) {
        registry.addInterceptor(new AuthInterceptor(jwtTokenService, objectMapper))
                .addPathPatterns("/api/**");
    }

    @Override
    public void addResourceHandlers(@NonNull ResourceHandlerRegistry registry) {
        String uploadPath = Paths.get("./uploads").toAbsolutePath().normalize().toUri().toString();
        registry.addResourceHandler("/uploads/**")
                .addResourceLocations(uploadPath);
    }

    private static final class AuthInterceptor implements HandlerInterceptor {

        private final JwtTokenService jwtTokenService;
        private final ObjectMapper objectMapper;

        private AuthInterceptor(JwtTokenService jwtTokenService, ObjectMapper objectMapper) {
            this.jwtTokenService = jwtTokenService;
            this.objectMapper = objectMapper;
        }

        @Override
        public boolean preHandle(@NonNull HttpServletRequest request, @NonNull HttpServletResponse response, @NonNull Object handler) throws Exception {
            if (!(handler instanceof HandlerMethod handlerMethod)) {
                return true;
            }

            RequireRoles requireRoles = handlerMethod.getMethodAnnotation(RequireRoles.class);
            if (requireRoles == null) {
                requireRoles = handlerMethod.getBeanType().getAnnotation(RequireRoles.class);
            }

            String authorization = request.getHeader("Authorization");
            UserProfile userProfile = null;
            if (authorization != null && authorization.startsWith("Bearer ")) {
                String token = authorization.substring(7).trim();
                userProfile = jwtTokenService.parseToken(token);
            }

            if (requireRoles != null) {
                if (userProfile == null) {
                    writeError(response, 401, "Missing authorization token");
                    return false;
                }
                if (!hasRequiredRole(userProfile, requireRoles.value())) {
                    writeError(response, 403, "Access denied");
                    return false;
                }
            }

            if (userProfile != null) {
                AuthContext.set(userProfile);
                request.setAttribute("currentUser", userProfile);
            }
            return true;
        }

        @Override
        public void afterCompletion(@NonNull HttpServletRequest request, @NonNull HttpServletResponse response, @NonNull Object handler, @Nullable Exception ex) {
            AuthContext.clear();
            request.removeAttribute("currentUser");
        }

        private boolean hasRequiredRole(UserProfile userProfile, String[] requiredRoles) {
            if (requiredRoles == null || requiredRoles.length == 0) {
                return true;
            }

            if (userProfile.getRoles() == null || userProfile.getRoles().isEmpty()) {
                return false;
            }

            return Arrays.stream(requiredRoles)
                    .anyMatch(required -> userProfile.getRoles().stream().anyMatch(role -> role.equalsIgnoreCase(required)));
        }

        private void writeError(HttpServletResponse response, int status, String message) throws IOException {
            response.setStatus(status);
            response.setCharacterEncoding(StandardCharsets.UTF_8.name());
            response.setContentType("application/json;charset=UTF-8");
            Map<String, Object> body = new HashMap<>();
            body.put("success", false);
            body.put("message", message);
            response.getWriter().write(objectMapper.writeValueAsString(body));
        }
    }
}
