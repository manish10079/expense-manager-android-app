package com.mknlabs.expensetracker.feature.smsinbox.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import com.adamglin.PhosphorIcons
import com.adamglin.phosphoricons.Regular
import com.adamglin.phosphoricons.regular.Bell
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.minimumInteractiveComponentSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.TransformOrigin
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.mknlabs.expensetracker.R
import kotlin.math.abs
import kotlin.math.sin

/**
 * Highest unread count rendered in full. Anything above it shows as an overflow badge instead, so a
 * three digit count can never widen the pill past the bell it sits on.
 */
internal const val MAX_SMS_INBOX_BADGE_COUNT = 99

/** How long one ring lasts, in total. Home's entrance animation runs the clock. */
internal const val SMS_INBOX_BELL_RING_DURATION_MS = 2_500

/** How far the bell swings before the decay takes it down. */
internal const val SMS_INBOX_BELL_RING_MAX_DEGREES = 16f

/** Number of swings in one ring. */
internal const val SMS_INBOX_BELL_RING_SWINGS = 5f

/** How much larger the badge grows at the top of a pulse. */
internal const val SMS_INBOX_BADGE_PULSE_MAX = 0.35f

/**
 * The bell's swing, in degrees, at [ringProgress] (0f = the ring just started, 1f = finished).
 *
 * A decaying sine rather than a keyframe table: it starts and ends at exactly 0°, alternates
 * direction, and shrinks by itself — a swing count or amplitude change is one constant, and the
 * whole curve stays testable without a device (see `SmsInboxBellButtonTest`).
 */
internal fun bellRingAngle(ringProgress: Float): Float {
    val progress = ringProgress.coerceIn(0f, 1f)
    if (progress <= 0f || progress >= 1f) return 0f
    return SMS_INBOX_BELL_RING_MAX_DEGREES *
        (1f - progress) *
        sin(TWO_PI * SMS_INBOX_BELL_RING_SWINGS * progress)
}

/**
 * The badge's scale at [ringProgress]: two quick pulses that settle back to exactly 1.
 *
 * Exactly 1 at both ends matters — a resting badge must not be left slightly enlarged, and the
 * pill's size is what keeps the count legible at any font scale.
 */
internal fun badgePulseScale(ringProgress: Float): Float {
    val progress = ringProgress.coerceIn(0f, 1f)
    if (progress <= 0f || progress >= 1f) return 1f
    return 1f + SMS_INBOX_BADGE_PULSE_MAX * abs(sin(TWO_PI * progress)) * (1f - progress)
}

/** One full turn of the swing/pulse curves. */
private const val TWO_PI = 2f * Math.PI.toFloat()

/**
 * True when a change of the badge count means a detection just arrived.
 *
 * Only a rise counts. The count falls every time the user reads a card, and ringing again there
 * would be the app nagging them for tidying up; a fresh detection is always NEW, so it can only
 * ever push the number up. A count that has not moved is a re-emission, not news.
 */
internal fun isNewDetectionArrival(previousCount: Int, newCount: Int): Boolean =
    newCount > previousCount && newCount > 0

/**
 * The number the bell should display, or `null` when no badge must be drawn at all.
 *
 * A zero badge is noise rather than information, so "nothing waiting" collapses to no badge. Unlike
 * the budget/recurring tab badge this is deliberately **not** Pro-gated: the inbox is where a
 * detected bank message is kept even when its notification was dismissed, so hiding the count would
 * hide the very thing the feature exists to protect.
 */
internal fun smsInboxBadgeCount(unreadCount: Int): Int? = unreadCount.takeIf { it > 0 }

/** True when [count] must render as an overflow badge rather than the exact value. */
internal fun isSmsInboxBadgeOverflow(count: Int): Boolean = count > MAX_SMS_INBOX_BADGE_COUNT

