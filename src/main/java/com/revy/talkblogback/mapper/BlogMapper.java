package com.revy.talkblogback.mapper;

import com.revy.talkblogback.pojo.Blog;
import com.revy.talkblogback.pojo.BlogLike;
import com.revy.talkblogback.pojo.InteractionUser;
import com.revy.talkblogback.pojo.UserFavorite;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import java.util.List;

@Mapper
public interface BlogMapper {

    Blog findById(@Param("id") Long id);

    List<Blog> findList(@Param("keyword") String keyword,
                        @Param("categoryId") Long categoryId,
                        @Param("sortBy") String sortBy,
                        @Param("offset") int offset,
                        @Param("limit") int limit);

    long count(@Param("keyword") String keyword,
               @Param("categoryId") Long categoryId);

    List<Blog> findByAuthorId(@Param("authorId") Long authorId,
                              @Param("offset") int offset,
                              @Param("limit") int limit);

    long countByAuthorId(@Param("authorId") Long authorId);

    List<Blog> findByTagId(@Param("tagId") Long tagId,
                           @Param("offset") int offset,
                           @Param("limit") int limit);

    long countByTagId(@Param("tagId") Long tagId);

    List<Blog> findPopular(@Param("limit") int limit);

    List<Blog> findTrending(@Param("days") int days, @Param("limit") int limit);

    int insert(Blog blog);

    int update(Blog blog);

    int deleteById(@Param("id") Long id);

    // --- likes ---

    int incrementLikeCount(@Param("blogId") Long blogId);

    int decrementLikeCount(@Param("blogId") Long blogId);

    BlogLike findBlogLike(@Param("blogId") Long blogId, @Param("userId") Long userId);

    int insertBlogLike(BlogLike blogLike);

    int deleteBlogLike(@Param("blogId") Long blogId, @Param("userId") Long userId);

    // --- favorites ---

    UserFavorite findFavorite(@Param("userId") Long userId, @Param("blogId") Long blogId);

    int insertFavorite(UserFavorite favorite);

    int deleteFavorite(@Param("userId") Long userId, @Param("blogId") Long blogId);

    List<Blog> findFavoriteBlogs(@Param("userId") Long userId,
                                  @Param("offset") int offset,
                                  @Param("limit") int limit);

    long countFavoriteBlogs(@Param("userId") Long userId);

    // --- views ---

    int incrementViewCount(@Param("blogId") Long blogId);

    int insertBlogView(@Param("blogId") Long blogId,
                       @Param("ipAddress") String ipAddress,
                       @Param("userId") Long userId);

    int findBlogViewToday(@Param("blogId") Long blogId,
                          @Param("ipAddress") String ipAddress);

    int findBlogViewTodayByUser(@Param("blogId") Long blogId,
                                @Param("userId") Long userId);

    // --- interaction users ---

    List<InteractionUser> findUsersWhoLiked(@Param("blogId") Long blogId,
                                            @Param("offset") int offset,
                                            @Param("limit") int limit);

    int countUsersWhoLiked(@Param("blogId") Long blogId);

    List<InteractionUser> findUsersWhoFavorited(@Param("blogId") Long blogId,
                                                 @Param("offset") int offset,
                                                 @Param("limit") int limit);

    int countUsersWhoFavorited(@Param("blogId") Long blogId);

    List<InteractionUser> findUsersWhoViewed(@Param("blogId") Long blogId,
                                              @Param("offset") int offset,
                                              @Param("limit") int limit);

    int countUsersWhoViewed(@Param("blogId") Long blogId);
}
