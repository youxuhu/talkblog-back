package com.revy.talkblogback.mapper;

import com.revy.talkblogback.pojo.Category;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import java.util.List;

@Mapper
public interface CategoryMapper {

    List<Category> findAll();

    Category findById(@Param("id") Long id);

    Category findBySlug(@Param("slug") String slug);

    int insert(Category category);

    int update(Category category);

    int deleteById(@Param("id") Long id);
}
