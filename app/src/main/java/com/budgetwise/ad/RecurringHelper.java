package com.budgetwise.ad;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

public class RecurringHelper {

    public static boolean insertRecurringExpense(Context context, RecurringExpense re) {
        SQLiteDatabase db = DatabaseHelper.getInstance(context).getWritableDatabase();
        ContentValues cv = new ContentValues();
        cv.put(DatabaseContract.RecurringExpenseEntry.COLUMN_RECURRING_ID, re.recurringId);
        cv.put(DatabaseContract.RecurringExpenseEntry.COLUMN_USER_ID, re.userId);
        cv.put(DatabaseContract.RecurringExpenseEntry.COLUMN_CATEGORY_ID, re.categoryId);
        cv.put(DatabaseContract.RecurringExpenseEntry.COLUMN_TITLE, re.title);
        cv.put(DatabaseContract.RecurringExpenseEntry.COLUMN_AMOUNT, re.amount);
        cv.put(DatabaseContract.RecurringExpenseEntry.COLUMN_NOTE, re.note);
        cv.put(DatabaseContract.RecurringExpenseEntry.COLUMN_INTERVAL, re.interval);
        cv.put(DatabaseContract.RecurringExpenseEntry.COLUMN_START_DATE, re.startDate);
        cv.put(DatabaseContract.RecurringExpenseEntry.COLUMN_NEXT_RUN_DATE, re.nextRunDate);
        cv.put(DatabaseContract.RecurringExpenseEntry.COLUMN_IS_ACTIVE, 1);
        long now = System.currentTimeMillis();
        cv.put(DatabaseContract.RecurringExpenseEntry.COLUMN_CREATED_AT, now);
        cv.put(DatabaseContract.RecurringExpenseEntry.COLUMN_UPDATED_AT, now);

        return db.insert(DatabaseContract.RecurringExpenseEntry.TABLE_NAME, null, cv) != -1;
    }

    public static RecurringExpense getRecurringExpenseById(Context context, String recurringId) {
        SQLiteDatabase db = DatabaseHelper.getInstance(context).getReadableDatabase();
        String userId = UserSession.getCurrentUserId(context);
        Cursor c = null;
        try {
            c = db.query(
                    DatabaseContract.RecurringExpenseEntry.TABLE_NAME,
                    null,
                    DatabaseContract.RecurringExpenseEntry.COLUMN_RECURRING_ID + " = ? AND " +
                            DatabaseContract.RecurringExpenseEntry.COLUMN_USER_ID + " = ?",
                    new String[]{recurringId, userId},
                    null, null, null
            );

            if (c != null && c.moveToFirst()) {
                return new RecurringExpense(
                        c.getString(c.getColumnIndexOrThrow(DatabaseContract.RecurringExpenseEntry.COLUMN_RECURRING_ID)),
                        userId,
                        c.getString(c.getColumnIndexOrThrow(DatabaseContract.RecurringExpenseEntry.COLUMN_CATEGORY_ID)),
                        c.getString(c.getColumnIndexOrThrow(DatabaseContract.RecurringExpenseEntry.COLUMN_TITLE)),
                        c.getDouble(c.getColumnIndexOrThrow(DatabaseContract.RecurringExpenseEntry.COLUMN_AMOUNT)),
                        c.getString(c.getColumnIndexOrThrow(DatabaseContract.RecurringExpenseEntry.COLUMN_NOTE)),
                        c.getString(c.getColumnIndexOrThrow(DatabaseContract.RecurringExpenseEntry.COLUMN_INTERVAL)),
                        c.getLong(c.getColumnIndexOrThrow(DatabaseContract.RecurringExpenseEntry.COLUMN_START_DATE)),
                        c.getLong(c.getColumnIndexOrThrow(DatabaseContract.RecurringExpenseEntry.COLUMN_NEXT_RUN_DATE))
                );
            }
        } finally {
            if (c != null) c.close();
        }
        return null;
    }

