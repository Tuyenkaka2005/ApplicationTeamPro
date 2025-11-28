// Expense.java - Model class for Expense (ĐÃ SẠCH, ĐÃ HOÀN CHỈNH)
package com.budgetwise.ad;

public class Expense {

    private String expenseId;
    private String userId;
    private String categoryId;
    private String title;
    private double amount;
    private String note;
    private long date;           // ngày chi tiêu (timestamp)
    private boolean isRecurring; // có phải là khoản được sinh từ recurring không
    private String recurringId;  // ID của RecurringExpense
    private long createdAt;
    private long updatedAt;

    // Constructor rỗng (Room/Firestore cần)
    public Expense() {
    }

    // Constructor đầy đủ (dùng khi tạo mới)
    public Expense(String expenseId, String userId, String categoryId,
                   String title, double amount, String note, long date) {
        this.expenseId = expenseId;
        this.userId = userId;
        this.categoryId = categoryId;
        this.title = title;
        this.amount = amount;
        this.note = note;
        this.date = date;
        this.isRecurring = false;
        this.recurringId = null;
        this.createdAt = System.currentTimeMillis();
        this.updatedAt = System.currentTimeMillis();
    }

    // ------------------- Getters -------------------
    public String getExpenseId() {
        return expenseId;
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

    public double getAmount() {
        return amount;
    }

    public String getNote() {
        return note != null ? note : "";
    }

    public long getDate() {
        return date;
    }

    public boolean isRecurring() {
        return isRecurring;
    }

    public String getRecurringId() {
        return recurringId;
    }

    public long getCreatedAt() {
        return createdAt;
    }

    public long getUpdatedAt() {
        return updatedAt;
    }

    // ------------------- Setters -------------------
    public void setExpenseId(String expenseId) {
        this.expenseId = expenseId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public void setCategoryId(String categoryId) {
        this.categoryId = categoryId;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public void setAmount(double amount) {
        this.amount = amount;
    }

    public void setNote(String note) {
        this.note = note;
    }

    public void setDate(long date) {
        this.date = date;
    }

    public void setRecurring(boolean recurring) {
        isRecurring = recurring;
    }

    public void setRecurringId(String recurringId) {
        this.recurringId = recurringId;
    }

    public void setCreatedAt(long createdAt) {
        this.createdAt = createdAt;
    }

    public void setUpdatedAt(long updatedAt) {
        this.updatedAt = updatedAt;
    }

    @Override
    public String toString() {
        return "Expense{" +
                "expenseId='" + expenseId + '\'' +
                ", title='" + title + '\'' +
                ", amount=" + amount +
                ", date=" + date +
                ", categoryId='" + categoryId + '\'' +
                ", isRecurring=" + isRecurring +
                '}';
    }
}