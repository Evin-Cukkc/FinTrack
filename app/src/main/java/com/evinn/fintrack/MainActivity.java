package com.evinn.fintrack;

import android.content.Intent;
import android.content.res.ColorStateList;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.ColorRes;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.evinn.fintrack.adapter.TransactionAdapter;
import com.evinn.fintrack.model.Transaction;
import com.evinn.fintrack.utils.Categories;
import com.evinn.fintrack.utils.CurrencyUtils;
import com.evinn.fintrack.utils.DateUtils;
import com.evinn.fintrack.utils.NavigationHelper;
import com.evinn.fintrack.viewmodel.TransactionViewModel;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.button.MaterialButtonToggleGroup;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

public class MainActivity extends AppCompatActivity {

    private RecyclerView recyclerViewTransactions;
    private TransactionAdapter transactionAdapter;
    private EditText etSearch;
    private FloatingActionButton fabAddTransaction;
    private TextView tvEmpty;

    // ViewModel untuk mengelola data transaksi
    private TransactionViewModel transactionViewModel;

    // Master list data transaksi
    private final List<Transaction> transactionList = new ArrayList<>();

    private TextView btnFilterSemua;
    private TextView btnFilterPemasukan;
    private TextView btnFilterPengeluaran;
    private TextView btnFilterTagihan;

    // Ringkasan saldo
    private TextView tvTotalBalance;
    private TextView tvHomeIncome;
    private TextView tvHomeExpense;

    private String filterSaatIni = "all";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Inisialisasi ViewModel
        transactionViewModel = new ViewModelProvider(this).get(TransactionViewModel.class);

        initViews();
        setupHeader();
        setupFilterButtons();
        setupRecyclerView();
        setupSearch();
        observeTransactions();

        updateFilterUI(btnFilterSemua);

