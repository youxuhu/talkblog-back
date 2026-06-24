package com.revy.talkblogback.pojo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class Blog {
    private Long id;
    private String title;
    private String content;
    private Long authorId;
    private String authorName;
    private String authorAvatar;
    private Short status;
    private Integer likeCount;
    private Boolean liked;
    private Boolean favorited;
    private Long categoryId;
    private String categoryName;
    private Long seriesId;
    private List<Tag> tags;
    private Integer viewCount;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public static final short STATUS_DRAFT = 0;
    public static final short STATUS_PUBLISHED = 1;
}