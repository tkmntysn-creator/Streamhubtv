package com.streamhub.tv.ui.theme

import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Shapes
import androidx.compose.ui.unit.dp

val StreamHubShapes = Shapes(
    extraSmall = RoundedCornerShape(8.dp),
    small = RoundedCornerShape(12.dp),
    medium = RoundedCornerShape(18.dp),
    large = RoundedCornerShape(26.dp),
    extraLarge = RoundedCornerShape(34.dp)
)

object AppRadii {
    val Card = RoundedCornerShape(20.dp)
    val Chip = RoundedCornerShape(50)
    val Sheet = RoundedCornerShape(topStart = 26.dp, topEnd = 26.dp)
    val GlassBar = RoundedCornerShape(28.dp)
}
