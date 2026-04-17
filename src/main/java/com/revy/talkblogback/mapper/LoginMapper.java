package com.revy.talkblogback.mapper;

import com.revy.talkblogback.pojo.FaceVector;
import com.revy.talkblogback.pojo.User;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

/**
 * 登录与注册相关的数据访问组件。
 */
@Mapper
public interface LoginMapper {

        /**
         * 根据邮箱查询用户。
         *
         * @param email 邮箱
         * @return 用户信息，不存在时返回 null
         */
        User findUserByEmail(@Param("email") String email);

        /**
         * 插入用户信息。
         *
         * @param user 用户对象
         * @return 影响行数
         */
        int insertUser(User user);

        /**
         * 将用户与人脸向量绑定，并将默认登录方式设置为 FACE。
         *
         * @param userId       用户 ID
         * @param faceVectorId 向量 ID
         * @return 影响行数
         */
        int bindFaceVector(@Param("userId") Long userId, @Param("faceVectorId") Long faceVectorId);

        /**
         * 给用户分配默认角色 USER。
         *
         * @param userId 用户 ID
         * @return 影响行数
         */
        int assignDefaultUserRole(@Param("userId") Long userId);

        /**
         * 根据用户 ID 查询对应的人脸向量。
         *
         * @param userId 用户 ID
         * @return 人脸向量
         */
        FaceVector findFaceVectorByUserId(@Param("userId") Long userId);

        /**
         * 计算指定用户的人脸向量与输入向量之间的余弦距离。
         * 数值越小表示越相似。
         *
         * @param userId      用户 ID
         * @param queryVector 输入向量
         * @return 余弦距离
         */
        Double computeCosineDistanceByUserId(@Param("userId") Long userId, @Param("queryVector") float[] queryVector);

        /**
         * 更新最后登录时间。
         *
         * @param userId 用户 ID
         * @return 影响行数
         */
        int updateLastLoginTime(@Param("userId") Long userId);
}