/**
 * Bell icon on Home that opens the detected-transaction inbox, with the unread count on it.
 *
 * Sized like [com.mknlabs.expensetracker.feature.home.ui.SettingsButton] (40dp visual, 48dp touch target)
 * so the two sit as peers in the greeting row. The badge is intentionally drawn outside the circular
 * clip — a badge tucked inside the circle has its corner shaved off — and the press indication is
 * off for the same reason the neighbouring settings button has it off: the row is glyph-only, so a
 * ripple would paint a circle around an icon that is not on a surface of its own.
 *
 * Coloured entirely from the theme: the count uses `primary` rather than `error`, because these are
 * informational detections waiting for a decision, not failures.
 *
 * @param ringProgress the ring/alert animation, 0f..1f, with 1f as the resting bell. Driven by the
 *        home screen's entrance animation rather than by this composable, so the swing is timed
 *        with the greeting wave and the settings spin — and so it also plays when the user returns
 *        from the background, which a composition-scoped effect would miss.
 */
@Composable
fun SmsInboxBellButton(
    unreadCount: Int,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    ringProgress: Float = 1f
) {
    val badgeCount = smsInboxBadgeCount(unreadCount)

    // Screen readers get the count as a sentence, never as a bare number floating next to an icon.
    val description = if (badgeCount == null) {
        stringResource(R.string.desc_sms_inbox_bell)
    } else {
        pluralStringResource(R.plurals.desc_sms_inbox_bell_unread, badgeCount, badgeCount)
    }

    Box(
        modifier = modifier
            .size(40.dp)
            .minimumInteractiveComponentSize()
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null,
                onClick = onClick
            )
            .semantics { contentDescription = description },
        contentAlignment = Alignment.Center
    ) {
        Icon(
            imageVector = PhosphorIcons.Regular.Bell,
            // The Box above already carries the label for the whole control.
            contentDescription = null,
            tint = MaterialTheme.colorScheme.primary,
            modifier = Modifier
                .size(26.dp)
                .graphicsLayer {
                    rotationZ = bellRingAngle(ringProgress)
                    // Pivoting near the top makes it swing on its handle, like a real bell,
                    // instead of spinning about its middle.
                    transformOrigin = TransformOrigin(0.5f, 0.08f)
                }
        )

        if (badgeCount != null) {
            SmsInboxCountBadge(
                count = badgeCount,
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .offset(x = 4.dp, y = (-2).dp)
                    .graphicsLayer {
                        // The count pulses while the bell rings, so the eye is drawn to the
                        // number rather than only to the icon.
                        val scale = badgePulseScale(ringProgress)
                        scaleX = scale
                        scaleY = scale
                    }
            )
        }
    }
}

/**
 * The count pill itself.
 *
 * Sized from typography with a minimum diameter instead of a fixed height, so it follows the system
 * font scale without clipping and keeps a one digit and a two digit count the same shape.
 *
 * The number is centred by a [Box] rather than by the text's own padding: a one digit count is
 * narrower than the pill's minimum width, and text laid out inside a wider box sits at the start
 * edge and against the top — which is exactly how a badge ends up looking off-centre. The padding
 * around the wrapping Box is what keeps the digits off the circle's edge.
 */
@Composable
private fun SmsInboxCountBadge(
    count: Int,
    modifier: Modifier = Modifier
) {
    val visibleText = if (isSmsInboxBadgeOverflow(count)) {
        stringResource(R.string.label_tab_count_overflow, MAX_SMS_INBOX_BADGE_COUNT)
    } else {
        stringResource(R.string.label_tab_count_short, count)
    }

    Box(
        modifier = modifier
            .defaultMinSize(minWidth = 18.dp, minHeight = 18.dp)
            .clip(CircleShape)
            .background(MaterialTheme.colorScheme.primary)
            .padding(horizontal = 4.dp, vertical = 1.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = visibleText,
            color = MaterialTheme.colorScheme.onPrimary,
            style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.SemiBold),
            maxLines = 1,
            overflow = TextOverflow.Clip,
            textAlign = TextAlign.Center
        )
    }
}
