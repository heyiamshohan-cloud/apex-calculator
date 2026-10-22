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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.background
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
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.shohankhan.apexcalculator.core.designsystem.ApexCard
import com.shohankhan.apexcalculator.core.designsystem.ApexRadius
import com.shohankhan.apexcalculator.core.designsystem.ApexSpacing
import com.shohankhan.apexcalculator.core.designsystem.ApexTextField
import com.shohankhan.apexcalculator.core.designsystem.BannerTone
import com.shohankhan.apexcalculator.core.designsystem.InfoBanner
import com.shohankhan.apexcalculator.core.designsystem.SelectableChip
import com.shohankhan.apexcalculator.core.designsystem.apexColors
import com.shohankhan.apexcalculator.core.math.Matrix
import com.shohankhan.apexcalculator.core.math.MatrixMath
import com.shohankhan.apexcalculator.core.math.MatrixResult
import com.shohankhan.apexcalculator.core.math.ResultFormatter
import com.shohankhan.apexcalculator.navigation.SecondaryScaffold

private enum class MatrixOperation(val label: String, val needsSecond: Boolean) {
    DETERMINANT("Determinant", false),
    INVERSE("Inverse", false),
    TRANSPOSE("Transpose", false),
    RREF("RREF", false),
    RANK("Rank", false),
    MULTIPLY("A × B", true),
    ADD("A + B", true),
    SOLVE("Solve A·x = b", true),
}

@Composable
fun MatrixWorkspaceScreen(onBack: () -> Unit) {
    val colors = MaterialTheme.apexColors
    var operationName by rememberSaveable { mutableStateOf(MatrixOperation.DETERMINANT.name) }
    var matrixA by rememberSaveable { mutableStateOf("1, 2; 3, 4") }
    var matrixB by rememberSaveable { mutableStateOf("5, 6; 7, 8") }
    val operation = MatrixOperation.entries.firstOrNull { it.name == operationName } ?: MatrixOperation.DETERMINANT
    var outcome by remember { mutableStateOf<MatrixOutcome?>(null) }
    val formatter = remember { ResultFormatter(com.shohankhan.apexcalculator.core.math.FormatSettings()) }

    SecondaryScaffold(title = "Matrix calculator", subtitle = "Up to 8 × 8", onBack = onBack) { padding ->
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
                            modifier = Modifier
                                .fillMaxWidth()
                                .horizontalScroll(rememberScrollState()),
                            horizontalArrangement = Arrangement.spacedBy(ApexSpacing.sm),
                        ) {
                            MatrixOperation.entries.forEach { entry ->
                                SelectableChip(
                                    label = entry.label,
                                    selected = entry == operation,
                                    onClick = {
                                        operationName = entry.name
                                        outcome = null
                                    },
                                )
                            }
                        }
                    }
                }
            }
            item {
                ApexCard {
                    Column {
                        ApexTextField(
                            value = matrixA,
                            onValueChange = {
                                matrixA = it
                                outcome = null
                            },
                            label = "Matrix A",
                            supportingText = "Rows separated by ; and values by commas. Example: 1, 2; 3, 4",
                            singleLine = false,
                            minLines = 2,
                        )
                        if (operation.needsSecond) {
                            Spacer(Modifier.height(ApexSpacing.sm))
                            ApexTextField(
                                value = matrixB,
                                onValueChange = {
                                    matrixB = it
                                    outcome = null
                                },
                                label = if (operation == MatrixOperation.SOLVE) "Vector b (one column)" else "Matrix B",
                                supportingText = if (operation == MatrixOperation.SOLVE) {
                                    "Example: 5; 6"
                                } else {
                                    "Example: 5, 6; 7, 8"
                                },
                                singleLine = false,
                                minLines = 2,
                            )
                        }
                        Spacer(Modifier.height(ApexSpacing.md))
                        Button(
                            onClick = {
                                outcome = runOperation(operation, matrixA, matrixB, formatter)
                            },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(48.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = colors.primary,
                                contentColor = colors.onPrimary,
                            ),
                        ) {
                            Text("Compute")
                        }
                    }
                }
            }
            when (val result = outcome) {
                null -> Unit
                is MatrixOutcome.Error -> item {
                    InfoBanner(text = result.message, tone = BannerTone.ERROR, title = "Check the matrix")
                }
                is MatrixOutcome.Ok -> item {
                    ApexCard {
                        Column {
                            Text(
                                text = operation.label,
                                style = MaterialTheme.typography.labelSmall,
                                color = colors.textSecondary,
                            )
                            if (result.scalar != null) {
                                Spacer(Modifier.height(ApexSpacing.xs))
                                Text(
                                    text = result.scalar,
                                    style = MaterialTheme.typography.headlineMedium,
                                    color = colors.primary,
                                )
                            }
                            result.matrix?.let { matrix ->
                                Spacer(Modifier.height(ApexSpacing.md))
                                MatrixGrid(matrix, formatter)
                            }
                            if (result.note != null) {
                                Spacer(Modifier.height(ApexSpacing.md))
                                InfoBanner(text = result.note, tone = BannerTone.INFO)
                            }
                        }
                    }
                }
            }
            item {
                Text(
                    text = "Reduced row echelon form uses exact pivoting with a 1e-10 tolerance.",
                    style = MaterialTheme.typography.bodySmall,
                    color = colors.textTertiary,
                )
            }
        }
    }
}

