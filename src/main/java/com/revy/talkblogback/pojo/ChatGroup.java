package com.revy.talkblogback.pojo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ChatGroup {
    private Long groupId;
    private String groupName;
    private String groupAvatar;
    private Long ownerId;
    private Short groupType;
    private Short isPublic;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public static final short GROUP_TYPE_GROUP = 1;
    public static final short GROUP_TYPE_PRIVATE = 2;

    public static final short PUBLIC_YES = 1;
    public static final short PUBLIC_NO = 2;
}
