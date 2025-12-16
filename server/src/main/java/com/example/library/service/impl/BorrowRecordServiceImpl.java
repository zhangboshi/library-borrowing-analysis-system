package com.example.library.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.example.library.entity.BorrowRecord;
import com.example.library.mapper.BorrowRecordMapper;
import com.example.library.model.dto.BorrowRecordCreateRequest;
import com.example.library.model.dto.BorrowRecordQuery;
import com.example.library.service.BorrowRecordService;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;

@Service
public class BorrowRecordServiceImpl extends ServiceImpl<BorrowRecordMapper, BorrowRecord> implements BorrowRecordService {

    @Override
    public BorrowRecord createBorrowRecord(BorrowRecordCreateRequest request) {
        BorrowRecord record = new BorrowRecord();
        record.setReaderId(request.getReaderId());
        record.setBookId(request.getBookId());
        record.setBorrowTime(request.getBorrowTime());
        record.setDueTime(request.getDueTime());
        record.setReturnTime(null);
        record.setStatus(StringUtils.hasText(request.getStatus()) ? request.getStatus() : "BORROWED");
        save(record);
        return record;
    }

    @Override
    public BorrowRecord returnBorrowRecord(Long id, LocalDateTime returnTime) {
        BorrowRecord existing = getById(id);
        if (existing == null) {
            return null;
        }
        existing.setStatus("RETURNED");
        existing.setReturnTime(returnTime != null ? returnTime : LocalDateTime.now());
        updateById(existing);
        return existing;
    }

    @Override
    public Page<BorrowRecord> pageBorrowRecords(BorrowRecordQuery query) {
        LambdaQueryWrapper<BorrowRecord> wrapper = new LambdaQueryWrapper<>();
        if (query.getStartTime() != null) {
            wrapper.ge(BorrowRecord::getBorrowTime, query.getStartTime());
        }
        if (query.getEndTime() != null) {
            wrapper.le(BorrowRecord::getBorrowTime, query.getEndTime());
        }
        if (StringUtils.hasText(query.getStatus())) {
            wrapper.eq(BorrowRecord::getStatus, query.getStatus());
        }
        if (StringUtils.hasText(query.getCategory())) {
            wrapper.inSql(BorrowRecord::getBookId,
                    "select id from book where category = '" + query.getCategory().replace(\"'\", \"''\") + "'");
        }
        if (StringUtils.hasText(query.getReaderType())) {
            wrapper.inSql(BorrowRecord::getReaderId,
                    "select id from reader where type = '" + query.getReaderType().replace(\"'\", \"''\") + "'");
        }
        wrapper.orderByDesc(BorrowRecord::getBorrowTime);
        Page<BorrowRecord> page = new Page<>(query.getPage(), query.getPageSize());
        return page(page, wrapper);
    }
}
