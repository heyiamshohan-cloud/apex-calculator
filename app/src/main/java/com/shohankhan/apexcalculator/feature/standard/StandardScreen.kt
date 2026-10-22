package com.shohankhan.apexcalculator.feature.standard

import androidx.compose.foundation.layout.Arrangement
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
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.shohankhan.apexcalculator.core.designsystem.ApexIcons
import com.shohankhan.apexcalculator.core.designsystem.ApexScreen
import com.shohankhan.apexcalculator.core.designsystem.ApexSpacing
import com.shohankhan.apexcalculator.core.designsystem.IconAction
import com.shohankhan.apexcalculator.core.designsystem.KeyButton
import com.shohankhan.apexcalculator.core.designsystem.KeyStyle
import com.shohankhan.apexcalculator.core.designsystem.apexColors
import androidx.compose.ui.text.input.TextFieldValue

@Composable
fun StandardScreen(
    viewModel: StandardViewModel = viewModel(),
    onOpenHistory: () -> Unit = {},
    onOpenFavorites: () -> Unit = {},
    onOpenSettings: () -> Unit = {},
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val colors = MaterialTheme.apexColors
    val formatter = remember(state.settings) {
        com.shohankhan.apexcalculator.core.math.ResultFormatter(state.settings.toFormatSettings())
    }
    val committed = state.committed
    val displayValue = if (state.justEvaluated) committed else state.preview
    val displayText = displayValue?.let { formatter.format(it) }.orEmpty()
    val secondary = displayValue?.let { formatter.secondary(it) }
    val clipboard = LocalClipboardManager.current

    ApexScreen(
        title = "Standard",
        subtitle = "APEX Calculator",
        actions = {
            IconAction(ApexIcons.History, "Calculation history", onOpenHistory)
            IconAction(ApexIcons.StarOutline, "Favorites", onOpenFavorites)
            IconAction(ApexIcons.Settings, "Settings", onOpenSettings)
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
                StandardDisplay(
                    state = state,
                    displayText = displayText,
                    secondary = secondary,
                    isPreview = !state.justEvaluated,
                    onExpressionChange = viewModel::onExpressionChange,
                    onBackspace = viewModel::onBackspace,
                    onUndo = viewModel::onUndo,
                    onRedo = viewModel::onRedo,
                    onMemory = viewModel::onMemory,
                    onCopy = {
                        val text = when {
                            displayText.isNotBlank() -> displayText
                            else -> state.expression.text
                        }
                        if (text.isNotBlank()) {
                            clipboard.setText(AnnotatedString(text))
                        }
                    },
                    modifier = modifier,
                )
            }
            val keypad: @Composable (Modifier) -> Unit = { modifier ->
                StandardKeypad(
                    onDigit = viewModel::onDigit,
                    onOperator = viewModel::onOperator,
                    onDecimal = viewModel::onDecimal,
                    onToggleSign = viewModel::onToggleSign,
                    onPercent = { viewModel.insert("%") },
                    onParen = viewModel::onParen,
                    onClear = viewModel::onClear,
                    onEquals = viewModel::onEquals,
                    hapticsEnabled = state.settings.hapticsEnabled,
                    modifier = modifier,
                )
            }
            if (landscape) {
                Row(
                    modifier = Modifier.fillMaxSize(),
                    horizontalArrangement = Arrangement.spacedBy(ApexSpacing.md),
                ) {
                    display(Modifier.weight(0.44f).fillMaxHeight())
                    keypad(Modifier.weight(0.56f).fillMaxHeight())
                }
            } else {
                Column(modifier = Modifier.fillMaxSize()) {
                    display(Modifier.weight(1f).fillMaxWidth())
                    keypad(Modifier.weight(1.15f).fillMaxWidth())
                    Spacer(Modifier.height(ApexSpacing.sm))
                }
            }
        }
    }
}

