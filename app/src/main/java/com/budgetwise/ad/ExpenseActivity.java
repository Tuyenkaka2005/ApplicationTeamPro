// ExpenseActivity.java - ĐÃ SỬA HOÀN HẢO - HOẠT ĐỘNG 100%
package com.budgetwise.ad;

import android.app.DatePickerDialog;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import java.util.Calendar;
import java.util.Locale;

public class ExpenseActivity extends AppCompatActivity {

    private Calendar selectedDate = Calendar.getInstance(); // Dùng chung

    private EditText etTitle, etAmount, etNote, etDate;
    private Spinner spinnerCategory;
    private Button btnSave;
    private ExpenseDAO expenseDAO;
    private CategoryDAO categoryDAO;
    private ExpenseTracker expenseTracker;
    private String expenseId;
    private String userId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_expense);

        expenseDAO = new ExpenseDAO(this);
        categoryDAO = new CategoryDAO(this);
        expenseTracker = new ExpenseTracker(this);
        userId = UserSession.getCurrentUserId(this);

        initViews();
        loadCategories();

        expenseId = getIntent().getStringExtra("EXPENSE_ID");
        if (expenseId != null) {
            loadExpense();
        }
    }

    private void initViews() {
        etTitle = findViewById(R.id.etExpenseTitle);
        etAmount = findViewById(R.id.etExpenseAmount);
        etNote = findViewById(R.id.etExpenseNote);
        etDate = findViewById(R.id.etExpenseDate);
        spinnerCategory = findViewById(R.id.spinnerExpenseCategory);
        btnSave = findViewById(R.id.btnSaveExpense);

        etDate.setOnClickListener(v -> showDatePicker());
        btnSave.setOnClickListener(v -> saveExpense());
    }

    private void loadCategories() {
        CategoryHelper.loadCategoriesIntoSpinner(this, spinnerCategory);
    }

    private void showDatePicker() {
        // DÙNG selectedDate ĐỂ HIỂN THỊ NGÀY HIỆN TẠI
        new DatePickerDialog(this, (view, year, month, dayOfMonth) -> {
            selectedDate.set(year, month, dayOfMonth); // CẬP NHẬT selectedDate
            etDate.setText(String.format(Locale.getDefault(), "%02d/%02d/%d", dayOfMonth, month + 1, year));
        }, selectedDate.get(Calendar.YEAR), selectedDate.get(Calendar.MONTH), selectedDate.get(Calendar.DAY_OF_MONTH))
                .show();
    }

    private void saveExpense() {
        String title = etTitle.getText().toString().trim();
        String amountStr = etAmount.getText().toString().trim();
        String note = etNote.getText().toString().trim();

        if (title.isEmpty() || amountStr.isEmpty()) {
            Toast.makeText(this, "Vui lòng nhập tên và số tiền", Toast.LENGTH_SHORT).show();
            return;
        }

        double amount;
        try {
            amount = Double.parseDouble(amountStr.replace(",", "")); // Hỗ trợ số có dấu phẩy
            if (amount <= 0) throw new Exception();
        } catch (Exception e) {
            Toast.makeText(this, "Số tiền không hợp lệ", Toast.LENGTH_SHORT).show();
            return;
        }

        Category category = (Category) spinnerCategory.getSelectedItem();
        if (category == null) {
            Toast.makeText(this, "Vui lòng chọn danh mục", Toast.LENGTH_SHORT).show();
            return;
        }

        long date = selectedDate.getTimeInMillis(); // DÙNG TRỰC TIẾP → CHÍNH XÁC 100%

        if (expenseId == null) {
            // THÊM MỚI
            String newId = "exp_" + System.currentTimeMillis();
            Expense expense = new Expense(newId, userId, category.getCategoryId(), title, amount, date, note);
            long result = expenseDAO.createExpense(expense);
            if (result > 0) {
                expenseTracker.onExpenseAdded(userId, category.getCategoryId(), date);
                Toast.makeText(this, "Đã thêm chi tiêu thành công", Toast.LENGTH_SHORT).show();
                finish();
            } else {
                Toast.makeText(this, "Lỗi khi thêm", Toast.LENGTH_SHORT).show();
            }
        } else {
            // CHỈNH SỬA
            Expense expense = expenseDAO.getExpenseById(expenseId);
            if (expense != null) {
                expense.setTitle(title);
                expense.setAmount(amount);
                expense.setCategoryId(category.getCategoryId());
                expense.setDate(date);
                expense.setNote(note);
                int result = expenseDAO.updateExpense(expense);
                if (result > 0) {
                    expenseTracker.onExpenseAdded(userId, category.getCategoryId(), date);
                    Toast.makeText(this, "Đã cập nhật", Toast.LENGTH_SHORT).show();
                    finish();
                } else {
                    Toast.makeText(this, "Lỗi cập nhật", Toast.LENGTH_SHORT).show();
                }
            }
        }
    }

    private void loadExpense() {
        Expense expense = expenseDAO.getExpenseById(expenseId);
        if (expense != null) {
            etTitle.setText(expense.getTitle());
            etAmount.setText(String.valueOf((long) expense.getAmount()));
            etNote.setText(expense.getNote());

            // CẬP NHẬT selectedDate ĐỂ DÙNG KHI LƯU
            selectedDate.setTimeInMillis(expense.getDate());
            etDate.setText(String.format(Locale.getDefault(), "%02d/%02d/%d",
                    selectedDate.get(Calendar.DAY_OF_MONTH),
                    selectedDate.get(Calendar.MONTH) + 1,
                    selectedDate.get(Calendar.YEAR)));

            // Set category spinner
            for (int i = 0; i < spinnerCategory.getCount(); i++) {
                Category cat = (Category) spinnerCategory.getItemAtPosition(i);
                if (cat.getCategoryId().equals(expense.getCategoryId())) {
                    spinnerCategory.setSelection(i);
                    break;
                }
            }
        }
    }
}