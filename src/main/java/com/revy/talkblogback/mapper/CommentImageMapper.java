package com.revy.talkblogback.mapper;

import com.revy.talkblogback.pojo.CommentImage;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface CommentImageMapper {

    int insertCommentImage(CommentImage commentImage);

    int insertCommentImages(@Param("commentId") Long commentId, @Param("images") List<String> imageUrls);

    List<CommentImage> findByCommentId(@Param("commentId") Long commentId);

    @org.apache.ibatis.annotations.Select("SELECT image_url FROM comment_images WHERE comment_id = #{commentId} ORDER BY image_order ASC")
    List<String> findImageUrlsByCommentId(@Param("commentId") Long commentId);

    int deleteByCommentId(@Param("commentId") Long commentId);
}
