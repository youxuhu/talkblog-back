package com.revy.talkblogback.mapper;

import com.revy.talkblogback.pojo.Blog;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import java.util.List;

@Mapper
public interface BlogMapper {

    Blog findById(@Param("id") Long id);

    List<Blog> findList(@Param("keyword") String keyword,
                        @Param("offset") int offset,
                        @Param("limit") int limit);

    long count(@Param("keyword") String keyword);

    List<Blog> findByAuthorId(@Param("authorId") Long authorId,
                              @Param("offset") int offset,
                              @Param("limit") int limit);

    long countByAuthorId(@Param("authorId") Long authorId);

    int insert(Blog blog);

    int update(Blog blog);

    int deleteById(@Param("id") Long id);
}