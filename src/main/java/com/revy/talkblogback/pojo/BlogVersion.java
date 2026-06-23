package com.revy.talkblogback.pojo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class BlogVersion {
    private Long id;
    private Long blogId;
    private String title;
    private String content;
    private String category;
    private Long seriesId;
    private Integer version;
    private LocalDateTime createdAt;
}
