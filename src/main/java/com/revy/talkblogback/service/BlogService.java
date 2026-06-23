package com.revy.talkblogback.service;

import com.revy.talkblogback.mapper.BlogMapper;
import com.revy.talkblogback.pojo.Blog;
import com.revy.talkblogback.pojo.Notification;
import com.revy.talkblogback.pojo.response.PageResult;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.time.LocalDateTime;
import java.util.List;

@Service
public class BlogService {

    private static final Logger log = LoggerFactory.getLogger(BlogService.class);

    private final BlogMapper blogMapper;
    private final NotificationService notificationService;
    private final SensitiveWordService sensitiveWordService;
    private final BlogVersionService blogVersionService;

    public BlogService(BlogMapper blogMapper, NotificationService notificationService,
                       SensitiveWordService sensitiveWordService, BlogVersionService blogVersionService) {
        this.blogMapper = blogMapper;
        this.notificationService = notificationService;
        this.sensitiveWordService = sensitiveWordService;
        this.blogVersionService = blogVersionService;
    }

    public Blog getById(Long id) {
        if (id == null) {
            return null;
        }
        return blogMapper.findById(id);
    }

    public PageResult<Blog> list(int page, int size, String keyword, Long seriesId, String category) {
        int safePage = Math.max(page, 1);
        int safeSize = Math.max(size, 1);
        int offset = (safePage - 1) * safeSize;

        List<Blog> list = blogMapper.findList(keyword, seriesId, category, offset, safeSize);
        long total = blogMapper.count(keyword, seriesId, category);
        return new PageResult<>(list, total, safePage, safeSize);
    }

    public PageResult<Blog> listMyBlogs(Long authorId, int page, int size, Short status) {
        if (authorId == null) {
            return new PageResult<>(List.of(), 0, page, size);
        }
        int safePage = Math.max(page, 1);
        int safeSize = Math.max(size, 1);
        int offset = (safePage - 1) * safeSize;

        List<Blog> list = blogMapper.findByAuthorId(authorId, status, offset, safeSize);
        long total = blogMapper.countByAuthorId(authorId, status);
        return new PageResult<>(list, total, safePage, safeSize);
    }

    public long countByAuthorId(Long authorId, Short status) {
        if (authorId == null) return 0;
        return blogMapper.countByAuthorId(authorId, status);
    }

    public Blog getByIdAndIncrementViewCount(Long id) {
        if (id == null) {
            return null;
        }
        blogMapper.incrementViewCount(id);
        return blogMapper.findById(id);
    }

    public Blog create(Blog blog) {
        if (blog == null) {
            throw new IllegalArgumentException("Blog is required.");
        }
        if (blog.getAuthorId() == null) {
            throw new IllegalArgumentException("Author is required.");
        }
        if (!hasText(blog.getTitle())) {
            throw new IllegalArgumentException("Title is required.");
        }
        if (!hasText(blog.getContent())) {
            throw new IllegalArgumentException("Content is required.");
        }

        blog.setTitle(sensitiveWordService.filter(blog.getTitle()));
        blog.setContent(sensitiveWordService.filter(blog.getContent()));

        if (blog.getScheduledAt() != null) {
            blog.setStatus(Blog.STATUS_DRAFT);
        } else {
            blog.setStatus(Blog.STATUS_PUBLISHED);
        }
        if (blog.getCategory() == null) {
            blog.setCategory("");
        }
        blogMapper.insert(blog);

        blogVersionService.saveVersion(blog);

        return blog;
    }

    public Blog update(Long id, Blog blog) {
        if (id == null) {
            throw new IllegalArgumentException("Blog ID is required.");
        }
        if (blog == null) {
            throw new IllegalArgumentException("Blog is required.");
        }

        Blog existing = blogMapper.findById(id);
        if (existing == null) {
            throw new IllegalArgumentException("Blog not found.");
        }

        boolean contentChanged = false;

        if (hasText(blog.getTitle())) {
            existing.setTitle(blog.getTitle());
            contentChanged = true;
        }
        if (hasText(blog.getContent())) {
            existing.setContent(blog.getContent());
            contentChanged = true;
        }
        if (blog.getCategory() != null) {
            existing.setCategory(blog.getCategory());
        }
        if (blog.getSeriesId() != null) {
            existing.setSeriesId(blog.getSeriesId());
        }
        if (blog.getScheduledAt() != null) {
            existing.setScheduledAt(blog.getScheduledAt());
        }
        if (blog.getStatus() != null) {
            existing.setStatus(blog.getStatus());
        }

        existing.setTitle(sensitiveWordService.filter(existing.getTitle()));
        existing.setContent(sensitiveWordService.filter(existing.getContent()));

        if (contentChanged) {
            blogVersionService.saveVersion(existing);
        }

        blogMapper.update(existing);
        return blogMapper.findById(id);
    }

    public boolean delete(Long id) {
        if (id == null) {
            return false;
        }
        return blogMapper.deleteById(id) > 0;
    }

    @Scheduled(fixedRate = 60000)
    @Transactional(rollbackFor = Exception.class)
    public int publishScheduledBlogs() {
        List<Blog> blogs = blogMapper.findScheduledToPublish(LocalDateTime.now());
        if (blogs.isEmpty()) {
            return 0;
        }
        for (Blog blog : blogs) {
            blogMapper.publishById(blog.getId());
            try {
                Notification notif = new Notification();
                notif.setUserId(blog.getAuthorId());
                notif.setType("blog_published");
                notif.setMessage("你的博客《" + blog.getTitle() + "》已定时发布");
                notif.setLink("/blog/" + blog.getId());
                notif.setIsRead(false);
                notificationService.createNotification(notif);
            } catch (Exception e) {
                log.warn("Failed to send publish notification for blog {}", blog.getId(), e);
            }
        }
        log.info("Published {} scheduled blogs", blogs.size());
        return blogs.size();
    }

    private boolean hasText(String str) {
        return str != null && !str.trim().isEmpty();
    }
}