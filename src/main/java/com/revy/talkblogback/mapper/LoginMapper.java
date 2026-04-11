package com.revy.talkblogback.mapper;

import com.revy.talkblogback.handler.PgVectorTypeHandler;
import com.revy.talkblogback.pojo.FaceVector;
import com.revy.talkblogback.pojo.User;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Options;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Result;
import org.apache.ibatis.annotations.Results;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

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
    @Select("""
                   SELECT user_id, username, email, phone, password_hash, face_vector_id, login_type, avatar_url,
            status, created_at, updated_at, last_login_time
            FROM users
            WHERE email = #{email}
            LIMIT 1
            """)
    User findUserByEmail(@Param("email") String email);

    /**
     * 插入用户信息。
     *
     * @param user 用户对象
     * @return 影响行数
     */
    @Insert("""
                   INSERT INTO users (username, email, password_hash, status, login_type, created_at, updated_at)
                   VALUES (#{username}, #{email}, #{passwordHash}, #{status}, #{loginType}, NOW(), NOW())
            """)
    @Options(useGeneratedKeys = true, keyProperty = "userId", keyColumn = "user_id")
    int insertUser(User user);

    /**
     * 将用户与人脸向量绑定，并将默认登录方式设置为 FACE。
     *
     * @param userId       用户 ID
     * @param faceVectorId 向量 ID
     * @return 影响行数
     */
    @Update("""
            UPDATE users
            SET face_vector_id = #{faceVectorId}, login_type = 'FACE', updated_at = NOW()
            WHERE user_id = #{userId}
            """)
    int bindFaceVector(@Param("userId") Long userId, @Param("faceVectorId") Long faceVectorId);

    /**
     * 给用户分配默认角色 USER。
     *
     * @param userId 用户 ID
     * @return 影响行数
     */
    @Insert("""
            INSERT INTO user_roles (user_id, role_id)
            SELECT #{userId}, role_id
            FROM roles
            WHERE role_name = 'USER'
            ON CONFLICT (user_id, role_id) DO NOTHING
            """)
    int assignDefaultUserRole(@Param("userId") Long userId);

    /**
     * 根据用户 ID 查询对应的人脸向量。
     *
     * @param userId 用户 ID
     * @return 人脸向量
     */
    @Select("""
            SELECT vector_id, user_id, face_vector, face_image_url, created_at, updated_at
            FROM face_vectors
            WHERE user_id = #{userId}
            LIMIT 1
            """)
    @Results(id = "faceVectorResult", value = {
            @Result(column = "vector_id", property = "vectorId"),
            @Result(column = "user_id", property = "userId"),
            @Result(column = "face_vector", property = "faceVector", typeHandler = PgVectorTypeHandler.class),
            @Result(column = "face_image_url", property = "faceImageUrl"),
            @Result(column = "created_at", property = "createdAt"),
            @Result(column = "updated_at", property = "updatedAt")
    })
    FaceVector findFaceVectorByUserId(@Param("userId") Long userId);

    /**
     * 计算指定用户的人脸向量与输入向量之间的余弦距离。
     * 数值越小表示越相似。
     *
     * @param userId      用户 ID
     * @param queryVector 输入向量
     * @return 余弦距离
     */
    @Select("""
            SELECT face_vector <=> #{queryVector, typeHandler=com.revy.talkblogback.handler.PgVectorTypeHandler}
            FROM face_vectors
            WHERE user_id = #{userId}
            LIMIT 1
            """)
    Double computeCosineDistanceByUserId(@Param("userId") Long userId, @Param("queryVector") float[] queryVector);

    /**
     * 更新最后登录时间。
     *
     * @param userId 用户 ID
     * @return 影响行数
     */
    @Update("""
            UPDATE users
            SET last_login_time = NOW(), updated_at = NOW()
            WHERE user_id = #{userId}
            """)
    int updateLastLoginTime(@Param("userId") Long userId);
}
