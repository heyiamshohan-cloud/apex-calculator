package com.shohankhan.apexcalculator.feature.tools

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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.rememberScrollState
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
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.unit.dp
import com.shohankhan.apexcalculator.core.designsystem.ApexCard
import com.shohankhan.apexcalculator.core.designsystem.ApexIcons
import com.shohankhan.apexcalculator.core.designsystem.ApexSpacing
import com.shohankhan.apexcalculator.core.designsystem.ApexTextField
import com.shohankhan.apexcalculator.core.designsystem.BannerTone
import com.shohankhan.apexcalculator.core.designsystem.IconAction
import com.shohankhan.apexcalculator.core.designsystem.InfoBanner
import com.shohankhan.apexcalculator.core.designsystem.SelectableChip
import com.shohankhan.apexcalculator.core.designsystem.ValueRow
import com.shohankhan.apexcalculator.core.designsystem.apexColors
import com.shohankhan.apexcalculator.core.math.AngleUnit
import com.shohankhan.apexcalculator.core.math.CalculatorEngine
import com.shohankhan.apexcalculator.core.math.ComplexNumber
import com.shohankhan.apexcalculator.core.math.EvalOptions
import com.shohankhan.apexcalculator.core.math.EvalOutcome
import com.shohankhan.apexcalculator.core.math.FormatSettings
import com.shohankhan.apexcalculator.core.math.Num
import com.shohankhan.apexcalculator.core.math.NumMath
import com.shohankhan.apexcalculator.core.math.ResultFormatter
import com.shohankhan.apexcalculator.navigation.SecondaryScaffold

private enum class ComplexOp(val label: String) {
    ADD("z₁ + z₂"),
    SUBTRACT("z₁ − z₂"),
    MULTIPLY("z₁ × z₂"),
    DIVIDE("z₁ ÷ z₂"),
    POWER("z₁ ^ z₂"),
}

/** Complex-number workspace with rectangular and polar output. */
@Composable
fun ComplexScreen(onBack: () -> Unit) {
    val colors = MaterialTheme.apexColors
    val clipboard = LocalClipboardManager.current
    var firstText by rememberSaveable { mutableStateOf("3 + 4i") }
    var secondText by rememberSaveable { mutableStateOf("1 - 2i") }
    var operationName by rememberSaveable { mutableStateOf(ComplexOp.MULTIPLY.name) }
    var outcome by remember { mutableStateOf<Pair<ComplexNumber, String>?>(null) }
    var errorText by remember { mutableStateOf<String?>(null) }
    val operation = ComplexOp.entries.firstOrNull { it.name == operationName } ?: ComplexOp.ADD
    val formatter = remember { ResultFormatter(FormatSettings()) }

    fun evaluate(text: String): ComplexNumber? {
        return when (val result = CalculatorEngine.evaluate(text, EvalOptions(complexMode = true))) {
            is EvalOutcome.Success -> NumMath.toComplex(result.value)
            is EvalOutcome.Failure -> {
                errorText = result.error.message
                null
            }
        }
    }

    fun compute() {
        outcome = null
        errorText = null
        val a = evaluate(firstText) ?: return
        val b = evaluate(secondText) ?: return
        val value = when (operation) {
            ComplexOp.ADD -> a + b
            ComplexOp.SUBTRACT -> a - b
            ComplexOp.MULTIPLY -> a * b
            ComplexOp.DIVIDE -> {
                if (b.modulus < ComplexNumber.EPSILON) {
                    errorText = "Division by zero is undefined"
                    return
                }
                a / b
            }
            ComplexOp.POWER -> a.pow(b)
        }
        outcome = value to formatter.format(Num.Cpx(value))
    }

    SecondaryScaffold(
        title = "Complex numbers",
        subtitle = "Rectangular and polar form",
        onBack = onBack,
        actions = {
            IconAction(
                icon = ApexIcons.Copy,
                description = "Copy result",
                onClick = {
                    val value = outcome?.first
                    if (value != null) clipboard.setText(AnnotatedString(value.toPolar(false)))
                },
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
                        Text("Operation", style = MaterialTheme.typography.titleSmall, color = colors.textPrimary)
                        Spacer(Modifier.height(ApexSpacing.sm))
                        Row(
                            modifier = Modifier.horizontalScroll(rememberScrollState()),
                            horizontalArrangement = Arrangement.spacedBy(ApexSpacing.sm),
                        ) {
                            ComplexOp.entries.forEach { entry ->
                                SelectableChip(
                                    label = entry.label,
                                    selected = entry == operation,
                                    onClick = { operationName = entry.name },
                                )
                            }
                        }
                        Spacer(Modifier.height(ApexSpacing.md))
                        ApexTextField(
                            value = firstText,
                            onValueChange = { firstText = it },
                            label = "z₁",
                            supportingText = "For example 3 + 4i",
                            singleLine = true,
                        )
                        Spacer(Modifier.height(ApexSpacing.sm))
                        ApexTextField(
                            value = secondText,
                            onValueChange = { secondText = it },
                            label = "z₂",
                            supportingText = "For example 1 - 2i",
                            singleLine = true,
                        )
                        Spacer(Modifier.height(ApexSpacing.md))
                        com.shohankhan.apexcalculator.core.designsystem.KeyButton(
                            label = "Compute",
                            style = com.shohankhan.apexcalculator.core.designsystem.KeyStyle.ACCENT,
                            onClick = ::compute,
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(48.dp),
                            labelSize = 16,
                            hapticsEnabled = false,
                        )
                    }
                }
            }
            errorText?.let { message ->
                item { InfoBanner(text = message, tone = BannerTone.ERROR, title = "Cannot compute") }
            }
            outcome?.let { (value, text) ->
                item {
                    ApexCard {
                        Column {
                            Text(
                                text = text,
                                style = MaterialTheme.typography.headlineMedium.copy(fontFamily = FontFamily.Monospace),
                                color = colors.primary,
                            )
                            Text("Rectangular form", style = MaterialTheme.typography.labelSmall, color = colors.textSecondary)
                            Spacer(Modifier.height(ApexSpacing.md))
                            ValueRow("Modulus |z|", formatter.format(Num.of(value.modulus)))
                            ValueRow("Argument θ", formatter.format(Num.of(value.argument)))
                            ValueRow("Conjugate", formatter.format(Num.Cpx(value.conjugate())))
                            ValueRow("Polar form", value.toPolar(false))
                            ValueRow("Polar form (degrees)", value.toPolar(true))
                        }
                    }
                }
            }
            item {
                Text(
                    text = "Arguments are reported in radians; the degree form is shown beside it.",
                    style = MaterialTheme.typography.bodySmall,
                    color = colors.textTertiary,
                )
            }
        }
    }
}
