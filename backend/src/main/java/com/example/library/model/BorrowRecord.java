package com.example.library.model;

import jakarta.persistence.*;

import java.time.OffsetDateTime;
import java.util.UUID;

@Entity
public class BorrowRecord {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne(optional = false)
    private Member member;

    @ManyToOne(optional = false)
    private Book book;

    private OffsetDateTime borrowedAt;
    private OffsetDateTime returnedAt;

    @Enumerated(EnumType.STRING)
    private BorrowStatus status = BorrowStatus.BORROWED;

    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
    }

    public Member getMember() {
        return member;
    }

    public void setMember(Member member) {
        this.member = member;
    }

    public Book getBook() {
        return book;
    }

    public void setBook(Book book) {
        this.book = book;
    }

    public OffsetDateTime getBorrowedAt() {
        return borrowedAt;
    }

    public void setBorrowedAt(OffsetDateTime borrowedAt) {
        this.borrowedAt = borrowedAt;
    }

    public OffsetDateTime getReturnedAt() {
        return returnedAt;
    }

    public void setReturnedAt(OffsetDateTime returnedAt) {
        this.returnedAt = returnedAt;
    }

    public BorrowStatus getStatus() {
        return status;
    }

    public void setStatus(BorrowStatus status) {
        this.status = status;
    }
}
