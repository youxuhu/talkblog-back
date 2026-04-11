package com.revy.talkblogback.mapper;

import com.revy.talkblogback.pojo.FaceVector;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

/**
 * MyBatis 映射接口，用于操作 face_vectors 表。
 */
@Mapper
public interface FaceVectorMapper {

    /**
     * 插入人脸向量。
     *
     * @param faceVector 人脸向量对象
     */
    void insertFaceVector(FaceVector faceVector);

    /**
     * 根据用户 ID 查询人脸向量。
     *
     * @param userId 用户 ID
     * @return 人脸向量对象
     */
    FaceVector getFaceVectorByUserId(@Param("userId") Long userId);

    /**
     * 查询与给定向量最相似的人脸向量。
     *
     * @param queryVector 查询向量
     * @return 最相似的人脸向量对象
     */
    FaceVector findClosestFaceVector(@Param("queryVector") float[] queryVector);
}