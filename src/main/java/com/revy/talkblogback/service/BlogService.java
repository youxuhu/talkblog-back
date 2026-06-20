package com.revy.talkblogback.service;

import com.revy.talkblogback.auth.AuthContext;
import com.revy.talkblogback.mapper.BlogMapper;
import com.revy.talkblogback.mapper.TagMapper;
import com.revy.talkblogback.pojo.Blog;
import com.revy.talkblogback.pojo.BlogLike;
import com.revy.talkblogback.pojo.InteractionUser;
import com.revy.talkblogback.pojo.Tag;
import com.revy.talkblogback.pojo.UserFavorite;
import com.revy.talkblogback.pojo.response.PageResult;
import com.revy.talkblogback.pojo.response.UserProfile;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;

@Service
public class BlogService {

    private final BlogMapper blogMapper;
    private final TagMapper tagMapper;
    private final RecommendationService recommendationService;

    public BlogService(BlogMapper blogMapper, TagMapper tagMapper, RecommendationService recommendationService) {
        this.blogMapper = blogMapper;
        this.tagMapper = tagMapper;
        this.recommendationService = recommendationService;
    }

    public Blog getById(Long id) {
        return getById(id, null);
    }

    public Blog getById(Long id, Long currentUserId) {
        if (id == null) {
            return null;
        }
        Blog blog = blogMapper.findById(id);
        if (blog != null) {
            blog.setTags(tagMapper.findByBlogId(id));
            if (currentUserId != null) {
                blog.setLiked(blogMapper.findBlogLike(id, currentUserId) != null);
                blog.setFavorited(blogMapper.findFavorite(currentUserId, id) != null);
            }
        }
        return blog;
    }

    public void recordView(Long blogId, String ipAddress) {
        if (blogId == null) return;
        Long userId = null;
        try {
            UserProfile profile = AuthContext.get();
            if (profile != null) userId = profile.getUserId();
        } catch (Exception ignored) {}

        boolean alreadyViewed = false;
        if (userId != null) {
            int todayViews = blogMapper.findBlogViewTodayByUser(blogId, userId);
            if (todayViews > 0) alreadyViewed = true;
        } else if (ipAddress != null) {
            int todayViews = blogMapper.findBlogViewToday(blogId, ipAddress);
            if (todayViews > 0) alreadyViewed = true;
        }
        if (!alreadyViewed) {
            blogMapper.incrementViewCount(blogId);
            blogMapper.insertBlogView(blogId, ipAddress, userId);
        }
        if (userId != null) {
            recommendationService.recordBehavior(blogId, "view", 1.0);
        }
    }

    public PageResult<Blog> list(int page, int size, String keyword) {
        return list(page, size, keyword, null, null, null, null);
    }

    public PageResult<Blog> list(int page, int size, String keyword, Long currentUserId) {
        return list(page, size, keyword, null, null, currentUserId, null);
    }

    public PageResult<Blog> list(int page, int size, String keyword, Long categoryId, Long tagId, Long currentUserId) {
        return list(page, size, keyword, categoryId, tagId, currentUserId, null);
    }

    public PageResult<Blog> list(int page, int size, String keyword, Long categoryId, Long tagId, Long currentUserId, String sortBy) {
        int safePage = Math.max(page, 1);
        int safeSize = Math.max(size, 1);
        int offset = (safePage - 1) * safeSize;

        List<Blog> list;
        long total;
        if (tagId != null) {
            list = blogMapper.findByTagId(tagId, offset, safeSize);
            total = blogMapper.countByTagId(tagId);
        } else {
            list = blogMapper.findList(keyword, categoryId, sortBy, offset, safeSize);
            total = blogMapper.count(keyword, categoryId);
        }
        if (currentUserId != null) {
            for (Blog blog : list) {
                blog.setFavorited(blogMapper.findFavorite(currentUserId, blog.getId()) != null);
                blog.setLiked(blogMapper.findBlogLike(blog.getId(), currentUserId) != null);
            }
        }
        return new PageResult<>(list, total, safePage, safeSize);
    }

