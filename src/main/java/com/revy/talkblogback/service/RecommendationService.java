package com.revy.talkblogback.service;

import com.revy.talkblogback.auth.AuthContext;
import com.revy.talkblogback.mapper.RecommendationMapper;
import com.revy.talkblogback.pojo.Blog;
import com.revy.talkblogback.pojo.UserBehaviorLog;
import com.revy.talkblogback.pojo.response.PageResult;
import com.revy.talkblogback.pojo.response.UserProfile;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class RecommendationService {

    private final RecommendationMapper recommendationMapper;

    public RecommendationService(RecommendationMapper recommendationMapper) {
        this.recommendationMapper = recommendationMapper;
    }

    public PageResult<Blog> getRecommendations(int page, int size) {
        int safePage = Math.max(page, 1);
        int safeSize = Math.max(size, 1);
        int offset = (safePage - 1) * safeSize;

        Long userId = null;
        try {
            UserProfile profile = AuthContext.get();
            if (profile != null) userId = profile.getUserId();
        } catch (Exception ignored) {}

        List<Blog> list;
        long total;

        if (userId != null) {
            refreshUserProfileIfNeeded(userId);
            list = recommendationMapper.findCandidateBlogs(userId, offset, safeSize);
            total = recommendationMapper.countCandidateBlogs(userId);
            if (list.isEmpty() && page == 1) {
                list = recommendationMapper.findColdStartBlogs(offset, safeSize);
                total = recommendationMapper.countColdStartBlogs();
            }
        } else {
            list = recommendationMapper.findColdStartBlogs(offset, safeSize);
            total = recommendationMapper.countColdStartBlogs();
        }

        return new PageResult<>(list, total, safePage, safeSize);
    }

    @Transactional(rollbackFor = Exception.class)
    public void recordBehavior(Long blogId, String behaviorType, Double score) {
        UserProfile profile = AuthContext.get();
        if (profile == null) return;

        Long userId = profile.getUserId();
        if (userId == null) return;

        int exists = recommendationMapper.findBehaviorExists(userId, blogId, behaviorType);
        if (exists > 0) return;

        UserBehaviorLog log = new UserBehaviorLog();
        log.setUserId(userId);
        log.setBlogId(blogId);
        log.setBehaviorType(behaviorType);
        log.setScore(score != null ? score : getDefaultScore(behaviorType));
        recommendationMapper.insertBehaviorLog(log);
    }

    private void refreshUserProfileIfNeeded(Long userId) {
        recommendationMapper.refreshUserInterestTags(userId);
        recommendationMapper.refreshUserPreferredCategories(userId);
    }

    private double getDefaultScore(String behaviorType) {
        return switch (behaviorType) {
            case "like" -> 3.0;
            case "favorite" -> 5.0;
            case "comment" -> 4.0;
            case "view" -> 1.0;
            default -> 1.0;
        };
    }
}
