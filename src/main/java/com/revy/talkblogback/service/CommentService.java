package com.revy.talkblogback.service;

import com.revy.talkblogback.auth.AuthContext;
import com.revy.talkblogback.auth.AuthProperties;
import com.revy.talkblogback.mapper.CommentImageMapper;
import com.revy.talkblogback.mapper.CommentMapper;
import com.revy.talkblogback.pojo.Comment;
import com.revy.talkblogback.pojo.CommentImage;
import com.revy.talkblogback.pojo.CommentLike;
import com.revy.talkblogback.pojo.User;
import com.revy.talkblogback.pojo.request.BatchReviewRequest;
import com.revy.talkblogback.pojo.request.CreateCommentRequest;
import com.revy.talkblogback.pojo.response.*;
import com.revy.talkblogback.mapper.LoginMapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.util.*;
import java.util.stream.Collectors;

@Service
public class CommentService {

    private final CommentMapper commentMapper;
    private final LoginMapper loginMapper;
    private final CommentImageMapper commentImageMapper;
    private final FileService fileService;
    private final RecommendationService recommendationService;

    public CommentService(CommentMapper commentMapper, LoginMapper loginMapper,
                         CommentImageMapper commentImageMapper, FileService fileService,
                         RecommendationService recommendationService) {
        this.commentMapper = commentMapper;
        this.loginMapper = loginMapper;
        this.commentImageMapper = commentImageMapper;
        this.fileService = fileService;
        this.recommendationService = recommendationService;
    }

    @Transactional(rollbackFor = Exception.class)
    public CommentDetail createComment(CreateCommentRequest request, List<MultipartFile> images) {
        User currentUser = getCurrentUser();

        if (request.getParentId() != null) {
            Comment parent = commentMapper.findCommentById(request.getParentId());
            if (parent == null) {
                throw new IllegalArgumentException("父评论不存在");
            }
        }

        Comment comment = new Comment();
        comment.setBlogId(request.getBlogId());
        comment.setUserId(currentUser.getUserId());
        comment.setParentId(request.getParentId());
        comment.setReplyToUserId(request.getReplyToUserId());
        comment.setContent(request.getContent());
        comment.setStatus(currentUserHasRole(currentUser, "ADMIN") || currentUserHasRole(currentUser, "SUPER_ADMIN")
                ? Comment.STATUS_APPROVED
                : Comment.STATUS_PENDING);
        comment.setIpAddress(getClientIpAddress());

        commentMapper.insertComment(comment);

        if (images != null && !images.isEmpty()) {
            List<String> imageUrls = fileService.uploadCommentImages(images);
            if (!imageUrls.isEmpty()) {
                commentImageMapper.insertCommentImages(comment.getCommentId(), imageUrls);
            }
        }

        recommendationService.recordBehavior(comment.getBlogId(), "comment", 4.0);

        return buildCommentDetail(comment);
    }

    @Transactional(rollbackFor = Exception.class)
    public CommentDetail createComment(CreateCommentRequest request) {
        return createComment(request, null);
    }

    public PageResult<CommentDetail> getCommentsByBlogId(Long blogId, int page, int size, Long parentId, Short status) {
        int offset = (page - 1) * size;
        List<CommentDetail> comments = commentMapper.findCommentsByBlogId(blogId, parentId, status, offset, size);
        long total = commentMapper.countCommentsByBlogId(blogId, parentId, status);

        for (CommentDetail comment : comments) {
            loadRepliesRecursively(comment);
        }

        return new PageResult<>(comments, total, page, size);
    }

    private void loadRepliesRecursively(CommentDetail comment) {
        List<CommentDetail> replies = commentMapper.findRepliesByParentId(comment.getCommentId());
        comment.setReplies(replies);
        for (CommentDetail reply : replies) {
            loadRepliesRecursively(reply);
        }
    }

    @Transactional(rollbackFor = Exception.class)
    public Map<String, Object> likeComment(Long commentId) {
        User currentUser = getCurrentUser();
        Comment comment = commentMapper.findCommentById(commentId);
        if (comment == null) {
            throw new IllegalArgumentException("评论不存在");
        }

        CommentLike existingLike = commentMapper.findCommentLike(commentId, currentUser.getUserId());
        if (existingLike != null) {
            commentMapper.deleteCommentLike(commentId, currentUser.getUserId());
            commentMapper.decrementLikeCount(commentId);
            return Map.of("liked", false, "likeCount", Math.max(comment.getLikeCount() - 1, 0));
        } else {
            CommentLike like = new CommentLike();
            like.setCommentId(commentId);
            like.setUserId(currentUser.getUserId());
            commentMapper.insertCommentLike(like);
            commentMapper.incrementLikeCount(commentId);
            return Map.of("liked", true, "likeCount", comment.getLikeCount() + 1);
        }
    }

    @Transactional(rollbackFor = Exception.class)
    public void deleteComment(Long commentId) {
        User currentUser = getCurrentUser();
        Comment comment = commentMapper.findCommentById(commentId);
        if (comment == null) {
            throw new IllegalArgumentException("评论不存在");
        }
        if (!comment.getUserId().equals(currentUser.getUserId())
                && !currentUserHasRole(currentUser, "ADMIN")
                && !currentUserHasRole(currentUser, "SUPER_ADMIN")) {
            throw new IllegalArgumentException("无权删除该评论");
        }

        deleteCommentTree(commentId);
    }

    private void deleteCommentTree(Long commentId) {
        List<CommentDetail> replies = commentMapper.findRepliesByParentId(commentId);
        for (CommentDetail reply : replies) {
            deleteCommentTree(reply.getCommentId());
        }
        commentImageMapper.deleteByCommentId(commentId);
        commentMapper.deleteCommentAdmin(commentId);
    }

