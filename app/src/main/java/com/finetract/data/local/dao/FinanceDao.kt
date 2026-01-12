package com.finetract.data.local.dao

import androidx.room.*
import com.finetract.data.local.entities.Budget
import com.finetract.data.local.entities.Category
import com.finetract.data.local.entities.Transaction
import com.finetract.data.local.entities.TransactionType
import kotlinx.coroutines.flow.Flow

data class CategoryTotal(
    val categoryId: Long,
    val categoryName: String,
    val totalAmount: Double
)

data class DailyTotal(
    val date: String,
    val totalAmount: Double
)

@Dao
interface FinanceDao {
    // Transactions
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTransaction(transaction: Transaction)

    @Query("SELECT * FROM transactions ORDER BY timestamp DESC")
    fun getAllTransactions(): Flow<List<Transaction>>

    @Query("""
        SELECT SUM(amount) FROM transactions 
        WHERE type = :type AND strftime('%m-%Y', timestamp / 1000, 'unixepoch') = :monthYear
    """)
    fun getMonthlyTotal(type: TransactionType, monthYear: String): Flow<Double?>

    @Query("""
        SELECT categoryId, name as categoryName, SUM(amount) as totalAmount 
        FROM transactions 
        JOIN categories ON transactions.categoryId = categories.id
        WHERE type = 'EXPENSE' AND strftime('%m-%Y', timestamp / 1000, 'unixepoch') = :monthYear
        GROUP BY categoryId
    """)
    fun getCategoryTotalsByMonth(monthYear: String): Flow<List<CategoryTotal>>

    @Query("""
        SELECT strftime('%Y-%m-%d', timestamp / 1000, 'unixepoch') as date, SUM(amount) as totalAmount
        FROM transactions
        WHERE type = 'EXPENSE' AND strftime('%m-%Y', timestamp / 1000, 'unixepoch') = :monthYear
        GROUP BY date
        ORDER BY date ASC
    """)
    fun getDailyTotalsByMonth(monthYear: String): Flow<List<DailyTotal>>

    // Categories
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCategory(category: Category)

    @Query("SELECT * FROM categories")
    fun getAllCategories(): Flow<List<Category>>

    @Query("DELETE FROM categories WHERE id = :id")
    suspend fun deleteCategory(id: Long)

    // Budgets
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertBudget(budget: Budget)

    @Query("SELECT * FROM budgets WHERE monthYear = :monthYear")
    fun getBudgetsByMonth(monthYear: String): Flow<List<Budget>>

    @Query("DELETE FROM transactions")
    suspend fun deleteAllTransactions()
}
