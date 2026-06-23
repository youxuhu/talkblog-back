package com.revy.talkblogback.pojo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class SensitiveWord {
    private Long id;
    private String word;
    private String replacement;
    private LocalDateTime createdAt;
}
