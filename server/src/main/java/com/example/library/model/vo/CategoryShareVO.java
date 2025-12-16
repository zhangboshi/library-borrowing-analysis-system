package com.example.library.model.vo;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class CategoryShareVO {
    private String category;
    private Long count;
}
