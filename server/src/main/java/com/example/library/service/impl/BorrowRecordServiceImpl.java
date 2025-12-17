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

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;

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
        record.setRenewCount(request.getRenewCount() == null ? 0 : request.getRenewCount());
        record.setFineAmount(BigDecimal.ZERO);
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

    @Override
    public BorrowRecord renewBorrowRecord(Long id, int extraDays) {
        BorrowRecord record = getById(id);
        if (record == null) {
            return null;
        }
        if ("RETURNED".equals(record.getStatus()) || "LOST".equals(record.getStatus())) {
            return record;
        }
        int renewCount = record.getRenewCount() == null ? 0 : record.getRenewCount();
        if (renewCount >= 2) {
            return record;
        }
        record.setRenewCount(renewCount + 1);
        record.setDueTime(record.getDueTime().plusDays(extraDays));
        updateById(record);
        return record;
    }

    @Override
    public int refreshOverdueAndFines() {
        LocalDateTime now = LocalDateTime.now();
        LambdaQueryWrapper<BorrowRecord> wrapper = new LambdaQueryWrapper<>();
        wrapper.ne(BorrowRecord::getStatus, "RETURNED")
                .lt(BorrowRecord::getDueTime, now);
        var list = list(wrapper);
        int updated = 0;
        for (BorrowRecord r : list) {
            long daysOverdue = ChronoUnit.DAYS.between(r.getDueTime().toLocalDate(), now.toLocalDate());
            BigDecimal fine = BigDecimal.valueOf(daysOverdue).max(BigDecimal.ZERO);
            r.setStatus("OVERDUE");
            r.setFineAmount(fine);
            updateById(r);
            updated++;
        }
        return updated;
    }
}
