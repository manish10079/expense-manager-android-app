package com.mknlabs.expensetracker.core.ui.components

import android.content.res.Configuration
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.ConfirmationNumber
import androidx.compose.material.icons.rounded.Email
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.adamglin.PhosphorIcons
import com.adamglin.phosphoricons.Regular
import com.adamglin.phosphoricons.regular.Check
import com.adamglin.phosphoricons.regular.Clock
import com.adamglin.phosphoricons.regular.Crown
import com.adamglin.phosphoricons.regular.SealCheck
import com.mknlabs.expensetracker.R
import com.mknlabs.expensetracker.core.ui.theme.ExpenseTrackerTheme
import com.mknlabs.expensetracker.core.ui.theme.accentInk
import com.mknlabs.expensetracker.core.ui.theme.brandGradient
import com.mknlabs.expensetracker.core.ui.theme.isDark
import com.mknlabs.expensetracker.monetization.MonetizationViewModel
import com.mknlabs.expensetracker.monetization.RedemptionState
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

// Design Specification Color Tokens
private val PrimaryGradientStart = Color(0xFF6A4DFF)
private val PrimaryGradientEnd = Color(0xFF6750A4)
private val SecondaryGradientStart = Color(0xFFFFC107)
private val SecondaryGradientEnd = Color(0xFFFF8F00)

private val HeroGradient = Brush.linearGradient(
    colors = listOf(PrimaryGradientStart, PrimaryGradientEnd)
)

private val SecondaryBrush = Brush.linearGradient(
    colors = listOf(SecondaryGradientStart, SecondaryGradientEnd)
)

private val SuccessGreen = Color(0xFF10B981)

@Composable
fun ProPassRedeemDialog(
    viewModel: MonetizationViewModel,
    onDismiss: () -> Unit,
    onVerifyEmail: () -> Unit = {}
) {
    val pendingPromoCode by viewModel.pendingPromoCode.collectAsStateWithLifecycle()
    var code by remember { mutableStateOf("") }
    var appliedPendingCode by remember { mutableStateOf("") }
    androidx.compose.runtime.LaunchedEffect(pendingPromoCode) {
        if (pendingPromoCode.isNotBlank() && pendingPromoCode != appliedPendingCode) {
            code = pendingPromoCode
            appliedPendingCode = pendingPromoCode
        }
    }
    val state by viewModel.redemptionState.collectAsStateWithLifecycle()
    val hasActiveStoreSubscription by viewModel.hasActiveStoreSubscription.collectAsStateWithLifecycle()
    val firebaseUser = com.google.firebase.auth.FirebaseAuth.getInstance().currentUser
    val isGoogleAccount = firebaseUser?.providerData?.any { it.providerId == "google.com" } == true
    val isEmailVerified = firebaseUser?.isEmailVerified == true || isGoogleAccount

    ProPassRedeemDialogContent(
        code = code,
        onCodeChange = { code = it.uppercase() },
        state = state,
        hasActiveStoreSubscription = hasActiveStoreSubscription,
        isEmailVerified = isEmailVerified,
        onRedeem = { viewModel.redeemProPass(code) },
        onDismiss = {
            viewModel.resetRedemptionState()
            onDismiss()
        },
        onVerifyEmail = {
            firebaseUser?.sendEmailVerification()
            onVerifyEmail()
        }
    )
}

@Composable
fun ProPassRedeemDialogContent(
    code: String,
    onCodeChange: (String) -> Unit,
    state: RedemptionState,
    hasActiveStoreSubscription: Boolean = false,
    isEmailVerified: Boolean = true,
    onRedeem: () -> Unit,
    onDismiss: () -> Unit,
    onVerifyEmail: () -> Unit = {}
) {
    if (hasActiveStoreSubscription) {
        ModernConfirmationDialog(
            onDismissRequest = onDismiss,
            title = stringResource(id = R.string.title_pro_pass_subscribed),
            text = stringResource(id = R.string.msg_pro_pass_subscribed),
            confirmButtonText = stringResource(id = R.string.btn_ok),
            onConfirm = onDismiss,
            headerIcon = Icons.Rounded.ConfirmationNumber,
            showDismissButton = false
        )
        return
    }

    if (!isEmailVerified) {
        ModernConfirmationDialog(
            onDismissRequest = onDismiss,
            title = stringResource(id = R.string.dialog_verify_email_title),
            text = stringResource(id = R.string.dialog_verify_email_pro_pass),
            confirmButtonText = stringResource(id = R.string.label_verify_now),
            onConfirm = {
                onVerifyEmail()
                onDismiss()
            },
            headerIcon = Icons.Rounded.Email,
            dismissButtonText = stringResource(id = R.string.label_later),
            onDismiss = onDismiss
        )
        return
    }

    Dialog(
        onDismissRequest = {
            if (state !is RedemptionState.Loading) {
                onDismiss()
            }
        },
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        ProPassRedeemDialogCard(
            code = code,
            onCodeChange = onCodeChange,
            state = state,
            onRedeem = onRedeem,
            onDismiss = onDismiss
        )
    }
}

