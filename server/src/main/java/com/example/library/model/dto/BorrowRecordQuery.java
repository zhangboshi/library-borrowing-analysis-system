package com.example.library.model.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Pattern;
import lombok.Data;
import org.springframework.format.annotation.DateTimeFormat;

import java.time.LocalDateTime;

@Data
public class BorrowRecordQuery {

    @Min(value = 1, message = "page must be >= 1")
    private long page = 1;

    @Min(value = 1, message = "pageSize must be >= 1")
    private long pageSize = 10;

    @DateTimeFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private LocalDateTime startTime;

    @DateTimeFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private LocalDateTime endTime;

    private String category;

    @Pattern(regexp = "BORROWED|RETURNED|OVERDUE|LOST", message = "status must be BORROWED, RETURNED, OVERDUE or LOST")
    private String status;

    @Pattern(regexp = "STUDENT|TEACHER|STAFF", message = "readerType must be STUDENT, TEACHER or STAFF")
    private String readerType;
}
