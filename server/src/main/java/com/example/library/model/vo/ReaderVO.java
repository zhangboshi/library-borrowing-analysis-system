package com.example.library.model.vo;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Builder
public class ReaderVO {
    private Long id;
    private String name;
    private String type;
    private String department;
    private String email;
    private String phone;
    private LocalDateTime createdAt;
}
