package com.mknlabs.expensetracker.feature.settings.ui

import com.mknlabs.expensetracker.core.ui.theme.appScreenBackground
import com.mknlabs.expensetracker.core.ui.theme.sheet

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import com.adamglin.PhosphorIcons
import com.adamglin.phosphoricons.Regular
import com.adamglin.phosphoricons.regular.ArrowCounterClockwise
import com.adamglin.phosphoricons.regular.ClockCountdown
import com.adamglin.phosphoricons.regular.ClockCounterClockwise
import com.adamglin.phosphoricons.regular.CloudArrowUp
import com.adamglin.phosphoricons.regular.Database
import com.adamglin.phosphoricons.regular.FileArrowDown
import com.adamglin.phosphoricons.regular.FileArrowUp
import com.adamglin.phosphoricons.regular.Note
import com.adamglin.phosphoricons.regular.Trash
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.Modifier
import com.mknlabs.expensetracker.core.ui.theme.accentInk
import com.mknlabs.expensetracker.core.ui.theme.accentSoft
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.res.pluralStringResource
import com.mknlabs.expensetracker.R
import com.mknlabs.expensetracker.core.ui.components.AppOutlinedFieldDefaults
import com.mknlabs.expensetracker.core.ui.components.AppTextButton
import com.mknlabs.expensetracker.monetization.AccessStatus
import com.mknlabs.expensetracker.monetization.Feature
import com.mknlabs.expensetracker.monetization.FeatureRegistry
import com.mknlabs.expensetracker.models.SettingsItemType
import com.mknlabs.expensetracker.core.ui.theme.Dimens
import com.mknlabs.expensetracker.core.ui.theme.SettingsTiles
import com.mknlabs.expensetracker.core.ui.theme.genderRow
import com.mknlabs.expensetracker.core.ui.theme.profileSheet
import com.mknlabs.expensetracker.core.ui.components.*
import com.mknlabs.expensetracker.core.ui.navigation.LocalUpgradeToPro
import com.mknlabs.expensetracker.core.ui.models.SelectionItem
import java.time.LocalDate
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import com.mknlabs.expensetracker.monetization.MonetizationViewModel
import com.mknlabs.expensetracker.core.ui.components.AdContainer
import com.mknlabs.expensetracker.core.ui.components.NativeAdCard
import com.mknlabs.expensetracker.monetization.AdPlacement
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.ui.text.input.KeyboardType
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.compose.ui.tooling.preview.Preview
import com.mknlabs.expensetracker.core.ui.theme.ExpenseTrackerTheme


@Preview(showBackground = true)
@Composable
internal fun DataManagementScreenPreview() {
    ExpenseTrackerTheme(darkTheme = true) {
        val flow = remember { kotlinx.coroutines.flow.MutableStateFlow(AccessStatus.Granted) }
        DataManagementContent(
            transactionCount = 120,
            isAutoBackupEnabled = true,
            autoBackupFrequencyDays = 7,
            isAdsEnabled = true,
            isAnonymous = false,
            onAutoBackupEnabledChange = {},
            onAutoBackupFrequencyChange = {},
            onDatabaseBackupFileSelected = {},
            onDatabaseRestoreFileSelected = {},
            onJsonExportFileSelected = {},
            onJsonImportFileSelected = {},
            onLegacyImportFileSelected = {},
            onDeleteAllTransactionsClick = {},
            onPrepareForExternalActivity = {},
            onBackClick = {},
            getAccessStatus = { _, _ -> flow }
        )
    }
}
