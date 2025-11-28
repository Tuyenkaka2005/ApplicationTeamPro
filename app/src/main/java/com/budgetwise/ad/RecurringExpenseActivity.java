package com.budgetwise.ad;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.List;

public class RecurringExpenseActivity extends AppCompatActivity {

    private EditText etTitle, etAmount, etNote;
    private Spinner spinnerCategory, spinnerInterval;
    private Button btnAddOrUpdate, btnDelete;
    private TextView tvTitle;
    private RecyclerView rvRecurring;
    private RecurringExpenseAdapter adapter;
    private List<RecurringExpense> recurringList = new ArrayList<>();

    private boolean isEditMode = false;
    private String editingExpenseId = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_recurring_expense);

        initViews();
        setupListeners();
        loadRecurringExpenses();

        // Check if we were opened for editing
        if (getIntent().hasExtra("RECURRING_ID")) {
            editingExpenseId = getIntent().getStringExtra("RECURRING_ID");
            isEditMode = (editingExpenseId != null);
            if (isEditMode) {
                prepareEditMode();
            }
        }
    }

    private void initViews() {
        tvTitle = findViewById(R.id.tvRecurringTitle);
        etTitle = findViewById(R.id.etRecurringTitle);
        etAmount = findViewById(R.id.etRecurringAmount);
        etNote = findViewById(R.id.etRecurringNote);
        spinnerCategory = findViewById(R.id.spinnerRecurringCategory);
        spinnerInterval = findViewById(R.id.spinnerRecurringInterval);
        btnAddOrUpdate = findViewById(R.id.btnAddRecurring);
        btnDelete = findViewById(R.id.btnDeleteRecurring);
        rvRecurring = findViewById(R.id.rvRecurringExpenses);

        CategoryHelper.loadCategoriesIntoSpinner(this, spinnerCategory);

        adapter = new RecurringExpenseAdapter(this, recurringList, new RecurringExpenseAdapter.OnItemActionListener() {
            @Override
            public void onEditClick(RecurringExpense expense) {
                isEditMode = true;
                editingExpenseId = expense.getRecurringId();
                prepareEditMode();
            }

            @Override
            public void onDeleteClick(RecurringExpense expense) {
                new AlertDialog.Builder(RecurringExpenseActivity.this)
                        .setTitle("Delete Recurring Expense")
                        .setMessage("Delete\"" + expense.getTitle() + "\" permanently?")
                        .setPositiveButton("Delete", (d, w) -> {
                            RecurringHelper.deleteRecurringExpense(RecurringExpenseActivity.this, expense.getRecurringId());
                            loadRecurringExpenses();
                            Toast.makeText(RecurringExpenseActivity.this, "Deleted!", Toast.LENGTH_SHORT).show();
                        })
                        .setNegativeButton("Cancel", null)
                        .show();
                    }
                });
                rvRecurring.setLayoutManager(new LinearLayoutManager(this));
                rvRecurring.setAdapter(adapter);
    }

    private void setupListeners() {
        btnAddOrUpdate.setOnClickListener(v -> saveRecurringExpense());
        btnDelete.setOnClickListener(v -> showDeleteConfirmationDialog());
    }

    private void handleEditClick(RecurringExpense expense) {
        isEditMode = true;
        editingExpenseId = expense.getRecurringId();
        prepareEditMode();
    }

    private void prepareEditMode() {
        RecurringExpense expenseToEdit = RecurringHelper.getRecurringExpenseById(this, editingExpenseId);
        if (expenseToEdit == null) {
            Toast.makeText(this, R.string.recurring_expense_error_not_found, Toast.LENGTH_SHORT).show();
            clearFormAndExitEditMode();
            return;
        }

        tvTitle.setText(R.string.recurring_expense_title_edit);
        etTitle.setText(expenseToEdit.getTitle());
        etAmount.setText(String.format("%.0f", expenseToEdit.getAmount()));
        etNote.setText(expenseToEdit.getNote());
        CategoryHelper.selectSpinnerValue(spinnerCategory, expenseToEdit.getCategoryId());

        String[] intervals = getResources().getStringArray(R.array.recurring_intervals);
        for (int i = 0; i < intervals.length; i++) {
            if (intervals[i].equals(expenseToEdit.getInterval())) {
                spinnerInterval.setSelection(i);
                break;
            }
        }

        btnAddOrUpdate.setText(R.string.recurring_expense_update_button);
        btnDelete.setVisibility(View.VISIBLE);
    }

    private void saveRecurringExpense() {
        String title = etTitle.getText().toString().trim();
        String amountStr = etAmount.getText().toString().trim();
        String note = etNote.getText().toString().trim();

        if (title.isEmpty() || amountStr.isEmpty()) {
            Toast.makeText(this, R.string.recurring_expense_error_enter_name_amount, Toast.LENGTH_SHORT).show();
            return;
        }
        if (spinnerCategory.getSelectedItem() == null) {
            Toast.makeText(this, R.string.recurring_expense_error_no_category, Toast.LENGTH_SHORT).show();
            return;
        }
        double amount;
        try {
            amount = Double.parseDouble(amountStr);
            if (amount <= 0) throw new NumberFormatException();
        } catch (Exception e) {
            Toast.makeText(this, R.string.recurring_expense_error_invalid_amount, Toast.LENGTH_SHORT).show();
            return;
        }

        if (isEditMode) {
            updateRecurringExpense(title, amount, note);
        } else {
            addRecurringExpense(title, amount, note);
        }
    }

    private void addRecurringExpense(String title, double amount, String note) {
        Category selectedCategory = (Category) spinnerCategory.getSelectedItem();
        String categoryId = selectedCategory.getCategoryId();
        String interval = spinnerInterval.getSelectedItem().toString();
        String userId = UserSession.getCurrentUserId(this);

        long startDate = System.currentTimeMillis();
        long nextRun = RecurringHelper.calculateNextRunDate(interval, startDate);

        RecurringExpense recurring = new RecurringExpense(
                java.util.UUID.randomUUID().toString(), userId, categoryId, title, amount,
                note.isEmpty() ? null : note, interval, startDate, nextRun);

        if (RecurringHelper.insertRecurringExpense(this, recurring)) {
            Toast.makeText(this, R.string.recurring_expense_success_added, Toast.LENGTH_SHORT).show();
            clearFormAndExitEditMode();
            loadRecurringExpenses();
        } else {
            Toast.makeText(this, R.string.recurring_expense_error_add, Toast.LENGTH_SHORT).show();
        }
    }

    private void updateRecurringExpense(String title, double amount, String note) {
        RecurringExpense originalExpense = RecurringHelper.getRecurringExpenseById(this, editingExpenseId);
        if (originalExpense == null) return;

        Category selectedCategory = (Category) spinnerCategory.getSelectedItem();
        String categoryId = selectedCategory.getCategoryId();
        String interval = spinnerInterval.getSelectedItem().toString();

        long nextRun = RecurringHelper.calculateNextRunDate(interval, originalExpense.getStartDate());

        RecurringExpense updatedExpense = new RecurringExpense(
                editingExpenseId, originalExpense.getUserId(), categoryId, title, amount,
                note.isEmpty() ? null : note, interval, originalExpense.getStartDate(), nextRun);

        if (RecurringHelper.updateRecurringExpense(this, updatedExpense)) {
            Toast.makeText(this, R.string.recurring_expense_success_updated, Toast.LENGTH_SHORT).show();
            clearFormAndExitEditMode();
            loadRecurringExpenses();
        } else {
            Toast.makeText(this, R.string.recurring_expense_error_update, Toast.LENGTH_SHORT).show();
        }
    }

    private void showDeleteConfirmationDialog() {
        new AlertDialog.Builder(this)
                .setTitle(R.string.recurring_expense_confirm_delete_title)
                .setMessage(R.string.recurring_expense_confirm_delete_message)
                .setPositiveButton(R.string.recurring_expense_delete_button, (dialog, which) -> deleteRecurringExpense())
                .setNegativeButton(android.R.string.cancel, null)
                .show();
    }

    private void deleteRecurringExpense() {
        if (RecurringHelper.deleteRecurringExpense(this, editingExpenseId)) {
            Toast.makeText(this, R.string.recurring_expense_success_deleted, Toast.LENGTH_SHORT).show();
            clearFormAndExitEditMode();
            loadRecurringExpenses();
        } else {
            Toast.makeText(this, R.string.recurring_expense_error_delete, Toast.LENGTH_SHORT).show();
        }
    }

    private void clearFormAndExitEditMode() {
        etTitle.setText("");
        etAmount.setText("");
        etNote.setText("");
        spinnerCategory.setSelection(0);
        spinnerInterval.setSelection(0);

        tvTitle.setText(R.string.recurring_expense_title_add);
        btnAddOrUpdate.setText(R.string.recurring_expense_add_button);
        btnDelete.setVisibility(View.GONE);

        isEditMode = false;
        editingExpenseId = null;
    }

    private void loadRecurringExpenses() {
        recurringList.clear();
        recurringList.addAll(RecurringHelper.getAllRecurringExpenses(this));
        adapter.notifyDataSetChanged();
    }

    @Override
    protected void onResume() {
        super.onResume();
        loadRecurringExpenses();
    }
}
