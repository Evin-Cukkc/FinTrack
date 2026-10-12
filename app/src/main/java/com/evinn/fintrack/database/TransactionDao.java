package com.evinn.fintrack.database;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Update;

import com.evinn.fintrack.model.Transaction;

import java.util.List;

@Dao
public interface TransactionDao {

    @Query("SELECT * FROM transactions ORDER BY id DESC")
    LiveData<List<Transaction>> getAllTransactions();

    @Insert
    long insert(Transaction transaction);

    @Update
    void update(Transaction transaction);

    @Delete
    void delete(Transaction transaction);

    @Query("DELETE FROM transactions")
    void deleteAll();

    @Query("SELECT COUNT(*) FROM transactions")
    int getTransactionCount();
}