package com.example.library.model.vo;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Builder
public class BookVO {
    private Long id;
    private String title;
    private String author;
    private String category;
    private String isbn;
    private Integer publishYear;
    private Integer totalCopies;
    private LocalDateTime createdAt;
}
