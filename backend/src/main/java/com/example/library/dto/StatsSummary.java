package com.example.library.dto;

public class StatsSummary {
    private long activeBorrowings;
    private long completedBorrowings;
    private long activeBorrowers;

    public StatsSummary() {
    }

    public StatsSummary(long activeBorrowings, long completedBorrowings, long activeBorrowers) {
        this.activeBorrowings = activeBorrowings;
        this.completedBorrowings = completedBorrowings;
        this.activeBorrowers = activeBorrowers;
    }

    public long getActiveBorrowings() {
        return activeBorrowings;
    }

    public void setActiveBorrowings(long activeBorrowings) {
        this.activeBorrowings = activeBorrowings;
    }

    public long getCompletedBorrowings() {
        return completedBorrowings;
    }

    public void setCompletedBorrowings(long completedBorrowings) {
        this.completedBorrowings = completedBorrowings;
    }

    public long getActiveBorrowers() {
        return activeBorrowers;
    }

    public void setActiveBorrowers(long activeBorrowers) {
        this.activeBorrowers = activeBorrowers;
    }
}
