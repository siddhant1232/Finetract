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
    val dailyExpenditure: Double = 0.0,
    val dailyLimit: Double = 500.0,
    val categoryTotals: List<com.finetract.data.local.dao.CategoryTotal> = emptyList(),
    val dailyTotals: List<com.finetract.data.local.dao.DailyTotal> = emptyList(),
    val budgets: List<Budget> = emptyList(),
    val currentMonthYear: String = "",
    val smoothedTrend: List<Double> = emptyList()
)

sealed class UiEffect {
    object ShowOverLimitPopup : UiEffect()
}

@HiltViewModel
class MainViewModel @Inject constructor(
    private val repository: FinanceRepository,
    private val savedStateHandle: SavedStateHandle
) : ViewModel() {

    private val _currentMonthYear = MutableStateFlow(getCurrentMonthYear())
    private val _currentDate = MutableStateFlow(getCurrentDate())

    private val _uiEffect = MutableSharedFlow<UiEffect>()
    val uiEffect = _uiEffect.asSharedFlow()

    private var hasShownLimitAlertToday = false

    val uiState: StateFlow<UiState> = combine(
        repository.getAllTransactions(),
        repository.getAllCategories(),
        _currentMonthYear.flatMapLatest { repository.getMonthlyTotal(TransactionType.INCOME, it) },
        _currentMonthYear.flatMapLatest { repository.getMonthlyTotal(TransactionType.EXPENSE, it) },
        _currentDate.flatMapLatest { repository.getDailyExpenditure(it) },
        repository.dailyLimit,
        _currentMonthYear.flatMapLatest { repository.getCategoryTotalsByMonth(it) },
        repository.getAllDailyTotals(),
        _currentMonthYear.flatMapLatest { repository.getBudgetsByMonth(it) },
        _currentMonthYear
    ) { args: Array<Any?> ->
        val dailyExp = args[4] as? Double ?: 0.0
        val limit = args[5] as? Double ?: 500.0
        val dailyTotals = args[7] as List<com.finetract.data.local.dao.DailyTotal>

        // Algorithm: Simple Moving Average (7-day window) for smoothing
        val windowSize = 7
        val smoothed = dailyTotals.map { it.totalAmount }
            .windowed(windowSize, 1, true) { it.average() }

        // Trigger Alert Logic
        if (dailyExp > limit && !hasShownLimitAlertToday) {
            hasShownLimitAlertToday = true
            _uiEffect.emit(UiEffect.ShowOverLimitPopup)
        } else if (dailyExp <= limit) {
            hasShownLimitAlertToday = false
        }
        
        UiState(
            transactions = args[0] as List<Transaction>,
            categories = args[1] as List<Category>,
            monthlyIncome = args[2] as? Double ?: 0.0,
            monthlyExpense = args[3] as? Double ?: 0.0,
            dailyExpenditure = dailyExp,
            dailyLimit = limit,
            categoryTotals = args[6] as List<com.finetract.data.local.dao.CategoryTotal>,
            dailyTotals = dailyTotals,
            budgets = args[8] as List<Budget>,
            currentMonthYear = args[9] as String,
            smoothedTrend = smoothed
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = UiState(currentMonthYear = getCurrentMonthYear())
    )

    private fun getCurrentMonthYear(): String {
        return SimpleDateFormat("MM-yyyy", Locale.getDefault()).format(Date())
    }

    private fun getCurrentDate(): String {
        return SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())
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

    fun updateDailyLimit(limit: Double) {
        viewModelScope.launch {
            repository.setDailyLimit(limit)
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