    public PageResult<Blog> listMyBlogs(Long authorId, int page, int size) {
        if (authorId == null) {
            return new PageResult<>(List.of(), 0, page, size);
        }
        int safePage = Math.max(page, 1);
        int safeSize = Math.max(size, 1);
        int offset = (safePage - 1) * safeSize;

        List<Blog> list = blogMapper.findByAuthorId(authorId, offset, safeSize);
        long total = blogMapper.countByAuthorId(authorId);
        return new PageResult<>(list, total, safePage, safeSize);
    }

    @Transactional(rollbackFor = Exception.class)
    public Blog create(Blog blog) {
        if (blog == null) {
            throw new IllegalArgumentException("Blog is required.");
        }
        if (blog.getAuthorId() == null) {
            throw new IllegalArgumentException("Author is required.");
        }
        if (!hasText(blog.getTitle())) {
            throw new IllegalArgumentException("Title is required.");
        }
        if (!hasText(blog.getContent())) {
            throw new IllegalArgumentException("Content is required.");
        }

        blog.setStatus(Blog.STATUS_PUBLISHED);
        blogMapper.insert(blog);

        saveTags(blog.getId(), blog.getTags());
        return blog;
    }

    @Transactional(rollbackFor = Exception.class)
    public Blog update(Long id, Blog blog) {
        if (id == null) {
            throw new IllegalArgumentException("Blog ID is required.");
        }
        if (blog == null) {
            throw new IllegalArgumentException("Blog is required.");
        }

        Blog existing = blogMapper.findById(id);
        if (existing == null) {
            throw new IllegalArgumentException("Blog not found.");
        }

        if (hasText(blog.getTitle())) {
            existing.setTitle(blog.getTitle());
        }
        if (hasText(blog.getContent())) {
            existing.setContent(blog.getContent());
        }
        if (blog.getStatus() != null) {
            existing.setStatus(blog.getStatus());
        }
        if (blog.getCategoryId() != null) {
            existing.setCategoryId(blog.getCategoryId());
        }

        blogMapper.update(existing);

        if (blog.getTags() != null) {
            saveTags(id, blog.getTags());
        }

        return blogMapper.findById(id);
    }

    private void saveTags(Long blogId, List<Tag> tags) {
        tagMapper.deleteBlogTags(blogId);
        if (tags != null && !tags.isEmpty()) {
            for (Tag tag : tags) {
                Long tagId = tag.getId();
                if (tagId == null && hasText(tag.getName())) {
                    Tag existing = tagMapper.findByName(tag.getName().trim());
                    if (existing != null) {
                        tagId = existing.getId();
                    } else {
                        Tag newTag = new Tag();
                        newTag.setName(tag.getName().trim());
                        newTag.setSlug(tag.getName().trim().toLowerCase().replaceAll("\\s+", "-"));
                        tagMapper.insert(newTag);
                        tagId = newTag.getId();
                    }
                }
                if (tagId != null) {
                    tagMapper.insertBlogTag(blogId, tagId);
                }
            }
        }
    }

    public boolean delete(Long id) {
        if (id == null) {
            return false;
        }
        return blogMapper.deleteById(id) > 0;
    }

