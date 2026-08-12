package com.mknlabs.expensetracker.utils

import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class DeviceIntegrityUtilsTest {

    // --- isEmulator ---------------------------------------------------------

    @Test
    fun standard_avd_fingerprint_is_emulator() {
        assertTrue(
            DeviceIntegrityUtils.isEmulator(
                fingerprint = "google/sdk_gphone64_x86_64/emu64xa:13/TE1A.220922.019/11045064:userdebug/dev-keys",
                model = "sdk_gphone64_x86_64",
                product = "sdk_gphone64_x86_64",
                hardware = "ranchu"
            )
        )
    }

    @Test
    fun genymotion_generic_fingerprint_is_emulator() {
        assertTrue(
            DeviceIntegrityUtils.isEmulator(
                fingerprint = "generic/vbox86p/vbox86p:5.0/LRX21M/buildbot11031842:userdebug/test-keys",
                model = "sdk",
                product = "vbox86p",
                hardware = "vbox86"
            )
        )
    }

    @Test
    fun android_sdk_model_is_emulator() {
        assertTrue(
            DeviceIntegrityUtils.isEmulator(
                fingerprint = "google/sdk_gphone_x86/generic_x86:12/SE1A.220307.006/8551283:userdebug/dev-keys",
                model = "Android SDK built for x86",
                product = "sdk_gphone_x86",
                hardware = "ranchu"
            )
        )
    }

    @Test
    fun legacy_goldfish_hardware_is_emulator() {
        assertTrue(
            DeviceIntegrityUtils.isEmulator(
                fingerprint = "google/foo/foo:9/PQ3A/123:user/release-keys",
                model = "Foo",
                product = "foo",
                hardware = "goldfish"
            )
        )
    }

    @Test
    fun emulator_keyword_in_model_is_emulator() {
        assertTrue(
            DeviceIntegrityUtils.isEmulator(
                fingerprint = "acme/acme/acme:13/TP1A/1:user/release-keys",
                model = "acme emulator",
                product = "acme",
                hardware = "acme"
            )
        )
    }

    @Test
    fun physical_pixel_device_is_not_emulator() {
        assertFalse(
            DeviceIntegrityUtils.isEmulator(
                fingerprint = "google/sunfish/sunfish:13/TP1A.220624.014/8849329:user/release-keys",
                model = "Pixel 4a",
                product = "sunfish",
                hardware = "sunfish"
            )
        )
    }

    @Test
    fun physical_samsung_device_is_not_emulator() {
        assertFalse(
            DeviceIntegrityUtils.isEmulator(
                fingerprint = "samsung/o1sxxx/o1sxxx:13/TP1A.220624.014/o1sxxx.001:user/release-keys",
                model = "SM-G991B",
                product = "o1sxxx",
                hardware = "exynos2100"
            )
        )
    }

    @Test
    fun physical_xiaomi_device_is_not_emulator() {
        assertFalse(
            DeviceIntegrityUtils.isEmulator(
                fingerprint = "Xiaomi/alioth/alioth:13/TKQ1.220829.002/V14.0.5.0.TKHMIXM:user/release-keys",
                model = "M2012K11AC",
                product = "alioth",
                hardware = "qcom"
            )
        )
    }

    // --- shouldShowIntegrityNotice -----------------------------------------

    @Test
    fun notice_shown_when_rooted_and_not_acknowledged() {
        assertTrue(
            DeviceIntegrityUtils.shouldShowIntegrityNotice(
                isRooted = true,
                isEmulator = false,
                acknowledged = false,
                benchmarkBuild = false
            )
        )
    }

    @Test
    fun notice_shown_when_emulator_and_not_acknowledged() {
        assertTrue(
            DeviceIntegrityUtils.shouldShowIntegrityNotice(
                isRooted = false,
                isEmulator = true,
                acknowledged = false,
                benchmarkBuild = false
            )
        )
    }

    @Test
    fun notice_hidden_when_clean_device() {
        assertFalse(
            DeviceIntegrityUtils.shouldShowIntegrityNotice(
                isRooted = false,
                isEmulator = false,
                acknowledged = false,
                benchmarkBuild = false
            )
        )
    }

    @Test
    fun notice_hidden_when_acknowledged() {
        assertFalse(
            DeviceIntegrityUtils.shouldShowIntegrityNotice(
                isRooted = true,
                isEmulator = false,
                acknowledged = true,
                benchmarkBuild = false
            )
        )
    }

    @Test
    fun notice_hidden_in_benchmark_build() {
        assertFalse(
            DeviceIntegrityUtils.shouldShowIntegrityNotice(
                isRooted = true,
                isEmulator = true,
                acknowledged = false,
                benchmarkBuild = true
            )
        )
    }
}
