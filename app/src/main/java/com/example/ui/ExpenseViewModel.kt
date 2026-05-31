package com.example.ui

import android.app.Application
import android.content.Context
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.AppDatabase
import com.example.data.Expense
import com.example.data.ExpenseRepository
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

class ExpenseViewModel(application: Application) : AndroidViewModel(application) {
    private val repository: ExpenseRepository
    private val sharedPrefs = application.getSharedPreferences("expense_tracker_prefs", Context.MODE_PRIVATE)

    // Data streams
    val allExpenses: StateFlow<List<Expense>>

    // Search and category states
    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    private val _selectedCategoryFilter = MutableStateFlow<String?>(null)
    val selectedCategoryFilter: StateFlow<String?> = _selectedCategoryFilter.asStateFlow()

    // Combined filtered stream
    val filteredExpenses: StateFlow<List<Expense>>

    // Customizable Monthly budget limit
    private val _monthlyBudget = MutableStateFlow(1000.0)
    val monthlyBudget: StateFlow<Double> = _monthlyBudget.asStateFlow()

    init {
        val database = AppDatabase.getDatabase(application)
        repository = ExpenseRepository(database.expenseDao())

        allExpenses = repository.allExpenses
            .stateIn(
                scope = viewModelScope,
                started = SharingStarted.WhileSubscribed(5000),
                initialValue = emptyList()
            )

        _monthlyBudget.value = sharedPrefs.getFloat("monthly_budget", 1000f).toDouble()

        filteredExpenses = combine(allExpenses, _searchQuery, _selectedCategoryFilter) { expenses, query, cat ->
            expenses.filter { expense ->
                val matchesQuery = expense.title.contains(query, ignoreCase = true) || 
                                 expense.description.contains(query, ignoreCase = true)
                val matchesCategory = cat == null || expense.category.equals(cat, ignoreCase = true)
                matchesQuery && matchesCategory
            }
        }.stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )
    }

    fun setSearchQuery(query: String) {
        _searchQuery.value = query
    }

    fun setCategoryFilter(category: String?) {
        _selectedCategoryFilter.value = category
    }

    fun setMonthlyBudget(budget: Double) {
        _monthlyBudget.value = budget
        sharedPrefs.edit().putFloat("monthly_budget", budget.toFloat()).apply()
    }

    fun addExpense(title: String, amount: Double, category: String, date: Long, description: String) {
        viewModelScope.launch {
            repository.insertExpense(
                Expense(
                    title = title,
                    amount = amount,
                    category = category,
                    date = date,
                    description = description
                )
            )
        }
    }

    fun updateExpense(id: Int, title: String, amount: Double, category: String, date: Long, description: String) {
        viewModelScope.launch {
            repository.updateExpense(
                Expense(
                    id = id,
                    title = title,
                    amount = amount,
                    category = category,
                    date = date,
                    description = description
                )
            )
        }
    }

    fun deleteExpense(expense: Expense) {
        viewModelScope.launch {
            repository.deleteExpense(expense)
        }
    }
}
