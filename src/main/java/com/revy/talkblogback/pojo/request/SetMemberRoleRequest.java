package com.revy.talkblogback.pojo.request;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class SetMemberRoleRequest {
    private Long userId;
    private Short role;
}
