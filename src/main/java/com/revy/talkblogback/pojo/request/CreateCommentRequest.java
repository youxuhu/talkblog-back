package com.revy.talkblogback.pojo.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class CreateCommentRequest {

    public static final int CONTENT_MAX_LENGTH = 500;

    @NotNull(message = "博客 ID 不能为空")
    private Long blogId;

    @NotBlank(message = "评论内容不能为空")
    private String content;

    private Long parentId;

    private Long replyToUserId;
}
