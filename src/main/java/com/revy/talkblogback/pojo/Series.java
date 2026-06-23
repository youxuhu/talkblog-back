package com.revy.talkblogback.pojo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class Series {
    private Long id;
    private String name;
    private String description;
    private Long authorId;
    private String authorName;
    private Integer blogCount;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
