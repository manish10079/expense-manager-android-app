package com.mkn0079.expensetracker

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.os.Build
import android.graphics.Color as AndroidColor
import android.view.WindowManager
import androidx.activity.SystemBarStyle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.compose.ui.graphics.toArgb
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.ui.Modifier
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeOut
import androidx.compose.animation.core.tween
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.fragment.app.FragmentActivity
import com.mkn0079.expensetracker.data.constants.defaultAppSettings
import com.mkn0079.expensetracker.data.local.AppSettingsDataStore
import com.mkn0079.expensetracker.data.local.UserProfileDataStore
import com.mkn0079.expensetracker.models.defaultUserProfile
import com.mkn0079.expensetracker.notifications.NotificationHelper
import com.mkn0079.expensetracker.ui.screens.SplashOverlay
import com.mkn0079.expensetracker.ui.theme.BackgroundDark
import com.mkn0079.expensetracker.ui.theme.ExpenseTrackerTheme
import com.mkn0079.expensetracker.ui.viewmodels.SplashViewModel
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : FragmentActivity() {

    private val splashViewModel: SplashViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        val splashScreen = installSplashScreen()
        
        // Let the system splash dismiss quickly so our custom overlay can show progress
        splashScreen.setKeepOnScreenCondition { false }
        
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

        applySystemBarColors()

        enableEdgeToEdge(
            statusBarStyle = SystemBarStyle.dark(BackgroundDark.toArgb()),
            navigationBarStyle = SystemBarStyle.dark(BackgroundDark.toArgb())
        )

        NotificationHelper.createNotificationChannels(this)
        checkAndRequestNotificationPermission()

        setContent {
            AppRoot(splashViewModel, intent)
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
    private fun AppRoot(viewModel: SplashViewModel, intent: Intent) {
        val isReady by viewModel.isReady.collectAsState()
        val context = applicationContext
        
        val initialNavDestination = intent.getStringExtra(NotificationHelper.EXTRA_NAV_DESTINATION)

        // Observe settings for theme and privacy
        val appSettings by AppSettingsDataStore
            .getAppSettingsFlow(context)
            .collectAsState(initial = null)
        val userProfile by UserProfileDataStore
            .getUserProfileFlow(context)
            .collectAsState(initial = defaultUserProfile)

        ExpenseTrackerTheme(darkTheme = appSettings?.darkThemeEnabled ?: defaultAppSettings.darkThemeEnabled) {
            Box(modifier = Modifier.fillMaxSize()) {
                // Main content renders underneath once settings are available
                if (appSettings != null) {
                    val settings = appSettings!!

                    LaunchedEffect(
                        settings.blurInRecentsEnabled,
                        settings.screenshotProtectionEnabled
                    ) {
                        applyPrivacySettings(
                            shouldBlurInRecents = settings.blurInRecentsEnabled,
                            shouldBlockScreenshots = settings.screenshotProtectionEnabled
                        )
                    }

                    MainScreen(
                        isReady = isReady,
                        appSettings = settings,
                        userProfile = userProfile,
                        initialNavDestination = initialNavDestination
                    )
                }

                // Splash overlay on top
                AnimatedVisibility(
                    visible = !isReady,
                    exit = fadeOut(
                        animationSpec = tween(durationMillis = 600)
                    )
                ) {
                    SplashOverlay(viewModel = viewModel)
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

    private fun applySystemBarColors() {
        window.statusBarColor = AndroidColor.BLACK
        window.navigationBarColor = AndroidColor.BLACK

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            window.isStatusBarContrastEnforced = false
            window.isNavigationBarContrastEnforced = false
        }
    }
}
