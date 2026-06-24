package com.revy.talkblogback.pojo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class InteractionUser {
    private Long userId;
    private String username;
    private String avatarUrl;
    private LocalDateTime interactedAt;
}
