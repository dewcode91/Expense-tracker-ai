package com.example.ui

import android.app.Application
import android.content.Context
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.AppDatabase
import com.example.data.Expense
import com.example.data.ExpenseRepository
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

data class CategoryInfo(
    val id: String,
    val name: String,
    val color: Color,
    val icon: ImageVector
)

object CategoryHelper {
    val defaultCategories = listOf(
        CategoryInfo("house_rent", "House Rent", Color(0xFFE76F51), Icons.Default.Home),
        CategoryInfo("fuel", "Fuel(Petrol/Diesel)", Color(0xFF2A9D8F), Icons.Default.Build),
        CategoryInfo("online_shopping", "Online Shopping", Color(0xFFE9C46A), Icons.Default.ShoppingCart),
        CategoryInfo("tours_travels", "Tours and Travels", Color(0xFF264653), Icons.Default.Star),
        CategoryInfo("ration_grocery", "Ration and Grocery", Color(0xFFF4A261), Icons.Default.ShoppingCart),
        CategoryInfo("vegetables", "Vegetables and others", Color(0xFF4C9F70), Icons.Default.Favorite),
        CategoryInfo("bill_payments", "Bill Payments/ Fund Transfer", Color(0xFF3D5A80), Icons.Default.Call),
        CategoryInfo("hostel_college_exam_fee", "Hostel/College/Exam Fee", Color(0xFF9D4EDD), Icons.Default.Person),
        CategoryInfo("construction", "Construction", Color(0xFFE07A5F), Icons.Default.Build),
        CategoryInfo("books_stationery", "Books & Stationery", Color(0xFF81B29A), Icons.Default.Star),
        CategoryInfo("miscellaneous", "Miscellaneous", Color(0xFF6C757D), Icons.Default.Info)
    )

    var customCategories: List<CategoryInfo>? = null

    val categories: List<CategoryInfo>
        get() = customCategories ?: defaultCategories

    fun getCategory(name: String): CategoryInfo {
        return categories.find { it.name.equals(name, ignoreCase = true) }
            ?: CategoryInfo("others", name, Color(0xFF6C757D), Icons.Default.Info)
    }

    fun getIconByName(name: String): ImageVector {
        return when (name) {
            "Favorite" -> Icons.Default.Favorite
            "Build" -> Icons.Default.Build
            "Home" -> Icons.Default.Home
            "Star" -> Icons.Default.Star
            "ShoppingCart" -> Icons.Default.ShoppingCart
            "Info" -> Icons.Default.Info
            "Play" -> Icons.Default.PlayArrow
            "Person" -> Icons.Default.Person
            "Phone" -> Icons.Default.Call
            "Settings" -> Icons.Default.Settings
            else -> Icons.Default.Info
        }
    }

