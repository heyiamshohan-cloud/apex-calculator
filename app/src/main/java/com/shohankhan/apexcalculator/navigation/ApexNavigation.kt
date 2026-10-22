package com.shohankhan.apexcalculator.navigation

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.selected
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.shohankhan.apexcalculator.core.designsystem.ApexIcons
import com.shohankhan.apexcalculator.core.designsystem.ApexRadius
import com.shohankhan.apexcalculator.core.designsystem.ApexSpacing
import com.shohankhan.apexcalculator.core.designsystem.ApexTopBar
import com.shohankhan.apexcalculator.core.designsystem.DividerLine
import com.shohankhan.apexcalculator.core.designsystem.IconAction
import com.shohankhan.apexcalculator.core.designsystem.apexColors
import com.shohankhan.apexcalculator.core.domain.model.ApexTab

fun ApexTab.icon(): ImageVector = when (this) {
    ApexTab.STANDARD -> ApexIcons.Calculator
    ApexTab.SCIENTIFIC -> ApexIcons.Scientific
    ApexTab.FORMULA -> ApexIcons.Formula
    ApexTab.QUICKSOLVE -> ApexIcons.QuickSolve
    ApexTab.PROGRAMMER -> ApexIcons.Programmer
    ApexTab.TOOLS -> ApexIcons.Tools
}

fun ApexTab.shortLabel(): String = when (this) {
    ApexTab.STANDARD -> "Standard"
    ApexTab.SCIENTIFIC -> "Science"
    ApexTab.FORMULA -> "Formula"
    ApexTab.QUICKSOLVE -> "Solve"
    ApexTab.PROGRAMMER -> "Program"
    ApexTab.TOOLS -> "Tools"
}

@Composable
fun ApexBottomNavigation(
    currentTab: ApexTab,
    onNavigate: (ApexTab) -> Unit,
    modifier: Modifier = Modifier,
) {
    val colors = MaterialTheme.apexColors
    Surface(color = colors.surface, modifier = modifier) {
        Column(Modifier.navigationBarsPadding()) {
            DividerLine()
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(64.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                ApexTab.entries.forEach { tab ->
                    BottomNavItem(
                        tab = tab,
                        selected = tab == currentTab,
                        onClick = { onNavigate(tab) },
                        modifier = Modifier.weight(1f),
                    )
                }
            }
        }
    }
}

@Composable
private fun BottomNavItem(
    tab: ApexTab,
    selected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val colors = MaterialTheme.apexColors
    Column(
        modifier = modifier
            .fillMaxHeight()
            .clickable(role = Role.Tab, onClick = onClick)
            .semantics { this.selected = selected }
            .padding(vertical = ApexSpacing.xs),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
    ) {
        Box(
            modifier = Modifier
                .width(42.dp)
                .height(26.dp)
                .clip(RoundedCornerShape(ApexRadius.pill))
                .background(if (selected) colors.primaryContainer else androidx.compose.ui.graphics.Color.Transparent),
            contentAlignment = Alignment.Center,
        ) {
            Icon(
                imageVector = tab.icon(),
                contentDescription = null,
                tint = if (selected) colors.primary else colors.textTertiary,
                modifier = Modifier.size(19.dp),
            )
        }
        Spacer(Modifier.height(2.dp))
        Text(
            text = tab.shortLabel(),
            fontSize = 10.sp,
            lineHeight = 12.sp,
            maxLines = 1,
            softWrap = false,
            overflow = TextOverflow.Ellipsis,
            textAlign = TextAlign.Center,
            color = if (selected) colors.primary else colors.textTertiary,
        )
    }
}

