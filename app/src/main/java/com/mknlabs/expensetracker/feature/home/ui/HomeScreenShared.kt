package com.mknlabs.expensetracker.feature.home.ui


import com.mknlabs.expensetracker.core.ui.theme.appScreenBackground
import com.mknlabs.expensetracker.core.ui.theme.sheet
import com.mknlabs.expensetracker.core.ui.theme.track

import android.content.res.Configuration
import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import androidx.compose.animation.core.FastOutLinearInEasing
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.LinearOutSlowInEasing
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import com.adamglin.PhosphorIcons
import com.adamglin.phosphoricons.Fill
import com.adamglin.phosphoricons.Regular
import com.adamglin.phosphoricons.fill.CheckCircle
import com.adamglin.phosphoricons.regular.CalendarBlank
import com.adamglin.phosphoricons.regular.ChatCircle
import com.adamglin.phosphoricons.regular.Crown
import com.adamglin.phosphoricons.regular.CaretRight
import com.adamglin.phosphoricons.regular.Circle
import com.adamglin.phosphoricons.regular.Gear
import com.adamglin.phosphoricons.regular.Info
import com.adamglin.phosphoricons.regular.PiggyBank
import com.adamglin.phosphoricons.regular.X
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import com.mknlabs.expensetracker.core.ui.theme.accentInk
import com.mknlabs.expensetracker.core.ui.theme.accentSoft
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringArrayResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.tooling.preview.PreviewFontScale
import androidx.compose.ui.tooling.preview.PreviewScreenSizes
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.mknlabs.expensetracker.BuildConfig
import com.mknlabs.expensetracker.R
import com.mknlabs.expensetracker.core.ui.components.AppTextButton
import com.mknlabs.expensetracker.data.constants.DEFAULT_CURRENCY_ID
import com.mknlabs.expensetracker.data.constants.DEFAULT_DATE_FORMAT_PATTERN
import com.mknlabs.expensetracker.data.constants.DEFAULT_TIME_FORMAT
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.MoreVert
import com.mknlabs.expensetracker.models.AmountFormatPreferences
import com.mknlabs.expensetracker.models.CategoryType
import com.mknlabs.expensetracker.models.Transaction
import com.mknlabs.expensetracker.models.TransactionCardCustomizationSettings
import com.mknlabs.expensetracker.models.UserProfile
import com.mknlabs.expensetracker.models.avatarInitials
import com.mknlabs.expensetracker.models.defaultUserProfile
import com.mknlabs.expensetracker.models.hasPhoneNumber
import com.mknlabs.expensetracker.core.ui.adaptive.LocalAppWindowInfo
import com.mknlabs.expensetracker.core.ui.components.*
import com.mknlabs.expensetracker.core.ui.HomeFirstRunPromptGate
import com.mknlabs.expensetracker.core.ui.components.LocalSplashGone
import com.mknlabs.expensetracker.core.ui.components.MissingPermissionsBottomSheet
import com.mknlabs.expensetracker.core.ui.components.RateAppBottomSheet
import com.mknlabs.expensetracker.core.ui.components.ReferOfferBottomSheet
import com.mknlabs.expensetracker.data.local.ReferPromptStore
import com.mknlabs.expensetracker.data.local.RateAppPromptStore
import com.mknlabs.expensetracker.data.local.SmsInboxStaleRemindStore
import com.mknlabs.expensetracker.utils.RateAppPrompt
import com.mknlabs.expensetracker.utils.SmsInboxStaleRemind
import kotlinx.coroutines.delay
import com.mknlabs.expensetracker.utils.LaunchSheetPick
import kotlinx.coroutines.launch
import com.mknlabs.expensetracker.core.ui.theme.Dimens
import com.mknlabs.expensetracker.core.ui.theme.ExpenseTrackerTheme
import com.mknlabs.expensetracker.core.ui.theme.isDark
import com.mknlabs.expensetracker.core.ui.theme.NavOnDark
import com.mknlabs.expensetracker.core.ui.theme.NavOnLight
import com.mknlabs.expensetracker.core.ui.theme.darkOnlyGradient
import com.mknlabs.expensetracker.core.ui.theme.expense
import com.mknlabs.expensetracker.core.ui.theme.income
import com.mknlabs.expensetracker.core.ui.theme.standardCardGradient
import com.mknlabs.expensetracker.utils.UiText
import com.mknlabs.expensetracker.feature.smsinbox.ui.SmsSetupUiState
import com.mknlabs.expensetracker.feature.smsinbox.ui.SmsSetupViewModel
import com.mknlabs.expensetracker.utils.DeviceVendorUtils
import com.mknlabs.expensetracker.utils.findFragmentActivity
import com.mknlabs.expensetracker.utils.PermissionNudge
import com.mknlabs.expensetracker.data.local.PermissionNudgeState
import com.mknlabs.expensetracker.data.local.PermissionNudgeStore
import com.mknlabs.expensetracker.utils.defaultAmountFormatPreferences
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import com.mknlabs.expensetracker.monetization.AdPlacement

