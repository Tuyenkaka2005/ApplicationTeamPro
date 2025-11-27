package com.budgetwise.ad;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import com.budgetwise.ad.DatabaseContract.*;

import java.util.ArrayList;
import java.util.List;

public class DatabaseHelper extends SQLiteOpenHelper {

    private static final String TAG = "DatabaseHelper";
    private static DatabaseHelper instance;
    private Context context;

    public static synchronized DatabaseHelper getInstance(Context context) {
        if (instance == null) {
            instance = new DatabaseHelper(context.getApplicationContext());
        }
        return instance;
    }

    private DatabaseHelper(Context context) {
        super(context, DatabaseContract.DATABASE_NAME, null, DatabaseContract.DATABASE_VERSION);
        this.context = context.getApplicationContext();
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        Log.d(TAG, "Creating database tables...");

        // Tạo tất cả bảng
        db.execSQL(UserEntry.SQL_CREATE_TABLE);
        db.execSQL(CategoryEntry.SQL_CREATE_TABLE);
        db.execSQL(ExpenseEntry.SQL_CREATE_TABLE);
        db.execSQL(BudgetEntry.SQL_CREATE_TABLE);
        db.execSQL(RecurringExpenseEntry.SQL_CREATE_TABLE);
        db.execSQL(NotificationEntry.SQL_CREATE_TABLE);
        db.execSQL(SyncEntry.SQL_CREATE_TABLE);

        // Tạo index để tăng tốc truy vấn
        db.execSQL(ExpenseEntry.SQL_CREATE_INDEX_USER);
        db.execSQL(ExpenseEntry.SQL_CREATE_INDEX_DATE);
        db.execSQL(ExpenseEntry.SQL_CREATE_INDEX_CATEGORY);
        db.execSQL(ExpenseEntry.SQL_CREATE_INDEX_RECURRING);

        // Chèn dữ liệu mặc định
        insertDefaultCategories(db);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        Log.d(TAG, "Upgrading database from version " + oldVersion + " to " + newVersion);
        // Chiến lược đơn giản: xóa và tạo lại (chỉ dùng cho dev)
        db.execSQL("DROP TABLE IF EXISTS " + SyncEntry.TABLE_NAME);
        db.execSQL("DROP TABLE IF EXISTS " + NotificationEntry.TABLE_NAME);
        db.execSQL("DROP TABLE IF EXISTS " + RecurringExpenseEntry.TABLE_NAME);
        db.execSQL("DROP TABLE IF EXISTS " + BudgetEntry.TABLE_NAME);
        db.execSQL("DROP TABLE IF EXISTS " + ExpenseEntry.TABLE_NAME);
        db.execSQL("DROP TABLE IF EXISTS " + CategoryEntry.TABLE_NAME);
        db.execSQL("DROP TABLE IF EXISTS " + UserEntry.TABLE_NAME);
        onCreate(db);
    }

    private void insertDefaultCategories(SQLiteDatabase db) {
        Category[] defaults = Category.getDefaultCategories();
        for (Category cat : defaults) {
            ContentValues cv = new ContentValues();
            cv.put(CategoryEntry.COLUMN_CATEGORY_ID, cat.getCategoryId());
            cv.put(CategoryEntry.COLUMN_USER_ID, (String) null); // null = danh mục hệ thống
            cv.put(CategoryEntry.COLUMN_NAME, cat.getName());
            cv.put(CategoryEntry.COLUMN_ICON, cat.getIcon());
            cv.put(CategoryEntry.COLUMN_COLOR, cat.getColor());
            cv.put(CategoryEntry.COLUMN_IS_DEFAULT, 1);
            cv.put(CategoryEntry.COLUMN_IS_ACTIVE, 1);
            cv.put(CategoryEntry.COLUMN_CREATED_AT, System.currentTimeMillis());
            cv.put(CategoryEntry.COLUMN_UPDATED_AT, System.currentTimeMillis());
            db.insert(CategoryEntry.TABLE_NAME, null, cv);
        }
        Log.d(TAG, "Inserted " + defaults.length + " default categories");
    }


    // HÀM BÁO CÁO THEO DANH MỤC - HOÀN HẢO, KHÔNG LỖI
    public List<CategoryReportItem> getCategoryReport(String userId, long startDate, long endDate) {
        List<CategoryReportItem> reportItems = new ArrayList<>();
        SQLiteDatabase db = this.getReadableDatabase();

        String query = "SELECT " +
                "e." + ExpenseEntry.COLUMN_CATEGORY_ID + ", " +
                "c." + CategoryEntry.COLUMN_NAME + ", " +
                "c." + CategoryEntry.COLUMN_ICON + ", " +
                "c." + CategoryEntry.COLUMN_COLOR + ", " +
                "COALESCE(SUM(e." + ExpenseEntry.COLUMN_AMOUNT + "), 0) as total_amount " +
                "FROM " + ExpenseEntry.TABLE_NAME + " AS e " +
                "JOIN " + CategoryEntry.TABLE_NAME + " AS c " +
                "ON e." + ExpenseEntry.COLUMN_CATEGORY_ID + " = c." + CategoryEntry.COLUMN_CATEGORY_ID + " " +
                "WHERE e." + ExpenseEntry.COLUMN_USER_ID + " = ? " +
                "AND e." + ExpenseEntry.COLUMN_DATE + " BETWEEN ? AND ? " +
                "GROUP BY e." + ExpenseEntry.COLUMN_CATEGORY_ID + " " +
                "ORDER BY total_amount DESC";

        String[] args = { userId, String.valueOf(startDate), String.valueOf(endDate) };

        Cursor cursor = null;
        try {
            cursor = db.rawQuery(query, args);
            while (cursor.moveToNext()) {
                String catId = cursor.getString(cursor.getColumnIndexOrThrow(ExpenseEntry.COLUMN_CATEGORY_ID));
                String catName = cursor.getString(cursor.getColumnIndexOrThrow(CategoryEntry.COLUMN_NAME));
                String catIcon = cursor.getString(cursor.getColumnIndexOrThrow(CategoryEntry.COLUMN_ICON));
                String catColor = cursor.getString(cursor.getColumnIndexOrThrow(CategoryEntry.COLUMN_COLOR));
                double amount = cursor.getDouble(cursor.getColumnIndexOrThrow("total_amount"));

                reportItems.add(new CategoryReportItem(catId, catName, catIcon, catColor, amount));
            }
        } catch (Exception e) {
            Log.e(TAG, "Error in getCategoryReport", e);
        } finally {
            if (cursor != null) cursor.close();
        }

        return reportItems;
    }

    // Bonus: Lấy tổng chi tiêu trong khoảng thời gian
    public double getTotalSpent(String userId, long startDate, long endDate) {
        SQLiteDatabase db = this.getReadableDatabase();
        String query = "SELECT COALESCE(SUM(" + ExpenseEntry.COLUMN_AMOUNT + "), 0) FROM " +
                ExpenseEntry.TABLE_NAME + " WHERE " +
                ExpenseEntry.COLUMN_USER_ID + " = ? AND " +
                ExpenseEntry.COLUMN_DATE + " BETWEEN ? AND ?";
        Cursor c = db.rawQuery(query, new String[]{userId, String.valueOf(startDate), String.valueOf(endDate)});
        double total = 0;
        if (c.moveToFirst()) {
            total = c.getDouble(0);
        }
        c.close();
        return total;
    }
}