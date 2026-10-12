package com.evinn.fintrack.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.evinn.fintrack.R;
import com.evinn.fintrack.model.Transaction;
import com.evinn.fintrack.utils.CurrencyUtils;

import java.util.List;

public class TransactionAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    // Interface tunggal untuk memproses Click dan Long Click
    public interface OnTransactionClickListener {
        void onTransactionClick(Transaction transaction);
        boolean onTransactionLongClick(Transaction transaction);
    }

    private static final int TYPE_INCOME = 1;
    private static final int TYPE_EXPENSE = 2;
    private static final int TYPE_BILL = 3;

    private final List<Transaction> transactionList;
    private OnTransactionClickListener listener;

    public TransactionAdapter(List<Transaction> transactionList) {
        this(transactionList, null);
    }

    public TransactionAdapter(List<Transaction> transactionList, OnTransactionClickListener listener) {
        this.transactionList = transactionList;
        this.listener = listener;
    }

    @Override
    public int getItemViewType(int position) {
        String type = transactionList.get(position).getType();

        if ("income".equalsIgnoreCase(type)) {
            return TYPE_INCOME;
        }

        if ("bill".equalsIgnoreCase(type)) {
            return TYPE_BILL;
        }

        return TYPE_EXPENSE;
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_transaction, parent, false);

        if (viewType == TYPE_INCOME) {
            return new IncomeViewHolder(view);
        }

        if (viewType == TYPE_BILL) {
            return new BillViewHolder(view);
        }

        return new ExpenseViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        Transaction transaction = transactionList.get(position);

        // Click Event
        holder.itemView.setOnClickListener(v -> {
            if (listener != null) {
                listener.onTransactionClick(transaction);
            }
        });

        // Long Click Event
        holder.itemView.setOnLongClickListener(v -> {
            if (listener != null) {
                return listener.onTransactionLongClick(transaction);
            }
            return false;
        });

        TextView tvIcon = holder.itemView.findViewById(R.id.tvTransactionIcon);
        TextView tvTitle = holder.itemView.findViewById(R.id.tvTransactionTitle);
        TextView tvCategory = holder.itemView.findViewById(R.id.tvTransactionCategory);
        TextView tvDate = holder.itemView.findViewById(R.id.tvTransactionDate);
        TextView tvAmount = holder.itemView.findViewById(R.id.tvTransactionAmount);
        TextView tvStatus = holder.itemView.findViewById(R.id.tvTransactionStatus);

        tvTitle.setText(transaction.getTitle());
        tvCategory.setText(transaction.getCategory());
        tvDate.setText(transaction.getDate());
        tvAmount.setText(CurrencyUtils.formatRupiah(transaction.getAmount()));

        String status = transaction.getStatus();
        tvStatus.setText(status);
        tvStatus.setVisibility(status == null || status.trim().isEmpty() ? View.GONE : View.VISIBLE);

        int viewType = getItemViewType(position);
        int colorRes;
        String defaultIcon;

        switch (viewType) {
            case TYPE_INCOME:
                colorRes = R.color.income_green;
                defaultIcon = "💰";
                break;

            case TYPE_BILL:
                colorRes = R.color.bill_orange;
                defaultIcon = "⚡";
                break;

            case TYPE_EXPENSE:
            default:
                colorRes = R.color.expense_red;
                defaultIcon = "💳";
                break;
        }

        // Panggil helper untuk mencocokkan icon berdasarkan Title & Category
        String iconText = getTransactionIcon(transaction.getTitle(), transaction.getCategory(), defaultIcon);

        int color = ContextCompat.getColor(holder.itemView.getContext(), colorRes);

        tvIcon.setText(iconText);
        tvAmount.setTextColor(color);
        tvStatus.setTextColor(color);
    }

    private String getTransactionIcon(String title, String category, String defaultIcon) {
        String titleKey = title != null ? title.toLowerCase().trim() : "";
        String categoryKey = category != null ? category.toLowerCase().trim() : "";

        // 1. Pengecekan Kata Kunci Spesifik pada Judul (Brand/Merchant)
        if (titleKey.contains("netflix") || titleKey.contains("cinema") || titleKey.contains("bioskop")) {
            return "🎬";
        }
        if (titleKey.contains("starbucks") || titleKey.contains("kopi") || titleKey.contains("coffee")) {
            return "☕";
        }
        if (titleKey.contains("pln") || titleKey.contains("listrik") || titleKey.contains("electricity")) {
            return "⚡";
        }
        if (titleKey.contains("book") || titleKey.contains("gramedia") || titleKey.contains("buku")) {
            return "📚";
        }
        if (titleKey.contains("salary") || titleKey.contains("gaji")) {
            return "💰";
        }
        if (titleKey.contains("gojek") || titleKey.contains("grab") || titleKey.contains("uber") || titleKey.contains("bensin")) {
            return "🚗";
        }

        // 2. Pengecekan Berdasarkan Kategori Utama (Fallback)
        switch (categoryKey) {
            case "food":
            case "makanan":
            case "restaurant":
            case "restoran":
                return "🍔";

            case "coffee":
            case "kopi":
                return "☕";

            case "shopping":
            case "belanja":
                return "🛍️";

            case "transport":
            case "transportation":
            case "transportasi":
                return "🚗";

            case "entertainment":
            case "hiburan":
                return "🎬";

            case "subscription":
            case "langganan":
                return "📺";

            case "electricity":
            case "listrik":
                return "⚡";

            case "book":
            case "books":
            case "buku":
                return "📚";

            case "health":
            case "kesehatan":
            case "medical":
                return "💊";

            case "salary":
            case "gaji":
                return "💰";

            case "education":
            case "pendidikan":
                return "🎓";

            default:
                return defaultIcon;
        }
    }

    public void updateList(List<Transaction> newList) {
        if (transactionList != null && newList != null) {
            transactionList.clear();
            transactionList.addAll(newList);
            notifyDataSetChanged();
        }
    }

    @Override
    public int getItemCount() {
        return transactionList != null ? transactionList.size() : 0;
    }
}
