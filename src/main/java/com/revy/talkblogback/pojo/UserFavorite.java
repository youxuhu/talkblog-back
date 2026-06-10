package com.revy.talkblogback.pojo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class UserFavorite {
    private Long id;
    private Long userId;
    private Long blogId;
    private LocalDateTime createdAt;
}
