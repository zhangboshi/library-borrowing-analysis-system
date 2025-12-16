package com.example.library.model.dto;

import jakarta.validation.constraints.Future;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import lombok.Data;
import org.springframework.format.annotation.DateTimeFormat;

import java.time.LocalDateTime;

@Data
public class BorrowRecordCreateRequest {

    @NotNull(message = "readerId is required")
    @Min(value = 1, message = "readerId must be positive")
    private Long readerId;

    @NotNull(message = "bookId is required")
    @Min(value = 1, message = "bookId must be positive")
    private Long bookId;

    @NotNull(message = "borrowTime is required")
    @DateTimeFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private LocalDateTime borrowTime;

    @NotNull(message = "dueTime is required")
    @Future(message = "dueTime must be in the future")
    @DateTimeFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private LocalDateTime dueTime;

    @Pattern(regexp = "BORROWED|RETURNED|OVERDUE|LOST", message = "status must be BORROWED, RETURNED, OVERDUE or LOST")
    private String status;
}
