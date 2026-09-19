package com.mknlabs.expensetracker.feature.smsinbox.data.notification

import android.content.Context
import com.mknlabs.expensetracker.feature.smsinbox.domain.repository.SmsNotificationCleaner
import com.mknlabs.expensetracker.sms.SmsNotificationManager
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Adapts [SmsNotificationCleaner] onto the app's own notification plumbing.
 *
 * A thin delegate on purpose: `SmsNotificationManager` already owns the per-detection
 * notification IDs, so the inbox never has to know how a card is built or where it
 * lives in the shade.
 */
@Singleton
class AndroidSmsNotificationCleaner @Inject constructor(
    @ApplicationContext private val context: Context
) : SmsNotificationCleaner {

    override fun clear(notificationId: Int) {
        SmsNotificationManager.cancel(context, notificationId)
        // The group summary's count and total are derived from the children still in the
        // shade, so it has to be rebuilt or it would keep counting a detection that has
        // already been decided — but only once the cancellation has landed, otherwise
        // the rebuild reads the card back out of a not-yet-updated snapshot.
        SmsNotificationManager.scheduleGroupSummaryRefresh(context)
    }
}
