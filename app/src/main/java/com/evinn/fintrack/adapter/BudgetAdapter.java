package com.evinn.fintrack.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.evinn.fintrack.R;
import com.evinn.fintrack.model.Budget;
import com.evinn.fintrack.utils.CurrencyUtils;
import com.google.android.material.progressindicator.LinearProgressIndicator;

import java.util.List;
import java.util.Locale;

public class BudgetAdapter
        extends RecyclerView.Adapter<BudgetAdapter.BudgetViewHolder> {

    private final List<Budget> budgetList;

    public BudgetAdapter(List<Budget> budgetList) {
        this.budgetList = budgetList;
    }

    @NonNull
    @Override
    public BudgetViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_budget, parent, false);

        return new BudgetViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull BudgetViewHolder holder, int position) {
        Budget budget = budgetList.get(position);
        Context context = holder.itemView.getContext();

        double amount = budget.getAmount();
        double spent = budget.getSpent();
        double remaining = amount - spent;

        holder.tvCategory.setText(budget.getCategory());

        holder.tvBudgetAmount.setText(context.getString(
                R.string.budget_of, CurrencyUtils.formatRupiahWithoutSign(amount)));

        holder.tvSpent.setText(context.getString(
                R.string.budget_spent, CurrencyUtils.formatRupiahWithoutSign(spent)));

        if (remaining >= 0) {
            holder.tvRemaining.setText(context.getString(
                    R.string.budget_remaining, CurrencyUtils.formatRupiahWithoutSign(remaining)));
        } else {
            holder.tvRemaining.setText(context.getString(
                    R.string.budget_over, CurrencyUtils.formatRupiahWithoutSign(-remaining)));
        }

        double ratio = amount > 0 ? spent / amount : 0;
        int percent = (int) Math.round(ratio * 100);
        int progress = Math.min(percent, 100);

        // Hijau = aman, oranye = hampir habis (>= 80%), merah = melebihi anggaran
        int colorRes;
        if (ratio >= 1) {
            colorRes = R.color.expense_red;
        } else if (ratio >= 0.8) {
            colorRes = R.color.bill_orange;
        } else {
            colorRes = R.color.income_green;
        }
        int color = ContextCompat.getColor(context, colorRes);

        holder.tvPercent.setText(String.format(Locale.getDefault(), "%d%%", percent));
        holder.tvPercent.setTextColor(color);
        holder.tvRemaining.setTextColor(ContextCompat.getColor(
                context, ratio >= 1 ? R.color.expense_red : R.color.text_secondary));

        holder.progressBudget.setIndicatorColor(color);
        holder.progressBudget.setProgress(progress);
    }

    @Override
    public int getItemCount() {
        return budgetList.size();
    }

    static class BudgetViewHolder extends RecyclerView.ViewHolder {

        TextView tvCategory;
        TextView tvBudgetAmount;
        TextView tvSpent;
        TextView tvRemaining;
        TextView tvPercent;
        LinearProgressIndicator progressBudget;

        public BudgetViewHolder(@NonNull View itemView) {
            super(itemView);

            tvCategory = itemView.findViewById(R.id.tvBudgetCategory);
            tvBudgetAmount = itemView.findViewById(R.id.tvBudgetAmount);
            tvSpent = itemView.findViewById(R.id.tvBudgetSpent);
            tvRemaining = itemView.findViewById(R.id.tvBudgetRemaining);
            tvPercent = itemView.findViewById(R.id.tvBudgetPercent);
            progressBudget = itemView.findViewById(R.id.progressBudget);
        }
    }
}