    public static boolean updateRecurringExpense(Context context, RecurringExpense re) {
        SQLiteDatabase db = DatabaseHelper.getInstance(context).getWritableDatabase();
        ContentValues cv = new ContentValues();
        cv.put(DatabaseContract.RecurringExpenseEntry.COLUMN_CATEGORY_ID, re.categoryId);
        cv.put(DatabaseContract.RecurringExpenseEntry.COLUMN_TITLE, re.title);
        cv.put(DatabaseContract.RecurringExpenseEntry.COLUMN_AMOUNT, re.amount);
        cv.put(DatabaseContract.RecurringExpenseEntry.COLUMN_NOTE, re.note);
        cv.put(DatabaseContract.RecurringExpenseEntry.COLUMN_INTERVAL, re.interval);
        cv.put(DatabaseContract.RecurringExpenseEntry.COLUMN_NEXT_RUN_DATE, re.nextRunDate);
        cv.put(DatabaseContract.RecurringExpenseEntry.COLUMN_UPDATED_AT, System.currentTimeMillis());

        int rows = db.update(DatabaseContract.RecurringExpenseEntry.TABLE_NAME, cv,
                DatabaseContract.RecurringExpenseEntry.COLUMN_RECURRING_ID + " = ? AND " +
                        DatabaseContract.RecurringExpenseEntry.COLUMN_USER_ID + " = ?",
                new String[]{re.recurringId, re.userId});

        return rows > 0;
    }

    public static boolean deleteRecurringExpense(Context context, String recurringId) {
        SQLiteDatabase db = DatabaseHelper.getInstance(context).getWritableDatabase();
        ContentValues cv = new ContentValues();
        cv.put(DatabaseContract.RecurringExpenseEntry.COLUMN_IS_ACTIVE, 0);
        cv.put(DatabaseContract.RecurringExpenseEntry.COLUMN_UPDATED_AT, System.currentTimeMillis());

        String userId = UserSession.getCurrentUserId(context);

        int rows = db.update(DatabaseContract.RecurringExpenseEntry.TABLE_NAME, cv,
                DatabaseContract.RecurringExpenseEntry.COLUMN_RECURRING_ID + " = ? AND " +
                        DatabaseContract.RecurringExpenseEntry.COLUMN_USER_ID + " = ?",
                new String[]{recurringId, userId});
        return rows > 0;
    }

    public static List<RecurringExpense> getAllRecurringExpenses(Context context) {
        List<RecurringExpense> list = new ArrayList<>();
        SQLiteDatabase db = DatabaseHelper.getInstance(context).getReadableDatabase();
        String userId = UserSession.getCurrentUserId(context);

        Cursor c = db.rawQuery("SELECT * FROM " + DatabaseContract.RecurringExpenseEntry.TABLE_NAME +
                " WHERE " + DatabaseContract.RecurringExpenseEntry.COLUMN_USER_ID + "=? AND " +
                DatabaseContract.RecurringExpenseEntry.COLUMN_IS_ACTIVE + "=1", new String[]{userId});

        while (c.moveToNext()) {
            list.add(new RecurringExpense(
                    c.getString(c.getColumnIndexOrThrow(DatabaseContract.RecurringExpenseEntry.COLUMN_RECURRING_ID)),
                    userId,
                    c.getString(c.getColumnIndexOrThrow(DatabaseContract.RecurringExpenseEntry.COLUMN_CATEGORY_ID)),
                    c.getString(c.getColumnIndexOrThrow(DatabaseContract.RecurringExpenseEntry.COLUMN_TITLE)),
                    c.getDouble(c.getColumnIndexOrThrow(DatabaseContract.RecurringExpenseEntry.COLUMN_AMOUNT)),
                    c.getString(c.getColumnIndexOrThrow(DatabaseContract.RecurringExpenseEntry.COLUMN_NOTE)),
                    c.getString(c.getColumnIndexOrThrow(DatabaseContract.RecurringExpenseEntry.COLUMN_INTERVAL)),
                    c.getLong(c.getColumnIndexOrThrow(DatabaseContract.RecurringExpenseEntry.COLUMN_START_DATE)),
                    c.getLong(c.getColumnIndexOrThrow(DatabaseContract.RecurringExpenseEntry.COLUMN_NEXT_RUN_DATE))
            ));
        }
        c.close();
        return list;
    }

