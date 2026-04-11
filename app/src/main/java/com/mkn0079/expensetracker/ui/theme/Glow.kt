package com.mkn0079.expensetracker.ui.theme


import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.unit.dp

fun Modifier.purpleGlow(): Modifier {

    return this.shadow(
        elevation = 20.dp,
        ambientColor = PurplePrimary,
        spotColor = PurpleAccent
    )
}