@Composable
fun ProPassRedeemDialogCard(
    code: String,
    onCodeChange: (String) -> Unit,
    state: RedemptionState,
    onRedeem: () -> Unit,
    onDismiss: () -> Unit
) {
    val isDark = MaterialTheme.colorScheme.isDark
    val surfaceColor = if (isDark) Color(0xFF1C1C24) else Color(0xFFFFFFFF)
    val surfaceSubtleColor = if (isDark) Color(0xFF14141B) else Color(0xFFF7F7FB)
    val borderColor = if (isDark) Color(0xFF282836) else Color(0xFFE2E2EC)
    val textPrimary = if (isDark) Color(0xFFF1F1F5) else Color(0xFF121216)
    val textSecondary = if (isDark) Color(0xFF9E9EA9) else Color(0xFF626270)
    val textMuted = Color(0xFF626270)

    Surface(
        shape = RoundedCornerShape(28.dp),
        color = surfaceColor,
        border = BorderStroke(1.dp, borderColor),
        shadowElevation = 8.dp,
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp)
            .widthIn(max = 560.dp)
    ) {
        Box {
            if (state is RedemptionState.Success) {
                LoopingConfettiCanvas(modifier = Modifier.matchParentSize())
            }
            Column(
                modifier = Modifier.padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
            // 🔮 Main Hero Icon Container (Shared across both dialogs)
            Surface(
                shape = RoundedCornerShape(24.dp),
                color = Color.Transparent,
                shadowElevation = 8.dp,
                modifier = Modifier.size(80.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(80.dp)
                        .clip(RoundedCornerShape(24.dp))
                        .background(HeroGradient),
                    contentAlignment = Alignment.Center
                ) {
                    val heroIcon = if (state is RedemptionState.Success) PhosphorIcons.Regular.Crown else PhosphorIcons.Regular.SealCheck
                    Icon(
                        imageVector = heroIcon,
                        contentDescription = null,
                        tint = Color.White,
                        modifier = Modifier.size(44.dp)
                    )
                }
            }

            // 🏷️ Status Chip / Pill
            if (state is RedemptionState.Success) {
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(50))
                        .background(if (isDark) Color(0x2610B981) else Color(0x1A10B981))
                        .border(1.dp, SuccessGreen.copy(alpha = 0.3f), RoundedCornerShape(50))
                        .padding(horizontal = 12.dp, vertical = 6.dp)
                ) {
                    Text(
                        text = stringResource(R.string.label_code_accepted).uppercase(),
                        color = SuccessGreen,
                        style = TextStyle(
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 0.8.sp
                        )
                    )
                }
            } else {
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(50))
                        .background(if (isDark) Color(0x286A4DFF) else Color(0x1F6A4DFF))
                        .border(1.dp, if (isDark) Color(0x406A4DFF) else Color(0x2B6A4DFF), RoundedCornerShape(50))
                        .padding(horizontal = 12.dp, vertical = 6.dp)
                ) {
                    Text(
                        text = stringResource(R.string.label_enter_pro_code),
                        color = if (isDark) Color(0xFFA897FF) else Color(0xFF5332FF),
                        style = TextStyle(
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 0.8.sp
                        )
                    )
                }
            }

            // 📝 Dialog Body & Content
            when (state) {
                is RedemptionState.Idle, is RedemptionState.Loading -> {
                    Text(
                        text = stringResource(R.string.title_unlock_pro_access),
                        style = TextStyle(
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Bold,
                            textAlign = TextAlign.Center
                        ),
                        color = textPrimary
                    )
                    Text(
                        text = stringResource(R.string.msg_unlock_pro_access_body),
                        style = TextStyle(
                            fontSize = 13.sp,
                            lineHeight = 18.sp,
                            textAlign = TextAlign.Center
                        ),
                        color = textSecondary
                    )

                    Column(
                        modifier = Modifier.fillMaxWidth(),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        OutlinedTextField(
                            value = code,
                            onValueChange = onCodeChange,
                            placeholder = {
                                Text(
                                    text = stringResource(R.string.placeholder_pro_code_example),
                                    color = textMuted,
                                    fontSize = 15.sp,
                                    modifier = Modifier.fillMaxWidth(),
                                    textAlign = TextAlign.Center
                                )
                            },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(56.dp),
                            singleLine = true,
                            enabled = state !is RedemptionState.Loading,
                            keyboardOptions = KeyboardOptions(capitalization = KeyboardCapitalization.Characters),
                            shape = RoundedCornerShape(14.dp),
                            textStyle = TextStyle(
                                fontSize = 15.sp,
                                fontWeight = FontWeight.Bold,
                                letterSpacing = 1.5.sp,
                                textAlign = TextAlign.Center,
                                color = textPrimary
                            ),
                            colors = OutlinedTextFieldDefaults.colors(
                                cursorColor = MaterialTheme.colorScheme.accentInk,
                                focusedContainerColor = surfaceSubtleColor,
                                unfocusedContainerColor = surfaceSubtleColor,
                                disabledContainerColor = surfaceSubtleColor,
                                focusedBorderColor = PrimaryGradientStart,
                                unfocusedBorderColor = borderColor,
                                disabledBorderColor = borderColor
                            )
                        )

                        ProPassActionButton(
                            text = stringResource(R.string.label_activate_pro_code),
                            onClick = onRedeem,
                            enabled = code.isNotBlank() && state !is RedemptionState.Loading,
                            isLoading = state is RedemptionState.Loading
                        )
                    }
                }

                is RedemptionState.Error -> {
                    Text(
                        text = stringResource(R.string.title_unlock_pro_access),
                        style = TextStyle(
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Bold,
                            textAlign = TextAlign.Center
                        ),
                        color = textPrimary
                    )
                    Text(
                        text = stringResource(id = redemptionErrorMessageRes(state.error)),
                        style = TextStyle(
                            fontSize = 13.sp,
                            lineHeight = 18.sp,
                            textAlign = TextAlign.Center
                        ),
                        color = MaterialTheme.colorScheme.error
                    )

                    Column(
                        modifier = Modifier.fillMaxWidth(),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        OutlinedTextField(
                            value = code,
                            onValueChange = onCodeChange,
                            placeholder = {
                                Text(
                                    text = stringResource(R.string.placeholder_pro_code_example),
                                    color = textMuted,
                                    fontSize = 15.sp,
                                    modifier = Modifier.fillMaxWidth(),
                                    textAlign = TextAlign.Center
                                )
                            },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(56.dp),
                            singleLine = true,
                            isError = true,
                            keyboardOptions = KeyboardOptions(capitalization = KeyboardCapitalization.Characters),
                            shape = RoundedCornerShape(14.dp),
                            textStyle = TextStyle(
                                fontSize = 15.sp,
                                fontWeight = FontWeight.Bold,
                                letterSpacing = 1.5.sp,
                                textAlign = TextAlign.Center,
                                color = textPrimary
                            ),
                            colors = OutlinedTextFieldDefaults.colors(
                                cursorColor = MaterialTheme.colorScheme.accentInk,
                                focusedContainerColor = surfaceSubtleColor,
                                unfocusedContainerColor = surfaceSubtleColor,
                                disabledContainerColor = surfaceSubtleColor,
                                focusedBorderColor = MaterialTheme.colorScheme.error,
                                unfocusedBorderColor = MaterialTheme.colorScheme.error
                            )
                        )

                        ProPassActionButton(
                            text = stringResource(R.string.label_activate_pro_code),
                            onClick = onRedeem,
                            enabled = code.isNotBlank()
                        )
                    }
                }

                is RedemptionState.Success -> {
                    Text(
                        text = "Step Into Pro Access:\nYour Next ${state.days} Days Are Unlocked",
                        style = TextStyle(
                            fontSize = 20.sp,
                            lineHeight = 26.sp,
                            fontWeight = FontWeight.Bold,
                            textAlign = TextAlign.Center
                        ),
                        color = textPrimary
                    )
                    Text(
                        text = "Your promo code was successfully applied. All Pro features are unlocked for your account.",
                        style = TextStyle(
                            fontSize = 13.sp,
                            lineHeight = 18.sp,
                            textAlign = TextAlign.Center
                        ),
                        color = textSecondary
                    )

                    // 3-Row Feature Checklist Container Card
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(16.dp))
                            .background(surfaceSubtleColor)
                            .border(1.dp, borderColor, RoundedCornerShape(16.dp))
                            .padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        ProChecklistRow(text = "Full Pro Access - Ad Free", isDark = isDark)
                        ProChecklistRow(text = "Multi-Device Data Sync", isDark = isDark)
                        ProChecklistRow(text = "All Premium Features Unlocked", isDark = isDark)
                    }

                    Column(
                        modifier = Modifier.fillMaxWidth(),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        // Expiration Banner Box
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(44.dp)
                                .clip(RoundedCornerShape(12.dp))
                                .background(if (isDark) Color(0x1F6A4DFF) else Color(0x126A4DFF))
                                .border(1.dp, if (isDark) Color(0x406A4DFF) else Color(0x2B6A4DFF), RoundedCornerShape(12.dp))
                                .padding(horizontal = 14.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = PhosphorIcons.Regular.Clock,
                                contentDescription = null,
                                tint = PrimaryGradientStart,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "Valid until:",
                                fontSize = 12.sp,
                                color = textSecondary,
                                modifier = Modifier.weight(1f)
                            )
                            Text(
                                text = formatExpiryDateLabel(state.days),
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                color = if (isDark) Color(0xFFA897FF) else Color(0xFF5332FF)
                            )
                        }

                        ProPassActionButton(
                            text = "Start Exploring Pro",
                            onClick = onDismiss
                        )
                    }
                }
            }
        }
    }
}
}

