package com.evinn.fintrack;

import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.evinn.fintrack.adapter.BudgetAdapter;
import com.evinn.fintrack.database.AppDatabase;
import com.evinn.fintrack.database.BudgetDao;
import com.evinn.fintrack.database.TransactionDao;
import com.evinn.fintrack.model.Budget;
import com.evinn.fintrack.model.Transaction;
import com.evinn.fintrack.utils.Categories;
import com.evinn.fintrack.utils.CurrencyUtils;
import com.evinn.fintrack.utils.DateUtils;
import com.evinn.fintrack.utils.NavigationHelper;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class BudgetActivity extends AppCompatActivity {

    private RecyclerView recyclerViewBudgets;
    private FloatingActionButton fabAddBudget;
    private TextView tvBudgetSummary;
    private TextView tvBudgetEmpty;

    private BudgetAdapter budgetAdapter;
    private final List<Budget> budgetList = new ArrayList<>();

    private BudgetDao budgetDao;
    private TransactionDao transactionDao;
    private ExecutorService databaseExecutor;

    // Transaksi terbaru dari LiveData, dipakai untuk menghitung ulang pengeluaran
    private List<Transaction> latestTransactions = new ArrayList<>();

    // Supaya pesan "belum ada anggaran" tidak berkedip sebelum data selesai dimuat
    private boolean budgetsLoaded = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_budget);

        recyclerViewBudgets = findViewById(R.id.recyclerViewBudgets);
        fabAddBudget = findViewById(R.id.fabAddBudget);
        tvBudgetSummary = findViewById(R.id.tvBudgetSummary);
        tvBudgetEmpty = findViewById(R.id.tvBudgetEmpty);

        AppDatabase database = AppDatabase.getInstance(this);
        budgetDao = database.budgetDao();
        transactionDao = database.transactionDao();
        databaseExecutor = Executors.newSingleThreadExecutor();

        setupRecyclerView();
        observeTransactions();
        loadBudgets();

        fabAddBudget.setOnClickListener(v -> showAddBudgetDialog());

        NavigationHelper.setup(this, R.id.nav_budget);
    }

    private void setupRecyclerView() {
        recyclerViewBudgets.setLayoutManager(new LinearLayoutManager(this));
        budgetAdapter = new BudgetAdapter(budgetList);
        recyclerViewBudgets.setAdapter(budgetAdapter);
    }

    private void loadBudgets() {
        databaseExecutor.execute(() -> {
            List<Budget> budgets = budgetDao.getAllBudgets();
            runOnUiThread(() -> {
                budgetsLoaded = true;
                budgetList.clear();
                budgetList.addAll(budgets);
                calculateBudgetSpending(latestTransactions);
            });
        });
    }

    private void observeTransactions() {
        transactionDao.getAllTransactions().observe(this, transactions -> {
            if (transactions == null) {
                return;
            }
            latestTransactions = transactions;
            calculateBudgetSpending(transactions);
        });
    }

    private void calculateBudgetSpending(List<Transaction> transactions) {
        for (Budget budget : budgetList) {
            String budgetKey = Categories.canonical(budget.getCategory());
            double spent = 0;

            for (Transaction transaction : transactions) {
                if (transaction.getAmount() < 0
                        && DateUtils.isInCurrentMonth(transaction.getDate())
                        && budgetKey.equals(Categories.canonical(transaction.getCategory()))) {
                    spent += Math.abs(transaction.getAmount());
                }
            }

            budget.setSpent(spent);
        }

        budgetAdapter.notifyDataSetChanged();
        updateSummary();
    }

    // Ringkasan total + tampilan kosong
    private void updateSummary() {
        double totalLimit = 0;
        double totalSpent = 0;

        for (Budget budget : budgetList) {
            totalLimit += budget.getAmount();
            totalSpent += budget.getSpent();
        }

        boolean empty = budgetList.isEmpty();

        tvBudgetEmpty.setVisibility(budgetsLoaded && empty ? View.VISIBLE : View.GONE);

        if (empty) {
            tvBudgetSummary.setText(R.string.budget_subtitle);
        } else {
            tvBudgetSummary.setText(getString(
                    R.string.budget_summary,
                    CurrencyUtils.formatRupiahWithoutSign(totalSpent),
                    CurrencyUtils.formatRupiahWithoutSign(totalLimit)));
        }
    }

    private boolean categoryExists(String category) {
        String key = Categories.canonical(category);
        for (Budget budget : budgetList) {
            if (key.equals(Categories.canonical(budget.getCategory()))) {
                return true;
            }
        }
        return false;
    }

    private void showAddBudgetDialog() {
        View dialogView = getLayoutInflater().inflate(R.layout.dialog_add_budget, null);

        AutoCompleteTextView actvCategory = dialogView.findViewById(R.id.actvBudgetCategory);
        EditText etAmount = dialogView.findViewById(R.id.etBudgetAmount);

        actvCategory.setAdapter(new ArrayAdapter<>(
                this, android.R.layout.simple_list_item_1, Categories.list(this)));

        AlertDialog dialog = new MaterialAlertDialogBuilder(this)
                .setTitle(R.string.add_budget)
                .setView(dialogView)
                .setNegativeButton(R.string.cancel, null)
                .setPositiveButton(R.string.save, null)
                .create();

        // Listener dipasang manual agar dialog tidak tertutup saat input belum valid.
        dialog.setOnShowListener(dialogInterface ->
                dialog.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener(v -> {
                    String categoryText = actvCategory.getText().toString().trim();
                    String amountText = etAmount.getText().toString().trim();

                    if (categoryText.isEmpty() || amountText.isEmpty()) {
                        Toast.makeText(this, R.string.msg_fill_all, Toast.LENGTH_SHORT).show();
                        return;
                    }

                    double budgetAmount;
                    try {
                        budgetAmount = CurrencyUtils.parseAmount(amountText);
                    } catch (NumberFormatException e) {
                        Toast.makeText(this, R.string.msg_invalid_amount, Toast.LENGTH_SHORT).show();
                        return;
                    }

                    if (budgetAmount <= 0) {
                        Toast.makeText(this, R.string.msg_invalid_amount, Toast.LENGTH_SHORT).show();
                        return;
                    }

                    if (categoryExists(categoryText)) {
                        Toast.makeText(this, R.string.msg_budget_exists, Toast.LENGTH_SHORT).show();
                        return;
                    }

                    Budget budget = new Budget(categoryText, budgetAmount);

                    databaseExecutor.execute(() -> {
                        long id = budgetDao.insert(budget);
                        budget.setId((int) id);

                        runOnUiThread(() -> {
                            budgetList.add(0, budget);
                            calculateBudgetSpending(latestTransactions);
                            recyclerViewBudgets.scrollToPosition(0);
                        });
                    });

                    dialog.dismiss();
                    Toast.makeText(this, R.string.msg_budget_saved, Toast.LENGTH_SHORT).show();
                }));

        dialog.show();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (databaseExecutor != null && !databaseExecutor.isShutdown()) {
            databaseExecutor.shutdown();
        }
    }
}
