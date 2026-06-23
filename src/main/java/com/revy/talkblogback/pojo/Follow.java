package com.revy.talkblogback.pojo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class Follow {
    private Long id;
    private Long followerId;
    private Long followeeId;
    private LocalDateTime createdAt;
}
