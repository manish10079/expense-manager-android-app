package com.mknlabs.expensetracker.feature.settings.ui
import com.mknlabs.expensetracker.core.ui.components.rememberSectionEnterAlphas

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.TrendingUp
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.Modifier
import com.mknlabs.expensetracker.core.ui.theme.appScreenBackground
import com.mknlabs.expensetracker.core.ui.theme.accentInk
import com.mknlabs.expensetracker.core.ui.theme.accentSoft
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.mknlabs.expensetracker.data.constants.categoryIconOptions
import com.mknlabs.expensetracker.core.ui.adaptive.LocalAppWindowInfo
import com.mknlabs.expensetracker.core.ui.theme.isDark
import com.mknlabs.expensetracker.models.CategoryType
import com.mknlabs.expensetracker.models.PaymentType
import com.mknlabs.expensetracker.core.ui.components.AppHeader
import com.mknlabs.expensetracker.core.ui.components.CategoryColorRow
import com.mknlabs.expensetracker.core.ui.components.IconPickerGrid
import com.mknlabs.expensetracker.core.ui.components.IconPickerTileSize
import com.mknlabs.expensetracker.core.ui.models.CategoryIconOption
import com.mknlabs.expensetracker.core.ui.models.CategoryManagementTab
import com.mknlabs.expensetracker.core.ui.theme.Dimens
import com.mknlabs.expensetracker.core.ui.theme.brandGradient
import com.mknlabs.expensetracker.core.ui.theme.disabled
import com.mknlabs.expensetracker.core.ui.theme.identityColor
import com.mknlabs.expensetracker.core.ui.theme.onCta
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.platform.LocalContext
import com.mknlabs.expensetracker.R


@androidx.compose.ui.tooling.preview.Preview(showBackground = true, name = "Add Category - Light")
@Composable
internal fun AddCategoryScreenContentPreview() {
    com.mknlabs.expensetracker.core.ui.theme.ExpenseTrackerTheme {
        AddCategoryScreenContent(
            uiState = AddCategoryUiState(),
            existingCategories = emptyList(),
            existingPaymentMethods = emptyList(),
            onBackClick = {},
            onCategoryCreated = {},
            onNameChange = {},
            onIconSearchQueryChange = {},
            onIconSelected = {},
            onColorSelected = {},
            onSaveCategory = {}
        )
    }
}

/**
 * The state the light preview cannot show: a colour already chosen.
 *
 * The stored hex is a light swatch drawn inside the dark theme, which is exactly the case the
 * swatch row exists to handle — a colour picked in one theme has to still read as selected in
 * the other, or a coloured category would look uncoloured and a second tap would silently
 * replace a colour the user never meant to change.
 */
@androidx.compose.ui.tooling.preview.Preview(showBackground = true, name = "Add Category - Dark, colour chosen")
@Composable
internal fun AddCategoryScreenContentColoredPreview() {
    com.mknlabs.expensetracker.core.ui.theme.ExpenseTrackerTheme(darkTheme = true) {
        AddCategoryScreenContent(
            uiState = AddCategoryUiState(
                name = "Coffee runs",
                selectedColorHex = "#D97706"
            ),
            existingCategories = emptyList(),
            existingPaymentMethods = emptyList(),
            onBackClick = {},
            onCategoryCreated = {},
            onNameChange = {},
            onIconSearchQueryChange = {},
            onIconSelected = {},
            onColorSelected = {},
            onSaveCategory = {}
        )
    }
}
