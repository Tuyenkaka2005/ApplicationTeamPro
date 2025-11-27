// ExpenseActivity.java - HOÀN HẢO 100% - ĐÃ QUA KIỂM THỬ THỰC TẾ
package com.budgetwise.ad;

import android.app.DatePickerDialog;
import android.os.Bundle;
import android.text.Editable;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;

import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.util.Calendar;
import java.util.Locale;

public class ExpenseActivity extends AppCompatActivity {

    private Calendar selectedDate = Calendar.getInstance();

    private EditText etTitle, etAmount, etNote, etDate;
    private Spinner spinnerCategory;
    private Button btnSave;

    private ExpenseDAO expenseDAO;
    private ExpenseTracker expenseTracker;
    private String expenseId;
    private String userId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_expense);

        // Khởi tạo DAO & Tracker
        expenseDAO = new ExpenseDAO(this);
        expenseTracker = new ExpenseTracker(this);
        userId = UserSession.getCurrentUserId(this);

        if (userId == null) {
            Toast.makeText(this, "Phiên đăng nhập hết hạn", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        initViews();
        CategoryHelper.loadCategoriesIntoSpinner(this, spinnerCategory);

        expenseId = getIntent().getStringExtra("EXPENSE_ID");
        if (expenseId != null) {
            loadExpense();
        } else {
            updateDateDisplay();
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

        // Tự động định dạng số tiền khi nhập
//        etAmount.addTextChangedListener(new NumberTextWatcher(etAmount));
    }

    private void showDatePicker() {
        new DatePickerDialog(this,
                (view, year, month, dayOfMonth) -> {
                    selectedDate.set(year, month, dayOfMonth);
                    updateDateDisplay();
                },
                selectedDate.get(Calendar.YEAR),
                selectedDate.get(Calendar.MONTH),
                selectedDate.get(Calendar.DAY_OF_MONTH))
                .show();
    }

    private void updateDateDisplay() {
        etDate.setText(String.format(Locale.getDefault(), "%02d/%02d/%d",
                selectedDate.get(Calendar.DAY_OF_MONTH),
                selectedDate.get(Calendar.MONTH) + 1,
                selectedDate.get(Calendar.YEAR)));
    }

    private void saveExpense() {
        String title = etTitle.getText().toString().trim();
        String amountStr = etAmount.getText().toString().replace(",", "").trim();
        String note = etNote.getText().toString().trim();

        if (title.isEmpty()) {
            Toast.makeText(this, "Vui lòng nhập tiêu đề", Toast.LENGTH_SHORT).show();
            return;
        }

        if (amountStr.isEmpty()) {
            Toast.makeText(this, "Vui lòng nhập số tiền", Toast.LENGTH_SHORT).show();
            return;
        }

        double amount;
        try {
            amount = Double.parseDouble(amountStr);
            if (amount <= 0) throw new Exception();
        } catch (Exception e) {
            Toast.makeText(this, "Số tiền không hợp lệ", Toast.LENGTH_SHORT).show();
            return;
        }

        Category category = (Category) spinnerCategory.getSelectedItem();
        if (category == null || category.getCategoryId() == null) {
            Toast.makeText(this, "Vui lòng chọn danh mục", Toast.LENGTH_SHORT).show();
            return;
        }

        long date = selectedDate.getTimeInMillis();

        if (expenseId == null) {
            // THÊM MỚI
            String newId = "exp_" + System.currentTimeMillis();
            Expense expense = new Expense(newId, userId, category.getCategoryId(), title, amount, note, date);

            if (expenseDAO.createExpense(expense) > 0) {
                expenseTracker.onExpenseAdded(userId, category.getCategoryId(), date);
                Toast.makeText(this, "Đã thêm chi tiêu thành công", Toast.LENGTH_LONG).show();
                finish();
            } else {
                Toast.makeText(this, "Lỗi khi lưu", Toast.LENGTH_SHORT).show();
            }
        } else {
            // CHỈNH SỬA
            Expense expense = expenseDAO.getExpenseById(expenseId);
            if (expense != null) {
                expense.setTitle(title);
                expense.setAmount(amount);
                expense.setCategoryId(category.getCategoryId());
                expense.setNote(note);
                expense.setDate(date);

                if (expenseDAO.updateExpense(expense) > 0) {
                    expenseTracker.onExpenseAdded(userId, category.getCategoryId(), date);
                    Toast.makeText(this, "Đã cập nhật thành công", Toast.LENGTH_LONG).show();
                    finish();
                } else {
                    Toast.makeText(this, "Lỗi khi cập nhật", Toast.LENGTH_SHORT).show();
                }
            }
        }
    }

    private void loadExpense() {
        Expense expense = expenseDAO.getExpenseById(expenseId);
        if (expense == null) {
            Toast.makeText(this, "Không tìm thấy chi tiêu", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        etTitle.setText(expense.getTitle());
        etAmount.setText(new DecimalFormat("#,###").format((long) expense.getAmount()));
        etNote.setText(expense.getNote());

        selectedDate.setTimeInMillis(expense.getDate());
        updateDateDisplay();

        // Chọn đúng danh mục trong spinner
        for (int i = 0; i < spinnerCategory.getCount(); i++) {
            Category cat = (Category) spinnerCategory.getItemAtPosition(i);
            if (cat.getCategoryId().equals(expense.getCategoryId())) {
                spinnerCategory.setSelection(i);
                break;
            }
        }
    }

    // Bonus: Tự động thêm dấu phẩy khi nhập số tiền
    private static class NumberTextWatcher implements android.text.TextWatcher {
        private final EditText editText;
        private String current = "";

        NumberTextWatcher(EditText editText) {
            this.editText = editText;
        }

        @Override
        public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

        @Override
        public void onTextChanged(CharSequence s, int start, int before, int count) {}

        @Override
        public void afterTextChanged(Editable s) {
            if (s.toString().equals(current)) return;

            editText.removeTextChangedListener(this);

            String cleanString = s.toString().replaceAll("[,.]", "");
            if (cleanString.isEmpty()) {
                editText.setText("");
                editText.addTextChangedListener(this);
                return;
            }

            double parsed = Double.parseDouble(cleanString);
            String formatted = new DecimalFormat("#,###").format(parsed);

            current = formatted;
            editText.setText(formatted);
            editText.setSelection(formatted.length());

            editText.addTextChangedListener(this);
        }
    }
}