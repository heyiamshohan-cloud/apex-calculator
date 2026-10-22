package com.shohankhan.apexcalculator.feature.scientific

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.shohankhan.apexcalculator.core.designsystem.ApexCard
import com.shohankhan.apexcalculator.core.designsystem.ApexIcons
import com.shohankhan.apexcalculator.core.designsystem.ApexRadius
import com.shohankhan.apexcalculator.core.designsystem.ApexSpacing
import com.shohankhan.apexcalculator.core.designsystem.ApexTextField
import com.shohankhan.apexcalculator.core.designsystem.BannerTone
import com.shohankhan.apexcalculator.core.designsystem.IconAction
import com.shohankhan.apexcalculator.core.designsystem.InfoBanner
import com.shohankhan.apexcalculator.core.designsystem.KeyButton
import com.shohankhan.apexcalculator.core.designsystem.KeyStyle
import com.shohankhan.apexcalculator.core.designsystem.SelectableChip
import com.shohankhan.apexcalculator.core.designsystem.apexColors
import com.shohankhan.apexcalculator.core.domain.model.ApexTab
import com.shohankhan.apexcalculator.navigation.TabScaffold

@Composable
fun ScientificScreen(
    onNavigate: (ApexTab) -> Unit = {},
    onOpenHistory: () -> Unit = {},
    onOpenFavorites: () -> Unit = {},
    onOpenSettings: () -> Unit = {},
    onOpenAbout: () -> Unit = {},
    onOpenPrivacy: () -> Unit = {},
    viewModel: ScientificViewModel = viewModel(),
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val colors = MaterialTheme.apexColors
    val formatter = viewModel.formatter()
    val clipboard = LocalClipboardManager.current
    val displayValue = if (state.justEvaluated) state.committed else state.preview
    val displayText = displayValue?.let { formatter.format(it) }.orEmpty()
    val secondary = displayValue?.let { formatter.secondary(it) }

    TabScaffold(
        tab = ApexTab.SCIENTIFIC,
        onNavigate = onNavigate,
        onOpenHistory = onOpenHistory,
        onOpenFavorites = onOpenFavorites,
        onOpenSettings = onOpenSettings,
        onOpenAbout = onOpenAbout,
        onOpenPrivacy = onOpenPrivacy,
        subtitle = "${state.angleMode.shortLabel}${if (state.complexMode) " · complex" else ""}",
        actions = {
            IconAction(
                icon = ApexIcons.Sigma,
                description = "Calculus tools",
                onClick = viewModel::openCalculus,
            )
            IconAction(
                icon = ApexIcons.Copy,
                description = "Copy",
                onClick = {
                    val text = viewModel.copyPayload()
                    if (text.isNotBlank()) clipboard.setText(AnnotatedString(text))
                },
            )
        },
    ) { padding ->
        BoxWithConstraints(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(horizontal = ApexSpacing.md),
        ) {
            val landscape = maxWidth > maxHeight && maxHeight < 520.dp
            val display: @Composable (Modifier) -> Unit = { modifier ->
                ScientificDisplay(
                    state = state,
                    displayText = displayText,
                    secondary = secondary,
                    onExpressionChange = viewModel::onExpressionChange,
                    modifier = modifier,
                )
            }
            val controls: @Composable (Modifier) -> Unit = { modifier ->
                Column(modifier = modifier, verticalArrangement = Arrangement.spacedBy(ApexSpacing.sm)) {
                    Row(horizontalArrangement = Arrangement.spacedBy(ApexSpacing.sm)) {
                        SelectableChip(
                            label = "SHIFT",
                            selected = state.shift,
                            onClick = viewModel::toggleShift,
                        )
                        SelectableChip(
                            label = state.angleMode.shortLabel,
                            selected = state.angleMode.shortLabel == "RAD",
                            onClick = viewModel::toggleAngleMode,
                        )
                        SelectableChip(
                            label = "i",
                            selected = state.complexMode,
                            onClick = viewModel::toggleComplex,
                        )
                    }
                    FunctionStrip(
                        shift = state.shift,
                        onInsert = viewModel::insert,
                        onCalculus = viewModel::openCalculus,
                    )
                    ScientificKeypad(
                        onDigit = viewModel::onDigit,
                        onOperator = viewModel::onOperator,
                        onDecimal = viewModel::onDecimal,
                        onParen = viewModel::onParen,
                        onClear = viewModel::onClear,
                        onBackspace = viewModel::onBackspace,
                        onEquals = viewModel::onEquals,
                        onAnswer = viewModel::useAnswer,
                        hapticsEnabled = state.settings.hapticsEnabled,
                        modifier = Modifier.weight(1f),
                    )
                }
            }
            if (landscape) {
                Row(
                    modifier = Modifier.fillMaxSize(),
                    horizontalArrangement = Arrangement.spacedBy(ApexSpacing.md),
                ) {
                    display(Modifier.weight(0.42f).fillMaxHeight())
                    controls(Modifier.weight(0.58f).fillMaxHeight())
                }
            } else {
                Column(modifier = Modifier.fillMaxSize()) {
                    display(Modifier.weight(0.8f).fillMaxWidth())
                    controls(Modifier.weight(1.6f).fillMaxWidth())
                    Spacer(Modifier.height(ApexSpacing.sm))
                }
            }
        }
    }

    if (state.calculusVisible) {
        CalculusDialog(
            state = state.calculus,
            onDismiss = viewModel::closeCalculus,
            onUpdate = viewModel::updateCalculus,
            onDerivative = viewModel::computeDerivative,
            onIntegral = viewModel::computeIntegral,
            onRoot = viewModel::computeRoot,
        )
    }
}

