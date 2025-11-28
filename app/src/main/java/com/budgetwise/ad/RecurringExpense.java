package com.budgetwise.ad;

public class RecurringExpense {
    public String recurringId, userId, categoryId, title, note, interval;
    public double amount;
    public long startDate, nextRunDate;

    public RecurringExpense(String recurringId, String userId, String categoryId,
                            String title, double amount, String note,
                            String interval, long startDate, long nextRunDate) {
        this.recurringId = recurringId;
        this.userId = userId;
        this.categoryId = categoryId;
        this.title = title;
        this.amount = amount;
        this.note = note;
        this.interval = interval;
        this.startDate = startDate;
        this.nextRunDate = nextRunDate;
    }

    public String getRecurringId() {
        return recurringId;
    }

    public String getUserId() {
        return userId;
    }

    public String getCategoryId() {
        return categoryId;
    }

    public String getTitle() {
        return title;
    }

    public String getNote() {
        return note;
    }

    public String getInterval() {
        return interval;
    }

    public double getAmount() {
        return amount;
    }

    public long getStartDate() {
        return startDate;
    }

    public long getNextRunDate() {
        return nextRunDate;
    }
}
