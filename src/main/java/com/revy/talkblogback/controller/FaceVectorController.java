package com.revy.talkblogback.controller;

import com.revy.talkblogback.auth.AuthContext;
import com.revy.talkblogback.auth.RequireRoles;
import com.revy.talkblogback.pojo.FaceVector;
import com.revy.talkblogback.pojo.response.ApiResponse;
import com.revy.talkblogback.pojo.response.UserProfile;
import com.revy.talkblogback.service.FaceVectorService;
import com.revy.talkblogback.service.UserService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/face")
public class FaceVectorController {

    private final FaceVectorService faceVectorService;
    private final UserService userService;

    public FaceVectorController(FaceVectorService faceVectorService, UserService userService) {
        this.faceVectorService = faceVectorService;
        this.userService = userService;
    }

    @PostMapping("/register")
    public String registerFace(@RequestBody FaceVector faceVector) {
        faceVectorService.saveFaceVector(faceVector);
        return "Face vector registered successfully!";
    }

    @GetMapping("/user/{userId}")
    public FaceVector getFaceVectorByUserId(@PathVariable Long userId) {
        return faceVectorService.getFaceVectorByUserId(userId);
    }

    @PostMapping("/match")
    public FaceVector matchFace(@RequestBody Map<String, Object> request) {
        Object vectorObject = request.get("face_vector");
        if (!(vectorObject instanceof List)) {
            throw new IllegalArgumentException("Invalid face_vector format");
        }

        List<Double> vectorList = ((List<?>) vectorObject).stream()
                .filter(item -> item instanceof Number)
                .map(item -> ((Number) item).doubleValue())
                .collect(Collectors.toList());

        float[] queryVector = new float[vectorList.size()];
        for (int i = 0; i < vectorList.size(); i++) {
            queryVector[i] = vectorList.get(i).floatValue();
        }

        return faceVectorService.findClosestFaceVector(queryVector);
    }

    @PostMapping("/re-register")
    @RequireRoles
    public ResponseEntity<ApiResponse<Void>> reRegisterFace(@RequestBody Map<String, String> request) {
        UserProfile currentUser = AuthContext.get();
        if (currentUser == null) {
            return ResponseEntity.status(401).body(ApiResponse.failure("Unauthorized"));
        }
        String image = request.get("image");
        if (image == null || image.isBlank()) {
            return ResponseEntity.badRequest().body(ApiResponse.failure("Face image is required"));
        }
        try {
            userService.reRegisterFace(currentUser.getUserId(), image);
            return ResponseEntity.ok(ApiResponse.success("人脸更新成功"));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(ApiResponse.failure(e.getMessage()));
        }
    }
}