    fun getNameByIcon(icon: ImageVector): String {
        return when (icon) {
            Icons.Default.Favorite -> "Favorite"
            Icons.Default.Build -> "Build"
            Icons.Default.Home -> "Home"
            Icons.Default.Star -> "Star"
            Icons.Default.ShoppingCart -> "ShoppingCart"
            Icons.Default.Info -> "Info"
            Icons.Default.PlayArrow -> "Play"
            Icons.Default.Person -> "Person"
            Icons.Default.Call -> "Phone"
            Icons.Default.Settings -> "Settings"
            else -> "Info"
        }
    }
}

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

    // App Theme Day/Night mode
    private val _isDarkTheme = MutableStateFlow(sharedPrefs.getBoolean("is_dark_theme", false))
    val isDarkTheme: StateFlow<Boolean> = _isDarkTheme.asStateFlow()

    // Manage Profile state
    private val _profileName = MutableStateFlow(sharedPrefs.getString("profile_name", "Alex Rivera") ?: "Alex Rivera")
    val profileName: StateFlow<String> = _profileName.asStateFlow()

    private val _profileAvatarColor = MutableStateFlow(sharedPrefs.getLong("profile_avatar_color", 0xFFEADDFF))
    val profileAvatarColor: StateFlow<Long> = _profileAvatarColor.asStateFlow()

    // Dynamic Categories
    private val _categories = MutableStateFlow<List<CategoryInfo>>(emptyList())
    val categories: StateFlow<List<CategoryInfo>> = _categories.asStateFlow()

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

        // Load custom categories or default categories
        loadCategories()

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

    fun setDarkTheme(isDark: Boolean) {
        _isDarkTheme.value = isDark
        sharedPrefs.edit().putBoolean("is_dark_theme", isDark).apply()
    }

    fun setProfileName(name: String) {
        _profileName.value = name
        sharedPrefs.edit().putString("profile_name", name).apply()
    }

    fun setProfileAvatarColor(colorVal: Long) {
        _profileAvatarColor.value = colorVal
        sharedPrefs.edit().putLong("profile_avatar_color", colorVal).apply()
    }

    private fun loadCategories() {
        val saved = sharedPrefs.getString("custom_categories", "")
        if (saved.isNullOrEmpty()) {
            _categories.value = CategoryHelper.categories
            CategoryHelper.customCategories = CategoryHelper.categories
        } else {
            val list = mutableListOf<CategoryInfo>()
            saved.split(";").forEach { item ->
                val tokens = item.split("|")
                if (tokens.size == 4) {
                    val id = tokens[0]
                    val name = tokens[1]
                    val colorVal = tokens[2].toLongOrNull() ?: 0xFF4F378B
                    val iconName = tokens[3]
                    val categoryColor = if (colorVal in -2147483648L..4294967295L) {
                        Color(colorVal.toInt())
                    } else {
                        Color(colorVal.toULong())
                    }
                    list.add(
                        CategoryInfo(
                            id = id,
                            name = name,
                            color = categoryColor,
                            icon = CategoryHelper.getIconByName(iconName)
                        )
                    )
                }
            }
            if (list.isEmpty()) {
                _categories.value = CategoryHelper.categories
                CategoryHelper.customCategories = CategoryHelper.categories
            } else {
                _categories.value = list
                CategoryHelper.customCategories = list
            }
        }
    }

    fun addCategory(name: String, color: Color, icon: ImageVector) {
        val id = name.lowercase().trim().replace(" ", "_").replace("[^a-z0-9_]".toRegex(), "")
        if (id.isEmpty()) return
        
        val newCat = CategoryInfo(
            id = id,
            name = name.trim(),
            color = color,
            icon = icon
        )
        val currentList = _categories.value.toMutableList()
        // Check if category already exists to avoid duplication
        if (currentList.none { it.name.equals(name, ignoreCase = true) }) {
            currentList.add(newCat)
            _categories.value = currentList
            CategoryHelper.customCategories = currentList
            
            // Serialize using stable 32-bit ARGB
            val serialized = currentList.joinToString(";") { cat ->
                "${cat.id}|${cat.name}|${cat.color.toArgb()}|${CategoryHelper.getNameByIcon(cat.icon)}"
            }
            sharedPrefs.edit().putString("custom_categories", serialized).apply()
        }
    }

    fun updateCategory(oldCat: CategoryInfo, newName: String, newColor: Color, newIcon: ImageVector) {
        val trimmedName = newName.trim()
        if (trimmedName.isEmpty()) return

        val currentList = _categories.value.map { cat ->
            if (cat.id == oldCat.id) {
                CategoryInfo(
                    id = cat.id,
                    name = trimmedName,
                    color = newColor,
                    icon = newIcon
                )
            } else {
                cat
            }
        }

        _categories.value = currentList
        CategoryHelper.customCategories = currentList

        // Serialize
        val serialized = currentList.joinToString(";") { cat ->
            "${cat.id}|${cat.name}|${cat.color.toArgb()}|${CategoryHelper.getNameByIcon(cat.icon)}"
        }
        sharedPrefs.edit().putString("custom_categories", serialized).apply()

        // Sync name updates with database transactions safely
        if (!oldCat.name.equals(trimmedName, ignoreCase = true)) {
            viewModelScope.launch {
                repository.updateExpenseCategory(oldCat.name, trimmedName)
            }
        }
    }

    fun deleteCategory(cat: CategoryInfo) {
        val currentList = _categories.value.filter { it.id != cat.id }
        _categories.value = currentList
        CategoryHelper.customCategories = currentList

        // Serialize
        val serialized = currentList.joinToString(";") { c ->
            "${c.id}|${c.name}|${c.color.toArgb()}|${CategoryHelper.getNameByIcon(c.icon)}"
        }
        sharedPrefs.edit().putString("custom_categories", serialized).apply()
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
