package com.evinn.fintrack.database;

import android.content.Context;

import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;

import com.evinn.fintrack.model.Budget;
import com.evinn.fintrack.model.Transaction;

@Database(
        entities = {
                Transaction.class,
                Budget.class
        },
        version = 2,
        exportSchema = false
)
public abstract class AppDatabase extends RoomDatabase {

    public abstract TransactionDao transactionDao();

    public abstract BudgetDao budgetDao();

    private static volatile AppDatabase INSTANCE;

    public static AppDatabase getInstance(Context context) {

        if (INSTANCE == null) {

            synchronized (AppDatabase.class) {

                if (INSTANCE == null) {

                    INSTANCE = Room.databaseBuilder(
                            context.getApplicationContext(),
                            AppDatabase.class,
                            "fintrack_database"
                    )
                    .fallbackToDestructiveMigration()
                    .build();

                }
            }
        }

        return INSTANCE;
    }
}