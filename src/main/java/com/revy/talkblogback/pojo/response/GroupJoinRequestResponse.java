package com.revy.talkblogback.pojo.response;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class GroupJoinRequestResponse {
    @JsonProperty("id")
    private Long id;
    @JsonProperty("group_id")
    private Long groupId;
    @JsonProperty("user_id")
    private Long userId;
    @JsonProperty("username")
    private String username;
    @JsonProperty("status")
    private Short status;
    @JsonProperty("reason")
    private String reason;
    @JsonProperty("created_at")
    private LocalDateTime createdAt;
}
