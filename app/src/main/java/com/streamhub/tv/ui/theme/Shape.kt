package com.streamhub.tv.ui.theme

import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Shapes
import androidx.compose.ui.unit.dp

val StreamHubShapes = Shapes(
    extraSmall = RoundedCornerShape(6.dp),
    small = RoundedCornerShape(10.dp),
    medium = RoundedCornerShape(16.dp),
    large = RoundedCornerShape(24.dp),
    extraLarge = RoundedCornerShape(32.dp)
)

// Extra radii used for specific components (channel cards, chips, pill buttons)
object AppRadii {
    val Card = RoundedCornerShape(18.dp)
    val Chip = RoundedCornerShape(50)
    val Sheet = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp)
}
