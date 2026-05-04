package com.mkn0079.expensetracker

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.view.WindowManager
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.ui.graphics.Color
import androidx.compose.animation.core.LinearOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.window.Popup
import androidx.compose.ui.window.PopupProperties
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.fragment.app.FragmentActivity
import com.mkn0079.expensetracker.data.local.AppSettingsDataStore
import com.mkn0079.expensetracker.data.local.UserProfileDataStore
import com.mkn0079.expensetracker.models.AppThemeMode
import com.mkn0079.expensetracker.models.defaultUserProfile
import com.mkn0079.expensetracker.notifications.NotificationHelper
import com.mkn0079.expensetracker.ui.screens.SplashOverlay
import com.mkn0079.expensetracker.ui.theme.ExpenseTrackerTheme
import com.mkn0079.expensetracker.utils.BiometricAuthManager
import com.mkn0079.expensetracker.utils.findFragmentActivity
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext
import com.mkn0079.expensetracker.ui.viewmodels.SplashViewModel
import com.mkn0079.expensetracker.ui.viewmodels.InitTask
import com.mkn0079.expensetracker.ui.viewmodels.AppLockViewModel
import com.mkn0079.expensetracker.ui.viewmodels.AppLockState
import com.mkn0079.expensetracker.ui.components.AppLockOverlay // We will use the component logic but possibly refactor it
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : FragmentActivity() {

    private val splashViewModel: SplashViewModel by viewModels()
    private val appLockViewModel: AppLockViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        val splashScreen = installSplashScreen()
        
        // Keep the system splash screen visible until our custom splash screen is ready to take over.
        // This prevents the "flicker" or "blank frame" during the hand-off.
        splashScreen.setKeepOnScreenCondition { 
            splashViewModel.currentTask.value == InitTask.Start
        }
        
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            splashScreen.setOnExitAnimationListener { splashScreenViewProvider ->
                splashScreenViewProvider.view
                    .animate()
                    .alpha(0f)
                    .setDuration(180L)
                    .withEndAction { splashScreenViewProvider.remove() }
                    .start()
            }
        }

        super.onCreate(savedInstanceState)

        enableEdgeToEdge()

        checkAndRequestNotificationPermission()

        setContent {
            AppRoot(splashViewModel, appLockViewModel, intent)
        }
    }

    private fun checkAndRequestNotificationPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.POST_NOTIFICATIONS), 101)
            }
        }
    }

    @Composable
    private fun AppRoot(
        splashViewModel: SplashViewModel,
        appLockViewModel: AppLockViewModel,
        intent: Intent
    ) {
        val isReady by splashViewModel.isReady.collectAsState()
        val appLockState by appLockViewModel.state.collectAsState()
        val recoveryPerformed by appLockViewModel.recoveryPerformed.collectAsState()
        val context = applicationContext
        
        val initialNavDestination = intent.getStringExtra(NotificationHelper.EXTRA_NAV_DESTINATION)

        val appSettings by AppSettingsDataStore
            .getAppSettingsFlow(context)
            .collectAsState(initial = null)
        val userProfile by UserProfileDataStore
            .getUserProfileFlow(context)
            .collectAsState(initial = defaultUserProfile)
            
        val systemDarkTheme = isSystemInDarkTheme()
        val darkTheme = when (appSettings?.themeMode ?: AppThemeMode.SYSTEM) {
            AppThemeMode.SYSTEM -> systemDarkTheme
            AppThemeMode.LIGHT -> false
            AppThemeMode.DARK -> true
        }

        ExpenseTrackerTheme(darkTheme = darkTheme) {
            Box(modifier = Modifier.fillMaxSize()) {
                // Apply privacy settings whenever settings are available
                appSettings?.let { settings ->
                    LaunchedEffect(
                        settings.blurInRecentsEnabled,
                        settings.screenshotProtectionEnabled
                    ) {
                        applyPrivacySettings(
                            shouldBlurInRecents = settings.blurInRecentsEnabled,
                            shouldBlockScreenshots = settings.screenshotProtectionEnabled
                        )
                    }
                }

                Box(modifier = Modifier.fillMaxSize()) {
                    // 1. Main App Content (Always present if ready, but can be covered)
                    if (isReady && appSettings != null) {
                        MainScreen(
                            isReady = true,
                            appSettings = appSettings!!,
                            userProfile = userProfile,
                            initialNavDestination = initialNavDestination,
                            isRecoveryPerformed = recoveryPerformed,
                            onRecoveryConsumed = { appLockViewModel.consumeRecovery() }
                        )
                    }

                    // 2. Decide what to show based on Lock State (Overlay Layer)
                    if (appSettings != null) {
                        val settings = appSettings!!
                        AnimatedContent(
                            targetState = appLockState,
                            transitionSpec = {
                                if (targetState is AppLockState.Unlocked) {
                                    // Unlock transition: Scale and Fade (Premium Feel)
                                    (fadeIn(animationSpec = tween(500, easing = LinearOutSlowInEasing)) +
                                            scaleIn(
                                                initialScale = 0.92f,
                                                animationSpec = tween(500, easing = LinearOutSlowInEasing)
                                            ))
                                        .togetherWith(
                                            fadeOut(animationSpec = tween(400)) +
                                                    scaleOut(
                                                        targetScale = 1.08f,
                                                        animationSpec = tween(400)
                                                    )
                                        )
                                } else {
                                    // Default transitions (Locking or Loading)
                                    fadeIn(animationSpec = tween(300)) togetherWith fadeOut(
                                        animationSpec = tween(300)
                                    )
                                }
                            },
                            label = "app_lock_transition",
                            modifier = Modifier.fillMaxSize()
                        ) { state ->
                            when (state) {
                                is AppLockState.Loading -> {
                                    // Fail-Secure: Show black screen while determining lock state
                                    Box(
                                        modifier = Modifier
                                            .fillMaxSize()
                                            .background(Color.Black)
                                    )
                                }

                                is AppLockState.Locked -> {
                                    // 3. App Lock Screen (Blocks Main Content)
                                    val activity = LocalContext.current.findFragmentActivity()
                                    val biometricAuthenticator = remember(activity) {
                                        activity?.let(BiometricAuthManager::createAuthenticator)
                                    }

                                    AppLockOverlay(
                                        isReady = true,
                                        appSettings = settings,
                                        onUnlockSuccess = { appLockViewModel.unlock() },
                                        onBiometricClick = {
                                            biometricAuthenticator?.authenticate(
                                                title = "Unlock Expense Tracker",
                                                subtitle = "Verify your biometric to continue.",
                                                negativeButtonText = "Use PIN",
                                                onSuccess = { appLockViewModel.unlock() }
                                            )
                                        },
                                        onForgotPinRecovery = appLockViewModel::disableLock
                                    )
                                }

                                is AppLockState.Unlocked -> {
                                    // Unlocked: Overlay is gone
                                    Box(modifier = Modifier.fillMaxSize())
                                }
                            }
                        }
                    }

                    // 3. Splash Screen (Absolute priority during boot, stays on top of everything)
                    if (!isReady) {
                        SplashOverlay(viewModel = splashViewModel)
                    }
                }
            }
        }
    }

    private fun applyPrivacySettings(
        shouldBlurInRecents: Boolean,
        shouldBlockScreenshots: Boolean
    ) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            setRecentsScreenshotEnabled(!shouldBlurInRecents)
        }

        val shouldUseSecureFlag = shouldBlockScreenshots ||
            (shouldBlurInRecents && Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU)

        if (shouldUseSecureFlag) {
            window.addFlags(WindowManager.LayoutParams.FLAG_SECURE)
        } else {
            window.clearFlags(WindowManager.LayoutParams.FLAG_SECURE)
        }
    }
}
