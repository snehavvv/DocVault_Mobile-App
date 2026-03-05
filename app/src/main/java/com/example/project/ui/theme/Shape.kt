package com.example.project.ui.theme

import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Shapes
import androidx.compose.ui.unit.dp

val Shapes = Shapes(
    extraSmall = RoundedCornerShape(4.dp),
    small = RoundedCornerShape(8.dp),
    medium = RoundedCornerShape(16.dp), // Industry standard for utility cards
    large = RoundedCornerShape(20.dp),  // Buttons and prominent elements
    extraLarge = RoundedCornerShape(28.dp)
)
