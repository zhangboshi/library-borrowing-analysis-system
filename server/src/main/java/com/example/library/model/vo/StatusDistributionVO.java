package com.example.library.model.vo;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class StatusDistributionVO {
    private String status;
    private Long count;
}
