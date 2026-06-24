package com.revy.talkblogback.mapper;

import com.revy.talkblogback.pojo.FaceVector;
import com.revy.talkblogback.pojo.User;
import com.revy.talkblogback.pojo.dto.UserPageRow;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

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
         * 根据用户 ID 查询用户。
         *
         * @param userId 用户 ID
         * @return 用户信息，不存在时返回 null
         */
        User findUserById(@Param("userId") Long userId);

        User findUserByUsername(@Param("username") String username);

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

        /**
         * 查询用户角色名称列表。
         *
         * @param userId 用户 ID
         * @return 角色名称列表
         */
        List<String> findRoleNamesByUserId(@Param("userId") Long userId);

        /**
         * 分页查询用户。
         *
         * @param keyword 搜索关键字
         * @param offset  偏移量
         * @param limit   页大小
         * @return 用户列表
         */
        List<UserPageRow> pageUsers(@Param("keyword") String keyword, @Param("offset") int offset, @Param("limit") int limit);

        /**
         * 统计用户总数。
         *
         * @param keyword 搜索关键字
         * @return 用户总数
         */
        long countUsers(@Param("keyword") String keyword);

        /**
         * 更新用户状态。
         *
         * @param userId 用户 ID
         * @param status 新状态
         * @return 影响行数
         */
        int updateUserStatus(@Param("userId") Long userId, @Param("status") Short status);

        int updateUsername(@Param("userId") Long userId, @Param("username") String username);

        int updateAvatarUrl(@Param("userId") Long userId, @Param("avatarUrl") String avatarUrl);

        int updatePasswordHash(@Param("userId") Long userId, @Param("passwordHash") String passwordHash);
}
