package com.calorieai.app.ui.components

import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.runtime.Composable
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

@Composable
fun rememberFabAwareBottomPadding(
    fabVisible: Boolean = true,
    fabHeight: Dp = 56.dp,
    fabMargin: Dp = 16.dp,
    extraPadding: Dp = 0.dp
): Dp {
    val navigationBottom = WindowInsets.navigationBars.asPaddingValues().calculateBottomPadding()
    val fabBottom = if (fabVisible) fabHeight + fabMargin + 12.dp else 0.dp
    val safeBottom = if (navigationBottom > fabBottom) navigationBottom else fabBottom
    return safeBottom + extraPadding
}
