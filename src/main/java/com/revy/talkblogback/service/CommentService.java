package com.revy.talkblogback.service;

import com.revy.talkblogback.auth.AuthContext;
import com.revy.talkblogback.mapper.BlogMapper;
import com.revy.talkblogback.mapper.CommentImageMapper;
import com.revy.talkblogback.mapper.CommentMapper;
import com.revy.talkblogback.mapper.CommentReportMapper;
import com.revy.talkblogback.mapper.LoginMapper;
import com.revy.talkblogback.pojo.*;
import com.revy.talkblogback.pojo.request.CreateCommentRequest;
import com.revy.talkblogback.pojo.request.CreateReportRequest;
import com.revy.talkblogback.pojo.response.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

@Service
public class CommentService {

    private static final Logger log = LoggerFactory.getLogger(CommentService.class);
    private static final Pattern MENTION_PATTERN = Pattern.compile("@(\\S{1,50})");

    private final CommentMapper commentMapper;
    private final LoginMapper loginMapper;
    private final CommentImageMapper commentImageMapper;
    private final CommentReportMapper commentReportMapper;
    private final FileService fileService;
    private final NotificationService notificationService;
    private final BlogMapper blogMapper;
    private final SensitiveWordService sensitiveWordService;

    public CommentService(CommentMapper commentMapper, LoginMapper loginMapper,
                          CommentImageMapper commentImageMapper, CommentReportMapper commentReportMapper,
                          FileService fileService,
                          NotificationService notificationService, BlogMapper blogMapper,
                          SensitiveWordService sensitiveWordService) {
        this.commentMapper = commentMapper;
        this.loginMapper = loginMapper;
        this.commentImageMapper = commentImageMapper;
        this.commentReportMapper = commentReportMapper;
        this.fileService = fileService;
        this.notificationService = notificationService;
        this.blogMapper = blogMapper;
        this.sensitiveWordService = sensitiveWordService;
    }

    @Transactional(rollbackFor = Exception.class)
    public CommentDetail createComment(CreateCommentRequest request, List<MultipartFile> images) {
        User currentUser = getCurrentUser();

        String content = request.getContent();
        if (content != null && content.codePointCount(0, content.length()) > CreateCommentRequest.CONTENT_MAX_LENGTH) {
            throw new IllegalArgumentException("评论内容不能超过 " + CreateCommentRequest.CONTENT_MAX_LENGTH + " 字");
        }

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
        comment.setContent(sensitiveWordService.filter(content));
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

        createCommentNotification(comment, currentUser);
        createMentionNotifications(comment, currentUser);

        return buildCommentDetail(comment);
    }

    @Transactional(rollbackFor = Exception.class)
    public CommentDetail createComment(CreateCommentRequest request) {
        return createComment(request, null);
    }

    @Transactional(rollbackFor = Exception.class)
    public CommentDetail updateComment(Long commentId, String newContent) {
        User currentUser = getCurrentUser();
        Comment comment = commentMapper.findCommentById(commentId);
        if (comment == null) {
            throw new IllegalArgumentException("评论不存在");
        }
        if (!comment.getUserId().equals(currentUser.getUserId())) {
            throw new IllegalArgumentException("只能编辑自己的评论");
        }
        if (newContent == null || newContent.trim().isEmpty()) {
            throw new IllegalArgumentException("评论内容不能为空");
        }
        if (newContent.codePointCount(0, newContent.length()) > CreateCommentRequest.CONTENT_MAX_LENGTH) {
            throw new IllegalArgumentException("评论内容不能超过 " + CreateCommentRequest.CONTENT_MAX_LENGTH + " 字");
        }

        comment.setContent(sensitiveWordService.filter(newContent));
        commentMapper.updateCommentContent(commentId, comment.getContent());

        createMentionNotifications(comment, currentUser);

        return buildCommentDetail(comment);
    }

