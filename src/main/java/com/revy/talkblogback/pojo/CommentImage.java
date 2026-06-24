package com.revy.talkblogback.pojo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class CommentImage {
    private Long imageId;
    private Long commentId;
    private String imageUrl;
    private Integer imageOrder;
    private LocalDateTime createdAt;
}
