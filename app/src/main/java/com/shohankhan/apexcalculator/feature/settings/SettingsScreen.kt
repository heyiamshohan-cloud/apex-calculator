package com.shohankhan.apexcalculator.feature.settings

import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.shohankhan.apexcalculator.core.designsystem.ApexCard
import com.shohankhan.apexcalculator.core.designsystem.ApexRadius
import com.shohankhan.apexcalculator.core.designsystem.ApexSpacing
import com.shohankhan.apexcalculator.core.designsystem.ConfirmDialog
import com.shohankhan.apexcalculator.core.designsystem.InfoBanner
import com.shohankhan.apexcalculator.core.designsystem.BannerTone
import com.shohankhan.apexcalculator.core.designsystem.SelectableChip
import com.shohankhan.apexcalculator.core.designsystem.apexColors
import com.shohankhan.apexcalculator.core.domain.model.AccentChoice
import com.shohankhan.apexcalculator.core.domain.model.AngleMode
import com.shohankhan.apexcalculator.core.domain.model.AppSettings
import com.shohankhan.apexcalculator.core.domain.model.NumberFormatMode
import com.shohankhan.apexcalculator.core.domain.model.RetentionPolicy
import com.shohankhan.apexcalculator.core.domain.model.ThemeMode
import com.shohankhan.apexcalculator.navigation.SecondaryScaffold

