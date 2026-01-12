package com.finetract.data.local

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.finetract.data.local.dao.FinanceDao
import com.finetract.data.local.entities.Budget
import com.finetract.data.local.entities.Category
import com.finetract.data.local.entities.Transaction
import com.finetract.data.local.converters.Converters

@Database(
    entities = [Transaction::class, Category::class, Budget::class],
    version = 1,
    exportSchema = false
)
@TypeConverters(Converters::class)
abstract class AppDatabase : RoomDatabase() {
    abstract fun financeDao(): FinanceDao
}
