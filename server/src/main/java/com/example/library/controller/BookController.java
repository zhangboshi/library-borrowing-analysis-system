package com.example.library.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.example.library.common.ApiResponse;
import com.example.library.common.PageResponse;
import com.example.library.common.RequireRole;
import com.example.library.entity.Book;
import com.example.library.model.vo.BookVO;
import com.example.library.service.BookService;
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
@RequestMapping("/api/books")
@Validated
@RequireRole({"ADMIN", "STAFF", "VIEWER"})
public class BookController {

    private final BookService bookService;

    public BookController(BookService bookService) {
        this.bookService = bookService;
    }

    @GetMapping
    public ApiResponse<PageResponse<BookVO>> listBooks(
            @RequestParam(defaultValue = "1") @Min(value = 1, message = "page must be >= 1") long page,
            @RequestParam(defaultValue = "10") @Min(value = 1, message = "pageSize must be >= 1") long pageSize) {
        Page<Book> result = bookService.page(new Page<>(page, pageSize));
        List<BookVO> records = result.getRecords().stream().map(this::toVO).collect(Collectors.toList());
        PageResponse<BookVO> response = PageResponse.of(result.getTotal(), result.getCurrent(), result.getSize(), records);
        return ApiResponse.success(response);
    }

    private BookVO toVO(Book book) {
        BookVO vo = BookVO.builder().build();
        BeanUtils.copyProperties(book, vo);
        return vo;
    }
}