    public PageResult<CommentRow> getMyComments(int page, int size) {
        User currentUser = getCurrentUser();
        int offset = (page - 1) * size;
        List<CommentRow> comments = commentMapper.findMyComments(currentUser.getUserId(), offset, size);
        for (CommentRow comment : comments) {
            comment.setImages(commentImageMapper.findImageUrlsByCommentId(comment.getCommentId()));
        }
        long total = commentMapper.countMyComments(currentUser.getUserId());
        return new PageResult<>(comments, total, page, size);
    }

    public PageResult<CommentRow> getAdminComments(int page, int size, String keyword, Short status, String blogId) {
        int offset = (page - 1) * size;
        List<CommentRow> comments = commentMapper.findAdminComments(keyword, status, blogId, offset, size);
        for (CommentRow comment : comments) {
            comment.setImages(commentImageMapper.findImageUrlsByCommentId(comment.getCommentId()));
        }
        long total = commentMapper.countAdminComments(keyword, status, blogId);
        return new PageResult<>(comments, total, page, size);
    }

    @Transactional(rollbackFor = Exception.class)
    public void reviewComment(Long commentId, Short status) {
        Comment comment = commentMapper.findCommentById(commentId);
        if (comment == null) {
            throw new IllegalArgumentException("评论不存在");
        }
        commentMapper.updateCommentStatus(commentId, status);
    }

    @Transactional(rollbackFor = Exception.class)
    public void batchReviewComments(List<Long> commentIds, Short status) {
        for (Long commentId : commentIds) {
            Comment comment = commentMapper.findCommentById(commentId);
            if (comment != null) {
                commentMapper.updateCommentStatus(commentId, status);
            }
        }
    }

    @Transactional(rollbackFor = Exception.class)
    public void adminDeleteComment(Long commentId) {
        Comment comment = commentMapper.findCommentById(commentId);
        if (comment == null) {
            throw new IllegalArgumentException("评论不存在");
        }
        deleteCommentTree(commentId);
    }

    public CommentStats getCommentStats(int days) {
        Map<String, Object> statsMap = commentMapper.findCommentStats(days);
        CommentStats stats = new CommentStats();
        stats.setTotalComments(getLongValue(statsMap, "total_comments"));
        stats.setPendingReview(getLongValue(statsMap, "pending_review"));
        stats.setApproved(getLongValue(statsMap, "approved"));
        stats.setRejected(getLongValue(statsMap, "rejected"));
        stats.setTodayComments(getLongValue(statsMap, "today_comments"));
        stats.setTotalLikes(getLongValue(statsMap, "total_likes"));

        long totalBlogs = getTotalBlogCount(days);
        stats.setAvgCommentsPerBlog(totalBlogs > 0 ? (double) stats.getTotalComments() / totalBlogs : 0.0);

        stats.setDailyTrend(commentMapper.findDailyTrend(days));
        stats.setTopBlogs(commentMapper.findTopBlogs(days, 5));
        stats.setTopCommenters(commentMapper.findTopCommenters(days, 5));

        return stats;
    }

    private long getTotalBlogCount(int days) {
        return commentMapper.findTopBlogs(days, 1000).size();
    }

    private CommentDetail buildCommentDetail(Comment comment) {
        User user = loginMapper.findUserById(comment.getUserId());
        CommentDetail detail = new CommentDetail();
        detail.setCommentId(comment.getCommentId());
        detail.setBlogId(comment.getBlogId());
        detail.setUserId(comment.getUserId());
        detail.setUsername(user != null ? user.getUsername() : "未知");
        detail.setContent(comment.getContent());
        detail.setLikeCount(comment.getLikeCount());
        detail.setCreatedAt(comment.getCreatedAt());
        detail.setStatus(comment.getStatus());
        detail.setReplies(new ArrayList<>());
        detail.setImages(commentImageMapper.findImageUrlsByCommentId(comment.getCommentId()));

        if (comment.getReplyToUserId() != null) {
            User replyToUser = loginMapper.findUserById(comment.getReplyToUserId());
            detail.setReplyToUsername(replyToUser != null ? replyToUser.getUsername() : null);
        }

        return detail;
    }

    private User getCurrentUser() {
        UserProfile profile = AuthContext.get();
        if (profile == null) {
            throw new IllegalArgumentException("未登录");
        }
        User user = loginMapper.findUserById(profile.getUserId());
        if (user == null) {
            throw new IllegalArgumentException("用户不存在");
        }
        return user;
    }

    private boolean currentUserHasRole(User user, String roleName) {
        List<String> roles = loginMapper.findRoleNamesByUserId(user.getUserId());
        return roles.contains(roleName);
    }

    private String getClientIpAddress() {
        try {
            jakarta.servlet.http.HttpServletRequest request =
                    ((org.springframework.web.context.request.RequestContextHolder
                            .getRequestAttributes()) instanceof org.springframework.web.context.request.ServletRequestAttributes sra)
                            ? sra.getRequest() : null;
            if (request != null) {
                String ip = request.getHeader("X-Forwarded-For");
                if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
                    ip = request.getHeader("X-Real-IP");
                }
                if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
                    ip = request.getRemoteAddr();
                }
                return ip;
            }
        } catch (Exception ignored) {
        }
        return "127.0.0.1";
    }

    private long getLongValue(Map<String, Object> map, String key) {
        Object value = map.get(key);
        if (value == null) return 0L;
        if (value instanceof Number number) return number.longValue();
        if (value instanceof String str) {
            try {
                return Long.parseLong(str);
            } catch (NumberFormatException ignored) {
                return 0L;
            }
        }
        return 0L;
    }
}
