package com.revy.talkblogback.pojo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class CommentReport {
    public static final short STATUS_PENDING = 0;
    public static final short STATUS_DISMISSED = 1;
    public static final short STATUS_RESOLVED = 2;

    private Long id;
    private Long commentId;
    private Long reporterId;
    private String reason;
    private String description;
    private Short status;
    private Long handledBy;
    private LocalDateTime handledAt;
    private LocalDateTime createdAt;
}
