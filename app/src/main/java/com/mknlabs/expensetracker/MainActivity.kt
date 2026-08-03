package com.mknlabs.expensetracker

import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.view.WindowManager
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.animation.AnimatedContent
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
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import com.mknlabs.expensetracker.data.local.AppSettingsDataStore
import com.mknlabs.expensetracker.data.local.UserProfileDataStore
import com.mknlabs.expensetracker.models.AppThemeMode
import com.mknlabs.expensetracker.models.defaultUserProfile
import com.mknlabs.expensetracker.notifications.NotificationHelper
import com.mknlabs.expensetracker.sms.ParsedSms
import com.mknlabs.expensetracker.sms.SmsNotificationManager
import com.mknlabs.expensetracker.sms.SmsNotificationManager.toParsedSms
import com.mknlabs.expensetracker.ui.screens.SplashOverlay
import com.mknlabs.expensetracker.ui.screens.MaintenanceScreen
import com.mknlabs.expensetracker.ui.screens.UpdateRequiredScreen
import android.net.Uri
import com.mknlabs.expensetracker.ui.theme.ExpenseTrackerTheme
import com.mknlabs.expensetracker.utils.BiometricAuthManager
import com.mknlabs.expensetracker.utils.findFragmentActivity
import com.mknlabs.expensetracker.utils.ThemePreferenceSync
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext
import com.mknlabs.expensetracker.ui.viewmodels.InitTask
import com.mknlabs.expensetracker.ui.viewmodels.SplashViewModel
import com.mknlabs.expensetracker.ui.viewmodels.AppLockViewModel
import com.mknlabs.expensetracker.ui.viewmodels.AppLockState
import com.mknlabs.expensetracker.ui.components.AppLockOverlay
import com.mknlabs.expensetracker.ui.theme.AppLockLoadingBackground
import dagger.hilt.android.AndroidEntryPoint

import androidx.activity.SystemBarStyle
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue

import com.mknlabs.expensetracker.monetization.AdsCoordinator
import com.google.firebase.auth.FirebaseAuth
import com.mknlabs.expensetracker.domain.repository.AuthRepository
import com.mknlabs.expensetracker.ui.viewmodels.AuthViewModel
import com.mknlabs.expensetracker.ui.viewmodels.MonetizationViewModel
import com.mknlabs.expensetracker.models.PinVisualMode
import com.mknlabs.expensetracker.models.UserTier
import javax.inject.Inject

@AndroidEntryPoint
class MainActivity : AppCompatActivity() {

    @Inject
    lateinit var adsCoordinator: AdsCoordinator

    @Inject
    lateinit var authRepository: AuthRepository

    private val splashViewModel: SplashViewModel by viewModels()
    private val appLockViewModel: AppLockViewModel by viewModels()
    
    // AuthViewModel is provided at the Composable level to avoid activity scope leaks,
    // but we can use it to handle incoming intents.
    private var currentIntent by mutableStateOf<Intent?>(null)

    override fun onCreate(savedInstanceState: Bundle?) {
        android.util.Log.d("AUTH", "MainActivity: onCreate")
        // Task 2: Apply Theme BEFORE super.onCreate()
        val syncTheme = ThemePreferenceSync.getTheme(this)
        val mode = when (syncTheme) {
            "LIGHT" -> AppCompatDelegate.MODE_NIGHT_NO
            "DARK" -> AppCompatDelegate.MODE_NIGHT_YES
            else -> AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM
        }
        AppCompatDelegate.setDefaultNightMode(mode)

        val splashScreen = installSplashScreen()
        
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
        
        currentIntent = intent

        // Monitor Firebase Auth State
        com.google.firebase.auth.FirebaseAuth.getInstance().addAuthStateListener { auth ->
            android.util.Log.d("AUTH", "Firebase Auth State Change: Current user = ${auth.currentUser?.uid}")
        }

        // Initialize AdMob with Privacy Flow (UMP)
        adsCoordinator.initPrivacyFlow(this) {
            // Ads are ready to be loaded or SDK is initialized
        }

        enableEdgeToEdge()

        setContent {
            AppRoot(splashViewModel, appLockViewModel, currentIntent)
        }
    }

    override fun onStart() {
        super.onStart()
        val user = com.google.firebase.auth.FirebaseAuth.getInstance().currentUser
        android.util.Log.d("AUTH", "MainActivity: onStart, user = ${user?.uid}")
    }

    override fun onResume() {
        super.onResume()
        android.util.Log.d("AUTH", "MainActivity: onResume")
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        currentIntent = intent
    }

