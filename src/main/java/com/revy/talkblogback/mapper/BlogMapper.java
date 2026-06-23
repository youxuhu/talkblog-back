package com.revy.talkblogback.mapper;

import com.revy.talkblogback.pojo.Blog;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import java.util.List;

@Mapper
public interface BlogMapper {

    Blog findById(@Param("id") Long id);

    List<Blog> findList(@Param("keyword") String keyword,
                        @Param("seriesId") Long seriesId,
                        @Param("category") String category,
                        @Param("offset") int offset,
                        @Param("limit") int limit);

    long count(@Param("keyword") String keyword,
               @Param("seriesId") Long seriesId,
               @Param("category") String category);

    List<Blog> findByAuthorId(@Param("authorId") Long authorId,
                              @Param("status") Short status,
                              @Param("offset") int offset,
                              @Param("limit") int limit);

    long countByAuthorId(@Param("authorId") Long authorId,
                         @Param("status") Short status);

    int insert(Blog blog);

    int update(Blog blog);

    int deleteById(@Param("id") Long id);

    List<Blog> findScheduledToPublish(@Param("now") java.time.LocalDateTime now);

    int publishById(@Param("id") Long id);

    int incrementViewCount(@Param("id") Long id);
}