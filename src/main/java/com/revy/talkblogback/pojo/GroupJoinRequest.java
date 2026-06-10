package com.revy.talkblogback.pojo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class GroupJoinRequest {
    public static final short STATUS_PENDING = 0;
    public static final short STATUS_APPROVED = 1;
    public static final short STATUS_REJECTED = 2;

    private Long id;
    private Long groupId;
    private Long userId;
    private Short status;
    private String reason;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