    @Composable
    private fun AppRoot(
        splashViewModel: SplashViewModel,
        appLockViewModel: AppLockViewModel,
        intent: Intent?
    ) {
        val isReady by splashViewModel.isReady.collectAsState()
        val isUpdateRequired by splashViewModel.isUpdateRequired.collectAsState()
        val isUnderMaintenance by splashViewModel.isUnderMaintenance.collectAsState()
        val appLockState by appLockViewModel.state.collectAsState()
        val recoveryPerformed by appLockViewModel.recoveryPerformed.collectAsState()
        val context = LocalContext.current
        val activity = context.findFragmentActivity()
        
        // Pass a dummy AuthViewModel if needed for logic, but we get the real one in MainScreen
        val authViewModel: AuthViewModel = androidx.hilt.navigation.compose.hiltViewModel()
        val monetizationViewModel: MonetizationViewModel = androidx.hilt.navigation.compose.hiltViewModel()
        val effectiveUserTier by monetizationViewModel.userTier.collectAsState()
        
        val initialNavDestination = intent?.getStringExtra(NotificationHelper.EXTRA_NAV_DESTINATION)

        // Smart SMS Import "Open" action prefill (plan §8) — amount + note draft.
        val initialAddTransactionAmount = intent?.getStringExtra(SmsNotificationManager.EXTRA_OPEN_AMOUNT)
        val initialAddTransactionNote = intent?.getStringExtra(SmsNotificationManager.EXTRA_OPEN_NOTE)

        // Smart SMS Import "Change" action payload (plan §8 / Phase 4) — the
        // full ParsedSms rides in PendingIntent extras and is consumed by the
        // lightweight Change bottom sheet. Null for every other launch path.
        val initialParsedSms: ParsedSms? = intent?.toParsedSms()

        // Handle Magic Link Intent
        LaunchedEffect(intent) {
            intent?.data?.let { data ->
                val link = data.toString()
                if (authRepository.isSignInWithEmailLink(link)) {
                    authViewModel.completeMagicLinkSignIn(link)
                }
            }
        }

        val appSettings by AppSettingsDataStore
            .getAppSettingsFlow(context)
            .collectAsState(initial = null)
        val userProfile by UserProfileDataStore
            .getUserProfileFlow(context)
            .collectAsState(initial = defaultUserProfile)
            
        val systemDarkTheme = isSystemInDarkTheme()
        val syncTheme = remember { ThemePreferenceSync.getTheme(context) }
        
        val darkTheme = if (appSettings == null) {
            when (syncTheme) {
                "LIGHT" -> false
                "DARK" -> true
                else -> systemDarkTheme
            }
        } else {
            when (appSettings!!.themeMode) {
                AppThemeMode.SYSTEM -> systemDarkTheme
                AppThemeMode.LIGHT -> false
                AppThemeMode.DARK -> true
            }
        }

        DisposableEffect(darkTheme) {
            enableEdgeToEdge(
                statusBarStyle = SystemBarStyle.auto(
                    android.graphics.Color.TRANSPARENT,
                    android.graphics.Color.TRANSPARENT,
                    detectDarkMode = { darkTheme }
                ),
                navigationBarStyle = SystemBarStyle.auto(
                    android.graphics.Color.TRANSPARENT,
                    android.graphics.Color.TRANSPARENT,
                    detectDarkMode = { darkTheme }
                )
            )
            onDispose { }
        }

        ExpenseTrackerTheme(darkTheme = darkTheme) {
            Box(modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background)
            ) {
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
                    when {
                        isUnderMaintenance -> {
                            MaintenanceScreen()
                        }
                        isUpdateRequired -> {
                            UpdateRequiredScreen(onUpdateClick = {
                                try {
                                    val intent = Intent(Intent.ACTION_VIEW, Uri.parse("market://details?id=${packageName}"))
                                    startActivity(intent)
                                } catch (e: Exception) {
                                    val intent = Intent(Intent.ACTION_VIEW, Uri.parse("https://play.google.com/store/apps/details?id=${packageName}"))
                                    startActivity(intent)
                                }
                            })
                        }
                        isReady && appSettings != null -> {
                            val settings = appSettings!!
                            
                            // Layer 1: Main App Content (Always in composition to preserve NavController state)
                            MainScreen(
                                isReady = true,
                                appSettings = settings,
                                userProfile = userProfile,
                                initialNavDestination = initialNavDestination,
                                initialAddTransactionAmount = initialAddTransactionAmount,
                                initialAddTransactionNote = initialAddTransactionNote,
                                initialParsedSms = initialParsedSms,
                                isRecoveryPerformed = recoveryPerformed,
                                onRecoveryConsumed = { appLockViewModel.consumeRecovery() }
                            )

                            // Layer 2: App Lock Overlay
                            AnimatedContent(
                                targetState = appLockState,
                                transitionSpec = {
                                    if (targetState is AppLockState.Unlocked) {
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
                                        Box(
                                            modifier = Modifier
                                                .fillMaxSize()
                                                .background(AppLockLoadingBackground)
                                        )
                                    }

                                    is AppLockState.Locked -> {
                                        val biometricAuthenticator = remember(activity) {
                                            activity?.let(BiometricAuthManager::createAuthenticator)
                                        }

                                        AppLockOverlay(
                                            isReady = true,
                                            appSettings = settings,
                                            onUnlockSuccess = { appLockViewModel.unlock() },
                                            autoTriggerBiometricOnShow = true,
                                            onBiometricClick = {
                                                biometricAuthenticator?.authenticate(
                                                    title = "Unlock Expense Tracker",
                                                    subtitle = "Verify your biometric to continue.",
                                                    negativeButtonText = "Use PIN",
                                                    onSuccess = { appLockViewModel.unlock() }
                                                )
                                            },
                                            onForgotPinRecovery = appLockViewModel::disableLock,
                                            pinVisualMode = if (effectiveUserTier == UserTier.PREMIUM) PinVisualMode.PRO_ANIMATED else PinVisualMode.NORMAL,
                                            scrambledPinKeypadEnabled = effectiveUserTier == UserTier.PREMIUM
                                        )
                                    }

                                    is AppLockState.Unlocked -> {
                                        // Empty Box when unlocked to reveal MainScreen underneath
                                        Box(Modifier.fillMaxSize())
                                    }
                                }
                            }
                        }
                        !isReady -> {
                            SplashOverlay(viewModel = splashViewModel)
                        }
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
