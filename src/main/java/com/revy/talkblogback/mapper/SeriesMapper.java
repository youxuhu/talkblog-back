package com.revy.talkblogback.mapper;

import com.revy.talkblogback.pojo.Blog;
import com.revy.talkblogback.pojo.Series;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface SeriesMapper {

    Series findById(@Param("id") Long id);

    List<Series> findList(@Param("offset") int offset, @Param("limit") int limit);

    long count();

    List<Series> findAll();

    int insert(Series series);

    int update(Series series);

    int deleteById(@Param("id") Long id);

    List<Blog> findBlogsBySeriesId(@Param("seriesId") Long seriesId,
                                   @Param("offset") int offset,
                                   @Param("limit") int limit);

    long countBlogsBySeriesId(@Param("seriesId") Long seriesId);

    List<Series> findByAuthorId(@Param("authorId") Long authorId,
                                 @Param("offset") int offset,
                                 @Param("limit") int limit);

    long countByAuthorId(@Param("authorId") Long authorId);
}
