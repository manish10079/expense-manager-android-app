package com.mknlabs.expensetracker.utils

import android.content.Context
import android.os.Build
import com.scottyab.rootbeer.RootBeer
import java.util.Locale

/**
 * Device-integrity helpers (security plan Phase 2, Items 7 & 8): root + emulator
 * detection powering a one-time, non-blocking notice on modified devices.
 *
 * Detection is deliberately advisory-only — no feature gating, no hard blocks
 * (custom-ROM users are real users). Side-effect-free decision functions are
 * JVM-unit-testable via injected parameters, mirroring [DeviceVendorUtils].
 */
object DeviceIntegrityUtils {

    /**
     * True when the device looks rooted (su binaries, Magisk/KernelSU paths,
     * test-keys builds, writable system, ...) per RootBeer. Fail-open (false) on
     * any library/native error — this only drives an informational notice, so a
     * detection hiccup must never surface an error to the user.
     */
    fun isRooted(context: Context): Boolean {
        return try {
            RootBeer(context.applicationContext).isRooted()
        } catch (t: Throwable) {
            false
        }
    }

    /** Convenience overload reading the live device state. */
    fun isEmulator(): Boolean =
        isEmulator(
            fingerprint = Build.FINGERPRINT,
            model = Build.MODEL,
            product = Build.PRODUCT,
            hardware = Build.HARDWARE
        )

    /**
     * Pure, testable emulator heuristic (checklist recipe + standard signals):
     * generic fingerprints (Genymotion / legacy AVDs), SDK/emulator model names,
     * `sdk_*` products, and the goldfish/ranchu emulator hardware strings.
     *
     * Substring checks are deliberately broad: a false positive (an odd vendor
     * ROM misread as an emulator) costs only a one-time informational notice,
     * whereas a false negative would defeat the feature's purpose. Detection is
     * non-blocking, so this trade-off is intentional.
     */
    fun isEmulator(
        fingerprint: String = Build.FINGERPRINT,
        model: String = Build.MODEL,
        product: String = Build.PRODUCT,
        hardware: String = Build.HARDWARE
    ): Boolean {
        val f = fingerprint.lowercase(Locale.ROOT)
        val m = model.lowercase(Locale.ROOT)
        val p = product.lowercase(Locale.ROOT)
        val h = hardware.lowercase(Locale.ROOT)
        return f.contains("generic") ||
            m.contains("sdk") ||
            m.contains("emulator") ||
            p.contains("sdk_") ||
            h.contains("goldfish") ||
            h.contains("ranchu")
    }

    /**
     * Pure decision: show the one-time integrity notice when the device is rooted
     * or an emulator, the user has not already acknowledged it, and this is not a
     * benchmark build (the baseline-profile/benchmark harness runs on an emulator
     * and must keep the Home screen unobstructed — same convention as the SMS and
     * MIUI setup cards in [DeviceVendorUtils]).
     */
    fun shouldShowIntegrityNotice(
        isRooted: Boolean,
        isEmulator: Boolean,
        acknowledged: Boolean,
        benchmarkBuild: Boolean
    ): Boolean = (isRooted || isEmulator) && !acknowledged && !benchmarkBuild
}
