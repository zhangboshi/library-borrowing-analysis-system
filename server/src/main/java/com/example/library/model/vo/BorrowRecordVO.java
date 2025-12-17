package com.example.library.model.vo;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Builder
public class BorrowRecordVO {
    private Long id;
    private Long readerId;
    private String readerName;
    private Long bookId;
    private String bookTitle;
    private String bookCategory;
    private LocalDateTime borrowTime;
    private LocalDateTime dueTime;
    private LocalDateTime returnTime;
    private String status;
    private Integer renewCount;
    private java.math.BigDecimal fineAmount;
    private LocalDateTime createdAt;
}
