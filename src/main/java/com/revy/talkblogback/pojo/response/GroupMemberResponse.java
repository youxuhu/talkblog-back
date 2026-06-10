package com.revy.talkblogback.pojo.response;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class GroupMemberResponse {
    private Long id;
    @JsonProperty("group_id")
    private Long groupId;
    @JsonProperty("user_id")
    private Long userId;
    private Short role;
    @JsonProperty("joined_at")
    private LocalDateTime joinedAt;
    private String username;
    private String email;
    private String avatar;
}
