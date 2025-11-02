package com.example.freshcookapp.ui.component

import androidx.compose.foundation.layout.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

@Composable
fun ScreenContainer(
    modifier: Modifier = Modifier,
    horizontalPadding: Dp = 20.dp,
    verticalPadding: Dp = 0.dp,
    content: @Composable ColumnScope.() -> Unit
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(
                start = horizontalPadding,
                end = horizontalPadding,
                top = verticalPadding,
                bottom = verticalPadding
            )
    ) {
        content()
    }
}