    public static long calculateNextRunDate(String interval, long current) {
        Calendar cal = Calendar.getInstance();
        cal.setTimeInMillis(current);
        if (interval.contains("Monthly")) {
            cal.add(Calendar.MONTH, 1);
        } else if (interval.contains("Weekly")) {
            cal.add(Calendar.WEEK_OF_YEAR, 1);
        } else if (interval.contains("2 weeks")) {
            cal.add(Calendar.WEEK_OF_YEAR, 2);
        } else if (interval.contains("Yearly")) {
            cal.add(Calendar.YEAR, 1);
        }
        return cal.getTimeInMillis();
    }

    public static void generateMissedRecurringExpenses(Context context) {
        List<RecurringExpense> all = getAllRecurringExpenses(context);
        SQLiteDatabase db = DatabaseHelper.getInstance(context).getWritableDatabase();
        long now = System.currentTimeMillis();

        db.beginTransaction();
        try {
            for (RecurringExpense re : all) {
                long currentNextRun = getLatestNextRunDate(context, re.recurringId);

                while (currentNextRun <= now) {
                    createExpenseFromRecurring(db, re, currentNextRun);
                    currentNextRun = calculateNextRunDate(re.interval, currentNextRun);

                    ContentValues cv = new ContentValues();
                    cv.put(DatabaseContract.RecurringExpenseEntry.COLUMN_NEXT_RUN_DATE, currentNextRun);
                    db.update(DatabaseContract.RecurringExpenseEntry.TABLE_NAME, cv,
                            DatabaseContract.RecurringExpenseEntry.COLUMN_RECURRING_ID + "=?",
                            new String[]{re.recurringId});
                }
            }
            db.setTransactionSuccessful();
        } finally {
            db.endTransaction();
        }
    }


    private static long getLatestNextRunDate(Context context, String recurringId) {
        SQLiteDatabase db = DatabaseHelper.getInstance(context).getReadableDatabase();
        Cursor c = db.rawQuery(
                "SELECT " + DatabaseContract.RecurringExpenseEntry.COLUMN_NEXT_RUN_DATE +
                        " FROM " + DatabaseContract.RecurringExpenseEntry.TABLE_NAME +
                        " WHERE " + DatabaseContract.RecurringExpenseEntry.COLUMN_RECURRING_ID + "=?",
                new String[]{recurringId}
        );
        long nextRun = System.currentTimeMillis();
        if (c.moveToFirst()) {
            nextRun = c.getLong(0);
        }
        c.close();
        return nextRun;
    }

    private static void createExpenseFromRecurring(SQLiteDatabase db, RecurringExpense re, long date) {
        ContentValues cv = new ContentValues();
        cv.put(DatabaseContract.ExpenseEntry.COLUMN_EXPENSE_ID, java.util.UUID.randomUUID().toString());
        cv.put(DatabaseContract.ExpenseEntry.COLUMN_USER_ID, re.userId);
        cv.put(DatabaseContract.ExpenseEntry.COLUMN_CATEGORY_ID, re.categoryId);
        cv.put(DatabaseContract.ExpenseEntry.COLUMN_TITLE, re.title);
        cv.put(DatabaseContract.ExpenseEntry.COLUMN_AMOUNT, re.amount);
        cv.put(DatabaseContract.ExpenseEntry.COLUMN_DATE, date);
        cv.put(DatabaseContract.ExpenseEntry.COLUMN_NOTE, re.note != null ? re.note + " (định kỳ)" : "(định kỳ)");
        cv.put(DatabaseContract.ExpenseEntry.COLUMN_IS_RECURRING, 1);
        cv.put(DatabaseContract.ExpenseEntry.COLUMN_RECURRING_ID, re.recurringId);
        long now = System.currentTimeMillis();
        cv.put(DatabaseContract.ExpenseEntry.COLUMN_CREATED_AT, now);
        cv.put(DatabaseContract.ExpenseEntry.COLUMN_UPDATED_AT, now);
        db.insert(DatabaseContract.ExpenseEntry.TABLE_NAME, null, cv);
    }
}