package com.example.library.service;

import com.example.library.model.Book;
import com.example.library.model.BorrowRecord;
import com.example.library.model.BorrowStatus;
import com.example.library.model.Member;
import com.example.library.repository.BookRepository;
import com.example.library.repository.BorrowRecordRepository;
import com.example.library.repository.MemberRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.OffsetDateTime;
import java.util.NoSuchElementException;
import java.util.UUID;

@Service
public class BorrowService {
    private final BorrowRecordRepository recordRepository;
    private final BookRepository bookRepository;
    private final MemberRepository memberRepository;

    public BorrowService(BorrowRecordRepository recordRepository, BookRepository bookRepository, MemberRepository memberRepository) {
        this.recordRepository = recordRepository;
        this.bookRepository = bookRepository;
        this.memberRepository = memberRepository;
    }

    @Transactional
    public BorrowRecord borrow(String username, UUID bookId) {
        Member member = memberRepository.findByUsername(username)
                .orElseThrow(() -> new NoSuchElementException("Member not found"));
        Book book = bookRepository.findById(bookId)
                .orElseThrow(() -> new NoSuchElementException("Book not found"));
        if (book.getAvailableCopies() <= 0) {
            throw new IllegalStateException("No copies available");
        }
        book.setAvailableCopies(book.getAvailableCopies() - 1);
        BorrowRecord record = new BorrowRecord();
        record.setMember(member);
        record.setBook(book);
        record.setBorrowedAt(OffsetDateTime.now());
        record.setStatus(BorrowStatus.BORROWED);
        bookRepository.save(book);
        return recordRepository.save(record);
    }

    @Transactional
    public BorrowRecord returnBook(UUID recordId) {
        BorrowRecord record = recordRepository.findById(recordId)
                .orElseThrow(() -> new NoSuchElementException("Record not found"));
        if (record.getStatus() == BorrowStatus.RETURNED) {
            return record;
        }
        Book book = record.getBook();
        book.setAvailableCopies(book.getAvailableCopies() + 1);
        record.setStatus(BorrowStatus.RETURNED);
        record.setReturnedAt(OffsetDateTime.now());
        bookRepository.save(book);
        return recordRepository.save(record);
    }
}
