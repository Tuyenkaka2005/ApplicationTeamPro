package com.budgetwise.ad;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class RecurringExpenseAdapter extends RecyclerView.Adapter<RecurringExpenseAdapter.ViewHolder> {

    public interface OnItemActionListener {
        void onEditClick(RecurringExpense expense);
        void onDeleteClick(RecurringExpense expense);
    }

    private final Context context;
    private final List<RecurringExpense> list;
    private final OnItemActionListener listener;

    public RecurringExpenseAdapter(Context context, List<RecurringExpense> list, OnItemActionListener listener) {
        this.context = context;
        this.list = list;
        this.listener = listener;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(context).inflate(R.layout.item_recurring_expense, parent, false);
        return new ViewHolder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        RecurringExpense re = list.get(position);

        holder.tvRecurringCategory.setText(
                CategoryHelper.getCategoryNameById(context, re.getCategoryId())
                        + " • each " + re.getInterval().toLowerCase()
        );

        holder.tvRecurringTitle.setText(re.getTitle());
        holder.tvRecurringAmount.setText(String.format(Locale.getDefault(), "%,.0f đ", re.getAmount()));

        SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());
        holder.tvRecurringInterval.setText("next time: " + sdf.format(new Date(re.getNextRunDate())));

        // Click vào toàn bộ item → Edit
        holder.itemView.setOnClickListener(v -> listener.onEditClick(re));

        // Click nút xóa → Delete
        holder.itemView.findViewById(R.id.btnDeleteItem).setOnClickListener(v -> listener.onDeleteClick(re));
    }

    @Override
    public int getItemCount() {
        return list.size();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvRecurringTitle, tvRecurringAmount, tvRecurringCategory, tvRecurringInterval, tvNextRun;
        ViewHolder(View v) {
            super(v);
            tvRecurringTitle = v.findViewById(R.id.tvRecurringTitle);
            tvRecurringAmount = v.findViewById(R.id.tvRecurringAmount);
            tvRecurringCategory = v.findViewById(R.id.tvRecurringCategory);
            tvRecurringInterval = v.findViewById(R.id.tvRecurringInterval);
            tvNextRun = v.findViewById(R.id.tvNextRun);
        }
    }
}