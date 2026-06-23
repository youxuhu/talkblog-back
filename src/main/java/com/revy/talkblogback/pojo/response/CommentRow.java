package com.revy.talkblogback.pojo.response;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class CommentRow {

    private Long commentId;
    private Long blogId;
    private String blogTitle;
    private Long userId;
    private String username;
    private String avatarUrl;
    private String email;
    private String content;
    private Integer likeCount;
    private String ipAddress;
    private Short status;
    private Boolean isPinned;
    private LocalDateTime createdAt;
    private List<String> images;
}