@Composable
fun SettingsScreen(
    onBack: () -> Unit,
    onOpenPrivacy: () -> Unit,
    viewModel: SettingsViewModel = viewModel(),
) {
    val settings by viewModel.settings.collectAsStateWithLifecycle()
    val message by viewModel.message.collectAsStateWithLifecycle()
    val colors = MaterialTheme.apexColors
    var confirmHistory by remember { mutableStateOf(false) }
    var confirmFavorites by remember { mutableStateOf(false) }
    var confirmReset by remember { mutableStateOf(false) }

    SecondaryScaffold(
        title = "Settings",
        subtitle = "Stored on this device",
        onBack = onBack,
        actions = {
            TextButton(onClick = { confirmReset = true }) { Text("Reset", color = colors.primary) }
        },
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(horizontal = ApexSpacing.md),
            contentPadding = PaddingValues(bottom = ApexSpacing.xxxl),
            verticalArrangement = Arrangement.spacedBy(ApexSpacing.md),
        ) {
            message?.let { text ->
                item {
                    InfoBanner(text = text, tone = BannerTone.SUCCESS, title = "Saved")
                }
            }
            item {
                SettingsSection(title = "Appearance") {
                    ChoiceRow(
                        label = "Theme",
                        options = ThemeMode.entries.map { it.label },
                        selectedIndex = ThemeMode.entries.indexOf(settings.themeMode),
                        onSelect = { index -> viewModel.update { it.copy(themeMode = ThemeMode.entries[index]) } },
                    )
                    Spacer(Modifier.height(ApexSpacing.sm))
                    ChoiceRow(
                        label = "Accent",
                        options = AccentChoice.entries.map { it.label },
                        selectedIndex = AccentChoice.entries.indexOf(settings.accent),
                        onSelect = { index -> viewModel.update { it.copy(accent = AccentChoice.entries[index]) } },
                    )
                    Spacer(Modifier.height(ApexSpacing.sm))
                    ToggleRow(
                        label = "Haptic feedback",
                        description = "Subtle vibration when a key is pressed",
                        checked = settings.hapticsEnabled,
                        onCheckedChange = { value -> viewModel.update { it.copy(hapticsEnabled = value) } },
                    )
                    ToggleRow(
                        label = "Reduced motion",
                        description = "Shortens animations throughout the app",
                        checked = settings.reducedMotion,
                        onCheckedChange = { value -> viewModel.update { it.copy(reducedMotion = value) } },
                    )
                }
            }
            item {
                SettingsSection(title = "Calculation") {
                    ChoiceRow(
                        label = "Angle mode",
                        options = AngleMode.entries.map { it.label },
                        selectedIndex = AngleMode.entries.indexOf(settings.angleMode),
                        onSelect = { index -> viewModel.update { it.copy(angleMode = AngleMode.entries[index]) } },
                    )
                    Spacer(Modifier.height(ApexSpacing.sm))
                    ChoiceRow(
                        label = "Number format",
                        options = NumberFormatMode.entries.map { it.label },
                        selectedIndex = NumberFormatMode.entries.indexOf(settings.numberFormat),
                        onSelect = { index -> viewModel.update { it.copy(numberFormat = NumberFormatMode.entries[index]) } },
                    )
                    Spacer(Modifier.height(ApexSpacing.sm))
                    StepperRow(
                        label = "Decimal places",
                        value = settings.decimalPlaces,
                        range = 0..12,
                        onChange = { value -> viewModel.update { it.copy(decimalPlaces = value) } },
                    )
                    StepperRow(
                        label = "Significant figures",
                        value = settings.significantFigures,
                        range = 1..15,
                        onChange = { value -> viewModel.update { it.copy(significantFigures = value) } },
                    )
                    StepperRow(
                        label = "Scientific notation above",
                        value = settings.scientificThreshold,
                        range = 4..30,
                        onChange = { value -> viewModel.update { it.copy(scientificThreshold = value) } },
                    )
                    ToggleRow(
                        label = "Thousands separators",
                        description = "Group digits in large results",
                        checked = settings.thousandsSeparator,
                        onCheckedChange = { value -> viewModel.update { it.copy(thousandsSeparator = value) } },
                    )
                    ToggleRow(
                        label = "Show exact fractions",
                        description = "Display rational results such as 1/3 when possible",
                        checked = settings.fractionDisplay,
                        onCheckedChange = { value -> viewModel.update { it.copy(fractionDisplay = value) } },
                    )
                }
            }
            item {
                SettingsSection(title = "History") {
                    Text(
                        text = "Automatic cleanup",
                        style = MaterialTheme.typography.bodyMedium,
                        color = colors.textPrimary,
                    )
                    Text(
                        text = "Calculations older than the selected window are deleted on launch and in the background.",
                        style = MaterialTheme.typography.bodySmall,
                        color = colors.textSecondary,
                    )
                    Spacer(Modifier.height(ApexSpacing.sm))
                    Row(
                        modifier = Modifier.horizontalScroll(rememberScrollState()),
                        horizontalArrangement = Arrangement.spacedBy(ApexSpacing.sm),
                    ) {
                        RetentionPolicy.entries.forEach { policy ->
                            SelectableChip(
                                label = policy.label,
                                selected = policy == settings.historyRetention,
                                onClick = { viewModel.update { it.copy(historyRetention = policy) } },
                            )
                        }
                    }
                    Spacer(Modifier.height(ApexSpacing.sm))
                    ToggleRow(
                        label = "Save calculations automatically",
                        description = "Successful evaluations are stored in history",
                        checked = settings.autoSaveHistory,
                        onCheckedChange = { value -> viewModel.update { it.copy(autoSaveHistory = value) } },
                    )
                    ToggleRow(
                        label = "Remember memory and last answer",
                        description = "Restores the M register after restarting the app",
                        checked = settings.persistMemory,
                        onCheckedChange = { value -> viewModel.update { it.copy(persistMemory = value) } },
                    )
                }
            }
            item {
                SettingsSection(title = "Data") {
                    Text(
                        text = "All data stays on this device. Nothing is uploaded, and the app has no network permission.",
                        style = MaterialTheme.typography.bodySmall,
                        color = colors.textSecondary,
                    )
                    Spacer(Modifier.height(ApexSpacing.sm))
                    Row(horizontalArrangement = Arrangement.spacedBy(ApexSpacing.sm)) {
                        TextButton(onClick = { confirmHistory = true }) {
                            Text("Delete history", color = colors.error)
                        }
                        TextButton(onClick = { confirmFavorites = true }) {
                            Text("Delete favorites", color = colors.error)
                        }
                        TextButton(onClick = onOpenPrivacy) { Text("Privacy", color = colors.primary) }
                    }
                }
            }
            item {
                Text(
                    text = "APEX Calculator · version ${appVersion()} (${appBuild()})",
                    style = MaterialTheme.typography.bodySmall,
                    color = colors.textTertiary,
                )
            }
        }
    }

    if (confirmHistory) {
        ConfirmDialog(
            title = "Delete calculation history?",
            message = "Every stored calculation will be removed. Favorites and settings are kept.",
            confirmLabel = "Delete history",
            destructive = true,
            onConfirm = {
                confirmHistory = false
                viewModel.clearHistory()
            },
            onDismiss = { confirmHistory = false },
        )
    }
    if (confirmFavorites) {
        ConfirmDialog(
            title = "Delete favorites?",
            message = "Saved formulas and tools will be removed from this device.",
            confirmLabel = "Delete favorites",
            destructive = true,
            onConfirm = {
                confirmFavorites = false
                viewModel.clearFavorites()
            },
            onDismiss = { confirmFavorites = false },
        )
    }
    if (confirmReset) {
        ConfirmDialog(
            title = "Restore default settings?",
            message = "Theme, formatting, angle mode and retention return to their defaults. History and favorites are kept.",
            confirmLabel = "Restore defaults",
            onConfirm = {
                confirmReset = false
                viewModel.resetSettings()
            },
            onDismiss = { confirmReset = false },
        )
    }
}

