package com.revy.talkblogback.pojo.response;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ChatGroupResponse {
    @JsonProperty("group_id")
    private Long groupId;
    @JsonProperty("group_name")
    private String groupName;
    @JsonProperty("group_avatar")
    private String groupAvatar;
    @JsonProperty("owner_id")
    private Long ownerId;
    @JsonProperty("group_type")
    private Short groupType;
    @JsonProperty("is_public")
    private Short isPublic;
    @JsonProperty("created_at")
    private LocalDateTime createdAt;
    @JsonProperty("updated_at")
    private LocalDateTime updatedAt;
    @JsonProperty("member_count")
    private Long memberCount;
    @JsonProperty("owner_name")
    private String ownerName;
}
