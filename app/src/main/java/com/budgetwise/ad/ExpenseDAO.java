package com.budgetwise.ad;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import com.budgetwise.ad.DatabaseContract.ExpenseEntry;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

public class ExpenseDAO {
    private final DatabaseHelper dbHelper;

    public ExpenseDAO(Context context) {
        dbHelper = DatabaseHelper.getInstance(context);
    }

    /* ============================================================= */
    /*  TÌM KIẾM & LỌC CHI TIÊU (dùng cho SearchFilterActivity)      */
    /* ============================================================= */
    public List<Expense> searchExpenses(String userId,
                                        String keyword,
                                        String categoryId,
                                        Integer month,
                                        Integer year,
                                        Double minAmount) {
        List<Expense> list = new ArrayList<>();
        SQLiteDatabase db = dbHelper.getReadableDatabase();

        String sql = "SELECT * FROM " + ExpenseEntry.TABLE_NAME +
                " WHERE " + ExpenseEntry.COLUMN_USER_ID + " = ?";
        List<String> args = new ArrayList<>();
        args.add(userId);

        if (keyword != null && !keyword.trim().isEmpty()) {
            sql += " AND (" + ExpenseEntry.COLUMN_TITLE + " LIKE ? OR " +
                    ExpenseEntry.COLUMN_NOTE + " LIKE ?)";
            args.add("%" + keyword.trim() + "%");
            args.add("%" + keyword.trim() + "%");
        }
        if (categoryId != null) {
            sql += " AND " + ExpenseEntry.COLUMN_CATEGORY_ID + " = ?";
            args.add(categoryId);
        }
        if (minAmount != null) {
            sql += " AND " + ExpenseEntry.COLUMN_AMOUNT + " >= ?";
            args.add(String.valueOf(minAmount));
        }
        if (month != null && year != null) {
            Calendar c = Calendar.getInstance();
            c.set(year, month - 1, 1, 0, 0, 0);
            long start = c.getTimeInMillis();
            c.add(Calendar.MONTH, 1);
            c.add(Calendar.MILLISECOND, -1);
            long end = c.getTimeInMillis();

            sql += " AND " + ExpenseEntry.COLUMN_DATE + " BETWEEN ? AND ?";
            args.add(String.valueOf(start));
            args.add(String.valueOf(end));
        }

        sql += " ORDER BY " + ExpenseEntry.COLUMN_DATE + " DESC";

        Cursor cursor = db.rawQuery(sql, args.toArray(new String[0]));
        while (cursor.moveToNext()) {
            list.add(cursorToExpense(cursor));
        }
        cursor.close();
        return list;
    }

    /* ============================================================= */
    /*  CRUD CƠ BẢN                                                 */
    /* ============================================================= */

    public long createExpense(Expense expense) {
        SQLiteDatabase db = dbHelper.getWritableDatabase();

        ContentValues cv = new ContentValues();
        cv.put(ExpenseEntry.COLUMN_EXPENSE_ID, expense.getExpenseId());
        cv.put(ExpenseEntry.COLUMN_USER_ID, expense.getUserId());
        cv.put(ExpenseEntry.COLUMN_CATEGORY_ID, expense.getCategoryId());
        cv.put(ExpenseEntry.COLUMN_TITLE, expense.getTitle());
        cv.put(ExpenseEntry.COLUMN_AMOUNT, expense.getAmount());
        cv.put(ExpenseEntry.COLUMN_DATE, expense.getDate());
        cv.put(ExpenseEntry.COLUMN_NOTE, expense.getNote());
        cv.put(ExpenseEntry.COLUMN_IS_RECURRING, expense.isRecurring() ? 1 : 0);
        cv.put(ExpenseEntry.COLUMN_RECURRING_ID, expense.getRecurringId());
        cv.put(ExpenseEntry.COLUMN_CREATED_AT, expense.getCreatedAt());
        cv.put(ExpenseEntry.COLUMN_UPDATED_AT, expense.getUpdatedAt());

        return db.insert(ExpenseEntry.TABLE_NAME, null, cv);
    }