@Composable
private fun SettingsSection(
    title: String,
    content: @Composable () -> Unit,
) {
    val colors = MaterialTheme.apexColors
    Column {
        Text(
            text = title,
            style = MaterialTheme.typography.labelLarge,
            color = colors.textSecondary,
            modifier = Modifier.padding(start = ApexSpacing.xs, bottom = ApexSpacing.xs),
        )
        ApexCard {
            Column { content() }
        }
    }
}

@Composable
private fun ChoiceRow(
    label: String,
    options: List<String>,
    selectedIndex: Int,
    onSelect: (Int) -> Unit,
) {
    val colors = MaterialTheme.apexColors
    Column {
        Text(label, style = MaterialTheme.typography.bodyMedium, color = colors.textPrimary)
        Spacer(Modifier.height(ApexSpacing.xs))
        Row(
            modifier = Modifier.horizontalScroll(rememberScrollState()),
            horizontalArrangement = Arrangement.spacedBy(ApexSpacing.sm),
        ) {
            options.forEachIndexed { index, option ->
                SelectableChip(
                    label = option,
                    selected = index == selectedIndex,
                    onClick = { onSelect(index) },
                )
            }
        }
    }
}

@Composable
private fun ToggleRow(
    label: String,
    description: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
) {
    val colors = MaterialTheme.apexColors
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = ApexSpacing.xs),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(label, style = MaterialTheme.typography.bodyMedium, color = colors.textPrimary)
            Text(description, style = MaterialTheme.typography.bodySmall, color = colors.textSecondary)
        }
        Spacer(Modifier.width(ApexSpacing.sm))
        Switch(checked = checked, onCheckedChange = onCheckedChange)
    }
}

@Composable
private fun StepperRow(
    label: String,
    value: Int,
    range: IntRange,
    onChange: (Int) -> Unit,
) {
    val colors = MaterialTheme.apexColors
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = ApexSpacing.xs),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodyMedium,
            color = colors.textPrimary,
            modifier = Modifier.weight(1f),
        )
        TextButton(
            onClick = { onChange((value - 1).coerceIn(range)) },
            enabled = value > range.first,
        ) { Text("−", style = MaterialTheme.typography.titleMedium) }
        Text(
            text = value.toString(),
            style = MaterialTheme.typography.titleSmall,
            color = colors.primary,
            modifier = Modifier.width(32.dp),
            textAlign = androidx.compose.ui.text.style.TextAlign.Center,
        )
        TextButton(
            onClick = { onChange((value + 1).coerceIn(range)) },
            enabled = value < range.last,
        ) { Text("+", style = MaterialTheme.typography.titleMedium) }
    }
}

@Composable
private fun appVersion(): String {
    val context = androidx.compose.ui.platform.LocalContext.current
    return remember(context) {
        runCatching {
            context.packageManager.getPackageInfo(context.packageName, 0).versionName ?: "1.0.0"
        }.getOrDefault("1.0.0")
    }
}

@Composable
private fun appBuild(): String {
    val context = androidx.compose.ui.platform.LocalContext.current
    return remember(context) {
        runCatching {
            val info = context.packageManager.getPackageInfo(context.packageName, 0)
            @Suppress("DEPRECATION")
            info.versionCode.toString()
        }.getOrDefault("1")
    }
}
