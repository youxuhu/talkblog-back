package com.revy.talkblogback.pojo.request;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class BatchReviewRequest {

    @NotEmpty(message = "评论 ID 列表不能为空")
    private List<Long> commentIds;

    @NotNull(message = "状态不能为空")
    private Short status;
}
