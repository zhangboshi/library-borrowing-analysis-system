package com.example.library;

import com.example.library.dto.StatsSummary;
import com.example.library.model.Book;
import com.example.library.model.BorrowRecord;
import com.example.library.model.BorrowStatus;
import com.example.library.model.Member;
import com.example.library.repository.BookRepository;
import com.example.library.repository.BorrowRecordRepository;
import com.example.library.repository.MemberRepository;
import com.example.library.service.AuthService;
import com.example.library.service.StatsService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.Import;

import java.time.OffsetDateTime;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@Import({StatsService.class, AuthService.class})
class StatsServiceTest {

    @Autowired
    BorrowRecordRepository recordRepository;
    @Autowired
    BookRepository bookRepository;
    @Autowired
    MemberRepository memberRepository;
    @Autowired
    StatsService statsService;
    @Autowired
    AuthService authService;

    Member member;
    Book book;

    @BeforeEach
    void setUp() {
        member = new Member();
        member.setUsername("tester");
        member.setPasswordHash(authService.encodePassword("pw"));
        member.setRole("USER");
        memberRepository.save(member);

        book = new Book();
        book.setTitle("Sample");
        book.setAvailableCopies(1);
        bookRepository.save(book);
    }

    @Test
    void countsBorrowingStates() {
        BorrowRecord active = new BorrowRecord();
        active.setBook(book);
        active.setMember(member);
        active.setBorrowedAt(OffsetDateTime.now());
        active.setStatus(BorrowStatus.BORROWED);

        BorrowRecord returned = new BorrowRecord();
        returned.setBook(book);
        returned.setMember(member);
        returned.setBorrowedAt(OffsetDateTime.now().minusDays(1));
        returned.setReturnedAt(OffsetDateTime.now());
        returned.setStatus(BorrowStatus.RETURNED);
        recordRepository.save(active);
        recordRepository.save(returned);

        StatsSummary summary = statsService.summary();

        assertThat(summary.getActiveBorrowings()).isEqualTo(1);
        assertThat(summary.getCompletedBorrowings()).isEqualTo(1);
        assertThat(summary.getActiveBorrowers()).isEqualTo(1);
    }
}