    public int updateExpense(Expense expense) {
        SQLiteDatabase db = dbHelper.getWritableDatabase();

        ContentValues cv = new ContentValues();
        cv.put(ExpenseEntry.COLUMN_CATEGORY_ID, expense.getCategoryId());
        cv.put(ExpenseEntry.COLUMN_TITLE, expense.getTitle());
        cv.put(ExpenseEntry.COLUMN_AMOUNT, expense.getAmount());
        cv.put(ExpenseEntry.COLUMN_DATE, expense.getDate());
        cv.put(ExpenseEntry.COLUMN_NOTE, expense.getNote());
        cv.put(ExpenseEntry.COLUMN_UPDATED_AT, System.currentTimeMillis());

        return db.update(ExpenseEntry.TABLE_NAME, cv,
                ExpenseEntry.COLUMN_EXPENSE_ID + " = ?",
                new String[]{expense.getExpenseId()});
    }

    public int deleteExpense(String expenseId) {
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        return db.delete(ExpenseEntry.TABLE_NAME,
                ExpenseEntry.COLUMN_EXPENSE_ID + " = ?",
                new String[]{expenseId});
    }

    public Expense getExpenseById(String expenseId) {
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        Cursor cursor = db.query(ExpenseEntry.TABLE_NAME, null,
                ExpenseEntry.COLUMN_EXPENSE_ID + " = ?",
                new String[]{expenseId}, null, null, null);

        Expense expense = null;
        if (cursor.moveToFirst()) {
            expense = cursorToExpense(cursor);
        }
        cursor.close();
        return expense;
    }

    /* ============================================================= */
    /*  LẤY DANH SÁCH CHI TIÊU THEO THÁNG/NĂM (ExpenseListActivity) */
    /* ============================================================= */
    public List<Expense> getUserExpenses(String userId, int month, int year) {
        List<Expense> expenses = new ArrayList<>();
        SQLiteDatabase db = dbHelper.getReadableDatabase();

        String selection = ExpenseEntry.COLUMN_USER_ID + " = ? AND " +
                "strftime('%m', datetime(" + ExpenseEntry.COLUMN_DATE + "/1000, 'unixepoch')) = ? AND " +
                "strftime('%Y', datetime(" + ExpenseEntry.COLUMN_DATE + "/1000, 'unixepoch')) = ?";

        String[] args = {
                userId,
                String.format("%02d", month),
                String.valueOf(year)
        };

        Cursor cursor = db.query(ExpenseEntry.TABLE_NAME, null, selection, args,
                null, null, ExpenseEntry.COLUMN_DATE + " DESC");

        while (cursor.moveToNext()) {
            expenses.add(cursorToExpense(cursor));
        }
        cursor.close();
        return expenses;
    }

    /* ============================================================= */
    /*  CHUYỂN CURSOR → OBJECT (dùng chung)                         */
    /* ============================================================= */
    private Expense cursorToExpense(Cursor cursor) {
        Expense e = new Expense();

        e.setExpenseId(cursor.getString(cursor.getColumnIndexOrThrow(ExpenseEntry.COLUMN_EXPENSE_ID)));
        e.setUserId(cursor.getString(cursor.getColumnIndexOrThrow(ExpenseEntry.COLUMN_USER_ID)));
        e.setCategoryId(cursor.getString(cursor.getColumnIndexOrThrow(ExpenseEntry.COLUMN_CATEGORY_ID)));
        e.setTitle(cursor.getString(cursor.getColumnIndexOrThrow(ExpenseEntry.COLUMN_TITLE)));
        e.setAmount(cursor.getDouble(cursor.getColumnIndexOrThrow(ExpenseEntry.COLUMN_AMOUNT)));
        e.setDate(cursor.getLong(cursor.getColumnIndexOrThrow(ExpenseEntry.COLUMN_DATE)));
        e.setNote(cursor.getString(cursor.getColumnIndexOrThrow(ExpenseEntry.COLUMN_NOTE)));

        // Các cột recurring
        int recurringCol = cursor.getColumnIndex(ExpenseEntry.COLUMN_IS_RECURRING);
        if (recurringCol != -1) {
            e.setRecurring(cursor.getInt(recurringCol) == 1);
        }
        int recurringIdCol = cursor.getColumnIndex(ExpenseEntry.COLUMN_RECURRING_ID);
        if (recurringIdCol != -1 && !cursor.isNull(recurringIdCol)) {
            e.setRecurringId(cursor.getString(recurringIdCol));
        }

        int createdAtCol = cursor.getColumnIndex(ExpenseEntry.COLUMN_CREATED_AT);
        if (createdAtCol != -1) {
            e.setCreatedAt(cursor.getLong(createdAtCol));
        }
        int updatedAtCol = cursor.getColumnIndex(ExpenseEntry.COLUMN_UPDATED_AT);
        if (updatedAtCol != -1) {
            e.setUpdatedAt(cursor.getLong(updatedAtCol));
        }

        return e;
    }
}