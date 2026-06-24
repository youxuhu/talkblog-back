package com.revy.talkblogback.pojo.response;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class CommentDetail {

    private Long commentId;
    private Long blogId;
    private Long userId;
    private String username;
    private String content;
    private Integer likeCount;
    private LocalDateTime createdAt;
    private Short status;
    private String replyToUsername;
    private List<CommentDetail> replies;
    private List<String> images;
}
