package com.revy.talkblogback.pojo.request;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class CreateGroupRequest {
    private String groupName;
    private String groupType;
    private String isPublic;
    private List<Long> memberIds;
}
