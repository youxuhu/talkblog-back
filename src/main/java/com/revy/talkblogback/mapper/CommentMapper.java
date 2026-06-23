package com.revy.talkblogback.mapper;

import com.revy.talkblogback.pojo.Comment;
import com.revy.talkblogback.pojo.CommentLike;
import com.revy.talkblogback.pojo.response.CommentDetail;
import com.revy.talkblogback.pojo.response.CommentRow;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;
import java.util.Map;

@Mapper
public interface CommentMapper {

    int insertComment(Comment comment);

    Comment findCommentById(@Param("commentId") Long commentId);

    List<CommentDetail> findCommentsByBlogId(@Param("blogId") Long blogId,
                                             @Param("parentId") Long parentId,
                                             @Param("status") Short status,
                                             @Param("sort") String sort,
                                             @Param("offset") int offset,
                                             @Param("limit") int limit);

    long countCommentsByBlogId(@Param("blogId") Long blogId,
                               @Param("parentId") Long parentId,
                               @Param("status") Short status);

    List<CommentDetail> findRepliesByParentId(@Param("parentId") Long parentId);

    int updateCommentStatus(@Param("commentId") Long commentId, @Param("status") Short status);

    int deleteComment(@Param("commentId") Long commentId, @Param("userId") Long userId);

    int deleteCommentAdmin(@Param("commentId") Long commentId);

    int updateCommentContent(@Param("commentId") Long commentId, @Param("content") String content);

    int togglePin(@Param("commentId") Long commentId);

    Boolean findIsPinned(@Param("commentId") Long commentId);

    int incrementLikeCount(@Param("commentId") Long commentId);

    int decrementLikeCount(@Param("commentId") Long commentId);

    CommentLike findCommentLike(@Param("commentId") Long commentId, @Param("userId") Long userId);

    int insertCommentLike(CommentLike commentLike);

    int deleteCommentLike(@Param("commentId") Long commentId, @Param("userId") Long userId);

    List<CommentRow> findAdminComments(@Param("keyword") String keyword,
                                       @Param("status") Short status,
                                       @Param("blogId") String blogId,
                                       @Param("offset") int offset,
                                       @Param("limit") int limit);

    long countAdminComments(@Param("keyword") String keyword,
                            @Param("status") Short status,
                            @Param("blogId") String blogId);

    List<CommentRow> findMyComments(@Param("userId") Long userId,
                                    @Param("offset") int offset,
                                    @Param("limit") int limit);

    long countMyComments(@Param("userId") Long userId);

    Map<String, Object> findCommentStats(@Param("days") int days);

    List<Map<String, Object>> findDailyTrend(@Param("days") int days);

    List<Map<String, Object>> findTopBlogs(@Param("days") int days, @Param("limit") int limit);

    List<Map<String, Object>> findTopCommenters(@Param("days") int days, @Param("limit") int limit);
}
