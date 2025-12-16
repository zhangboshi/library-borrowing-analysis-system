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
    private LocalDateTime createdAt;
}
