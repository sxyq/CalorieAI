package com.calorieai.app.ui.components

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

@Composable
fun FabAwareListScaffold(
    modifier: Modifier = Modifier,
    fabVisible: Boolean = true,
    fabHeight: Dp = 56.dp,
    fabMargin: Dp = 16.dp,
    listExtraBottomPadding: Dp = 0.dp,
    containerColor: Color = Color.Unspecified,
    topBar: @Composable () -> Unit = {},
    floatingActionButton: @Composable () -> Unit = {},
    content: @Composable (scaffoldPadding: PaddingValues, listBottomSafePadding: Dp) -> Unit
) {
    val listBottomSafePadding = rememberFabAwareBottomPadding(
        fabVisible = fabVisible,
        fabHeight = fabHeight,
        fabMargin = fabMargin,
        extraPadding = listExtraBottomPadding
    )

    Scaffold(
        modifier = modifier,
        containerColor = containerColor,
        topBar = topBar,
        floatingActionButton = floatingActionButton
    ) { scaffoldPadding ->
        content(scaffoldPadding, listBottomSafePadding)
    }
}
