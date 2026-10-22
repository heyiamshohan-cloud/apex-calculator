package com.shohankhan.apexcalculator.feature.about

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.shohankhan.apexcalculator.core.designsystem.ApexCard
import com.shohankhan.apexcalculator.core.designsystem.ApexSpacing
import com.shohankhan.apexcalculator.core.designsystem.BannerTone
import com.shohankhan.apexcalculator.core.designsystem.ConfirmDialog
import com.shohankhan.apexcalculator.core.designsystem.InfoBanner
import com.shohankhan.apexcalculator.core.designsystem.KeyButton
import com.shohankhan.apexcalculator.core.designsystem.KeyStyle
import com.shohankhan.apexcalculator.core.designsystem.ValueRow
import com.shohankhan.apexcalculator.core.designsystem.apexColors
import com.shohankhan.apexcalculator.feature.settings.SettingsViewModel
import com.shohankhan.apexcalculator.navigation.SecondaryScaffold

@Composable
fun PrivacyScreen(
    onBack: () -> Unit,
    viewModel: SettingsViewModel = viewModel(),
) {
    val settings by viewModel.settings.collectAsStateWithLifecycle()
    val message by viewModel.message.collectAsStateWithLifecycle()
    val colors = MaterialTheme.apexColors
    var confirmHistory by remember { mutableStateOf(false) }
    var confirmFavorites by remember { mutableStateOf(false) }

    SecondaryScaffold(title = "Privacy & data", subtitle = "No account, no cloud, no tracking", onBack = onBack) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(horizontal = ApexSpacing.md),
            contentPadding = PaddingValues(bottom = ApexSpacing.xxxl),
            verticalArrangement = Arrangement.spacedBy(ApexSpacing.md),
        ) {
            message?.let { text ->
                item { InfoBanner(text = text, tone = BannerTone.SUCCESS, title = "Done") }
            }
            item {
                ApexCard {
                    Column {
                        Text("What the app stores", style = MaterialTheme.typography.titleSmall, color = colors.textPrimary)
                        Spacer(Modifier.height(ApexSpacing.sm))
                        Text(
                            text = "APEX Calculator keeps three kinds of data on this device and nowhere else:\n\n" +
                                "1. Calculation history — expressions, results, source and timestamp.\n" +
                                "2. Favorites — saved formulas and tools.\n" +
                                "3. Settings — theme, accent, angle mode, formatting, retention and the memory register.\n\n" +
                                "History and favorites live in a local Room database; settings live in a local DataStore " +
                                "file. The app does not declare the INTERNET permission, so it cannot transmit this " +
                                "data even if a library tried to.",
                            style = MaterialTheme.typography.bodySmall,
                            color = colors.textSecondary,
                        )
                    }
                }
            }
            item {
                ApexCard {
                    Column {
                        Text("Automatic deletion", style = MaterialTheme.typography.titleSmall, color = colors.textPrimary)
                        Spacer(Modifier.height(ApexSpacing.sm))
                        ValueRow("Current policy", settings.historyRetention.label)
                        ValueRow("Cleanup on launch", "Yes")
                        ValueRow("Background cleanup", "Daily, via WorkManager")
                        Spacer(Modifier.height(ApexSpacing.sm))
                        Text(
                            text = "Expired records are deleted from the database. Favorites are never touched by the " +
                                "cleanup and survive every retention policy.",
                            style = MaterialTheme.typography.bodySmall,
                            color = colors.textSecondary,
                        )
                    }
                }
            }
            item {
                ApexCard {
                    Column {
                        Text("Permissions", style = MaterialTheme.typography.titleSmall, color = colors.textPrimary)
                        Spacer(Modifier.height(ApexSpacing.sm))
                        Text(
                            text = "The app requests no runtime permissions. It has no camera, microphone, location, " +
                                "contacts, storage or network access. Clipboard writes happen only when you tap Copy.",
                            style = MaterialTheme.typography.bodySmall,
                            color = colors.textSecondary,
                        )
                    }
                }
            }
            item {
                ApexCard {
                    Column {
                        Text("Delete your data", style = MaterialTheme.typography.titleSmall, color = colors.textPrimary)
                        Spacer(Modifier.height(ApexSpacing.sm))
                        KeyButton(
                            label = "Delete calculation history",
                            style = KeyStyle.FUNCTION,
                            onClick = { confirmHistory = true },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(46.dp),
                            labelSize = 15,
                            hapticsEnabled = false,
                        )
                        Spacer(Modifier.height(ApexSpacing.sm))
                        KeyButton(
                            label = "Delete favorites",
                            style = KeyStyle.FUNCTION,
                            onClick = { confirmFavorites = true },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(46.dp),
                            labelSize = 15,
                            hapticsEnabled = false,
                        )
                        Spacer(Modifier.height(ApexSpacing.sm))
                        Text(
                            text = "Uninstalling the app removes the database and the settings file as well.",
                            style = MaterialTheme.typography.bodySmall,
                            color = colors.textTertiary,
                        )
                    }
                }
            }
            item {
                Text(
                    text = "Contact: helloiamshohan@gmail.com",
                    style = MaterialTheme.typography.bodySmall,
                    color = colors.textTertiary,
                )
            }
        }
    }

    if (confirmHistory) {
        ConfirmDialog(
            title = "Delete calculation history?",
            message = "This cannot be undone. Favorites and settings are kept.",
            confirmLabel = "Delete",
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
            message = "This cannot be undone. History and settings are kept.",
            confirmLabel = "Delete",
            destructive = true,
            onConfirm = {
                confirmFavorites = false
                viewModel.clearFavorites()
            },
            onDismiss = { confirmFavorites = false },
        )
    }
}