@Composable
private fun StandardDisplay(
    state: StandardUiState,
    displayText: String,
    secondary: String?,
    isPreview: Boolean,
    onExpressionChange: (TextFieldValue) -> Unit,
    onBackspace: () -> Unit,
    onUndo: () -> Unit,
    onRedo: () -> Unit,
    onMemory: (MemoryOp) -> Unit,
    onCopy: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val colors = MaterialTheme.apexColors
    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.Bottom,
        horizontalAlignment = Alignment.End,
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            TextButton(onClick = onCopy, enabled = displayText.isNotBlank() || state.expression.text.isNotBlank()) {
                Text("Copy", style = MaterialTheme.typography.labelSmall, color = colors.textSecondary)
            }
            if (state.memory.isSet) {
                TextButton(onClick = { onMemory(MemoryOp.RECALL) }) {
                    Text("MR", style = MaterialTheme.typography.labelSmall, color = colors.primary)
                }
            }
            Spacer(Modifier.weight(1f))
            IconAction(
                icon = ApexIcons.Undo,
                description = "Undo",
                onClick = onUndo,
                enabled = state.canUndo,
            )
            IconAction(
                icon = ApexIcons.Refresh,
                description = "Redo",
                onClick = onRedo,
                enabled = state.canRedo,
            )
            IconAction(
                icon = ApexIcons.Delete,
                description = "Backspace",
                onClick = onBackspace,
                enabled = state.hasContent,
            )
        }

        state.previous?.let { previous ->
            Text(
                text = "${previous.expression} = ${previous.result}",
                style = MaterialTheme.typography.bodySmall,
                color = colors.textTertiary,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier.fillMaxWidth(),
                textAlign = TextAlign.End,
            )
            Spacer(Modifier.height(ApexSpacing.xs))
        }

        BasicTextField(
            value = state.expression,
            onValueChange = onExpressionChange,
            textStyle = TextStyle(
                fontFamily = FontFamily.Monospace,
                fontSize = 22.sp,
                lineHeight = 30.sp,
                color = colors.textPrimary,
                textAlign = TextAlign.End,
            ),
            cursorBrush = SolidColor(colors.primary),
            modifier = Modifier
                .fillMaxWidth()
                .heightIn(min = 44.dp),
            decorationBox = { innerTextField ->
                if (state.expression.text.isEmpty()) {
                    Text(
                        text = "Type or tap to calculate",
                        style = MaterialTheme.typography.bodyMedium,
                        color = colors.textTertiary,
                        modifier = Modifier.fillMaxWidth(),
                        textAlign = TextAlign.End,
                    )
                }
                innerTextField()
            },
        )

        Spacer(Modifier.height(ApexSpacing.sm))

        Text(
            text = displayText.ifBlank { "0" },
            style = MaterialTheme.typography.displayLarge,
            color = if (isPreview) colors.textSecondary else colors.primary,
            textAlign = TextAlign.End,
            maxLines = 2,
            overflow = TextOverflow.Ellipsis,
            modifier = Modifier.fillMaxWidth(),
        )

        if (secondary != null) {
            Text(
                text = secondary,
                style = MaterialTheme.typography.labelMedium,
                color = colors.textTertiary,
                textAlign = TextAlign.End,
                modifier = Modifier.fillMaxWidth(),
            )
        }

        if (state.error != null) {
            Spacer(Modifier.height(ApexSpacing.sm))
            Text(
                text = state.error.message,
                style = MaterialTheme.typography.bodySmall,
                color = colors.error,
                textAlign = TextAlign.End,
                modifier = Modifier.fillMaxWidth(),
            )
        }
        Spacer(Modifier.height(ApexSpacing.sm))
    }
}

private data class KeySpec(
    val label: String,
    val style: KeyStyle,
    val weight: Float = 1f,
    val fontSize: Int = 22,
    val description: String? = null,
    val action: () -> Unit,
)

@Composable
private fun StandardKeypad(
    onDigit: (String) -> Unit,
    onOperator: (String) -> Unit,
    onDecimal: () -> Unit,
    onToggleSign: () -> Unit,
    onPercent: () -> Unit,
    onParen: () -> Unit,
    onClear: () -> Unit,
    onEquals: () -> Unit,
    hapticsEnabled: Boolean,
    modifier: Modifier = Modifier,
) {
    val rows = listOf(
        listOf(
            KeySpec("AC", KeyStyle.DANGER, fontSize = 20, description = "Clear all", action = onClear),
            KeySpec("( )", KeyStyle.FUNCTION, fontSize = 20, description = "Parenthesis", action = onParen),
            KeySpec("%", KeyStyle.FUNCTION, fontSize = 20, description = "Percent", action = onPercent),
            KeySpec("÷", KeyStyle.OPERATOR, fontSize = 26, description = "Divide") { onOperator("÷") },
        ),
        listOf(
            KeySpec("7", KeyStyle.DIGIT) { onDigit("7") },
            KeySpec("8", KeyStyle.DIGIT) { onDigit("8") },
            KeySpec("9", KeyStyle.DIGIT) { onDigit("9") },
            KeySpec("×", KeyStyle.OPERATOR, fontSize = 26, description = "Multiply") { onOperator("×") },
        ),
        listOf(
            KeySpec("4", KeyStyle.DIGIT) { onDigit("4") },
            KeySpec("5", KeyStyle.DIGIT) { onDigit("5") },
            KeySpec("6", KeyStyle.DIGIT) { onDigit("6") },
            KeySpec("−", KeyStyle.OPERATOR, fontSize = 26, description = "Subtract") { onOperator("-") },
        ),
        listOf(
            KeySpec("1", KeyStyle.DIGIT) { onDigit("1") },
            KeySpec("2", KeyStyle.DIGIT) { onDigit("2") },
            KeySpec("3", KeyStyle.DIGIT) { onDigit("3") },
            KeySpec("+", KeyStyle.OPERATOR, fontSize = 26, description = "Add") { onOperator("+") },
        ),
        listOf(
            KeySpec("±", KeyStyle.FUNCTION, fontSize = 20, description = "Toggle sign", action = onToggleSign),
            KeySpec("0", KeyStyle.DIGIT) { onDigit("0") },
            KeySpec(".", KeyStyle.DIGIT, description = "Decimal point", action = onDecimal),
            KeySpec("=", KeyStyle.ACCENT, fontSize = 26, description = "Equals", action = onEquals),
        ),
    )
    Column(
        modifier = modifier.fillMaxHeight(),
        verticalArrangement = Arrangement.spacedBy(ApexSpacing.sm),
    ) {
        rows.forEach { row ->
            Row(
                modifier = Modifier.fillMaxWidth().weight(1f),
                horizontalArrangement = Arrangement.spacedBy(ApexSpacing.sm),
            ) {
                row.forEach { key ->
                    KeyButton(
                        label = key.label,
                        style = key.style,
                        onClick = key.action,
                        modifier = Modifier.weight(key.weight).fillMaxHeight(),
                        contentDescription = key.description,
                        labelSize = key.fontSize,
                        hapticsEnabled = hapticsEnabled,
                    )
                }
            }
        }
    }
}