@Composable
private fun ScientificDisplay(
    state: ScientificUiState,
    displayText: String,
    secondary: String?,
    onExpressionChange: (androidx.compose.ui.text.input.TextFieldValue) -> Unit,
    modifier: Modifier = Modifier,
) {
    val colors = MaterialTheme.apexColors
    Column(modifier = modifier.fillMaxWidth(), verticalArrangement = Arrangement.Bottom) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(
                text = if (state.justEvaluated) "Result" else "Expression",
                style = MaterialTheme.typography.labelSmall,
                color = colors.textTertiary,
            )
            Spacer(Modifier.weight(1f))
            if (state.lastAnswer != null) {
                Text(
                    text = "ANS ${state.lastAnswer}",
                    style = MaterialTheme.typography.labelSmall,
                    color = colors.textTertiary,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
            }
        }
        Spacer(Modifier.height(ApexSpacing.xs))
        BasicTextField(
            value = state.expression,
            onValueChange = onExpressionChange,
            textStyle = TextStyle(
                color = colors.textPrimary,
                fontSize = 28.sp,
                fontFamily = FontFamily.Monospace,
            ),
            cursorBrush = SolidColor(colors.primary),
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Ascii),
            modifier = Modifier
                .fillMaxWidth()
                .heightIn(min = 44.dp),
            decorationBox = { inner ->
                if (state.expression.text.isEmpty()) {
                    Text(
                        text = "0",
                        style = MaterialTheme.typography.headlineMedium.copy(fontFamily = FontFamily.Monospace),
                        color = colors.textTertiary,
                    )
                }
                inner()
            },
        )
        Spacer(Modifier.height(ApexSpacing.xs))
        Text(
            text = when {
                state.error != null -> state.error.message
                displayText.isNotBlank() -> "= $displayText"
                else -> " "
            },
            style = MaterialTheme.typography.headlineSmall.copy(fontFamily = FontFamily.Monospace),
            color = if (state.error != null) colors.error else colors.primary,
            maxLines = 2,
            overflow = TextOverflow.Ellipsis,
            modifier = Modifier.fillMaxWidth(),
        )
        if (secondary != null) {
            Text(
                text = secondary,
                style = MaterialTheme.typography.bodySmall,
                color = colors.textSecondary,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
        }
        Spacer(Modifier.height(ApexSpacing.sm))
    }
}

private data class FunctionChip(
    val label: String,
    val insert: String,
    val altLabel: String = label,
    val altInsert: String = insert,
)

