package com.evinn.fintrack.repository;

import androidx.lifecycle.LiveData;

import com.evinn.fintrack.database.TransactionDao;
import com.evinn.fintrack.model.Transaction;

import java.util.List;

public class TransactionRepository {

    private final TransactionDao transactionDao;

    public TransactionRepository(TransactionDao transactionDao) {
        this.transactionDao = transactionDao;
    }

    public LiveData<List<Transaction>> getAllTransactions() {
        return transactionDao.getAllTransactions();
    }

    public long insert(Transaction transaction) {
        return transactionDao.insert(transaction);
    }

    public void update(Transaction transaction) {
        transactionDao.update(transaction);
    }

    public void delete(Transaction transaction) {
        transactionDao.delete(transaction);
    }
}