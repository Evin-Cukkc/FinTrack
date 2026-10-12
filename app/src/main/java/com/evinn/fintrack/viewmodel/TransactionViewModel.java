package com.evinn.fintrack.viewmodel;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;

import com.evinn.fintrack.database.AppDatabase;
import com.evinn.fintrack.database.TransactionDao;
import com.evinn.fintrack.model.Transaction;

import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class TransactionViewModel extends AndroidViewModel {

    private final TransactionDao transactionDao;
    private final LiveData<List<Transaction>> allTransactions;
    private final ExecutorService databaseExecutor;

    public TransactionViewModel(@NonNull Application application) {
        super(application);

        AppDatabase database = AppDatabase.getInstance(application);
        transactionDao = database.transactionDao();

        allTransactions = transactionDao.getAllTransactions();

        databaseExecutor = Executors.newSingleThreadExecutor();
    }

    public LiveData<List<Transaction>> getAllTransactions() {
        return allTransactions;
    }

    public void insert(Transaction transaction) {
        databaseExecutor.execute(() -> {
            long id = transactionDao.insert(transaction);
            transaction.setId((int) id);
        });
    }

    public void update(Transaction transaction) {
        databaseExecutor.execute(() -> transactionDao.update(transaction));
    }

    public void delete(Transaction transaction) {
        databaseExecutor.execute(() -> {
            transactionDao.delete(transaction);
        });
    }

    @Override
    protected void onCleared() {
        super.onCleared();
        databaseExecutor.shutdown();
    }
}