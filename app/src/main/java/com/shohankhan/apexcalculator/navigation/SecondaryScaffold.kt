package com.shohankhan.apexcalculator.navigation

import androidx.compose.foundation.layout.RowScope
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.foundation.layout.PaddingValues
import com.shohankhan.apexcalculator.core.designsystem.ApexScreen

/** Standard chrome for screens opened above the tab level. */
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
