package com.example.project.ui.theme

import androidx.compose.runtime.compositionLocalOf
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

data class DocuSpacing(
    val tiny: Dp = 4.dp,
    val small: Dp = 8.dp,
    val medium: Dp = 16.dp,
    val large: Dp = 24.dp,
    val huge: Dp = 32.dp
)

val LocalSpacing = compositionLocalOf { DocuSpacing() }