    public PageResult<CommentDetail> getCommentsByBlogId(Long blogId, int page, int size, Long parentId, Short status, String sort) {
        int offset = (page - 1) * size;
        List<CommentDetail> comments = commentMapper.findCommentsByBlogId(blogId, parentId, status, sort, offset, size);
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
            createLikeNotification(comment, currentUser);
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

    @Transactional(rollbackFor = Exception.class)
    public boolean togglePin(Long commentId) {
        User currentUser = getCurrentUser();
        Comment comment = commentMapper.findCommentById(commentId);
        if (comment == null) {
            throw new IllegalArgumentException("评论不存在");
        }
        commentMapper.togglePin(commentId);
        Boolean pinned = commentMapper.findIsPinned(commentId);
        return pinned != null && pinned;
    }

    @Transactional(rollbackFor = Exception.class)
    public void reportComment(Long commentId, CreateReportRequest request) {
        User currentUser = getCurrentUser();
        Comment comment = commentMapper.findCommentById(commentId);
        if (comment == null) {
            throw new IllegalArgumentException("评论不存在");
        }

        int existing = commentReportMapper.countByCommentIdAndReporter(commentId, currentUser.getUserId());
        if (existing > 0) {
            throw new IllegalArgumentException("你已经举报过该评论");
        }

        CommentReport report = new CommentReport();
        report.setCommentId(commentId);
        report.setReporterId(currentUser.getUserId());
        report.setReason(request.getReason());
        report.setDescription(request.getDescription());
        report.setStatus(CommentReport.STATUS_PENDING);
        commentReportMapper.insertReport(report);
    }

    public PageResult<CommentReport> getReports(int page, int size, Short status) {
        int offset = (page - 1) * size;
        List<CommentReport> list = commentReportMapper.findReports(status, offset, size);
        long total = commentReportMapper.countReports(status);
        return new PageResult<>(list, total, page, size);
    }

    @Transactional(rollbackFor = Exception.class)
    public void handleReport(Long reportId, Short status) {
        User currentUser = getCurrentUser();
        CommentReport report = commentReportMapper.findById(reportId);
        if (report == null) {
            throw new IllegalArgumentException("举报记录不存在");
        }
        commentReportMapper.updateReportStatus(reportId, status, currentUser.getUserId());

        if (status == CommentReport.STATUS_RESOLVED) {
            adminDeleteComment(report.getCommentId());
        }
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

    public long countByUserId(Long userId) {
        return commentMapper.countMyComments(userId);
    }

    public PageResult<CommentRow> getUserComments(Long userId, int page, int size) {
        int offset = (page - 1) * size;
        List<CommentRow> comments = commentMapper.findMyComments(userId, offset, size);
        for (CommentRow comment : comments) {
            comment.setImages(commentImageMapper.findImageUrlsByCommentId(comment.getCommentId()));
        }
        long total = commentMapper.countMyComments(userId);
        return new PageResult<>(comments, total, page, size);
    }

    private long getTotalBlogCount(int days) {
        return commentMapper.findTopBlogs(days, 1000).size();
    }

    private void createCommentNotification(Comment comment, User actor) {
        try {
            if (comment.getParentId() != null) {
                Comment parent = commentMapper.findCommentById(comment.getParentId());
                if (parent != null && !parent.getUserId().equals(actor.getUserId())) {
                    Notification notif = new Notification();
                    notif.setUserId(parent.getUserId());
                    notif.setType("reply");
                    notif.setMessage(actor.getUsername() + " 回复了你的评论");
                    notif.setLink("/blog/" + comment.getBlogId());
                    notif.setIsRead(false);
                    notificationService.createNotification(notif);
                }
            } else {
                Blog blog = blogMapper.findById(comment.getBlogId());
                if (blog != null && !blog.getAuthorId().equals(actor.getUserId())) {
                    Notification notif = new Notification();
                    notif.setUserId(blog.getAuthorId());
                    notif.setType("comment");
                    notif.setMessage(actor.getUsername() + " 评论了你的博客《" + blog.getTitle() + "》");
                    notif.setLink("/blog/" + comment.getBlogId());
                    notif.setIsRead(false);
                    notificationService.createNotification(notif);
                }
            }
        } catch (Exception e) {
            log.warn("Failed to create comment notification", e);
        }
    }

    private void createMentionNotifications(Comment comment, User actor) {
        try {
            Set<String> mentionedUsernames = parseMentions(comment.getContent());
            for (String username : mentionedUsernames) {
                User mentioned = loginMapper.findUserByUsername(username);
                if (mentioned != null && !mentioned.getUserId().equals(actor.getUserId())) {
                    Notification notif = new Notification();
                    notif.setUserId(mentioned.getUserId());
                    notif.setType("mention");
                    notif.setMessage(actor.getUsername() + " 在评论中提到了你");
                    notif.setLink("/blog/" + comment.getBlogId());
                    notif.setIsRead(false);
                    notificationService.createNotification(notif);
                }
            }
        } catch (Exception e) {
            log.warn("Failed to create mention notifications", e);
        }
    }

    static Set<String> parseMentions(String content) {
        if (content == null) return Set.of();
        Set<String> mentions = new LinkedHashSet<>();
        Matcher matcher = MENTION_PATTERN.matcher(content);
        while (matcher.find()) {
            String username = matcher.group(1);
            if (username.length() >= 2) {
                mentions.add(username);
            }
        }
        return mentions;
    }

    private void createLikeNotification(Comment comment, User actor) {
        try {
            if (!comment.getUserId().equals(actor.getUserId())) {
                Blog blog = blogMapper.findById(comment.getBlogId());
                String blogTitle = blog != null ? blog.getTitle() : "";
                Notification notif = new Notification();
                notif.setUserId(comment.getUserId());
                notif.setType("like");
                notif.setMessage(actor.getUsername() + " 赞了你在《" + blogTitle + "》的评论");
                notif.setLink("/blog/" + comment.getBlogId());
                notif.setIsRead(false);
                notificationService.createNotification(notif);
            }
        } catch (Exception e) {
            log.warn("Failed to create like notification", e);
        }
    }

    private CommentDetail buildCommentDetail(Comment comment) {
        User user = loginMapper.findUserById(comment.getUserId());
        CommentDetail detail = new CommentDetail();
        detail.setCommentId(comment.getCommentId());
        detail.setBlogId(comment.getBlogId());
        detail.setUserId(comment.getUserId());
        detail.setUsername(user != null ? user.getUsername() : "未知");
        detail.setAvatarUrl(user != null ? user.getAvatarUrl() : null);
        detail.setContent(comment.getContent());
        detail.setLikeCount(comment.getLikeCount());
        detail.setCreatedAt(comment.getCreatedAt());
        detail.setStatus(comment.getStatus());
        detail.setIsPinned(comment.getIsPinned());
        detail.setEditCount(comment.getEditCount());
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
