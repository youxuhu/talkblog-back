package com.revy.talkblogback.pojo.request;

import lombok.Data;

@Data
public class UpdateChatroomRequest {
    private String name;
    private String description;
    private Integer maxMembers;
    private Boolean isPrivate;
    private String status;
}