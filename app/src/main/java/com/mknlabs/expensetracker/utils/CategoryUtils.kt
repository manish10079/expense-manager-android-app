package com.mknlabs.expensetracker.utils

import androidx.compose.ui.graphics.vector.ImageVector
import com.mknlabs.expensetracker.data.constants.categoryMap
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*

fun getCategoryIcon(categoryId: Int): ImageVector {
    return categoryMap[categoryId]?.icon ?: Icons.Filled.QuestionMark
}