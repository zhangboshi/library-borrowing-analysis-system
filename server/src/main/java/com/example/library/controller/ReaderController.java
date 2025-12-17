package com.example.library.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.example.library.common.ApiResponse;
import com.example.library.common.PageResponse;
import com.example.library.common.RequireRole;
import com.example.library.entity.Reader;
import com.example.library.model.vo.ReaderVO;
import com.example.library.service.ReaderService;
import jakarta.validation.constraints.Min;
import org.springframework.beans.BeanUtils;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/readers")
@Validated
@RequireRole({"ADMIN", "STAFF", "VIEWER"})
public class ReaderController {

    private final ReaderService readerService;

    public ReaderController(ReaderService readerService) {
        this.readerService = readerService;
    }

    @GetMapping
    public ApiResponse<PageResponse<ReaderVO>> listReaders(
            @RequestParam(defaultValue = "1") @Min(value = 1, message = "page must be >= 1") long page,
            @RequestParam(defaultValue = "10") @Min(value = 1, message = "pageSize must be >= 1") long pageSize) {
        Page<Reader> result = readerService.page(new Page<>(page, pageSize));
        List<ReaderVO> records = result.getRecords().stream().map(this::toVO).collect(Collectors.toList());
        PageResponse<ReaderVO> response = PageResponse.of(result.getTotal(), result.getCurrent(), result.getSize(), records);
        return ApiResponse.success(response);
    }

    private ReaderVO toVO(Reader reader) {
        ReaderVO vo = ReaderVO.builder().build();
        BeanUtils.copyProperties(reader, vo);
        return vo;
    }
}
