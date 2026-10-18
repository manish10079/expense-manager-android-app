package com.mknlabs.expensetracker.core.ui.theme


import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Shapes
import androidx.compose.ui.unit.dp

val Shapes = Shapes(

    small = RoundedCornerShape(12.dp),

    medium = RoundedCornerShape(20.dp),

    large = RoundedCornerShape(28.dp)
)

/**
 * The 16dp radius the Edit Profile mockup draws its cards with — and its Save Changes button
 * with, which is why the button reads the card's shape rather than a pill of its own. Kept out
 * of [Shapes]: these are the Material roles for the app at large, and this one belongs to the
 * mockup's cards.
 */
val CardShape16 = RoundedCornerShape(16.dp)