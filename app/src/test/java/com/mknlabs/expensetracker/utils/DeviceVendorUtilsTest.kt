package com.mknlabs.expensetracker.utils

import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class DeviceVendorUtilsTest {

    // --- isMiuiDevice -----------------------------------------------------

    @Test
    fun xiaomi_manufacturer_is_miui() {
        assertTrue(DeviceVendorUtils.isMiuiDevice(manufacturer = "Xiaomi", miuiVersionProperty = null))
    }

    @Test
    fun redmi_manufacturer_is_miui() {
        assertTrue(DeviceVendorUtils.isMiuiDevice(manufacturer = "Redmi", miuiVersionProperty = null))
    }

    @Test
    fun poco_manufacturer_is_miui() {
        assertTrue(DeviceVendorUtils.isMiuiDevice(manufacturer = "POCO", miuiVersionProperty = null))
    }

    @Test
    fun miui_version_property_detects_rebranded_vendors() {
        assertTrue(DeviceVendorUtils.isMiuiDevice(manufacturer = "samsung", miuiVersionProperty = "V14.0.2"))
    }

    @Test
    fun non_miui_manufacturer_without_property_is_not_miui() {
        assertFalse(DeviceVendorUtils.isMiuiDevice(manufacturer = "samsung", miuiVersionProperty = null))
        assertFalse(DeviceVendorUtils.isMiuiDevice(manufacturer = "google", miuiVersionProperty = null))
        assertFalse(DeviceVendorUtils.isMiuiDevice(manufacturer = "oppo", miuiVersionProperty = ""))
    }

    // --- shouldShowSmsPermissionCard ---------------------------------------

    @Test
    fun permission_card_hidden_when_permission_granted() {
        assertFalse(DeviceVendorUtils.shouldShowSmsPermissionCard(smsPermissionGranted = true, promptAlreadyShown = true, cardDismissed = false, benchmarkBuild = false))
    }

    @Test
    fun permission_card_shown_when_denied_after_prompt_and_not_dismissed() {
        assertTrue(DeviceVendorUtils.shouldShowSmsPermissionCard(smsPermissionGranted = false, promptAlreadyShown = true, cardDismissed = false, benchmarkBuild = false))
    }

    @Test
    fun permission_card_hidden_before_prompt_shown() {
        // The card must not race the initial one-shot permission dialog.
        assertFalse(DeviceVendorUtils.shouldShowSmsPermissionCard(smsPermissionGranted = false, promptAlreadyShown = false, cardDismissed = false, benchmarkBuild = false))
    }

    @Test
    fun permission_card_hidden_when_dismissed() {
        assertFalse(DeviceVendorUtils.shouldShowSmsPermissionCard(smsPermissionGranted = false, promptAlreadyShown = true, cardDismissed = true, benchmarkBuild = false))
    }

    @Test
    fun permission_card_hidden_in_benchmark_build() {
        assertFalse(DeviceVendorUtils.shouldShowSmsPermissionCard(smsPermissionGranted = false, promptAlreadyShown = true, cardDismissed = false, benchmarkBuild = true))
    }

    // --- shouldShowMiuiSetupCard -------------------------------------------

    @Test
    fun miui_card_shown_when_autostart_unknown() {
        assertTrue(DeviceVendorUtils.shouldShowMiuiSetupCard(
            isMiuiDevice = true, miuiAutostartAllowed = null,
            smsPermissionGranted = true, acknowledged = false, benchmarkBuild = false
        ))
    }

    @Test
    fun miui_card_shown_when_autostart_disabled() {
        assertTrue(DeviceVendorUtils.shouldShowMiuiSetupCard(
            isMiuiDevice = true, miuiAutostartAllowed = false,
            smsPermissionGranted = true, acknowledged = false, benchmarkBuild = false
        ))
    }

    @Test
    fun miui_card_hidden_when_autostart_enabled() {
        assertFalse(DeviceVendorUtils.shouldShowMiuiSetupCard(
            isMiuiDevice = true, miuiAutostartAllowed = true,
            smsPermissionGranted = true, acknowledged = false, benchmarkBuild = false
        ))
    }

    @Test
    fun miui_card_hidden_when_permission_missing() {
        assertFalse(DeviceVendorUtils.shouldShowMiuiSetupCard(
            isMiuiDevice = true, miuiAutostartAllowed = null,
            smsPermissionGranted = false, acknowledged = false, benchmarkBuild = false
        ))
    }

    @Test
    fun miui_card_hidden_when_acknowledged() {
        assertFalse(DeviceVendorUtils.shouldShowMiuiSetupCard(
            isMiuiDevice = true, miuiAutostartAllowed = null,
            smsPermissionGranted = true, acknowledged = true, benchmarkBuild = false
        ))
    }

    @Test
    fun miui_card_hidden_on_non_miui_device() {
        assertFalse(DeviceVendorUtils.shouldShowMiuiSetupCard(
            isMiuiDevice = false, miuiAutostartAllowed = null,
            smsPermissionGranted = true, acknowledged = false, benchmarkBuild = false
        ))
    }

    @Test
    fun miui_card_hidden_in_benchmark_build() {
        assertFalse(DeviceVendorUtils.shouldShowMiuiSetupCard(
            isMiuiDevice = true, miuiAutostartAllowed = null,
            smsPermissionGranted = true, acknowledged = false, benchmarkBuild = true
        ))
    }
}
