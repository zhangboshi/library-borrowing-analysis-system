package com.example.library.model.vo;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class FrequencyBucketVO {
    private String bucket;
    private Long count;
}
