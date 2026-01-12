package com.finetract.ui

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.finetract.data.local.entities.Budget
import com.finetract.data.local.entities.Category
import com.finetract.data.local.entities.Transaction
import com.finetract.data.local.entities.TransactionType
import com.finetract.data.repository.FinanceRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*
import javax.inject.Inject

data class UiState(
    val transactions: List<Transaction> = emptyList(),
    val categories: List<Category> = emptyList(),
    val monthlyIncome: Double = 0.0,
    val monthlyExpense: Double = 0.0,
    val categoryTotals: List<com.finetract.data.local.dao.CategoryTotal> = emptyList(),
    val dailyTotals: List<com.finetract.data.local.dao.DailyTotal> = emptyList(),
    val budgets: List<Budget> = emptyList(),
    val currentMonthYear: String = ""
)

@HiltViewModel
class MainViewModel @Inject constructor(
    private val repository: FinanceRepository,
    private val savedStateHandle: SavedStateHandle
) : ViewModel() {

    private val _currentMonthYear = MutableStateFlow(getCurrentMonthYear())
    
    val uiState: StateFlow<UiState> = combine(
        repository.getAllTransactions(),
        repository.getAllCategories(),
        _currentMonthYear.flatMapLatest { repository.getMonthlyTotal(TransactionType.INCOME, it) },
        _currentMonthYear.flatMapLatest { repository.getMonthlyTotal(TransactionType.EXPENSE, it) },
        _currentMonthYear.flatMapLatest { repository.getCategoryTotalsByMonth(it) },
        _currentMonthYear.flatMapLatest { repository.getDailyTotalsByMonth(it) },
        _currentMonthYear.flatMapLatest { repository.getBudgetsByMonth(it) },
        _currentMonthYear
    ) { args: Array<Any?> ->
        UiState(
            transactions = args[0] as List<Transaction>,
            categories = args[1] as List<Category>,
            monthlyIncome = args[2] as? Double ?: 0.0,
            monthlyExpense = args[3] as? Double ?: 0.0,
            categoryTotals = args[4] as List<com.finetract.data.local.dao.CategoryTotal>,
            dailyTotals = args[5] as List<com.finetract.data.local.dao.DailyTotal>,
            budgets = args[6] as List<Budget>,
            currentMonthYear = args[7] as String
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = UiState(currentMonthYear = getCurrentMonthYear())
    )

    private fun getCurrentMonthYear(): String {
        return SimpleDateFormat("MM-yyyy", Locale.getDefault()).format(Date())
    }

    fun addTransaction(amount: Double, categoryId: Long, note: String, type: TransactionType) {
        viewModelScope.launch {
            repository.insertTransaction(
                Transaction(
                    amount = amount,
                    timestamp = System.currentTimeMillis(),
                    categoryId = categoryId,
                    note = note,
                    type = type
                )
            )
        }
    }

    fun addCategory(name: String, budgetLimit: Double) {
        viewModelScope.launch {
            repository.insertCategory(
                Category(name = name, iconMetadata = "default", budgetLimit = budgetLimit)
            )
        }
    }

    fun resetData() {
        viewModelScope.launch {
            repository.clearAllData()
        }
    }
}
