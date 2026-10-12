package com.evinn.fintrack;

import android.os.Bundle;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import com.evinn.fintrack.database.AppDatabase;
import com.evinn.fintrack.database.TransactionDao;
import com.evinn.fintrack.model.Transaction;
import com.evinn.fintrack.utils.CurrencyUtils;
import com.evinn.fintrack.utils.NavigationHelper;
import com.google.android.material.progressindicator.LinearProgressIndicator;

import java.util.Locale;

public class AnalyticsActivity extends AppCompatActivity {

    private TextView tvAnalyticsBalance;
    private TextView tvTotalIncome;
    private TextView tvTotalExpense;
    private TextView tvSavingsRate;
    private TextView tvTransactionCount;
    private LinearProgressIndicator progressSavings;
    private AnalyticsChartView analyticsChart;

    private TransactionDao transactionDao;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_analytics);

        // Inisialisasi View
        tvAnalyticsBalance = findViewById(R.id.tvAnalyticsBalance);
        tvTotalIncome = findViewById(R.id.tvTotalIncome);
        tvTotalExpense = findViewById(R.id.tvTotalExpense);
        tvSavingsRate = findViewById(R.id.tvSavingsRate);
        tvTransactionCount = findViewById(R.id.tvTransactionCount);
        progressSavings = findViewById(R.id.progressSavings);
        analyticsChart = findViewById(R.id.analyticsChart);

        // Inisialisasi Database
        AppDatabase database = AppDatabase.getInstance(this);
        transactionDao = database.transactionDao();

        NavigationHelper.setup(this, R.id.nav_analytics);

        loadAnalytics();
    }

    private void loadAnalytics() {
        // Menggunakan LiveData untuk mengamati perubahan data secara real-time
        transactionDao.getAllTransactions().observe(this, transactions -> {
            if (transactions == null) return;

            double income = 0;
            double expense = 0;

            for (Transaction transaction : transactions) {
                double amount = transaction.getAmount();

                if (amount > 0) {
                    income += amount;
                } else {
                    expense += Math.abs(amount);
                }
            }

            double balance = income - expense;
            double savingsRate = (income > 0) ? (balance / income) * 100 : 0;

            tvAnalyticsBalance.setText(CurrencyUtils.formatRupiahWithoutSign(balance));
            tvTotalIncome.setText("+ " + CurrencyUtils.formatRupiahWithoutSign(income));
            tvTotalExpense.setText("- " + CurrencyUtils.formatRupiahWithoutSign(expense));

            tvSavingsRate.setText(String.format(
                    new Locale("id", "ID"), "%.1f%%", savingsRate));
            tvTransactionCount.setText(String.valueOf(transactions.size()));

            // Bar tabungan: hijau jika positif, merah jika pengeluaran melebihi pemasukan
            int progress = (int) Math.max(0, Math.min(100, Math.round(savingsRate)));
            progressSavings.setIndicatorColor(ContextCompat.getColor(
                    this, savingsRate >= 0 ? R.color.accent_green : R.color.expense_red));
            progressSavings.setProgress(progress);

            if (analyticsChart != null) {
                analyticsChart.setData(income, expense);
            }
        });
    }
}
