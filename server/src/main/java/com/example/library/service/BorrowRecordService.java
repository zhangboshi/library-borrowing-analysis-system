package com.example.library.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.example.library.entity.BorrowRecord;
import com.example.library.model.dto.BorrowRecordCreateRequest;
import com.example.library.model.dto.BorrowRecordQuery;

public interface BorrowRecordService extends IService<BorrowRecord> {

    BorrowRecord createBorrowRecord(BorrowRecordCreateRequest request);

    BorrowRecord returnBorrowRecord(Long id, java.time.LocalDateTime returnTime);

    Page<BorrowRecord> pageBorrowRecords(BorrowRecordQuery query);
}