private val functionChips = listOf(
    FunctionChip("sin", "sin(", "sin⁻¹", "asin("),
    FunctionChip("cos", "cos(", "cos⁻¹", "acos("),
    FunctionChip("tan", "tan(", "tan⁻¹", "atan("),
    FunctionChip("sinh", "sinh(", "sinh⁻¹", "asinh("),
    FunctionChip("cosh", "cosh(", "cosh⁻¹", "acosh("),
    FunctionChip("tanh", "tanh(", "tanh⁻¹", "atanh("),
    FunctionChip("ln", "ln(", "eˣ", "exp("),
    FunctionChip("log", "log(", "10ˣ", "10^("),
    FunctionChip("log₂", "log2(", "2ˣ", "2^("),
    FunctionChip("√", "sqrt(", "∛", "cbrt("),
    FunctionChip("x²", "^2", "x³", "^3"),
    FunctionChip("xʸ", "^", "ʸ√x", "root("),
    FunctionChip("1/x", "1/", "n!", "!"),
    FunctionChip("π", "pi", "e", "e"),
    FunctionChip("nCr", "ncr(", "nPr", "npr("),
    FunctionChip("gcd", "gcd(", "lcm", "lcm("),
    FunctionChip("|x|", "|", "round", "round("),
    FunctionChip("mod", "mod(", "abs", "abs("),
    FunctionChip("i", "i", "φ", "phi"),
    FunctionChip("d/dx", "d", "∫", "s"),
)

@Composable
private fun FunctionStrip(
    shift: Boolean,
    onInsert: (String) -> Unit,
    onCalculus: () -> Unit,
) {
    val colors = MaterialTheme.apexColors
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .horizontalScroll(rememberScrollState()),
        horizontalArrangement = Arrangement.spacedBy(ApexSpacing.xs),
    ) {
        functionChips.forEach { chip ->
            val label = if (shift) chip.altLabel else chip.label
            val insert = if (shift) chip.altInsert else chip.insert
            Box(
                modifier = Modifier
                    .heightIn(min = 38.dp)
                    .widthIn(min = 46.dp)
                    .background(colors.surfaceVariant, RoundedCornerShape(ApexRadius.sm))
                    .clickable {
                        if (label == "d/dx" || label == "∫") onCalculus() else onInsert(insert)
                    }
                    .padding(horizontal = ApexSpacing.sm, vertical = ApexSpacing.xs),
                contentAlignment = Alignment.Center,
            ) {
                Text(
                    text = label,
                    style = MaterialTheme.typography.labelLarge,
                    color = colors.textPrimary,
                    maxLines = 1,
                )
            }
        }
    }
}

private data class SciKey(
    val label: String,
    val style: KeyStyle,
    val fontSize: Int = 20,
    val description: String? = null,
    val action: () -> Unit,
)

@Composable
private fun ScientificKeypad(
    onDigit: (String) -> Unit,
    onOperator: (String) -> Unit,
    onDecimal: () -> Unit,
    onParen: () -> Unit,
    onClear: () -> Unit,
    onBackspace: () -> Unit,
    onEquals: () -> Unit,
    onAnswer: () -> Unit,
    hapticsEnabled: Boolean,
    modifier: Modifier = Modifier,
) {
    val rows = listOf(
        listOf(
            SciKey("AC", KeyStyle.DANGER, description = "Clear all", action = onClear),
            SciKey("( )", KeyStyle.FUNCTION, description = "Parenthesis", action = onParen),
            SciKey("⌫", KeyStyle.FUNCTION, description = "Backspace", action = onBackspace),
            SciKey("÷", KeyStyle.OPERATOR, 24, "Divide") { onOperator("÷") },
        ),
        listOf(
            SciKey("7", KeyStyle.DIGIT, 22) { onDigit("7") },
            SciKey("8", KeyStyle.DIGIT, 22) { onDigit("8") },
            SciKey("9", KeyStyle.DIGIT, 22) { onDigit("9") },
            SciKey("×", KeyStyle.OPERATOR, 24, "Multiply") { onOperator("×") },
        ),
        listOf(
            SciKey("4", KeyStyle.DIGIT, 22) { onDigit("4") },
            SciKey("5", KeyStyle.DIGIT, 22) { onDigit("5") },
            SciKey("6", KeyStyle.DIGIT, 22) { onDigit("6") },
            SciKey("−", KeyStyle.OPERATOR, 24, "Subtract") { onOperator("-") },
        ),
        listOf(
            SciKey("1", KeyStyle.DIGIT, 22) { onDigit("1") },
            SciKey("2", KeyStyle.DIGIT, 22) { onDigit("2") },
            SciKey("3", KeyStyle.DIGIT, 22) { onDigit("3") },
            SciKey("+", KeyStyle.OPERATOR, 24, "Add") { onOperator("+") },
        ),
        listOf(
            SciKey("ANS", KeyStyle.FUNCTION, 15, "Last answer", action = onAnswer),
            SciKey("0", KeyStyle.DIGIT, 22) { onDigit("0") },
            SciKey(".", KeyStyle.DIGIT, 22, "Decimal point", action = onDecimal),
            SciKey("=", KeyStyle.ACCENT, 24, "Equals", action = onEquals),
        ),
    )
    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(ApexSpacing.sm),
    ) {
        rows.forEach { row ->
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
                horizontalArrangement = Arrangement.spacedBy(ApexSpacing.sm),
            ) {
                row.forEach { key ->
                    KeyButton(
                        label = key.label,
                        style = key.style,
                        onClick = key.action,
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxHeight(),
                        contentDescription = key.description,
                        labelSize = key.fontSize,
                        hapticsEnabled = hapticsEnabled,
                    )
                }
            }
        }
    }
}

