package com.revy.talkblogback.pojo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class Comment {

    public static final short STATUS_PENDING = 0;
    public static final short STATUS_APPROVED = 1;
    public static final short STATUS_REJECTED = 2;

    private Long commentId;
    private Long blogId;
    private Long userId;
    private Long parentId;
    private Long replyToUserId;
    private String content;
    private Short status;
    private Integer likeCount;
    private String ipAddress;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