@Composable
private fun ProChecklistRow(text: String, isDark: Boolean) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Box(
            modifier = Modifier
                .size(22.dp)
                .clip(CircleShape)
                .background(if (isDark) Color(0x2610B981) else Color(0x1A10B981)),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = PhosphorIcons.Regular.Check,
                contentDescription = null,
                tint = SuccessGreen,
                modifier = Modifier.size(12.dp)
            )
        }
        Spacer(modifier = Modifier.width(10.dp))
        Text(
            text = text,
            fontSize = 13.sp,
            fontWeight = FontWeight.Medium,
            color = if (isDark) Color(0xFFF1F1F5) else Color(0xFF121216)
        )
    }
}

@Composable
private fun ProPassActionButton(
    text: String,
    onClick: () -> Unit,
    enabled: Boolean = true,
    isLoading: Boolean = false
) {
    val shape = RoundedCornerShape(14.dp)
    Button(
        onClick = onClick,
        enabled = enabled,
        shape = shape,
        colors = ButtonDefaults.buttonColors(
            containerColor = Color.Transparent,
            disabledContainerColor = Color.Transparent,
            contentColor = Color.White,
            disabledContentColor = Color.White.copy(alpha = 0.6f)
        ),
        contentPadding = PaddingValues(0.dp),
        modifier = Modifier
            .fillMaxWidth()
            .height(50.dp)
    ) {
        Box(
            modifier = Modifier
                .clip(shape)
                .background(brush = brandGradient(alpha = if (enabled) 1f else 0.45f))
                .padding(horizontal = 20.dp, vertical = 12.dp)
                .fillMaxWidth(),
            contentAlignment = Alignment.Center
        ) {
            if (isLoading) {
                CircularProgressIndicator(
                    modifier = Modifier.size(20.dp),
                    color = Color.White,
                    strokeWidth = 2.dp
                )
            } else {
                Text(
                    text = text,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
            }
        }
    }
}

private fun formatExpiryDateLabel(days: Int): String {
    val calendar = Calendar.getInstance()
    calendar.add(Calendar.DAY_OF_YEAR, days)
    val formatter = SimpleDateFormat("MMM dd, yyyy", Locale.getDefault())
    return formatter.format(calendar.time)
}

@Preview(name = "Pro Pass Redeem Dialog - Input Code (Light)", showBackground = true)
@Composable
private fun ProPassRedeemDialogInputCodeLightPreview() {
    ExpenseTrackerTheme(darkTheme = false) {
        ProPassRedeemDialogCard(
            code = "PROPASS7",
            onCodeChange = {},
            state = RedemptionState.Idle,
            onRedeem = {},
            onDismiss = {}
        )
    }
}

@Preview(name = "Pro Pass Redeem Dialog - Input Code (Dark)", showBackground = true, uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
private fun ProPassRedeemDialogInputCodeDarkPreview() {
    ExpenseTrackerTheme(darkTheme = true) {
        ProPassRedeemDialogCard(
            code = "PROPASS7",
            onCodeChange = {},
            state = RedemptionState.Idle,
            onRedeem = {},
            onDismiss = {}
        )
    }
}

@Preview(name = "Pro Pass Redeem Dialog - Success (Light)", showBackground = true)
@Composable
private fun ProPassRedeemDialogSuccessLightPreview() {
    ExpenseTrackerTheme(darkTheme = false) {
        ProPassRedeemDialogCard(
            code = "PROPASS7",
            onCodeChange = {},
            state = RedemptionState.Success(days = 7),
            onRedeem = {},
            onDismiss = {}
        )
    }
}

@Preview(name = "Pro Pass Redeem Dialog - Success (Dark)", showBackground = true, uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
private fun ProPassRedeemDialogSuccessDarkPreview() {
    ExpenseTrackerTheme(darkTheme = true) {
        ProPassRedeemDialogCard(
            code = "PROPASS7",
            onCodeChange = {},
            state = RedemptionState.Success(days = 7),
            onRedeem = {},
            onDismiss = {}
        )
    }
}
