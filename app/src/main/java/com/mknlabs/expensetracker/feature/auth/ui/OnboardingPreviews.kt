package com.mknlabs.expensetracker.feature.auth.ui

import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Preview
import com.mknlabs.expensetracker.core.ui.theme.ExpenseTrackerTheme

@Preview(
    name = "1. Track Expenses - Dark",
    showBackground = true,
    showSystemUi = true,
    device = "spec:width=412dp,height=915dp,dpi=420"
)
@Composable
private fun PreviewOnboardingPage1_TrackExpenses_Dark() {
    ExpenseTrackerTheme(darkTheme = true) {
        OnboardingScreenContent(initialPage = 0)
    }
}

@Preview(
    name = "1. Track Expenses - Light",
    showBackground = true,
    showSystemUi = true,
    device = "spec:width=412dp,height=915dp,dpi=420"
)
@Composable
private fun PreviewOnboardingPage1_TrackExpenses_Light() {
    ExpenseTrackerTheme(darkTheme = false) {
        OnboardingScreenContent(initialPage = 0)
    }
}

@Preview(
    name = "2. Secure & Private - Dark",
    showBackground = true,
    showSystemUi = true,
    device = "spec:width=412dp,height=915dp,dpi=420"
)
@Composable
private fun PreviewOnboardingPage2_SecurePrivate_Dark() {
    ExpenseTrackerTheme(darkTheme = true) {
        OnboardingScreenContent(initialPage = 1)
    }
}

@Preview(
    name = "2. Secure & Private - Light",
    showBackground = true,
    showSystemUi = true,
    device = "spec:width=412dp,height=915dp,dpi=420"
)
@Composable
private fun PreviewOnboardingPage2_SecurePrivate_Light() {
    ExpenseTrackerTheme(darkTheme = false) {
        OnboardingScreenContent(initialPage = 1)
    }
}

@Preview(
    name = "3. Visual Analytics - Dark",
    showBackground = true,
    showSystemUi = true,
    device = "spec:width=412dp,height=915dp,dpi=420"
)
@Composable
private fun PreviewOnboardingPage3_VisualAnalytics_Dark() {
    ExpenseTrackerTheme(darkTheme = true) {
        OnboardingScreenContent(initialPage = 2)
    }
}

@Preview(
    name = "3. Visual Analytics - Light",
    showBackground = true,
    showSystemUi = true,
    device = "spec:width=412dp,height=915dp,dpi=420"
)
@Composable
private fun PreviewOnboardingPage3_VisualAnalytics_Light() {
    ExpenseTrackerTheme(darkTheme = false) {
        OnboardingScreenContent(initialPage = 2)
    }
}

@Preview(
    name = "4. Premium & Privacy - Dark",
    showBackground = true,
    showSystemUi = true,
    device = "spec:width=412dp,height=915dp,dpi=420"
)
@Composable
private fun PreviewOnboardingPage4_PremiumPrivacy_Dark() {
    ExpenseTrackerTheme(darkTheme = true) {
        OnboardingScreenContent(initialPage = 3)
    }
}

@Preview(
    name = "4. Premium & Privacy - Light",
    showBackground = true,
    showSystemUi = true,
    device = "spec:width=412dp,height=915dp,dpi=420"
)
@Composable
private fun PreviewOnboardingPage4_PremiumPrivacy_Light() {
    ExpenseTrackerTheme(darkTheme = false) {
        OnboardingScreenContent(initialPage = 3)
    }
}

@Preview(
    name = "5. Auth Gate - Dark",
    showBackground = true,
    showSystemUi = true,
    device = "spec:width=412dp,height=915dp,dpi=420"
)
@Composable
private fun PreviewOnboardingPage5_Auth_Dark() {
    ExpenseTrackerTheme(darkTheme = true) {
        OnboardingScreenContent(initialPage = 5)
    }
}

@Preview(
    name = "5. Auth Gate - Light",
    showBackground = true,
    showSystemUi = true,
    device = "spec:width=412dp,height=915dp,dpi=420"
)
@Composable
private fun PreviewOnboardingPage5_Auth_Light() {
    ExpenseTrackerTheme(darkTheme = false) {
        OnboardingScreenContent(initialPage = 5)
    }
}

@Preview(
    name = "6. Profile Setup - Dark",
    showBackground = true,
    showSystemUi = true,
    device = "spec:width=412dp,height=915dp,dpi=420"
)
@Composable
private fun PreviewOnboardingPage6_ProfileSetup_Dark() {
    ExpenseTrackerTheme(darkTheme = true) {
        OnboardingScreenContent(initialPage = 6)
    }
}

@Preview(
    name = "6. Profile Setup - Light",
    showBackground = true,
    showSystemUi = true,
    device = "spec:width=412dp,height=915dp,dpi=420"
)
@Composable
private fun PreviewOnboardingPage6_ProfileSetup_Light() {
    ExpenseTrackerTheme(darkTheme = false) {
        OnboardingScreenContent(initialPage = 6)
    }
}

@Preview(
    name = "7. Welcome Back - Dark",
    showBackground = true,
    showSystemUi = true,
    device = "spec:width=412dp,height=915dp,dpi=420"
)
@Composable
private fun PreviewOnboardingPage7_WelcomeBack_Dark() {
    ExpenseTrackerTheme(darkTheme = true) {
        OnboardingScreenContent(
            initialPage = 7,
            returningUserProfile = ReturningUserProfile(
                fullName = "Alex Morgan",
                gender = "Non-Binary"
            )
        )
    }
}

@Preview(
    name = "7. Welcome Back - Light",
    showBackground = true,
    showSystemUi = true,
    device = "spec:width=412dp,height=915dp,dpi=420"
)
@Composable
private fun PreviewOnboardingPage7_WelcomeBack_Light() {
    ExpenseTrackerTheme(darkTheme = false) {
        OnboardingScreenContent(
            initialPage = 7,
            returningUserProfile = ReturningUserProfile(
                fullName = "Alex Morgan",
                gender = "Non-Binary"
            )
        )
    }
}
