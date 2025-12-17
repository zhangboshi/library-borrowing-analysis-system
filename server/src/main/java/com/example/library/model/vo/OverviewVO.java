package com.example.library.model.vo;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class OverviewVO {
    private Long totalBorrow;
    private Long totalReaders;
    private Long totalBooks;
    private Long unreturned;
    private Long recent7Days;
}
