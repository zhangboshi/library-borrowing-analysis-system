package com.example.library.api;

import com.example.library.dto.BorrowRequest;
import com.example.library.dto.BorrowResponse;
import com.example.library.model.BorrowRecord;
import com.example.library.service.BorrowService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/borrow")
public class BorrowController {
    private final BorrowService borrowService;

    public BorrowController(BorrowService borrowService) {
        this.borrowService = borrowService;
    }

    @PostMapping
    public ResponseEntity<BorrowResponse> borrow(@RequestBody @Valid BorrowRequest request) {
        BorrowRecord record = borrowService.borrow(request.getMemberUsername(), request.getBookId());
        return ResponseEntity.ok(toResponse(record));
    }

    @PostMapping("/{id}/return")
    public ResponseEntity<BorrowResponse> returnBook(@PathVariable UUID id) {
        BorrowRecord record = borrowService.returnBook(id);
        return ResponseEntity.ok(toResponse(record));
    }

    private BorrowResponse toResponse(BorrowRecord record) {
        return new BorrowResponse(record.getId(), record.getBorrowedAt(), record.getReturnedAt(), record.getStatus().name());
    }
}
