package com.revy.talkblogback.mapper;

import com.revy.talkblogback.pojo.Follow;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;
import java.util.Map;

@Mapper
public interface FollowMapper {

    int insert(Follow follow);

    int delete(@Param("followerId") Long followerId, @Param("followeeId") Long followeeId);

    Follow findByFollowerAndFollowee(@Param("followerId") Long followerId, @Param("followeeId") Long followeeId);

    long countFollowers(@Param("userId") Long userId);

    long countFollowees(@Param("userId") Long userId);

    List<Map<String, Object>> findFollowers(@Param("userId") Long userId,
                                             @Param("offset") int offset,
                                             @Param("limit") int limit);

    List<Map<String, Object>> findFollowees(@Param("userId") Long userId,
                                             @Param("offset") int offset,
                                             @Param("limit") int limit);
}