@Composable
private fun MatrixGrid(matrix: Matrix, formatter: ResultFormatter) {
    val colors = MaterialTheme.apexColors
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .horizontalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(ApexSpacing.xs),
    ) {
        matrix.toRows().forEach { row ->
            Row(horizontalArrangement = Arrangement.spacedBy(ApexSpacing.xs)) {
                row.forEach { value ->
                    Text(
                        text = formatter.format(com.shohankhan.apexcalculator.core.math.Num.of(value)),
                        style = MaterialTheme.typography.bodyMedium,
                        color = colors.textPrimary,
                        textAlign = TextAlign.End,
                        modifier = Modifier
                            .background(colors.surfaceVariant, RoundedCornerShape(ApexRadius.sm))
                            .padding(horizontal = ApexSpacing.sm, vertical = ApexSpacing.xs),
                    )
                }
            }
        }
    }
}

private data class MatrixOutcome(
    val scalar: String? = null,
    val matrix: Matrix? = null,
    val note: String? = null,
)

private fun runOperation(
    operation: MatrixOperation,
    aText: String,
    bText: String,
    formatter: ResultFormatter,
): MatrixOutcome {
    val a = parseMatrix(aText) ?: return MatrixOutcome(scalar = "Matrix A is not valid")
    val b = if (operation.needsSecond) parseMatrix(bText) ?: return MatrixOutcome(scalar = "Matrix B is not valid") else null
    return when (operation) {
        MatrixOperation.DETERMINANT -> when (val result = MatrixMath.determinant(a)) {
            is MatrixResult.Scalar -> MatrixOutcome(
                scalar = formatter.format(com.shohankhan.apexcalculator.core.math.Num.of(result.value)),
                note = "The determinant is zero, so the matrix is singular." .takeIf { kotlin.math.abs(result.value) < MatrixMath.EPSILON },
            )
            is MatrixResult.Failure -> MatrixOutcome(scalar = result.message)
            is MatrixResult.MatrixValue -> MatrixOutcome(scalar = "Unexpected result")
        }
        MatrixOperation.INVERSE -> matrixOutcome(MatrixMath.inverse(a), formatter)
        MatrixOperation.TRANSPOSE -> matrixOutcome(MatrixMath.transpose(a), formatter)
        MatrixOperation.RREF -> MatrixOutcome(
            matrix = MatrixMath.rref(a),
            note = "Rank ${MatrixMath.rank(a)}",
        )
        MatrixOperation.RANK -> MatrixOutcome(
            scalar = MatrixMath.rank(a).toString(),
            note = "Rank computed from the row echelon form.",
        )
        MatrixOperation.MULTIPLY -> b?.let { matrixOutcome(MatrixMath.multiply(a, it), formatter) } ?: MatrixOutcome(scalar = "Missing B")
        MatrixOperation.ADD -> b?.let { matrixOutcome(MatrixMath.add(a, it), formatter) } ?: MatrixOutcome(scalar = "Missing B")
        MatrixOperation.SOLVE -> b?.let { matrixOutcome(MatrixMath.solve(a, it), formatter) } ?: MatrixOutcome(scalar = "Missing b")
    }
}

private fun matrixOutcome(result: MatrixResult, formatter: ResultFormatter): MatrixOutcome = when (result) {
    is MatrixResult.MatrixValue -> MatrixOutcome(matrix = result.matrix)
    is MatrixResult.Scalar -> MatrixOutcome(
        scalar = formatter.format(com.shohankhan.apexcalculator.core.math.Num.of(result.value)),
    )
    is MatrixResult.Failure -> MatrixOutcome(scalar = result.message)
}

private fun parseMatrix(text: String): Matrix? {
    val rows = text.split(';').map { it.trim() }.filter { it.isNotEmpty() }
    if (rows.isEmpty() || rows.size > MatrixMath.MAX_DIMENSION) return null
    val parsed = rows.map { row ->
        row.split(',', ' ', '\t').map { it.trim() }.filter { it.isNotEmpty() }.map { cell ->
            cell.toDoubleOrNull() ?: return null
        }
    }
    val width = parsed.first().size
    if (width == 0 || width > MatrixMath.MAX_DIMENSION) return null
    if (parsed.any { it.size != width }) return null
    return Matrix.of(parsed)
}
