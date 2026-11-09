package com.mknlabs.expensetracker.widget.overlay

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.mknlabs.expensetracker.core.ui.theme.ExpenseTrackerTheme

@Preview(name = "Type sheet • light", showBackground = true, widthDp = 360, heightDp = 420)
@Composable
private fun OverlayTypeSheetLightPreview() {
    OverlaySheetPreview(dark = false) { OverlayTextContent(onClose = {}) }
}

@Preview(name = "Type sheet • dark", showBackground = true, widthDp = 360, heightDp = 420)
@Composable
private fun OverlayTypeSheetDarkPreview() {
    OverlaySheetPreview(dark = true) { OverlayTextContent(onClose = {}) }
}

@Preview(name = "Talk sheet • light", showBackground = true, widthDp = 360, heightDp = 420)
@Composable
private fun OverlayTalkSheetLightPreview() {
    OverlaySheetPreview(dark = false) {
        OverlayVoiceSheet(
            transcript = "Lunch 250",
            isProcessing = false,
            onCancel = {},
            onDone = {}
        )
    }
}

@Preview(name = "Talk sheet • dark", showBackground = true, widthDp = 360, heightDp = 420)
@Composable
private fun OverlayTalkSheetDarkPreview() {
    OverlaySheetPreview(dark = true) {
        OverlayVoiceSheet(
            transcript = "Lunch 250",
            isProcessing = false,
            onCancel = {},
            onDone = {}
        )
    }
}

@Preview(name = "Scan sheet • light", showBackground = true, widthDp = 360, heightDp = 420)
@Composable
private fun OverlayScanSheetLightPreview() {
    OverlaySheetPreview(dark = false) {
        OverlayScanSheet(onCamera = {}, onPick = {}, onClose = {})
    }
}

@Preview(name = "Scan sheet • dark", showBackground = true, widthDp = 360, heightDp = 420)
@Composable
private fun OverlayScanSheetDarkPreview() {
    OverlaySheetPreview(dark = true) {
        OverlayScanSheet(onCamera = {}, onPick = {}, onClose = {})
    }
}

@Composable
private fun OverlaySheetPreview(
    dark: Boolean,
    sheetColor: Color = if (dark) Color(0xF20B1220) else Color.White.copy(alpha = 0.80f),
    content: @Composable () -> Unit
) {
    ExpenseTrackerTheme(darkTheme = dark) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.Black.copy(alpha = 0.4f)),
            contentAlignment = Alignment.BottomCenter
        ) {
            Surface(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp),
                color = sheetColor,
                tonalElevation = 6.dp
            ) {
                Column(modifier = Modifier.padding(top = 12.dp)) {
                    Box(
                        modifier = Modifier
                            .align(Alignment.CenterHorizontally)
                            .padding(bottom = 8.dp)
                            .width(32.dp)
                            .background(
                                MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.4f),
                                RoundedCornerShape(16.dp)
                            )
                            .padding(vertical = 2.dp)
                    )
                    content()
                }
            }
        }
    }
}
