package com.mknlabs.expensetracker.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import com.mknlabs.expensetracker.ui.shimmerEffect
import com.mknlabs.expensetracker.ui.theme.standardCardGradient

/**
 * A shimmer placeholder for Native Ads that mimics the layout of a TransactionCard.
 * Ensures UI stability while ads are loading and maintains the "Fintech Premium" look.
 */
@Composable
fun NativeAdShimmer() {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(20.dp))
            .background(standardCardGradient())
            .border(
                width = 1.dp,
                color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.65f),
                shape = RoundedCornerShape(20.dp)
            )
            .padding(vertical = 14.dp, horizontal = 16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Icon placeholder (Increased to 52dp)
        Box(
            modifier = Modifier
                .size(52.dp)
                .clip(CircleShape)
                .shimmerEffect()
        )

        Spacer(modifier = Modifier.width(14.dp))

        Column(
            modifier = Modifier.weight(1f)
        ) {
            // Headline placeholder
            Box(
                modifier = Modifier
                    .fillMaxWidth(0.7f)
                    .height(16.dp)
                    .clip(RoundedCornerShape(4.dp))
                    .shimmerEffect()
            )

            Spacer(modifier = Modifier.height(6.dp))

            // Body placeholder
            Box(
                modifier = Modifier
                    .fillMaxWidth(0.9f)
                    .height(12.dp)
                    .clip(RoundedCornerShape(4.dp))
                    .shimmerEffect()
            )
        }

        Spacer(modifier = Modifier.width(12.dp))

        // CTA Button placeholder
        Box(
            modifier = Modifier
                .width(80.dp)
                .height(34.dp)
                .clip(RoundedCornerShape(17.dp))
                .shimmerEffect()
        )
    }
}

@androidx.compose.ui.tooling.preview.Preview(showBackground = true)
@Composable
fun NativeAdShimmerLightPreview() {
    com.mknlabs.expensetracker.ui.theme.ExpenseTrackerTheme(darkTheme = false) {
        androidx.compose.material3.Surface {
            Box(modifier = Modifier.padding(16.dp)) {
                NativeAdShimmer()
            }
        }
    }
}

@androidx.compose.ui.tooling.preview.Preview(showBackground = true)
@Composable
fun NativeAdShimmerDarkPreview() {
    com.mknlabs.expensetracker.ui.theme.ExpenseTrackerTheme(darkTheme = true) {
        androidx.compose.material3.Surface {
            Box(modifier = Modifier.padding(16.dp)) {
                NativeAdShimmer()
            }
        }
    }
}
