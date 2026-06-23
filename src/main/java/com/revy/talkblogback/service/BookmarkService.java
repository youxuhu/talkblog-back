package com.revy.talkblogback.service;

import com.revy.talkblogback.mapper.BookmarkMapper;
import com.revy.talkblogback.pojo.Blog;
import com.revy.talkblogback.pojo.Bookmark;
import com.revy.talkblogback.pojo.response.PageResult;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class BookmarkService {

    private final BookmarkMapper bookmarkMapper;

    public BookmarkService(BookmarkMapper bookmarkMapper) {
        this.bookmarkMapper = bookmarkMapper;
    }

    @Transactional(rollbackFor = Exception.class)
    public boolean toggle(Long userId, Long blogId) {
        Bookmark existing = bookmarkMapper.findByUserAndBlog(userId, blogId);
        if (existing != null) {
            bookmarkMapper.delete(userId, blogId);
            return false;
        } else {
            Bookmark bookmark = new Bookmark();
            bookmark.setUserId(userId);
            bookmark.setBlogId(blogId);
            bookmarkMapper.insert(bookmark);
            return true;
        }
    }

    public boolean isBookmarked(Long userId, Long blogId) {
        return bookmarkMapper.findByUserAndBlog(userId, blogId) != null;
    }

    public long countByUserId(Long userId) {
        return bookmarkMapper.countBookmarkedBlogs(userId);
    }

    public PageResult<Blog> getBookmarkedBlogs(Long userId, int page, int size) {
        int safePage = Math.max(page, 1);
        int safeSize = Math.max(size, 1);
        int offset = (safePage - 1) * safeSize;

        List<Blog> list = bookmarkMapper.findBookmarkedBlogs(userId, offset, safeSize);
        long total = bookmarkMapper.countBookmarkedBlogs(userId);
        return new PageResult<>(list, total, safePage, safeSize);
    }
}
