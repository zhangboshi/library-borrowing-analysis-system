package com.example.library.dto;

import java.time.OffsetDateTime;
import java.util.UUID;

public class BorrowResponse {
    private UUID recordId;
    private OffsetDateTime borrowedAt;
    private OffsetDateTime returnedAt;
    private String status;

    public BorrowResponse() {
    }

    public BorrowResponse(UUID recordId, OffsetDateTime borrowedAt, OffsetDateTime returnedAt, String status) {
        this.recordId = recordId;
        this.borrowedAt = borrowedAt;
        this.returnedAt = returnedAt;
        this.status = status;
    }

    public UUID getRecordId() {
        return recordId;
    }

    public void setRecordId(UUID recordId) {
        this.recordId = recordId;
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

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }
}
