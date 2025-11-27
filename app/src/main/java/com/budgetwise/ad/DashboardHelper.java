package com.budgetwise.ad;

import android.content.Context;
import java.util.Calendar;

public class DashboardHelper {
    private final BudgetDAO budgetDAO;
    private final DatabaseHelper dbHelper;

    public DashboardHelper(Context context) {
        budgetDAO = new BudgetDAO(context);
        dbHelper = DatabaseHelper.getInstance(context);
    }

    // Lấy tổng chi tiêu tháng hiện tại
    public double getTotalSpentThisMonth(String userId) {
        Calendar cal = Calendar.getInstance();
        int month = cal.get(Calendar.MONTH) + 1;
        int year = cal.get(Calendar.YEAR);

        // Dùng hàm có sẵn trong DatabaseHelper (rất tốt!)
        cal.set(year, month - 1, 1, 0, 0, 0);
        long startOfMonth = cal.getTimeInMillis();

        cal.add(Calendar.MONTH, 1);
        cal.add(Calendar.MILLISECOND, -1);
        long endOfMonth = cal.getTimeInMillis();

        return dbHelper.getTotalSpent(userId, startOfMonth, endOfMonth);
    }

    // Lấy tổng ngân sách tháng hiện tại (tất cả danh mục active)
    public double getTotalBudgetThisMonth(String userId) {
        Calendar cal = Calendar.getInstance();
        int month = cal.get(Calendar.MONTH) + 1;
        int year = cal.get(Calendar.YEAR);

        return budgetDAO.getTotalBudgetByMonthYear(userId, month, year);
    }

    // Tính % đã dùng
    public double getSpendingPercentage(String userId) {
        double spent = getTotalSpentThisMonth(userId);
        double budget = getTotalBudgetThisMonth(userId);
        if (budget <= 0) return 0;
        return (spent / budget) * 100.0;
    }

    // Trả về object tiện dùng
    public static class Summary {
        public double totalSpent;
        public double totalBudget;
        public double remaining;
        public double percentage;

        public Summary(double spent, double budget) {
            this.totalSpent = spent;
            this.totalBudget = budget;
            this.remaining = budget - spent;
            this.percentage = budget > 0 ? (spent / budget) * 100.0 : 0;
        }
    }

    public Summary getMonthlySummary(String userId) {
        double spent = getTotalSpentThisMonth(userId);
        double budget = getTotalBudgetThisMonth(userId);
        return new Summary(spent, budget);
    }
}