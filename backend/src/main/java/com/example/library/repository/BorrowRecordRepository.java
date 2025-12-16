package com.example.library.repository;

import com.example.library.model.BorrowRecord;
import com.example.library.model.Member;
import com.example.library.model.BorrowStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.UUID;

public interface BorrowRecordRepository extends JpaRepository<BorrowRecord, UUID> {
    List<BorrowRecord> findByMember(Member member);
    long countByStatus(BorrowStatus status);

    @Query("select count(distinct r.member.id) from BorrowRecord r where r.status = 'BORROWED'")
    long countActiveBorrowers();
}
