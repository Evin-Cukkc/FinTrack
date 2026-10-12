package com.evinn.fintrack.adapter;

import android.view.View;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.evinn.fintrack.R;

public class ExpenseViewHolder extends RecyclerView.ViewHolder {

    TextView tvTitle;
    TextView tvCategory;
    TextView tvDate;
    TextView tvAmount;
    TextView tvStatus;

    public ExpenseViewHolder(@NonNull View itemView) {
        super(itemView);

        tvTitle = itemView.findViewById(R.id.tvTransactionTitle);
        tvCategory = itemView.findViewById(R.id.tvTransactionCategory);
        tvDate = itemView.findViewById(R.id.tvTransactionDate);
        tvAmount = itemView.findViewById(R.id.tvTransactionAmount);
        tvStatus = itemView.findViewById(R.id.tvTransactionStatus);
    }
}