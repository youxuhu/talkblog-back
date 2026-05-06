package com.revy.talkblogback.pojo.response;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.Map;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class CommentStats {

    private Long totalComments;
    private Long pendingReview;
    private Long approved;
    private Long rejected;
    private Long todayComments;
    private Long totalLikes;
    private Double avgCommentsPerBlog;
    private List<Map<String, Object>> dailyTrend;
    private List<Map<String, Object>> topBlogs;
    private List<Map<String, Object>> topCommenters;
}
