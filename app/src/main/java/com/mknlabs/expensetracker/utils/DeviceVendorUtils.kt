package com.mknlabs.expensetracker.utils

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.PowerManager
import android.provider.Settings
import java.util.Locale

/**
 * Device-vendor helpers for the Smart SMS Import feature (GEMINI.md: no scattered
 * logic; side-effect-free decision functions are unit-testable on the JVM).
 *
 * Background: on stock Android, `SMS_RECEIVED` broadcasts reach every app holding
 * RECEIVE_SMS. Xiaomi's MIUI is a documented exception — by default it restricts
 * background processing ("Autostart" disabled, aggressive battery management), so
 * the broadcast never wakes the app and SMS detection silently never runs
 * (https://dontkillmyapp.com/xiaomi). This object detects MIUI devices and drives
 * a one-time setup card that deep-links the user to the right settings.
 */
object DeviceVendorUtils {

    /** MIUI Security Center content provider consulted for the Autostart state. */
    const val MIUI_AUTOSTART_PROVIDER = "content://com.miui.securitycenter/autostart"

    /** Provider call method returning whether Autostart is enabled for the package. */
    const val MIUI_AUTOSTART_METHOD = "isAllowAutoStart"

    /**
     * True when the device is Xiaomi-branded (Xiaomi/Redmi/POCO) or reports a MIUI
     * version property (covers vendor forks that rebrand the manufacturer string).
     *
     * Parameters are injectable so the decision is unit-testable; the defaults read
     * the live device state.
     */
    /** Convenience overload reading the live device state (used by ViewModels). */
    fun isMiuiDevice(): Boolean =
        isMiuiDevice(manufacturer = Build.MANUFACTURER, miuiVersionProperty = readMiuiVersionProperty())

    /**
     * Pure, testable variant. [manufacturer] and [miuiVersionProperty] default to the
     * live device state so production callers can use the no-arg overload.
     */
    fun isMiuiDevice(
        manufacturer: String = Build.MANUFACTURER,
        miuiVersionProperty: String? = null
    ): Boolean {
        val brand = manufacturer.lowercase(Locale.ROOT)
        val hasMiuiVersion = !miuiVersionProperty.isNullOrBlank()
        return brand.contains("xiaomi") ||
            brand.contains("redmi") ||
            brand.contains("poco") ||
            hasMiuiVersion
    }

    /**
     * Reads `ro.miui.ui.version.name` via reflection (hidden API). Returns null when
     * unavailable — callers treat that as "not obviously MIUI" for detection.
     */
    fun readMiuiVersionProperty(): String? {
        return try {
            val systemProperties = Class.forName("android.os.SystemProperties")
            val get = systemProperties.getMethod("get", String::class.java)
            get.invoke(null, "ro.miui.ui.version.name") as? String
        } catch (e: Exception) {
            null
        }
    }

    /**
     * Queries MIUI's Security Center provider for the app's Autostart state.
     * Mirrors the approach of the documented XomaDev/MIUI-autostart library
     * (tested on MIUI 10-14) without adding a dependency.
     *
     * Returns:
     * - `true`  → Autostart is enabled (background broadcasts should arrive).
     * - `false` → Autostart is explicitly disabled → guidance card needed.
     * - `null`  → unknown (not MIUI, provider missing, or hidden-API access denied)
     *             → guidance card shown as a safe default.
     */
    fun isMiuiAutostartAllowed(context: Context): Boolean? {
        return try {
            val resolver = context.contentResolver
            val bundle = resolver.call(
                Uri.parse(MIUI_AUTOSTART_PROVIDER),
                MIUI_AUTOSTART_METHOD,
                context.packageName,
                null
            )
            bundle?.getBoolean("result", false)
        } catch (e: Exception) {
            null
        }
    }

    /**
     * Deep link to this app's details screen — the single place where the user can
     * reach MIUI's Autostart toggle, the "Other permissions → SMS" toggle, and the
     * stock Android runtime permissions on every vendor.
     */
    fun appDetailsSettingsIntent(context: Context): Intent =
        Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS, Uri.parse("package:${context.packageName}"))

    /**
     * Battery intent for the MIUI setup card:
     * - If the app already has the battery-optimization exemption → open the
     *   battery-optimization list so the user can still reach MIUI's per-app battery
     *   settings ("No restrictions").
     * - Otherwise → request the exemption (system dialog). This is only ever fired
     *   from an explicit user tap, which keeps it aligned with Play policy guidance.
     */
    fun batteryOptimizationIntent(context: Context): Intent {
        val powerManager = context.getSystemService(Context.POWER_SERVICE) as PowerManager
        val intent = if (powerManager.isIgnoringBatteryOptimizations(context.packageName)) {
            Intent(Settings.ACTION_IGNORE_BATTERY_OPTIMIZATION_SETTINGS)
        } else {
            Intent(
                Settings.ACTION_REQUEST_IGNORE_BATTERY_OPTIMIZATIONS,
                Uri.parse("package:${context.packageName}")
            )
        }
        return intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
    }

    /**
     * Pure decision: show the "grant SMS permission" card when the permission is
     * missing, the one-shot prompt has already been shown (the card replaces the
     * silent forever-off state — it must not race the initial dialog), the user has
     * not dismissed the notice, and this is not a benchmark build (the benchmark
     * harness pre-grants permissions and must keep the Home screen unobstructed for
     * the "See All" journey).
     */
    fun shouldShowSmsPermissionCard(
        smsPermissionGranted: Boolean,
        promptAlreadyShown: Boolean,
        cardDismissed: Boolean,
        benchmarkBuild: Boolean
    ): Boolean = !smsPermissionGranted && promptAlreadyShown && !cardDismissed && !benchmarkBuild

    /**
     * Pure decision: show the MIUI setup card when the device is Xiaomi, SMS
     * detection is permitted (otherwise the permission card takes priority), MIUI
     * Autostart is disabled or could not be verified (`null` → fail open to
     * guidance), the user has not acknowledged it, and this is not a benchmark build.
     */
    fun shouldShowMiuiSetupCard(
        isMiuiDevice: Boolean,
        miuiAutostartAllowed: Boolean?,
        smsPermissionGranted: Boolean,
        acknowledged: Boolean,
        benchmarkBuild: Boolean
    ): Boolean = isMiuiDevice &&
        smsPermissionGranted &&
        miuiAutostartAllowed != true &&
        !acknowledged &&
        !benchmarkBuild
}
