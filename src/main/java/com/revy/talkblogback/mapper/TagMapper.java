package com.revy.talkblogback.mapper;

import com.revy.talkblogback.pojo.Tag;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import java.util.List;

@Mapper
public interface TagMapper {

    List<Tag> findAll();

    List<Tag> searchByName(@Param("keyword") String keyword);

    Tag findById(@Param("id") Long id);

    Tag findByName(@Param("name") String name);

    List<Tag> findByBlogId(@Param("blogId") Long blogId);

    int insert(Tag tag);

    int deleteById(@Param("id") Long id);

    int insertBlogTag(@Param("blogId") Long blogId, @Param("tagId") Long tagId);

    int deleteBlogTags(@Param("blogId") Long blogId);
}
