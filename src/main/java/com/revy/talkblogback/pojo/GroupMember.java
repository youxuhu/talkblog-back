package com.revy.talkblogback.pojo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class GroupMember {
    private Long id;
    private Long groupId;
    private Long userId;
    private Short role;
    private LocalDateTime joinedAt;

    public static final short ROLE_MEMBER = 1;
    public static final short ROLE_ADMIN = 2;
    public static final short ROLE_OWNER = 3;
}
