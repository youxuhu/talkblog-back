package com.revy.talkblogback.pojo.request;

import lombok.Data;

@Data
public class CreateChatroomRequest {
    private String name;
    private String description;
    private Integer maxMembers;
    private Boolean isPrivate;
}