// Header inks from the homescreen redesign mock (homescree-redesign-2.html):
// the greeting is `text-white` / `text-slate-900`, the psychology line `text-slate-400` /
// `text-slate-500`. Kept here rather than in the theme so the header can follow the
// mock without repainting the rest of the app's text roles.
internal val GreetingNameDark = Color(0xFFFFFFFF)
internal val GreetingNameLight = Color(0xFF0F172A)
internal val PsychologyPhraseDark = Color(0xFF94A3B8)
internal val PsychologyPhraseLight = Color(0xFF64748B)

// The three-dot button's circle, taken from the mock: dark sits on
// `bg-surface-secondary` (#1A1D2B) with `border-white/5` and a `text-slate-300` icon;
// light is `bg-white` with `border-slate-200` and a `text-slate-600` icon.
internal val MoreButtonBackgroundLight = Color(0xFFFFFFFF)
internal val MoreButtonBorderDark = Color(0x0DFFFFFF)
internal val MoreButtonBorderLight = Color(0xFFE2E8F0)
internal val MoreButtonIconDark = Color(0xFFCBD5E1)
internal val MoreButtonIconLight = Color(0xFF475569)

// Unread notification dot, same as the mock's bell dot: `bg-accent-violet` in both modes.
internal val MoreButtonDotColor = Color(0xFF8B5CF6)

// Spent Today's icon tile, from the mock: indigo-500/10 fill, indigo-500/20 edge and an
// indigo-400 glyph in dark; indigo-50, indigo-100 and indigo-600 in light.
internal val SpentTodayTileDark = Color(0x1A6366F1)
internal val SpentTodayTileBorderDark = Color(0x336366F1)
internal val SpentTodayIconDark = Color(0xFF818CF8)
internal val SpentTodayTileLight = Color(0xFFEEF2FF)
internal val SpentTodayTileBorderLight = Color(0xFFE0E7FF)
internal val SpentTodayIconLight = Color(0xFF4F46E5)

// Spent Today's card chrome, from the mock: bg-surface #13151F with a white/5 edge in
// dark; bg-white with a slate-200/70 edge in light.
internal val SpentTodayCardDark = Color(0xFF101019)
internal val SpentTodayCardBorderDark = Color(0x1AFFFFFF)
internal val SpentTodayCardLight = Color(0xFFFFFFFF)
internal val SpentTodayCardBorderLight = Color(0xB3E2E8F0)

// My Goals, from the mock: violet-500/10 tile, violet-500/20 edge and violet-400 glyph
// in dark; violet-50, violet-100 and violet-600 in light. Same card chrome as Spent
// Today, and a violet-600 count badge ringed with the card fill.
internal val GoalsTileDark = Color(0x1A8B5CF6)
internal val GoalsTileBorderDark = Color(0x338B5CF6)
internal val GoalsIconDark = Color(0xFFA78BFA)
internal val GoalsTileLight = Color(0xFFF5F3FF)
internal val GoalsTileBorderLight = Color(0xFFEDE9FE)
internal val GoalsIconLight = Color(0xFF7C3AED)
internal val GoalsCardDark = Color(0xFF101019)
internal val GoalsCardBorderDark = Color(0x1AFFFFFF)
internal val GoalsCardLight = Color(0xFFFFFFFF)
internal val GoalsCardBorderLight = Color(0xB3E2E8F0)
internal val GoalsBadgeFill = Color(0xFF7C3AED)
internal val GoalsBadgeInk = Color(0xFFFFFFFF)
internal val GoalsBadgeRingDark = Color(0xFF101019)
internal val GoalsBadgeRingLight = Color(0xFFFFFFFF)

// Rotating one-liners shown where the tagline used to sit. A new one is picked every
// time the header becomes visible.

