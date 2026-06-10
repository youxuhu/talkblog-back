package com.revy.talkblogback.pojo.request;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class SendMessageRequest {
    private Integer messageType;
    private String content;
    private String fileUrl;
    private String fileName;
    private Long fileSize;
}
