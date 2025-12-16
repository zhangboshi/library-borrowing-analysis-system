package com.example.library.service;

import com.example.library.dto.StatsSummary;
import com.example.library.model.BorrowStatus;
import com.example.library.repository.BorrowRecordRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class StatsService {
    private final BorrowRecordRepository recordRepository;

    public StatsService(BorrowRecordRepository recordRepository) {
        this.recordRepository = recordRepository;
    }

    @Transactional(readOnly = true)
    public StatsSummary summary() {
        long active = recordRepository.countByStatus(BorrowStatus.BORROWED);
        long completed = recordRepository.countByStatus(BorrowStatus.RETURNED);
        long activeBorrowers = recordRepository.countActiveBorrowers();
        return new StatsSummary(active, completed, activeBorrowers);
    }
}
