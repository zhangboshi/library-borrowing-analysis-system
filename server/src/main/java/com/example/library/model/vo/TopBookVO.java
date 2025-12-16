package com.example.library.model.vo;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class TopBookVO {
    private Long bookId;
    private String title;
    private Long count;
}
