package com.mknlabs.expensetracker.data.repository

import android.content.Context
import android.util.Log
import com.android.installreferrer.api.InstallReferrerClient
import com.android.installreferrer.api.InstallReferrerStateListener
import com.mknlabs.expensetracker.data.local.PromoReferralStore
import com.mknlabs.expensetracker.monetization.PromoCodeParser
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlin.coroutines.resume

/**
 * Reads the Play Store install referrer once and, when it carries a promo code, stores it
 * for the redeem field. Failures are ignored: organic installs have nothing to capture.
 */
object InstallReferrerCapture {

    private const val TAG = "InstallReferrer"

    suspend fun captureIfNeeded(context: Context) {
        if (PromoReferralStore.wasInstallReferrerCaptured(context)) return
        val appContext = context.applicationContext
        val referrer = runCatching { readReferrer(appContext) }.getOrNull()
        PromoReferralStore.markInstallReferrerCaptured(appContext)
        val code = referrer?.let { PromoCodeParser.fromReferrer(it) }
        if (!code.isNullOrBlank()) {
            PromoReferralStore.savePendingCode(appContext, code)
            Log.d(TAG, "Stored pending promo from install referrer")
        }
    }

    private suspend fun readReferrer(context: Context): String? =
        suspendCancellableCoroutine { cont ->
            val client = InstallReferrerClient.newBuilder(context).build()
            cont.invokeOnCancellation {
                runCatching { client.endConnection() }
            }
            client.startConnection(object : InstallReferrerStateListener {
                override fun onInstallReferrerSetupFinished(responseCode: Int) {
                    val value = if (responseCode == InstallReferrerClient.InstallReferrerResponse.OK) {
                        runCatching { client.installReferrer.installReferrer }.getOrNull()
                    } else {
                        null
                    }
                    runCatching { client.endConnection() }
                    if (cont.isActive) cont.resume(value)
                }

                override fun onInstallReferrerServiceDisconnected() {
                    if (cont.isActive) cont.resume(null)
                }
            })
        }
}
