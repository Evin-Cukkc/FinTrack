package com.evinn.fintrack.database;

import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Update;

import com.evinn.fintrack.model.Budget;

import java.util.List;

@Dao
public interface BudgetDao {

    @Query("SELECT * FROM budgets ORDER BY id DESC")
    List<Budget> getAllBudgets();

    @Insert
    long insert(Budget budget);

    @Update
    void update(Budget budget);

    @Delete
    void delete(Budget budget);

    @Query("DELETE FROM budgets")
    void deleteAll();
}