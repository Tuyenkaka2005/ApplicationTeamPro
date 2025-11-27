package com.budgetwise.ad;

import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.activity.EdgeToEdge;
import com.google.android.material.appbar.MaterialToolbar;

public class MainActivity extends AppCompatActivity {

    private TextView tvGreeting;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);

        MaterialToolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        // Nút back (nếu cần)
        toolbar.setNavigationOnClickListener(v -> finish());

        // NÚT LOGOUT HOẠT ĐỘNG 100%
        findViewById(R.id.btnLogout).setOnClickListener(v -> showLogoutDialog());

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        initializeUI();
        updateGreeting();
    }
    private void showLogoutDialog() {
        new androidx.appcompat.app.AlertDialog.Builder(this)
                .setTitle("Đăng xuất")
                .setMessage("Bạn có chắc chắn muốn đăng xuất?")
                .setPositiveButton("Đăng xuất", (dialog, which) -> {
                    UserSession.clearUser(this);
                    Intent intent = new Intent(this, LoginActivity.class);
                    intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                    startActivity(intent);
                    finish();
                    Toast.makeText(this, "Đã đăng xuất thành công", Toast.LENGTH_LONG).show();
                })
                .setNegativeButton("Hủy", null)
                .show();
    }

    private void initializeUI() {
        tvGreeting = findViewById(R.id.tvGreeting);

        // Các card chức năng
        findViewById(R.id.cardExpenseTracking).setOnClickListener(v ->
                startActivity(new Intent(this, ExpenseListActivity.class)));

        findViewById(R.id.cardBudgetSetup).setOnClickListener(v ->
                startActivity(new Intent(this, BudgetActivity.class)));

        findViewById(R.id.cardExpenseOverview).setOnClickListener(v ->
                startActivity(new Intent(this, OverviewActivity.class)));

        findViewById(R.id.cardRecurringExpenses).setOnClickListener(v ->
                startActivity(new Intent(this, RecurringExpenseActivity.class)));

        findViewById(R.id.cardExpenseReport).setOnClickListener(v ->
                startActivity(new Intent(this, ReportActivity.class)));

        findViewById(R.id.cardSearchFilter).setOnClickListener(v ->
                startActivity(new Intent(this, SearchFilterActivity.class)));
    }

    private void updateGreeting() {
        String name = UserSession.getCurrentUserName(this);
        if (tvGreeting != null && name != null) {
            tvGreeting.setText("Xin chào, " + name + "!");
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main_menu, menu);
        return true;
    }

//    @Override
//    public boolean onOptionsItemSelected(MenuItem item) {
//        if (item.getItemId() == R.id.action_logout) {
//            new androidx.appcompat.app.AlertDialog.Builder(this)
//                    .setTitle("Đăng xuất")
//                    .setMessage("Bạn có chắc chắn muốn đăng xuất?")
//                    .setPositiveButton("Đăng xuất", (dialog, which) -> {
//                        UserSession.clearUser(this);
//                        Intent intent = new Intent(this, LoginActivity.class);
//                        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
//                        startActivity(intent);
//                        finish();
//                        Toast.makeText(this, "Đã đăng xuất thành công", Toast.LENGTH_SHORT).show();
//                    })
//                    .setNegativeButton("Hủy", null)
//                    .show();
//            return true;
//        }
//        return super.onOptionsItemSelected(item);
//    }

    @Override
    protected void onResume() {
        super.onResume();
        updateGreeting();
    }
}