        NavigationHelper.setup(this, R.id.nav_home);
    }

    private void initViews() {
        recyclerViewTransactions = findViewById(R.id.recyclerViewTransactions);
        btnFilterSemua = findViewById(R.id.btnFilterAll);
        btnFilterPemasukan = findViewById(R.id.btnFilterIncome);
        btnFilterPengeluaran = findViewById(R.id.btnFilterExpense);
        btnFilterTagihan = findViewById(R.id.btnFilterBill);
        etSearch = findViewById(R.id.etSearch);
        fabAddTransaction = findViewById(R.id.fabAddTransaction);
        tvEmpty = findViewById(R.id.tvEmpty);

        tvTotalBalance = findViewById(R.id.tvTotalBalance);
        tvHomeIncome = findViewById(R.id.tvHomeIncome);
        tvHomeExpense = findViewById(R.id.tvHomeExpense);

        fabAddTransaction.setOnClickListener(v -> showAddTransactionDialog());
    }

    // Sapaan sesuai jam, label bulan berjalan, dan pintasan ke profil.
    private void setupHeader() {
        TextView tvGreeting = findViewById(R.id.tvGreeting);
        TextView tvMonth = findViewById(R.id.tvCurrentMonth);
        View imgProfile = findViewById(R.id.imgHeaderProfile);

        int hour = Calendar.getInstance().get(Calendar.HOUR_OF_DAY);
        int greeting;
        if (hour >= 4 && hour < 11) {
            greeting = R.string.greeting_morning;
        } else if (hour >= 11 && hour < 15) {
            greeting = R.string.greeting_afternoon;
        } else if (hour >= 15 && hour < 18) {
            greeting = R.string.greeting_evening;
        } else {
            greeting = R.string.greeting_night;
        }

        tvGreeting.setText(greeting);
        tvMonth.setText(DateUtils.getCurrentMonthLabel());

        imgProfile.setOnClickListener(v ->
                startActivity(new Intent(MainActivity.this, ProfileActivity.class)));
    }

    private void setupRecyclerView() {
        recyclerViewTransactions.setLayoutManager(new LinearLayoutManager(this));

        transactionAdapter = new TransactionAdapter(
                new ArrayList<>(),
                new TransactionAdapter.OnTransactionClickListener() {
                    @Override
                    public void onTransactionClick(Transaction transaction) {
                        showEditTransactionDialog(transaction);
                    }

                    @Override
                    public boolean onTransactionLongClick(Transaction transaction) {
                        showDeleteConfirmationDialog(transaction);
                        return true;
                    }
                }
        );

        recyclerViewTransactions.setAdapter(transactionAdapter);
    }

    private void setupFilterButtons() {
        btnFilterSemua.setOnClickListener(v -> {
            filterTransactions("all");
            updateFilterUI(btnFilterSemua);
        });

        btnFilterPemasukan.setOnClickListener(v -> {
            filterTransactions("income");
            updateFilterUI(btnFilterPemasukan);
        });

        btnFilterPengeluaran.setOnClickListener(v -> {
            filterTransactions("expense");
            updateFilterUI(btnFilterPengeluaran);
        });

        btnFilterTagihan.setOnClickListener(v -> {
            filterTransactions("bill");
            updateFilterUI(btnFilterTagihan);
        });
    }

    private void filterTransactions(String filterType) {
        filterSaatIni = filterType;
        applyFilters();
    }

    private void setupSearch() {
        if (etSearch == null) return;

        etSearch.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                applyFilters();
            }

            @Override
            public void afterTextChanged(Editable s) {}
        });
    }

    private void observeTransactions() {
        transactionViewModel.getAllTransactions().observe(this, transactions -> {
            if (transactions == null) return;

            transactionList.clear();
            transactionList.addAll(transactions);

            double balance = 0;
            double monthIncome = 0;
            double monthExpense = 0;

            for (Transaction transaction : transactions) {
                double amount = transaction.getAmount();
                balance += amount;

                // Pemasukan/pengeluaran dihitung untuk bulan berjalan saja.
                if (DateUtils.isInCurrentMonth(transaction.getDate())) {
                    if (amount >= 0) {
                        monthIncome += amount;
                    } else {
                        monthExpense += Math.abs(amount);
                    }
                }
            }

            tvTotalBalance.setText(CurrencyUtils.formatRupiahWithoutSign(balance));
            tvHomeIncome.setText(CurrencyUtils.formatRupiahWithoutSign(monthIncome));
            tvHomeExpense.setText(CurrencyUtils.formatRupiahWithoutSign(monthExpense));

            applyFilters();
        });
    }

    private void applyFilters() {
        List<Transaction> filteredList = new ArrayList<>();

        String keyword = etSearch != null
                ? etSearch.getText().toString().toLowerCase().trim()
                : "";

        for (Transaction transaction : transactionList) {
            boolean matchesFilter = "all".equals(filterSaatIni)
                    || filterSaatIni.equalsIgnoreCase(transaction.getType());

            boolean matchesSearch = keyword.isEmpty()
                    || contains(transaction.getTitle(), keyword)
                    || contains(transaction.getCategory(), keyword)
                    || contains(transaction.getStatus(), keyword);

            if (matchesFilter && matchesSearch) {
                filteredList.add(transaction);
            }
        }

        if (transactionAdapter != null) {
            transactionAdapter.updateList(filteredList);
        }

        // Tampilan kosong yang informatif
        if (tvEmpty != null) {
            tvEmpty.setText(transactionList.isEmpty()
                    ? R.string.empty_transactions
                    : R.string.empty_search);
            tvEmpty.setVisibility(filteredList.isEmpty() ? View.VISIBLE : View.GONE);
        }
    }

    private boolean contains(String text, String keyword) {
        return text != null && text.toLowerCase().contains(keyword);
    }

    private void updateFilterUI(TextView selectedFilter) {
        TextView[] filters = {btnFilterSemua, btnFilterPemasukan, btnFilterPengeluaran, btnFilterTagihan};

        for (TextView filter : filters) {
            if (filter == null) continue;

            boolean selected = filter == selectedFilter;
            setButtonStyle(
                    filter,
                    selected ? R.color.brand_indigo : R.color.surface,
                    selected ? R.color.brand_indigo : R.color.outline,
                    selected ? R.color.text_primary : R.color.text_secondary
            );
        }
    }

    private void setButtonStyle(TextView button,
                                @ColorRes int backgroundRes,
                                @ColorRes int strokeRes,
                                @ColorRes int textRes) {
        int background = ContextCompat.getColor(this, backgroundRes);
        int stroke = ContextCompat.getColor(this, strokeRes);

        button.setTextColor(ContextCompat.getColor(this, textRes));

        if (button instanceof MaterialButton) {
            MaterialButton materialButton = (MaterialButton) button;
            materialButton.setBackgroundTintList(ColorStateList.valueOf(background));
            materialButton.setStrokeColor(ColorStateList.valueOf(stroke));
        } else {
            button.setBackgroundColor(background);
        }
    }

    private void showAddTransactionDialog() {
        View dialogView = getLayoutInflater().inflate(R.layout.dialog_add_transaction, null);

        EditText etTitle = dialogView.findViewById(R.id.etTitle);
        EditText etAmount = dialogView.findViewById(R.id.etAmount);
        MaterialButtonToggleGroup toggleType = dialogView.findViewById(R.id.toggleType);
        AutoCompleteTextView actvCategory = dialogView.findViewById(R.id.actvCategory);
        AutoCompleteTextView actvStatus = dialogView.findViewById(R.id.actvStatus);

        String[] categories = Categories.list(this);
        String[] statuses = {
                getString(R.string.status_done),
                getString(R.string.status_pending),
                getString(R.string.status_paid)
        };

        actvCategory.setAdapter(new ArrayAdapter<>(
                this, android.R.layout.simple_list_item_1, categories));
        actvCategory.setText(categories[0], false);

        actvStatus.setAdapter(new ArrayAdapter<>(
                this, android.R.layout.simple_list_item_1, statuses));
        actvStatus.setText(statuses[0], false);

        AlertDialog dialog = new MaterialAlertDialogBuilder(this)
                .setTitle(R.string.add_transaction)
                .setView(dialogView)
                .setNegativeButton(R.string.cancel, null)
                .setPositiveButton(R.string.add, null)
                .create();

        // Listener dipasang manual agar dialog tidak tertutup saat input belum valid.
        dialog.setOnShowListener(dialogInterface ->
                dialog.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener(v -> {
                    String title = etTitle.getText().toString().trim();
                    String category = actvCategory.getText().toString().trim();
                    String amountText = etAmount.getText().toString().trim();
                    String status = actvStatus.getText().toString().trim();

                    if (title.isEmpty() || category.isEmpty() || amountText.isEmpty()) {
                        Toast.makeText(this, R.string.msg_fill_all, Toast.LENGTH_SHORT).show();
                        return;
                    }

                    double amount;
                    try {
                        amount = Double.parseDouble(amountText);
                    } catch (NumberFormatException e) {
                        Toast.makeText(this, R.string.msg_invalid_amount, Toast.LENGTH_SHORT).show();
                        return;
                    }

                    if (amount <= 0) {
                        Toast.makeText(this, R.string.msg_invalid_amount, Toast.LENGTH_SHORT).show();
                        return;
                    }

                    int checkedId = toggleType.getCheckedButtonId();
                    String type = "expense";
                    if (checkedId == R.id.btnTypeIncome) {
                        type = "income";
                    } else if (checkedId == R.id.btnTypeBill) {
                        type = "bill";
                    }

                    if (status.isEmpty()) {
                        status = statuses[0];
                    }

                    // Pengeluaran & tagihan disimpan sebagai nilai negatif.
                    amount = "income".equals(type) ? Math.abs(amount) : -Math.abs(amount);

                    Transaction transaction = new Transaction(
                            title, category, DateUtils.getToday(), amount, type, status
                    );

                    transactionViewModel.insert(transaction);

                    dialog.dismiss();
                    Toast.makeText(this, R.string.msg_added, Toast.LENGTH_SHORT).show();
                }));

        dialog.show();
    }

    private void showEditTransactionDialog(Transaction transaction) {
        View dialogView = getLayoutInflater().inflate(R.layout.dialog_edit_transaction, null);

        EditText etEditTitle = dialogView.findViewById(R.id.etEditTitle);
        AutoCompleteTextView etEditCategory = dialogView.findViewById(R.id.etEditCategory);
        EditText etEditAmount = dialogView.findViewById(R.id.etEditAmount);

        etEditCategory.setAdapter(new ArrayAdapter<>(
                this, android.R.layout.simple_list_item_1, Categories.list(this)));

        etEditTitle.setText(transaction.getTitle());
        etEditCategory.setText(transaction.getCategory(), false);

        // Tampilkan nominal tanpa tanda; tanda ditentukan oleh tipe transaksi
        double absAmount = Math.abs(transaction.getAmount());
        etEditAmount.setText(absAmount == Math.rint(absAmount)
                ? String.valueOf((long) absAmount)
                : String.valueOf(absAmount));

        AlertDialog dialog = new MaterialAlertDialogBuilder(this)
                .setTitle(R.string.edit_transaction)
                .setView(dialogView)
                .setNegativeButton(R.string.cancel, null)
                .setPositiveButton(R.string.save, null)
                .create();

        dialog.setOnShowListener(dialogInterface ->
                dialog.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener(v -> {
                    String title = etEditTitle.getText().toString().trim();
                    String category = etEditCategory.getText().toString().trim();
                    String amountText = etEditAmount.getText().toString().trim();

                    if (title.isEmpty() || category.isEmpty() || amountText.isEmpty()) {
                        Toast.makeText(this, R.string.msg_fill_all, Toast.LENGTH_SHORT).show();
                        return;
                    }

                    double amount;
                    try {
                        amount = Math.abs(Double.parseDouble(amountText));
                    } catch (NumberFormatException e) {
                        Toast.makeText(this, R.string.msg_invalid_amount, Toast.LENGTH_SHORT).show();
                        return;
                    }

                    if (amount <= 0) {
                        Toast.makeText(this, R.string.msg_invalid_amount, Toast.LENGTH_SHORT).show();
                        return;
                    }

                    if ("expense".equalsIgnoreCase(transaction.getType())
                            || "bill".equalsIgnoreCase(transaction.getType())) {
                        amount = -amount;
                    }

                    transaction.setTitle(title);
                    transaction.setCategory(category);
                    transaction.setAmount(amount);
                    transactionViewModel.update(transaction);

                    dialog.dismiss();
                    Toast.makeText(this, R.string.msg_updated, Toast.LENGTH_SHORT).show();
                }));

        dialog.show();
    }

    private void showDeleteConfirmationDialog(Transaction transaction) {
        new MaterialAlertDialogBuilder(this)
                .setTitle(R.string.delete_transaction)
                .setMessage(getString(R.string.delete_confirm, transaction.getTitle()))
                .setNegativeButton(R.string.cancel, null)
                .setPositiveButton(R.string.delete, (dialog, which) -> {
                    transactionViewModel.delete(transaction);
                    Toast.makeText(this, R.string.msg_deleted, Toast.LENGTH_SHORT).show();
                })
                .show();
    }
}
