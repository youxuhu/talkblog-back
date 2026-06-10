package com.revy.talkblogback.pojo.response;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ChatMessageResponse {
    @JsonProperty("message_id")
    private Long messageId;
    @JsonProperty("group_id")
    private Long groupId;
    @JsonProperty("sender_id")
    private Long senderId;
    @JsonProperty("message_type")
    private Short messageType;
    private String content;
    @JsonProperty("file_url")
    private String fileUrl;
    @JsonProperty("file_name")
    private String fileName;
    @JsonProperty("file_size")
    private Long fileSize;
    @JsonProperty("created_at")
    private LocalDateTime createdAt;
    @JsonProperty("sender_username")
    private String senderUsername;
    @JsonProperty("sender_avatar")
    private String senderAvatar;
    @JsonProperty("is_recalled")
    private Short isRecalled;
}
