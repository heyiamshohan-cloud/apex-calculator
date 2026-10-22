package com.shohankhan.apexcalculator.feature.about

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.shohankhan.apexcalculator.core.designsystem.ApexCard
import com.shohankhan.apexcalculator.core.designsystem.ApexSpacing
import com.shohankhan.apexcalculator.core.designsystem.ValueRow
import com.shohankhan.apexcalculator.core.designsystem.apexColors
import com.shohankhan.apexcalculator.navigation.SecondaryScaffold

private data class LicenseEntry(val name: String, val license: String, val usage: String)

private val licenses = listOf(
    LicenseEntry("AndroidX Core, Activity, Lifecycle", "Apache License 2.0", "Runtime"),
    LicenseEntry("Jetpack Compose UI, Material 3, Animation", "Apache License 2.0", "Runtime"),
    LicenseEntry("Navigation Compose", "Apache License 2.0", "Runtime"),
    LicenseEntry("Room persistence", "Apache License 2.0", "Runtime"),
    LicenseEntry("DataStore Preferences", "Apache License 2.0", "Runtime"),
    LicenseEntry("WorkManager", "Apache License 2.0", "Runtime"),
    LicenseEntry("Kotlin standard library and kotlinx.coroutines", "Apache License 2.0", "Runtime"),
    LicenseEntry("JUnit 4 and kotlinx-coroutines-test", "EPL 1.0 / Apache 2.0", "Tests only"),
)

@Composable
fun AboutScreen(
    onBack: () -> Unit,
    onOpenPrivacy: () -> Unit,
) {
    val colors = MaterialTheme.apexColors
    val context = androidx.compose.ui.platform.LocalContext.current
    val info = remember(context) {
        runCatching { context.packageManager.getPackageInfo(context.packageName, 0) }.getOrNull()
    }
    val versionName = info?.versionName ?: "1.0.0"
    val versionCode = info?.let {
        @Suppress("DEPRECATION")
        it.versionCode.toString()
    } ?: "1"

    SecondaryScaffold(title = "About", subtitle = "APEX Calculator", onBack = onBack) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(horizontal = ApexSpacing.md),
            contentPadding = PaddingValues(bottom = ApexSpacing.xxxl),
            verticalArrangement = Arrangement.spacedBy(ApexSpacing.md),
        ) {
            item {
                ApexCard {
                    Column {
                        Text("APEX Calculator", style = MaterialTheme.typography.headlineSmall, color = colors.textPrimary)
                        Spacer(Modifier.height(ApexSpacing.xs))
                        Text(
                            text = "A premium offline calculator for everyday arithmetic, science, finance, algebra and data analysis.",
                            style = MaterialTheme.typography.bodyMedium,
                            color = colors.textSecondary,
                        )
                        Spacer(Modifier.height(ApexSpacing.md))
                        ValueRow("Version", versionName)
                        ValueRow("Build", versionCode)
                        ValueRow("Package", "com.shohankhan.apexcalculator")
                    }
                }
            }
            item {
                ApexCard {
                    Column {
                        Text("Developer", style = MaterialTheme.typography.titleSmall, color = colors.textPrimary)
                        Spacer(Modifier.height(ApexSpacing.sm))
                        ValueRow("Name", "Shohan Khan")
                        ValueRow("Email", "helloiamshohan@gmail.com")
                    }
                }
            }
            item {
                ApexCard {
                    Column {
                        Text("How results are produced", style = MaterialTheme.typography.titleSmall, color = colors.textPrimary)
                        Spacer(Modifier.height(ApexSpacing.sm))
                        Text(
                            text = "Every result is computed on this device by the APEX math engine: a hand-written lexer, " +
                                "parser and tree-walking evaluator with exact rational arithmetic, arbitrary-precision " +
                                "integers and complex numbers. No JavaScript engine, WebView or remote service is used, " +
                                "and the app does not request the INTERNET permission.",
                            style = MaterialTheme.typography.bodySmall,
                            color = colors.textSecondary,
                        )
                    }
                }
            }
            item {
                ApexCard {
                    Column {
                        Text("Percentage convention", style = MaterialTheme.typography.titleSmall, color = colors.textPrimary)
                        Spacer(Modifier.height(ApexSpacing.sm))
                        Text(
                            text = "A percentage that follows a plus or minus sign is relative to the value on its left: " +
                                "200 + 10% = 220 and 200 − 10% = 180. Anywhere else a percentage is a fraction: " +
                                "200 × 15% = 30 and 15% of 200 = 30. The same rule applies on every screen.",
                            style = MaterialTheme.typography.bodySmall,
                            color = colors.textSecondary,
                        )
                    }
                }
            }
            item {
                ApexCard {
                    Column {
                        Text("Third-party software", style = MaterialTheme.typography.titleSmall, color = colors.textPrimary)
                        Spacer(Modifier.height(ApexSpacing.xs))
                        Text(
                            text = "Only permissive, commercially compatible libraries are used. No analytics, advertising, " +
                                "tracking or telemetry SDK is included.",
                            style = MaterialTheme.typography.bodySmall,
                            color = colors.textSecondary,
                        )
                        Spacer(Modifier.height(ApexSpacing.sm))
                        licenses.forEach { entry ->
                            Column(modifier = Modifier.padding(vertical = ApexSpacing.xxs)) {
                                Text(entry.name, style = MaterialTheme.typography.bodySmall, color = colors.textPrimary)
                                Text(
                                    text = "${entry.license} · ${entry.usage}",
                                    style = MaterialTheme.typography.bodySmall.copy(fontSize = 11.sp),
                                    color = colors.textTertiary,
                                )
                            }
                        }
                    }
                }
            }
            item {
                ApexCard(onClick = onOpenPrivacy) {
                    Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text("Privacy & data", style = MaterialTheme.typography.titleSmall, color = colors.textPrimary)
                            Text(
                                text = "What is stored, where it lives and how to delete it.",
                                style = MaterialTheme.typography.bodySmall,
                                color = colors.textSecondary,
                            )
                        }
                        Text("›", color = colors.textTertiary)
                    }
                }
            }
            item {
                Text(
                    text = "Copyright © 2026 Shohan Khan. All rights reserved.",
                    style = MaterialTheme.typography.bodySmall,
                    color = colors.textTertiary,
                )
            }
        }
    }
}
