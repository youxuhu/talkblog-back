package com.revy.talkblogback.pojo.request;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class UpdateCommentStatusRequest {

    @NotNull(message = "状态不能为空")
    private Short status;
}
