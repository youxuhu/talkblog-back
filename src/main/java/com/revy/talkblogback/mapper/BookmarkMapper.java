package com.revy.talkblogback.mapper;

import com.revy.talkblogback.pojo.Bookmark;
import com.revy.talkblogback.pojo.Blog;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface BookmarkMapper {

    int insert(Bookmark bookmark);

    int delete(@Param("userId") Long userId, @Param("blogId") Long blogId);

    Bookmark findByUserAndBlog(@Param("userId") Long userId, @Param("blogId") Long blogId);

    List<Blog> findBookmarkedBlogs(@Param("userId") Long userId,
                                   @Param("offset") int offset,
                                   @Param("limit") int limit);

    long countBookmarkedBlogs(@Param("userId") Long userId);
}