@Composable
fun ApexNavigationRail(
    currentTab: ApexTab,
    onNavigate: (ApexTab) -> Unit,
    modifier: Modifier = Modifier,
) {
    val colors = MaterialTheme.apexColors
    Surface(color = colors.surface, modifier = modifier.fillMaxHeight()) {
        Column(
            modifier = Modifier
                .width(104.dp)
                .fillMaxHeight()
                .statusBarsPadding()
                .navigationBarsPadding()
                .padding(vertical = ApexSpacing.sm),
            verticalArrangement = Arrangement.spacedBy(ApexSpacing.xxs),
        ) {
            ApexTab.entries.forEach { tab ->
                val selected = tab == currentTab
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = ApexSpacing.xs)
                        .height(56.dp)
                        .clip(RoundedCornerShape(ApexRadius.md))
                        .background(if (selected) colors.primaryContainer else androidx.compose.ui.graphics.Color.Transparent)
                        .clickable(role = Role.Tab, onClick = { onNavigate(tab) })
                        .semantics { this.selected = selected }
                        .padding(horizontal = ApexSpacing.sm),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Icon(
                        imageVector = tab.icon(),
                        contentDescription = null,
                        tint = if (selected) colors.primary else colors.textTertiary,
                        modifier = Modifier.size(20.dp),
                    )
                    Spacer(Modifier.width(ApexSpacing.sm))
                    Text(
                        text = tab.shortLabel(),
                        style = MaterialTheme.typography.labelMedium,
                        color = if (selected) colors.primary else colors.textSecondary,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                    )
                }
            }
        }
    }
}

@Composable
fun TopBarOverflow(
    onOpenSettings: () -> Unit,
    onOpenAbout: () -> Unit,
    onOpenPrivacy: () -> Unit,
) {
    var expanded by remember { mutableStateOf(false) }
    Box {
        IconAction(
            icon = ApexIcons.More,
            description = "More options",
            onClick = { expanded = true },
        )
        DropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
            DropdownMenuItem(
                text = { Text("Settings") },
                onClick = {
                    expanded = false
                    onOpenSettings()
                },
            )
            DropdownMenuItem(
                text = { Text("About APEX Calculator") },
                onClick = {
                    expanded = false
                    onOpenAbout()
                },
            )
            DropdownMenuItem(
                text = { Text("Privacy & data") },
                onClick = {
                    expanded = false
                    onOpenPrivacy()
                },
            )
        }
    }
}

@Composable
fun TabScaffold(
    tab: ApexTab,
    onNavigate: (ApexTab) -> Unit,
    onOpenHistory: () -> Unit,
    onOpenFavorites: () -> Unit,
    onOpenSettings: () -> Unit,
    onOpenAbout: () -> Unit,
    onOpenPrivacy: () -> Unit,
    subtitle: String? = null,
    actions: @Composable RowScope.() -> Unit = {},
    snackbarHostState: SnackbarHostState = remember { SnackbarHostState() },
    content: @Composable (PaddingValues) -> Unit,
) {
    val colors = MaterialTheme.apexColors
    BoxWithConstraints(modifier = Modifier.fillMaxSize().background(colors.background)) {
        val wide = maxWidth >= 720.dp
        val topBar: @Composable () -> Unit = {
            ApexTopBar(
                title = tab.label,
                subtitle = subtitle,
                actions = {
                    actions()
                    IconAction(ApexIcons.History, "Calculation history", onOpenHistory)
                    IconAction(ApexIcons.StarOutline, "Favorites", onOpenFavorites)
                    TopBarOverflow(onOpenSettings, onOpenAbout, onOpenPrivacy)
                },
            )
        }
        if (wide) {
            Row(modifier = Modifier.fillMaxSize()) {
                ApexNavigationRail(currentTab = tab, onNavigate = onNavigate)
                Scaffold(
                    containerColor = colors.background,
                    topBar = topBar,
                    snackbarHost = { SnackbarHost(snackbarHostState) },
                ) { padding -> content(padding) }
            }
        } else {
            Scaffold(
                containerColor = colors.background,
                topBar = topBar,
                bottomBar = { ApexBottomNavigation(currentTab = tab, onNavigate = onNavigate) },
                snackbarHost = { SnackbarHost(snackbarHostState) },
            ) { padding -> content(padding) }
        }
    }
}
