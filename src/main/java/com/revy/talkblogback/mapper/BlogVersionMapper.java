package com.revy.talkblogback.mapper;

import com.revy.talkblogback.pojo.BlogVersion;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface BlogVersionMapper {

    int insert(BlogVersion blogVersion);

    List<BlogVersion> findByBlogId(@Param("blogId") Long blogId,
                                    @Param("offset") int offset,
                                    @Param("limit") int limit);

    long countByBlogId(@Param("blogId") Long blogId);

    BlogVersion findLatestByBlogId(@Param("blogId") Long blogId);
}
