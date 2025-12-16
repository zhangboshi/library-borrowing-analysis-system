package com.example.library.model.vo;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class AuthUserVO {
    private Long id;
    private String username;
    private String role;
}
