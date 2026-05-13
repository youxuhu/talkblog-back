package com.revy.talkblogback.service;

import com.revy.talkblogback.mapper.BlogMapper;
import com.revy.talkblogback.pojo.Blog;
import com.revy.talkblogback.pojo.response.PageResult;
import org.springframework.stereotype.Service;
import java.util.List;

@Service
public class BlogService {

    private final BlogMapper blogMapper;

    public BlogService(BlogMapper blogMapper) {
        this.blogMapper = blogMapper;
    }

    public Blog getById(Long id) {
        if (id == null) {
            return null;
        }
        return blogMapper.findById(id);
    }

    public PageResult<Blog> list(int page, int size, String keyword) {
        int safePage = Math.max(page, 1);
        int safeSize = Math.max(size, 1);
        int offset = (safePage - 1) * safeSize;

        List<Blog> list = blogMapper.findList(keyword, offset, safeSize);
        long total = blogMapper.count(keyword);
        return new PageResult<>(list, total, safePage, safeSize);
    }

    public PageResult<Blog> listMyBlogs(Long authorId, int page, int size) {
        if (authorId == null) {
            return new PageResult<>(List.of(), 0, page, size);
        }
        int safePage = Math.max(page, 1);
        int safeSize = Math.max(size, 1);
        int offset = (safePage - 1) * safeSize;

        List<Blog> list = blogMapper.findByAuthorId(authorId, offset, safeSize);
        long total = blogMapper.countByAuthorId(authorId);
        return new PageResult<>(list, total, safePage, safeSize);
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

        blog.setStatus(Blog.STATUS_PUBLISHED);
        blogMapper.insert(blog);
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

        if (hasText(blog.getTitle())) {
            existing.setTitle(blog.getTitle());
        }
        if (hasText(blog.getContent())) {
            existing.setContent(blog.getContent());
        }
        if (blog.getStatus() != null) {
            existing.setStatus(blog.getStatus());
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

    private boolean hasText(String str) {
        return str != null && !str.trim().isEmpty();
    }
}