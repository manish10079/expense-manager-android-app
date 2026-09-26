package com.mknlabs.expensetracker.core.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.AlertDialogDefaults
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.unit.dp
import com.mknlabs.expensetracker.core.ui.theme.isDark
import com.mknlabs.expensetracker.core.ui.theme.accentInk
import com.mknlabs.expensetracker.core.ui.theme.sheet

/** The spec's corner for a dialog's buttons and for the fields inside it. */
private val ActionShape = RoundedCornerShape(16.dp)

/** The spec's corner for a dialog itself, which is the card corner. */
private val DialogShape = RoundedCornerShape(24.dp)

/**
 * The shared chrome of a dialog, so that every dialog the light redesign touches reads as
 * the same white card. Each member falls back to what Material gave the dialog in dark,
 * because this pass is light-only.
 */
object AppDialogDefaults {

    /** The dialog's corner: the spec's card corner in light, Material's own in dark. */
    @Composable
    fun shape(): Shape =
        if (MaterialTheme.colorScheme.isDark) AlertDialogDefaults.shape else DialogShape

    /**
     * The dialog's container. Material's default is a tinted surface, and a tinted surface
     * is exactly what the light redesign removes, so light takes the spec's white.
     */
    @Composable
    fun containerColor(): Color =
        if (MaterialTheme.colorScheme.isDark) AlertDialogDefaults.containerColor
        else MaterialTheme.colorScheme.sheet

    /** The corner of an input field inside a dialog, per the spec's 16dp fields. */
    @Composable
    fun fieldShape(): Shape =
        if (MaterialTheme.colorScheme.isDark) OutlinedTextFieldDefaults.shape else ActionShape
}

/**
 * The primary action of a dialog: a filled brand-purple button on the spec's corner in
 * light, and the text-only action these dialogs have always used in dark.
 */
@Composable
fun AppDialogConfirmButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true
) {
    if (MaterialTheme.colorScheme.isDark) {
        // The bare TextButton takes colorScheme.accentInk, and in dark that is #7B61FF:
        // 3.95:1 on the dialog surface (#1E1E23), under the 4.5:1 this label needs.
        // accentInk is #9E84FF in dark, which clears it at 5.69:1, and is the same ink
        // the keypad hands its operators. In light the two are the same colour anyway.
        TextButton(
            onClick = onClick,
            modifier = modifier,
            enabled = enabled,
            colors = ButtonDefaults.textButtonColors(
                contentColor = MaterialTheme.colorScheme.accentInk
            )
        ) { Text(text) }
    } else {
        Button(
            onClick = onClick,
            modifier = modifier,
            enabled = enabled,
            shape = ActionShape
        ) {
            Text(text)
        }
    }
}

/**
 * The secondary action of a dialog: white with a purple edge and a purple label in light,
 * and the text-only action it has always been in dark. See [AppDialogConfirmButton].
 */
@Composable
fun AppDialogDismissButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true
) {
    if (MaterialTheme.colorScheme.isDark) {
        // The bare TextButton takes colorScheme.accentInk, and in dark that is #7B61FF:
        // 3.95:1 on the dialog surface (#1E1E23), under the 4.5:1 this label needs.
        // accentInk is #9E84FF in dark, which clears it at 5.69:1, and is the same ink
        // the keypad hands its operators. In light the two are the same colour anyway.
        TextButton(
            onClick = onClick,
            modifier = modifier,
            enabled = enabled,
            colors = ButtonDefaults.textButtonColors(
                contentColor = MaterialTheme.colorScheme.accentInk
            )
        ) { Text(text) }
    } else {
        OutlinedButton(
            onClick = onClick,
            modifier = modifier,
            enabled = enabled,
            shape = ActionShape,
            border = BorderStroke(1.dp, MaterialTheme.colorScheme.accentInk),
            colors = ButtonDefaults.outlinedButtonColors(
                contentColor = MaterialTheme.colorScheme.accentInk
            )
        ) {
            Text(text)
        }
    }
}
