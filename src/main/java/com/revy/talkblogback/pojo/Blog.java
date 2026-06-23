package com.revy.talkblogback.pojo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;

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
    private String category;
    private Long seriesId;
    private String seriesName;
    private Long viewCount;
    private LocalDateTime scheduledAt;
    private Short status;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public static final short STATUS_DRAFT = 0;
    public static final short STATUS_PUBLISHED = 1;
}