@Composable
private fun CalculusDialog(
    state: CalculusUiState,
    onDismiss: () -> Unit,
    onUpdate: ((CalculusUiState) -> CalculusUiState) -> Unit,
    onDerivative: () -> Unit,
    onIntegral: () -> Unit,
    onRoot: () -> Unit,
) {
    val colors = MaterialTheme.apexColors
    Dialog(onDismissRequest = onDismiss) {
        ApexCard(color = colors.surfaceElevated) {
            Column(
                modifier = Modifier
                    .heightIn(max = 560.dp)
                    .verticalScroll(rememberScrollState()),
            ) {
                Text("Calculus tools", style = MaterialTheme.typography.titleLarge, color = colors.textPrimary)
                Spacer(Modifier.height(ApexSpacing.sm))
                Text(
                    text = "Uses adaptive numerical methods on your function. The variable is x.",
                    style = MaterialTheme.typography.bodySmall,
                    color = colors.textSecondary,
                )
                Spacer(Modifier.height(ApexSpacing.md))
                ApexTextField(
                    value = state.function,
                    onValueChange = { value -> onUpdate { it.copy(function = value, output = null, error = null) } },
                    label = "f(x)",
                    singleLine = true,
                )
                Spacer(Modifier.height(ApexSpacing.sm))
                ApexTextField(
                    value = state.point,
                    onValueChange = { value -> onUpdate { it.copy(point = value, output = null, error = null) } },
                    label = "Point for derivative",
                    singleLine = true,
                )
                Spacer(Modifier.height(ApexSpacing.sm))
                Row(horizontalArrangement = Arrangement.spacedBy(ApexSpacing.sm)) {
                    Box(Modifier.weight(1f)) {
                        ApexTextField(
                            value = state.lower,
                            onValueChange = { value -> onUpdate { it.copy(lower = value, output = null, error = null) } },
                            label = "Lower bound",
                            singleLine = true,
                        )
                    }
                    Box(Modifier.weight(1f)) {
                        ApexTextField(
                            value = state.upper,
                            onValueChange = { value -> onUpdate { it.copy(upper = value, output = null, error = null) } },
                            label = "Upper bound",
                            singleLine = true,
                        )
                    }
                }
                Spacer(Modifier.height(ApexSpacing.md))
                Row(horizontalArrangement = Arrangement.spacedBy(ApexSpacing.sm)) {
                    TextButton(onClick = onDerivative) { Text("Derivative") }
                    TextButton(onClick = onIntegral) { Text("Integral") }
                    TextButton(onClick = onRoot) { Text("Root") }
                }
                when {
                    state.error != null -> InfoBanner(text = state.error, tone = BannerTone.ERROR)
                    state.output != null -> InfoBanner(text = state.output, tone = BannerTone.SUCCESS)
                }
                Spacer(Modifier.height(ApexSpacing.sm))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End,
                ) {
                    TextButton(onClick = onDismiss) { Text("Close") }
                }
            }
        }
    }
}
