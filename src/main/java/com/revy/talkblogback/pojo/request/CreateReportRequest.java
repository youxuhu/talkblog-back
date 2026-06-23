package com.revy.talkblogback.pojo.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class CreateReportRequest {

    @NotBlank(message = "举报原因不能为空")
    @Size(max = 50, message = "举报原因不能超过50字")
    private String reason;

    @Size(max = 500, message = "举报描述不能超过500字")
    private String description;
}
