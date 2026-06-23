package com.revy.talkblogback.pojo;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class Notification {
    private Long id;
    private Long userId;
    private String type;
    private String message;
    private String link;
    @JsonProperty("read")
    private Boolean isRead;
    private LocalDateTime createdAt;
}
