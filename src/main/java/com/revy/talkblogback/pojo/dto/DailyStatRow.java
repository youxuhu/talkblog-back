package com.revy.talkblogback.pojo.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class DailyStatRow {
    private String date;
    private Integer messageCount;
    private Integer activeMembers;
    private Integer newMembers;
}