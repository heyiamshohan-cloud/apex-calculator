package com.shohankhan.apexcalculator.feature.quicksolve

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.unit.dp
import com.shohankhan.apexcalculator.core.designsystem.ApexCard
import com.shohankhan.apexcalculator.core.designsystem.ApexSpacing
import com.shohankhan.apexcalculator.core.designsystem.ApexTextField
import com.shohankhan.apexcalculator.core.designsystem.ValueRow
import com.shohankhan.apexcalculator.core.designsystem.apexColors
import com.shohankhan.apexcalculator.core.domain.solver.SolverField
import com.shohankhan.apexcalculator.core.domain.solver.SolverFieldKind
import com.shohankhan.apexcalculator.core.domain.solver.SolverOutput

/** Form field styling for every solver input. */
@Composable
fun SolverFieldInput(
    field: SolverField,
    value: String,
    onChange: (String) -> Unit,
    modifier: Modifier = Modifier,
) {
    val multiline = field.kind == SolverFieldKind.EXPRESSION || field.kind == SolverFieldKind.LIST
    ApexTextField(
        value = value,
        onValueChange = onChange,
        label = field.label,
        placeholder = field.hint,
        suffix = field.suffix,
        singleLine = !multiline,
        minLines = if (multiline) 2 else 1,
        modifier = modifier,
    )
}

@Composable
fun SolverFormCard(
    fields: List<SolverField>,
    values: Map<String, String>,
    onValueChange: (String, String) -> Unit,
    actionLabel: String,
    onCalculate: () -> Unit,
) {
    val colors = MaterialTheme.apexColors
    ApexCard {
        Column {
            Text("Inputs", style = MaterialTheme.typography.titleSmall, color = colors.textPrimary)
            Spacer(Modifier.height(ApexSpacing.sm))
            fields.forEach { field ->
                SolverFieldInput(
                    field = field,
                    value = values[field.key].orEmpty(),
                    onChange = { onValueChange(field.key, it) },
                    modifier = Modifier.padding(bottom = ApexSpacing.sm),
                )
            }
            Spacer(Modifier.height(ApexSpacing.xs))
            Button(
                onClick = onCalculate,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(48.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = colors.primary,
                    contentColor = colors.onPrimary,
                ),
            ) {
                Text(actionLabel)
            }
        }
    }
}

@Composable
fun SolverResultCard(output: SolverOutput) {
    val colors = MaterialTheme.apexColors
    ApexCard {
        Column {
            Text(
                text = output.headline,
                style = MaterialTheme.typography.headlineMedium.copy(fontFamily = FontFamily.Monospace),
                color = colors.primary,
            )
            Text(
                text = output.headlineLabel,
                style = MaterialTheme.typography.labelSmall,
                color = colors.textSecondary,
            )
            if (output.rows.isNotEmpty()) {
                Spacer(Modifier.height(ApexSpacing.md))
                output.rows.forEach { (label, value) ->
                    ValueRow(label = label, value = value)
                }
            }
            if (output.steps.isNotEmpty()) {
                Spacer(Modifier.height(ApexSpacing.md))
                Text("How it is computed", style = MaterialTheme.typography.titleSmall, color = colors.textPrimary)
                Spacer(Modifier.height(ApexSpacing.xs))
                output.steps.forEachIndexed { index, step ->
                    Row(modifier = Modifier.padding(vertical = ApexSpacing.xxs)) {
                        Text(
                            text = "${index + 1}.",
                            style = MaterialTheme.typography.bodySmall,
                            color = colors.primary,
                            modifier = Modifier.width(20.dp),
                        )
                        Text(
                            text = step,
                            style = MaterialTheme.typography.bodySmall,
                            color = colors.textSecondary,
                        )
                    }
                }
            }
            if (output.note != null) {
                Spacer(Modifier.height(ApexSpacing.sm))
                Text(
                    text = output.note,
                    style = MaterialTheme.typography.bodySmall,
                    color = colors.textTertiary,
                )
            }
        }
    }
}

@Composable
fun ResultHeaderRow(label: String, value: String) {
    Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
        Text(label, style = MaterialTheme.typography.labelLarge, color = MaterialTheme.apexColors.textSecondary)
        Spacer(Modifier.weight(1f))
        Text(value, style = MaterialTheme.typography.titleSmall, color = MaterialTheme.apexColors.primary)
    }
}

val SolverContentPadding = PaddingValues(horizontal = ApexSpacing.md)
