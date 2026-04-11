package com.revy.talkblogback.controller;

import com.revy.talkblogback.pojo.FaceVector;
import com.revy.talkblogback.service.FaceVectorService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 控制器层，用于处理人脸向量相关的 HTTP 请求。
 */
@RestController
@RequestMapping("/api/face")
public class FaceVectorController {

    @Autowired
    private FaceVectorService faceVectorService;

    /**
     * 注册人脸向量。
     *
     * @param faceVector 人脸向量对象
     * @return 注册结果
     */
    @PostMapping("/register")
    public String registerFace(@RequestBody FaceVector faceVector) {
        faceVectorService.saveFaceVector(faceVector);
        return "Face vector registered successfully!";
    }

    /**
     * 根据用户 ID 查询人脸向量。
     *
     * @param userId 用户 ID
     * @return 人脸向量对象
     */
    @GetMapping("/user/{userId}")
    public FaceVector getFaceVectorByUserId(@PathVariable Long userId) {
        return faceVectorService.getFaceVectorByUserId(userId);
    }

    /**
     * 查询与给定向量最相似的人脸向量。
     *
     * @param request 包含查询向量的请求体
     * @return 最相似的人脸向量对象
     */
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
}