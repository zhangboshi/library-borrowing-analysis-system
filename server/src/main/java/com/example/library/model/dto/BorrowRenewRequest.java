package com.example.library.model.dto;

import jakarta.validation.constraints.Min;
import lombok.Data;

@Data
public class BorrowRenewRequest {

    @Min(value = 1, message = "extraDays must be positive")
    private Integer extraDays = 7;
}
