package com.revy.talkblogback.pojo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class GroupBanRecord {
    private Long id;
    private Long groupId;
    private Long userId;
    private String reason;
    private LocalDateTime bannedAt;
    private LocalDateTime expiredAt;
    private Long bannedBy;
}