    @Transactional(rollbackFor = Exception.class)
    public Map<String, Object> likeBlog(Long blogId) {
        Long currentUserId = getCurrentUserId();
        Blog blog = blogMapper.findById(blogId);
        if (blog == null) {
            throw new IllegalArgumentException("Blog not found.");
        }

        BlogLike existingLike = blogMapper.findBlogLike(blogId, currentUserId);
        if (existingLike != null) {
            blogMapper.deleteBlogLike(blogId, currentUserId);
            blogMapper.decrementLikeCount(blogId);
            int newCount = Math.max((blog.getLikeCount() == null ? 0 : blog.getLikeCount()) - 1, 0);
            return Map.of("liked", false, "likeCount", newCount);
        } else {
            BlogLike like = new BlogLike();
            like.setBlogId(blogId);
            like.setUserId(currentUserId);
            blogMapper.insertBlogLike(like);
            blogMapper.incrementLikeCount(blogId);
            int newCount = (blog.getLikeCount() == null ? 0 : blog.getLikeCount()) + 1;
            recommendationService.recordBehavior(blogId, "like", 3.0);
            return Map.of("liked", true, "likeCount", newCount);
        }
    }

    @Transactional(rollbackFor = Exception.class)
    public Map<String, Object> favoriteBlog(Long blogId) {
        Long currentUserId = getCurrentUserId();
        Blog blog = blogMapper.findById(blogId);
        if (blog == null) {
            throw new IllegalArgumentException("Blog not found.");
        }

        UserFavorite existingFav = blogMapper.findFavorite(currentUserId, blogId);
        if (existingFav != null) {
            blogMapper.deleteFavorite(currentUserId, blogId);
            return Map.of("favorited", false);
        } else {
            UserFavorite fav = new UserFavorite();
            fav.setUserId(currentUserId);
            fav.setBlogId(blogId);
            blogMapper.insertFavorite(fav);
            recommendationService.recordBehavior(blogId, "favorite", 5.0);
            return Map.of("favorited", true);
        }
    }

    public PageResult<Blog> getFavorites(int page, int size) {
        Long currentUserId = getCurrentUserId();
        int safePage = Math.max(page, 1);
        int safeSize = Math.max(size, 1);
        int offset = (safePage - 1) * safeSize;

        List<Blog> list = blogMapper.findFavoriteBlogs(currentUserId, offset, safeSize);
        for (Blog blog : list) {
            blog.setFavorited(true);
        }
        long total = blogMapper.countFavoriteBlogs(currentUserId);
        return new PageResult<>(list, total, safePage, safeSize);
    }

    public List<Blog> getPopular(int limit) {
        return blogMapper.findPopular(limit);
    }

    public List<Blog> getTrending(int days, int limit) {
        return blogMapper.findTrending(days, limit);
    }

    public List<InteractionUser> getUsersWhoLiked(Long blogId, int page, int size) {
        int safePage = Math.max(page, 1);
        int safeSize = Math.max(size, 1);
        int offset = (safePage - 1) * safeSize;
        return blogMapper.findUsersWhoLiked(blogId, offset, safeSize);
    }

    public int countUsersWhoLiked(Long blogId) {
        return blogMapper.countUsersWhoLiked(blogId);
    }

    public List<InteractionUser> getUsersWhoFavorited(Long blogId, int page, int size) {
        int safePage = Math.max(page, 1);
        int safeSize = Math.max(size, 1);
        int offset = (safePage - 1) * safeSize;
        return blogMapper.findUsersWhoFavorited(blogId, offset, safeSize);
    }

    public int countUsersWhoFavorited(Long blogId) {
        return blogMapper.countUsersWhoFavorited(blogId);
    }

    public List<InteractionUser> getUsersWhoViewed(Long blogId, int page, int size) {
        int safePage = Math.max(page, 1);
        int safeSize = Math.max(size, 1);
        int offset = (safePage - 1) * safeSize;
        return blogMapper.findUsersWhoViewed(blogId, offset, safeSize);
    }

    public int countUsersWhoViewed(Long blogId) {
        return blogMapper.countUsersWhoViewed(blogId);
    }

    private Long getCurrentUserId() {
        UserProfile profile = AuthContext.get();
        if (profile == null) {
            throw new IllegalArgumentException("未登录");
        }
        return profile.getUserId();
    }

    private boolean hasText(String str) {
        return str != null && !str.trim().isEmpty();
    }
}
