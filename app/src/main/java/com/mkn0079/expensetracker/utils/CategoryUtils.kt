package com.mkn0079.expensetracker.utils

import androidx.compose.ui.graphics.vector.ImageVector
import com.mkn0079.expensetracker.data.constants.categoryMap
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*

fun getCategoryIcon(categoryId: Int): ImageVector {
    return categoryMap[categoryId]?.icon ?: Icons.Filled.QuestionMark
}