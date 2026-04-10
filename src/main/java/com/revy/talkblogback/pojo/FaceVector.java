package com.revy.talkblogback.pojo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class FaceVector {
    private Long vectorId;
    private Long userId;
    private String faceVector;
    private String faceImageUrl;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
