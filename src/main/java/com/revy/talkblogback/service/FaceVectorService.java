package com.revy.talkblogback.service;

import com.revy.talkblogback.mapper.FaceVectorMapper;
import com.revy.talkblogback.pojo.FaceVector;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * 服务层，用于处理人脸向量相关的业务逻辑。
 */
@Service
public class FaceVectorService {

    @Autowired
    private FaceVectorMapper faceVectorMapper;

    /**
     * 保存人脸向量。
     *
     * @param faceVector 人脸向量对象
     */
    public void saveFaceVector(FaceVector faceVector) {
        faceVectorMapper.insertFaceVector(faceVector);
    }

    /**
     * 根据用户 ID 查询人脸向量。
     *
     * @param userId 用户 ID
     * @return 人脸向量对象
     */
    public FaceVector getFaceVectorByUserId(Long userId) {
        return faceVectorMapper.getFaceVectorByUserId(userId);
    }

    /**
     * 查询与给定向量最相似的人脸向量。
     *
     * @param queryVector 查询向量
     * @return 最相似的人脸向量对象
     */
    public FaceVector findClosestFaceVector(float[] queryVector) {
        return faceVectorMapper.findClosestFaceVector(queryVector);
    }

    public void updateFaceVector(FaceVector faceVector) {
        faceVectorMapper.updateFaceVector(faceVector);
    }

    public void deleteByUserId(Long userId) {
        faceVectorMapper.deleteByUserId(userId);
    }
}