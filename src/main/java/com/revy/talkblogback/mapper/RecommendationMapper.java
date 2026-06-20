package com.revy.talkblogback.mapper;

import com.revy.talkblogback.pojo.Blog;
import com.revy.talkblogback.pojo.UserBehaviorLog;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface RecommendationMapper {

    List<Blog> findCandidateBlogs(@Param("userId") Long userId,
                                  @Param("offset") int offset,
                                  @Param("limit") int limit);

    long countCandidateBlogs(@Param("userId") Long userId);

    List<Blog> findColdStartBlogs(@Param("offset") int offset,
                                  @Param("limit") int limit);

    long countColdStartBlogs();

    List<Long> findUserInteractedTagIds(@Param("userId") Long userId);

    List<Long> findUserPreferredCategoryIds(@Param("userId") Long userId);

    int insertBehaviorLog(UserBehaviorLog log);

    int refreshUserInterestTags(@Param("userId") Long userId);

    int refreshUserPreferredCategories(@Param("userId") Long userId);

    int findBehaviorExists(@Param("userId") Long userId,
                           @Param("blogId") Long blogId,
                           @Param("behaviorType") String behaviorType);
}
