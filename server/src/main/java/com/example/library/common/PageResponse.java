package com.example.library.common;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * Generic page response wrapper.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PageResponse<T> {

    private long total;

    private long page;

    private long pageSize;

    private List<T> records;

    public static <T> PageResponse<T> of(long total, long page, long pageSize, List<T> records) {
        return PageResponse.<T>builder()
                .total(total)
                .page(page)
                .pageSize(pageSize)
                .records(records)
                .build();
    }
}
