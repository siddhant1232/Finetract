package com.finetract.data.repository

import com.finetract.data.local.dao.FinanceDao
import com.finetract.data.local.entities.Budget
import com.finetract.data.local.entities.Category
import com.finetract.data.local.entities.Transaction
import com.finetract.data.local.entities.TransactionType
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class FinanceRepository @Inject constructor(
    private val financeDao: FinanceDao
) {
    fun getAllTransactions() = financeDao.getAllTransactions()
    
    fun getMonthlyTotal(type: TransactionType, monthYear: String) = 
        financeDao.getMonthlyTotal(type, monthYear)
    
    fun getCategoryTotalsByMonth(monthYear: String) = 
        financeDao.getCategoryTotalsByMonth(monthYear)
    
    fun getDailyTotalsByMonth(monthYear: String) = 
        financeDao.getDailyTotalsByMonth(monthYear)

    fun getAllCategories() = financeDao.getAllCategories()
    
    fun getBudgetsByMonth(monthYear: String) = 
        financeDao.getBudgetsByMonth(monthYear)

    suspend fun insertTransaction(transaction: Transaction) = 
        financeDao.insertTransaction(transaction)

    suspend fun insertCategory(category: Category) = 
        financeDao.insertCategory(category)

    suspend fun insertBudget(budget: Budget) = 
        financeDao.insertBudget(budget)

    suspend fun deleteCategory(id: Long) = 
        financeDao.deleteCategory(id)

    suspend fun clearAllData() = 
        financeDao.deleteAllTransactions()
}
