package com.budgetwise.ad;

import android.content.Intent;
import android.os.Bundle;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.activity.EdgeToEdge;

import com.google.android.material.appbar.MaterialToolbar;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);

        // Xử lý padding cho status bar / navigation bar
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(android.R.id.content), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        // Kiểm tra đăng nhập
        if (UserSession.getCurrentUserId(this) == null) {
            startActivity(new Intent(this, LoginActivity.class));
            finish();
            return;
        }

        setupToolbar();
        updateGreeting();
        initializeUI();
        OverviewHelper.generateMissedRecurringExpenses(this);
    }

    private void setupToolbar() {
        MaterialToolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        // Xử lý menu items (Đăng xuất, Hồ sơ, Cài đặt)
        toolbar.setOnMenuItemClickListener(item -> {
            int itemId = item.getItemId();

            if (itemId == R.id.action_logout) {
                showLogoutDialog();
                return true;
            } else if (itemId == R.id.action_profile) {
                Toast.makeText(this, "Chức năng Hồ sơ cá nhân đang phát triển", Toast.LENGTH_SHORT).show();
                return true;
            } else if (itemId == R.id.action_settings) {
                Toast.makeText(this, "Chức năng Cài đặt đang phát triển", Toast.LENGTH_SHORT).show();
                return true;
            }

            return false;
        });

        // Xử lý nút logout tùy chỉnh (nếu menu không hoạt động)
        findViewById(R.id.btnLogout).setOnClickListener(v -> showLogoutDialog());
    }

    private void updateGreeting() {
        TextView tvGreeting = findViewById(R.id.tvGreeting);
        TextView tvSubGreeting = findViewById(R.id.tvSubGreeting);

        String userName = UserSession.getCurrentUserName(this);
        if (userName != null && !userName.trim().isEmpty()) {
            tvGreeting.setText("Xin chào, " + userName + "!");
            tvSubGreeting.setText("Quản lý chi tiêu thông minh cùng BudgetWise");
        } else {
            tvGreeting.setText("Chào mừng trở lại!");
            tvSubGreeting.setText("Quản lý chi tiêu thông minh cùng BudgetWise");
        }
    }

    private void showLogoutDialog() {
        new AlertDialog.Builder(this)
                .setTitle("Đăng xuất")
                .setMessage("Bạn có chắc chắn muốn đăng xuất?")
                .setPositiveButton("Đăng xuất", (dialog, which) -> {
                    // Xóa hoàn toàn thông tin đăng nhập
                    UserSession.clearUser(this);

                    // Chuyển về màn hình đăng nhập và xóa hết activity cũ
                    Intent intent = new Intent(this, LoginActivity.class);
                    intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                    startActivity(intent);
                    finish();

                    Toast.makeText(this, "Đã đăng xuất thành công", Toast.LENGTH_SHORT).show();
                })
                .setNegativeButton("Hủy", null)
                .show();
    }

    private void initializeUI() {
        // Expense Tracking
        findViewById(R.id.cardExpenseTracking).setOnClickListener(v ->
                startActivity(new Intent(this, ExpenseListActivity.class)));

        // Budget Setup
        findViewById(R.id.cardBudgetSetup).setOnClickListener(v ->
                startActivity(new Intent(this, BudgetActivity.class)));

        // Expense Overview
        findViewById(R.id.cardExpenseOverview).setOnClickListener(v ->
                startActivity(new Intent(this, OverviewActivity.class)));

        // Recurring Expenses
        findViewById(R.id.cardRecurringExpenses).setOnClickListener(v ->
                startActivity(new Intent(this, RecurringExpenseActivity.class)));

        // Expense Report (nếu cần)
        findViewById(R.id.cardExpenseReport).setOnClickListener(v ->
                Toast.makeText(this, "Chức năng Báo cáo đang phát triển", Toast.LENGTH_SHORT).show());

        // Search & Filter (nếu cần)
        findViewById(R.id.cardSearchFilter).setOnClickListener(v ->
                Toast.makeText(this, "Chức năng Tìm kiếm đang phát triển", Toast.LENGTH_SHORT).show());
    }

    @Override
    protected void onResume() {
        super.onResume();
        // Cập nhật lại tên người dùng nếu thay đổi
        updateGreeting();
    }
}