package com.mkn0079.expensetracker

import android.os.Bundle
import android.os.Build
import android.graphics.Color as AndroidColor
import android.view.WindowManager
import androidx.activity.SystemBarStyle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.getValue
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.compose.ui.graphics.toArgb
import androidx.fragment.app.FragmentActivity
import androidx.lifecycle.lifecycleScope
import com.mkn0079.expensetracker.data.constants.defaultAppSettings
import com.mkn0079.expensetracker.data.local.AppSettingsDataStore
import com.mkn0079.expensetracker.data.local.UserProfileDataStore
import com.mkn0079.expensetracker.data.local.room.ExpenseTrackerDatabaseInitializer
import com.mkn0079.expensetracker.models.defaultUserProfile
import com.mkn0079.expensetracker.ui.theme.BackgroundDark
import com.mkn0079.expensetracker.ui.theme.ExpenseTrackerTheme
import kotlinx.coroutines.async
import kotlinx.coroutines.launch

class MainActivity : FragmentActivity() {
    @Volatile
    private var isLaunchReady = false

    override fun onCreate(savedInstanceState: Bundle?) {
        val splashScreen = installSplashScreen()
        splashScreen.setKeepOnScreenCondition { !isLaunchReady }
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

        lifecycleScope.launch {
            val appSettingsInitialization = async {
                AppSettingsDataStore.initialize(applicationContext)
            }
            val userProfileInitialization = async {
                UserProfileDataStore.initialize(applicationContext)
            }
            val databaseInitialization = async {
                ExpenseTrackerDatabaseInitializer.initialize(applicationContext)
            }

            appSettingsInitialization.await()
            userProfileInitialization.await()
            databaseInitialization.await()
            isLaunchReady = true
        }

        setContent {
            val appSettings by AppSettingsDataStore
                .getAppSettingsFlow(applicationContext)
                .collectAsState(initial = null)
            val userProfile by UserProfileDataStore
                .getUserProfileFlow(applicationContext)
                .collectAsState(initial = defaultUserProfile)

            ExpenseTrackerTheme(darkTheme = appSettings?.darkThemeEnabled ?: defaultAppSettings.darkThemeEnabled) {
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

                    MainScreen(
                        appSettings = settings,
                        userProfile = userProfile
                    )
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
