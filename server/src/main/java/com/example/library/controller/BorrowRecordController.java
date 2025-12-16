package com.example.library.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.example.library.common.ApiResponse;
import com.example.library.common.PageResponse;
import com.example.library.entity.Book;
import com.example.library.entity.BorrowRecord;
import com.example.library.entity.Reader;
import com.example.library.model.dto.BorrowRecordCreateRequest;
import com.example.library.model.dto.BorrowRecordQuery;
import com.example.library.model.dto.BorrowRecordReturnRequest;
import com.example.library.model.vo.BorrowRecordVO;
import com.example.library.service.BookService;
import com.example.library.service.BorrowRecordService;
import com.example.library.service.ReaderService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/borrow-records")
@Validated
public class BorrowRecordController {

    private final BorrowRecordService borrowRecordService;
    private final ReaderService readerService;
    private final BookService bookService;

    public BorrowRecordController(BorrowRecordService borrowRecordService,
                                  ReaderService readerService,
                                  BookService bookService) {
        this.borrowRecordService = borrowRecordService;
        this.readerService = readerService;
        this.bookService = bookService;
    }

    @GetMapping
    public ApiResponse<PageResponse<BorrowRecordVO>> listBorrowRecords(@Valid @ModelAttribute BorrowRecordQuery query) {
        if (query.getStartTime() != null && query.getEndTime() != null && query.getStartTime().isAfter(query.getEndTime())) {
            throw new IllegalArgumentException("startTime must be before endTime");
        }
        Page<BorrowRecord> result = borrowRecordService.pageBorrowRecords(query);
        Map<Long, Reader> readerMap = readerService.listByIds(
                        result.getRecords().stream().map(BorrowRecord::getReaderId).collect(Collectors.toSet()))
                .stream()
                .collect(Collectors.toMap(Reader::getId, Function.identity()));
        Map<Long, Book> bookMap = bookService.listByIds(
                        result.getRecords().stream().map(BorrowRecord::getBookId).collect(Collectors.toSet()))
                .stream()
                .collect(Collectors.toMap(Book::getId, Function.identity()));

        List<BorrowRecordVO> records = result.getRecords().stream()
                .map(record -> toVO(record, readerMap.get(record.getReaderId()), bookMap.get(record.getBookId())))
                .collect(Collectors.toList());
        PageResponse<BorrowRecordVO> response = PageResponse.of(result.getTotal(), result.getCurrent(), result.getSize(), records);
        return ApiResponse.success(response);
    }

    @PostMapping
    public ApiResponse<BorrowRecordVO> createBorrowRecord(@Valid @RequestBody BorrowRecordCreateRequest request) {
        if (request.getBorrowTime() != null && request.getDueTime() != null && request.getBorrowTime().isAfter(request.getDueTime())) {
            throw new IllegalArgumentException("dueTime must be after borrowTime");
        }
        BorrowRecord record = borrowRecordService.createBorrowRecord(request);
        Reader reader = readerService.getById(record.getReaderId());
        Book book = bookService.getById(record.getBookId());
        return ApiResponse.success(toVO(record, reader, book));
    }

    @PostMapping("/{id}/return")
    public ApiResponse<BorrowRecordVO> returnBorrowRecord(
            @PathVariable @Min(value = 1, message = "id must be positive") Long id,
            @Valid @RequestBody(required = false) BorrowRecordReturnRequest request) {
        LocalDateTime returnTime = request != null ? request.getReturnTime() : null;
        BorrowRecord record = borrowRecordService.returnBorrowRecord(id, returnTime);
        if (record == null) {
            return ApiResponse.failure("Borrow record not found");
        }
        Reader reader = readerService.getById(record.getReaderId());
        Book book = bookService.getById(record.getBookId());
        return ApiResponse.success(toVO(record, reader, book));
    }

    private BorrowRecordVO toVO(BorrowRecord record, Reader reader, Book book) {
        BorrowRecordVO.BorrowRecordVOBuilder builder = BorrowRecordVO.builder()
                .id(record.getId())
                .readerId(record.getReaderId())
                .bookId(record.getBookId())
                .borrowTime(record.getBorrowTime())
                .dueTime(record.getDueTime())
                .returnTime(record.getReturnTime())
                .status(record.getStatus())
                .createdAt(record.getCreatedAt());
        if (reader != null) {
            builder.readerName(reader.getName());
        }
        if (book != null) {
            builder.bookTitle(book.getTitle());
            builder.bookCategory(book.getCategory());
        }
        return builder.build();
    }
}
