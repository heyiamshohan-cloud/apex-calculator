package com.shohankhan.apexcalculator.navigation

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.foundation.layout.RowScope
import com.shohankhan.apexcalculator.core.designsystem.ApexScreen
import com.shohankhan.apexcalculator.core.designsystem.apexColors

/** Standard chrome for screens that sit above the tab level. */
@Composable
fun SecondaryScaffold(
    title: String,
    subtitle: String? = null,
    onBack: (() -> Unit)? = null,
    actions: @Composable RowScope.() -> Unit = {},
    bottomBar: @Composable () -> Unit = {},
    snackbarHostState: SnackbarHostState = remember { SnackbarHostState() },
    content: @Composable (PaddingValues) -> Unit,
) {
    ApexScreen(
        title = title,
        subtitle = subtitle,
        onBack = onBack,
        actions = actions,
        bottomBar = bottomBar,
        snackbarHostState = snackbarHostState,
        content = content,
    )
}

@Composable
fun ScreenBackgroundColor() = MaterialTheme.apexColors.background
