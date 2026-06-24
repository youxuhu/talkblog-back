package com.revy.talkblogback.pojo.response;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class AdminGroupDetailResponse {
    private ChatGroupResponse group;
    private List<GroupMemberResponse> members;
    private Long messageCount;
}
