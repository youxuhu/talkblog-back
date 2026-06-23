package com.revy.talkblogback.service;

import com.revy.talkblogback.mapper.BlogVersionMapper;
import com.revy.talkblogback.pojo.Blog;
import com.revy.talkblogback.pojo.BlogVersion;
import com.revy.talkblogback.pojo.response.PageResult;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class BlogVersionService {

    private final BlogVersionMapper blogVersionMapper;

    public BlogVersionService(BlogVersionMapper blogVersionMapper) {
        this.blogVersionMapper = blogVersionMapper;
    }

    @Transactional(rollbackFor = Exception.class)
    public void saveVersion(Blog blog) {
        BlogVersion latest = blogVersionMapper.findLatestByBlogId(blog.getId());
        int nextVersion = (latest != null) ? latest.getVersion() + 1 : 1;

        BlogVersion version = new BlogVersion();
        version.setBlogId(blog.getId());
        version.setTitle(blog.getTitle());
        version.setContent(blog.getContent());
        version.setCategory(blog.getCategory());
        version.setSeriesId(blog.getSeriesId());
        version.setVersion(nextVersion);
        blogVersionMapper.insert(version);
    }

    public PageResult<BlogVersion> getVersions(Long blogId, int page, int size) {
        int safePage = Math.max(page, 1);
        int safeSize = Math.max(size, 1);
        int offset = (safePage - 1) * safeSize;

        List<BlogVersion> list = blogVersionMapper.findByBlogId(blogId, offset, safeSize);
        long total = blogVersionMapper.countByBlogId(blogId);
        return new PageResult<>(list, total, safePage, safeSize);
    }
}
