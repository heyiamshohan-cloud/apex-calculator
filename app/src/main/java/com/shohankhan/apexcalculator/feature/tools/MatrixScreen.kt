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
import com.shohankhan.apexcalculator.core.designsystem.apexColors
import com.shohankhan.apexcalculator.core.math.FormatSettings
import com.shohankhan.apexcalculator.core.math.Matrix
import com.shohankhan.apexcalculator.core.math.MatrixMath
import com.shohankhan.apexcalculator.core.math.MatrixResult
import com.shohankhan.apexcalculator.core.math.ResultFormatter
import com.shohankhan.apexcalculator.navigation.SecondaryScaffold

private enum class MatrixOp(val label: String, val needsVector: Boolean) {
    DETERMINANT("det", false),
    INVERSE("Inverse", false),
    TRANSPOSE("Transpose", false),
    RREF("RREF", false),
    RANK("Rank", false),
    SOLVE("A·x = b", true),
}

/** Matrix workspace: up to 8×8, exact pivoting and explicit failure messages. */
@Composable
fun MatrixScreen(onBack: () -> Unit) {
    val colors = MaterialTheme.apexColors
    val clipboard = LocalClipboardManager.current
    var matrixText by rememberSaveable { mutableStateOf("4, 7; 2, 6") }
    var vectorText by rememberSaveable { mutableStateOf("1; 2") }
    var operationName by rememberSaveable { mutableStateOf(MatrixOp.DETERMINANT.name) }
    var resultText by remember { mutableStateOf<String?>(null) }
    var resultMatrix by remember { mutableStateOf<Matrix?>(null) }
    var errorText by remember { mutableStateOf<String?>(null) }
    val operation = MatrixOp.entries.firstOrNull { it.name == operationName } ?: MatrixOp.DETERMINANT
    val formatter = remember { ResultFormatter(FormatSettings()) }

    fun parse(text: String): Matrix? {
        val rows = text.split(';', '\n').map { it.trim() }.filter { it.isNotEmpty() }
        if (rows.isEmpty() || rows.size > MatrixMath.MAX_DIMENSION) return null
        val values = rows.map { row ->
            row.split(',', ' ', '\t').map { it.trim() }.filter { it.isNotEmpty() }
                .map { it.toDoubleOrNull() ?: return null }
        }
        if (values.any { it.size != values.first().size }) return null
        if (values.first().size > MatrixMath.MAX_DIMENSION) return null
        return Matrix.of(values)
    }

    fun compute() {
        resultMatrix = null
        errorText = null
        resultText = null
        val a = parse(matrixText)
        if (a == null) {
            errorText = "Matrix A is not valid. Use rows separated by ; and values by commas."
            return
        }
        val outcome = when (operation) {
            MatrixOp.DETERMINANT -> MatrixMath.determinant(a)
            MatrixOp.INVERSE -> MatrixMath.inverse(a)
            MatrixOp.TRANSPOSE -> MatrixMath.transpose(a)
            MatrixOp.RREF -> MatrixResult.MatrixValue(MatrixMath.rref(a))
            MatrixOp.RANK -> MatrixResult.Scalar(MatrixMath.rank(a).toDouble())
            MatrixOp.SOLVE -> {
                val b = parse(vectorText)
                if (b == null) {
                    errorText = "Vector b is not valid. Provide one value per row, separated by ;."
                    return
                }
                MatrixMath.solve(a, b)
            }
        }
        when (outcome) {
            is MatrixResult.Scalar -> resultText = formatter.format(com.shohankhan.apexcalculator.core.math.Num.of(outcome.value))
            is MatrixResult.MatrixValue -> resultMatrix = outcome.matrix
            is MatrixResult.Failure -> errorText = outcome.message
        }
    }

    SecondaryScaffold(
        title = "Matrix calculator",
        subtitle = "Determinants, inverses and linear systems",
        onBack = onBack,
        actions = {
            IconAction(
                icon = ApexIcons.Copy,
                description = "Copy result",
                onClick = {
                    val payload = resultText ?: resultMatrix?.toRows()
                        ?.joinToString("\n") { row -> row.joinToString(", ") { formatter.format(com.shohankhan.apexcalculator.core.math.Num.of(it)) } }
                    if (!payload.isNullOrBlank()) clipboard.setText(AnnotatedString(payload))
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
                            MatrixOp.entries.forEach { entry ->
                                SelectableChip(
                                    label = entry.label,
                                    selected = entry == operation,
                                    onClick = { operationName = entry.name },
                                )
                            }
                        }
                        Spacer(Modifier.height(ApexSpacing.md))
                        ApexTextField(
                            value = matrixText,
                            onValueChange = { matrixText = it },
                            label = "Matrix A",
                            supportingText = "Rows separated by ; and values by commas, for example 4, 7; 2, 6",
                            singleLine = false,
                            minLines = 2,
                        )
                        if (operation.needsVector) {
                            Spacer(Modifier.height(ApexSpacing.sm))
                            ApexTextField(
                                value = vectorText,
                                onValueChange = { vectorText = it },
                                label = "Vector b",
                                supportingText = "One value per row, separated by ;",
                                singleLine = false,
                                minLines = 2,
                            )
                        }
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
            resultText?.let { value ->
                item {
                    ApexCard {
                        Column {
                            Text(operation.label, style = MaterialTheme.typography.labelSmall, color = colors.textSecondary)
                            Text(
                                text = value,
                                style = MaterialTheme.typography.headlineMedium.copy(fontFamily = FontFamily.Monospace),
                                color = colors.primary,
                            )
                        }
                    }
                }
            }
            resultMatrix?.let { matrix ->
                item {
                    ApexCard {
                        Column {
                            Text(operation.label, style = MaterialTheme.typography.labelSmall, color = colors.textSecondary)
                            Spacer(Modifier.height(ApexSpacing.sm))
                            matrix.toRows().forEach { row ->
                                Text(
                                    text = row.joinToString("   ") { formatter.format(com.shohankhan.apexcalculator.core.math.Num.of(it)) },
                                    style = MaterialTheme.typography.bodyLarge.copy(fontFamily = FontFamily.Monospace),
                                    color = colors.textPrimary,
                                )
                            }
                        }
                    }
                }
            }
            item {
                Text(
                    text = "Maximum dimension ${MatrixMath.MAX_DIMENSION}×${MatrixMath.MAX_DIMENSION}. Pivoting tolerance ${MatrixMath.EPSILON}.",
                    style = MaterialTheme.typography.bodySmall,
                    color = colors.textTertiary,
                )
            }
        }
    }
}
