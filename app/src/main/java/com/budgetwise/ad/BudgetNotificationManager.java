package com.budgetwise.ad;

import android.Manifest;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.Context;
import android.content.pm.PackageManager;
import android.os.Build;
import android.widget.Toast;

import androidx.core.app.ActivityCompat;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;

import java.util.List;

public class BudgetNotificationManager {
    private Context context;
    private BudgetDAO budgetDAO;
    private static int notificationId = 9000;

    private static final String CHANNEL_ID = "budget_channel";

    public BudgetNotificationManager(Context context) {
        this.context = context;
        this.budgetDAO = new BudgetDAO(context);
        createNotificationChannel(); // Tạo channel ngay khi khởi tạo
    }

    public void checkAllBudgets(String userId, int month, int year) {
        List<BudgetDAO.BudgetStatus> statuses = budgetDAO.getUserBudgetStatuses(userId, month, year);

        for (BudgetDAO.BudgetStatus status : statuses) {
            if (status.isOverBudget()) {
                notifyUser("Vượt ngân sách: " + getCategoryName(status));
            } else if (status.isNearLimit()) {
                notifyUser("Gần hết ngân sách: " + getCategoryName(status));
            }
        }
    }

    public void checkBudgetAndNotify(String userId, String categoryId, String categoryName, int month, int year) {
        List<BudgetDAO.BudgetStatus> statuses = budgetDAO.getUserBudgetStatuses(userId, month, year);

        for (BudgetDAO.BudgetStatus status : statuses) {
            if (!status.budget.getCategoryId().equals(categoryId)) continue;

            if (status.isOverBudget()) {
                notifyUser("Vượt ngân sách danh mục: " + categoryName);
            } else if (status.isNearLimit()) {
                notifyUser("Cảnh báo: Sắp hết ngân sách " + categoryName);
            }
        }
    }

    private String getCategoryName(BudgetDAO.BudgetStatus status) {
        Category category = new CategoryDAO(context).getCategoryById(status.budget.getCategoryId());
        return category != null ? category.getName() : "Không xác định";
    }

    private void notifyUser(String message) {
        NotificationCompat.Builder builder = new NotificationCompat.Builder(context, CHANNEL_ID)
                .setSmallIcon(android.R.drawable.stat_notify_error)
                .setContentTitle("Cảnh báo ngân sách")
                .setContentText(message)
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setDefaults(Notification.DEFAULT_SOUND | Notification.DEFAULT_VIBRATE)
                .setAutoCancel(true);

        NotificationManagerCompat notificationManager = NotificationManagerCompat.from(context);

        // Kiểm tra quyền trước khi gửi
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ActivityCompat.checkSelfPermission(context, Manifest.permission.POST_NOTIFICATIONS)
                    != PackageManager.PERMISSION_GRANTED) {
                // Quyền chưa được cấp → chỉ hiện Toast (không crash)
                Toast.makeText(context, message, Toast.LENGTH_LONG).show();
                return;
            }
        }

        notificationManager.notify(notificationId++, builder.build());
    }

    private void createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(
                    CHANNEL_ID,
                    "Ngân sách",
                    NotificationManager.IMPORTANCE_HIGH
            );
            channel.setDescription("Thông báo khi gần hết hoặc vượt ngân sách");

            NotificationManager manager = context.getSystemService(NotificationManager.class);
            if (manager != null) {
                manager.createNotificationChannel(channel);
            }
        }
    }
}