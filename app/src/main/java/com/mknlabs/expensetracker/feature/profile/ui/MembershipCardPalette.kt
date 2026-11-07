package com.mknlabs.expensetracker.feature.profile.ui
import com.mknlabs.expensetracker.core.ui.components.rememberSectionEnterAlphas

import android.content.res.Configuration
import androidx.annotation.StringRes
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import com.adamglin.PhosphorIcons
import com.adamglin.phosphoricons.Regular
import com.adamglin.phosphoricons.regular.Crown
import com.adamglin.phosphoricons.regular.SignIn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.rounded.ConfirmationNumber
import androidx.compose.material.icons.rounded.Event
import androidx.compose.material.icons.outlined.LocalActivity
import androidx.compose.material.icons.outlined.Unpublished
import androidx.compose.material.icons.rounded.Lock
import androidx.compose.material.icons.rounded.Schedule

import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.Modifier
import com.mknlabs.expensetracker.core.ui.theme.appScreenBackground
import com.mknlabs.expensetracker.core.ui.theme.accentInk
import com.mknlabs.expensetracker.core.ui.theme.accentSoft
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.mknlabs.expensetracker.R
import com.mknlabs.expensetracker.core.ui.components.AppOutlinedButton
import com.mknlabs.expensetracker.models.UserTier
import com.mknlabs.expensetracker.core.ui.components.AppCard
import com.mknlabs.expensetracker.core.ui.components.AppCardDefaults
import com.mknlabs.expensetracker.core.ui.components.AppHeader
import com.mknlabs.expensetracker.core.ui.navigation.LocalUpgradeToPro
import com.mknlabs.expensetracker.core.ui.theme.Dimens
import com.mknlabs.expensetracker.core.ui.theme.ExpenseTrackerTheme
import com.mknlabs.expensetracker.core.ui.theme.brandGradient
import com.mknlabs.expensetracker.core.ui.theme.income
import com.mknlabs.expensetracker.core.ui.theme.membershipCard
import com.mknlabs.expensetracker.core.ui.theme.membershipCardBorder
import com.mknlabs.expensetracker.core.ui.theme.membershipFactClockInk
import com.mknlabs.expensetracker.core.ui.theme.membershipFactDateInk
import com.mknlabs.expensetracker.core.ui.theme.membershipFactInk
import com.mknlabs.expensetracker.core.ui.theme.membershipMutedInk
import com.mknlabs.expensetracker.core.ui.theme.isDark
import com.mknlabs.expensetracker.core.ui.theme.premiumGold
import com.mknlabs.expensetracker.core.ui.theme.TextSecondaryLight
import com.mknlabs.expensetracker.core.ui.theme.currentSpacing
import com.mknlabs.expensetracker.feature.paywall.ui.purchaseMessageRes
import com.mknlabs.expensetracker.feature.settings.ui.ReferAFriendViewModel
import com.mknlabs.expensetracker.monetization.EntitlementResolver
import com.mknlabs.expensetracker.monetization.MonetizationViewModel
import com.mknlabs.expensetracker.monetization.PurchaseState
import com.mknlabs.expensetracker.monetization.StoreEntitlement
import java.util.Locale


internal data class MembershipCardPalette(
    val surface: Color,
    val border: Color,
    val foreground: Color,
    val headerLabel: Color,
    val bodyText: Color,
    val accent: Color,
    val panelBackground: Color,
    val panelIcon: Color,
    val panelHeadline: Color,
)

@Composable
internal fun membershipCardPalette(status: MembershipStatus): MembershipCardPalette {
    val colorScheme = MaterialTheme.colorScheme
    return MembershipCardPalette(
        surface = colorScheme.membershipCard,
        border = colorScheme.membershipCardBorder,
        foreground = colorScheme.onSurface,
        headerLabel = if (status == MembershipStatus.SUBSCRIPTION || status == MembershipStatus.PRO_PASS) {
            colorScheme.premiumGold
        } else {
            colorScheme.membershipMutedInk
        },
        bodyText = colorScheme.onSurfaceVariant,
        accent = if (status == MembershipStatus.SUBSCRIPTION || status == MembershipStatus.PRO_PASS) {
            colorScheme.premiumGold
        } else {
            colorScheme.accentInk
        },
        panelBackground = colorScheme.onSurface.copy(alpha = 0.05f),
        // The one thing the states still say in colour: a subscriber holds access the store
        // keeps, a pass holds access that runs out, and neither is the other's claim. The
        // panel's own glyph carries that difference; everything around it is one neutral ink.
        panelIcon = if (status == MembershipStatus.SUBSCRIPTION) {
            colorScheme.income
        } else {
            colorScheme.accentInk
        },
        panelHeadline = colorScheme.onSurface,
    )
}

/** The icon behind a [MembershipGlyph]. */
internal fun glyphIcon(glyph: MembershipGlyph): ImageVector = when (glyph) {
    MembershipGlyph.VERIFIED -> Icons.Default.Check
    MembershipGlyph.TICKET -> Icons.Rounded.ConfirmationNumber
    MembershipGlyph.LOCK -> Icons.Rounded.Lock
    MembershipGlyph.DATE -> Icons.Rounded.Event
    MembershipGlyph.CLOCK -> Icons.Rounded.Schedule
}
