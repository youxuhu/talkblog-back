package com.revy.talkblogback.pojo.request;

import lombok.Data;

@Data
public class AddMemberRequest {
    private String userId;
    private String role;
}