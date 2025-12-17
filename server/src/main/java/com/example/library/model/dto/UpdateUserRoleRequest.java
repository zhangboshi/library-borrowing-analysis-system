package com.example.library.model.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.Data;

@Data
public class UpdateUserRoleRequest {

    @NotBlank(message = "role is required")
    @Pattern(regexp = "ADMIN|STAFF|VIEWER", message = "role must be ADMIN, STAFF or VIEWER")
    private String role;
}
