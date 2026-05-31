package com.example.ui

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector

data class CategoryInfo(
    val id: String,
    val name: String,
    val color: Color,
    val icon: ImageVector
)

object CategoryHelper {
    val categories = listOf(
        CategoryInfo("food", "Food", Color(0xFFF25C54), Icons.Default.Favorite),
        CategoryInfo("transport", "Transport", Color(0xFF2A6F97), Icons.Default.Build),
        CategoryInfo("utilities", "Utilities", Color(0xFFFFD166), Icons.Default.Home),
        CategoryInfo("entertainment", "Entertainment", Color(0xFF9D4EDD), Icons.Default.Star),
        CategoryInfo("shopping", "Shopping", Color(0xFF06D6A0), Icons.Default.ShoppingCart),
        CategoryInfo("others", "Others", Color(0xFF6C757D), Icons.Default.Info)
    )

    fun getCategory(name: String): CategoryInfo {
        return categories.find { it.name.equals(name, ignoreCase = true) }
            ?: CategoryInfo("others", name, Color(0xFF6C757D), Icons.Default.Info)
    }
}
