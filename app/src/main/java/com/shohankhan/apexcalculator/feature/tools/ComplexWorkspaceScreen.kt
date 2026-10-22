package com.shohankhan.apexcalculator.feature.tools

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.shohankhan.apexcalculator.core.designsystem.ApexCard
import com.shohankhan.apexcalculator.core.designsystem.ApexIcons
import com.shohankhan.apexcalculator.core.designsystem.ApexSpacing
import com.shohankhan.apexcalculator.core.designsystem.ApexTextField
import com.shohankhan.apexcalculator.core.designsystem.BannerTone
import com.shohankhan.apexcalculator.core.designsystem.IconAction
import com.shohankhan.apexcalculator.core.designsystem.InfoBanner
import com.shohankhan.apexcalculator.core.designsystem.ValueRow
import com.shohankhan.apexcalculator.core.designsystem.apexColors
import com.shohankhan.apexcalculator.core.domain.toEvalOptions
import com.shohankhan.apexcalculator.core.domain.toFormatSettings
import com.shohankhan.apexcalculator.core.math.CalculatorEngine
import com.shohankhan.apexcalculator.core.math.EvalOutcome
import com.shohankhan.apexcalculator.core.math.Num
import com.shohankhan.apexcalculator.core.math.NumMath
import com.shohankhan.apexcalculator.core.math.ResultFormatter
import com.shohankhan.apexcalculator.core.ui.SettingsStateViewModel
import com.shohankhan.apexcalculator.navigation.SecondaryScaffold

/** Complex-number workspace: exact expression input, rectangular and polar output. */
@Composable
fun ComplexWorkspaceScreen(
    onBack: () -> Unit,
    settingsViewModel: SettingsStateViewModel = viewModel(),
) {
    val settings by settingsViewModel.settings.collectAsStateWithLifecycle()
    val colors = MaterialTheme.apexColors
    val clipboard = LocalClipboardManager.current
    var expression by rememberSaveable { mutableStateOf("(3 + 4i) × (1 − 2i)") }
    var submitted by rememberSaveable { mutableStateOf(false) }

    val formatter = remember(settings) { ResultFormatter(settings.toFormatSettings()) }
    val outcome = remember(submitted, expression, settings) {
        if (!submitted) null else CalculatorEngine.evaluate(
            expression = expression,
            options = settings.toEvalOptions(complexMode = true),
        )
    }

    SecondaryScaffold(
        title = "Complex numbers",
        subtitle = "Rectangular and polar form",
        onBack = onBack,
        actions = {
            val text = when (val current = outcome) {
                is EvalOutcome.Success -> (current.value as? Num.Cpx)?.toPolar(settings.angleMode.name == "DEG")
                    ?: formatter.format(current.value)
                is EvalOutcome.Failure -> current.error.message
                null -> ""
            }
            IconAction(
                icon = ApexIcons.Copy,
                description = "Copy result",
                onClick = { if (text.isNotEmpty()) clipboard.setText(AnnotatedString(text)) },
            )
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
            item {
                ApexCard {
                    Column {
                        ApexTextField(
                            value = expression,
                            onValueChange = {
                                expression = it
                                submitted = false
                            },
                            label = "Expression",
                            supportingText = "Use i for the imaginary unit. Functions and constants are supported.",
                            singleLine = false,
                            minLines = 2,
                        )
                        Spacer(Modifier.height(ApexSpacing.md))
                        Button(
                            onClick = { submitted = true },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(48.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = colors.primary,
                                contentColor = colors.onPrimary,
                            ),
                        ) {
                            Text("Evaluate")
                        }
                    }
                }
            }
            when (val current = outcome) {
                null -> Unit
                is EvalOutcome.Failure -> item {
                    InfoBanner(text = current.error.message, tone = BannerTone.ERROR, title = "Cannot evaluate")
                }
                is EvalOutcome.Success -> {
                    val value = current.value
                    val complex = NumMath.toComplex(value)
                    item {
                        ApexCard {
                            Column {
                                Text(
                                    text = formatter.format(value),
                                    style = MaterialTheme.typography.headlineMedium,
                                    color = colors.primary,
                                )
                                Text(
                                    text = "Rectangular form",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = colors.textSecondary,
                                )
                                Spacer(Modifier.height(ApexSpacing.md))
                                ValueRow("Modulus |z|", formatter.format(Num.of(complex.modulus)))
                                ValueRow("Argument θ", formatter.format(Num.of(complex.argument)))
                                ValueRow("Conjugate", formatter.format(Num.Cpx(complex.conjugate())))
                                ValueRow(
                                    "Polar form",
                                    complex.toPolar(settings.angleMode.name == "DEG"),
                                )
                            }
                        }
                    }
                    formatter.secondary(value)?.let { secondary ->
                        item {
                            InfoBanner(text = secondary, tone = BannerTone.INFO, title = "Exact value")
                        }
                    }
                }
            }
            item {
                Text(
                    text = "Angles follow the ${settings.angleMode.shortLabel} setting. Arguments are reported in the same unit.",
                    style = MaterialTheme.typography.bodySmall,
                    color = colors.textTertiary,
                )
            }
        }
    }
}
