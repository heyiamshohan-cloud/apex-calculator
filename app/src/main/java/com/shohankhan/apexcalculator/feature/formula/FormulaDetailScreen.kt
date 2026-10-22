package com.shohankhan.apexcalculator.feature.formula

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
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.unit.sp
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.shohankhan.apexcalculator.core.designsystem.ApexCard
import com.shohankhan.apexcalculator.core.designsystem.ApexIcons
import com.shohankhan.apexcalculator.core.designsystem.ApexSpacing
import com.shohankhan.apexcalculator.core.designsystem.ApexTextField
import com.shohankhan.apexcalculator.core.designsystem.IconAction
import com.shohankhan.apexcalculator.core.designsystem.InfoBanner
import com.shohankhan.apexcalculator.core.designsystem.BannerTone
import com.shohankhan.apexcalculator.core.designsystem.SmallPill
import com.shohankhan.apexcalculator.core.designsystem.ValueRow
import com.shohankhan.apexcalculator.core.designsystem.apexColors
import com.shohankhan.apexcalculator.core.domain.formula.FormulaDefinition
import com.shohankhan.apexcalculator.core.domain.formula.formatResult
import com.shohankhan.apexcalculator.core.domain.formula.FormulaException
import com.shohankhan.apexcalculator.core.domain.formula.FormulaResult
import com.shohankhan.apexcalculator.core.domain.model.FavoriteKind
import com.shohankhan.apexcalculator.core.ui.CatalogViewModel
import com.shohankhan.apexcalculator.core.ui.StepList

@Composable
fun FormulaDetailScreen(
    formulaId: String,
    onBack: () -> Unit,
    catalogViewModel: CatalogViewModel = viewModel(),
) {
    val formula = remember(formulaId) { com.shohankhan.apexcalculator.core.domain.formula.FormulaCatalog.find(formulaId) }
    if (formula == null) {
        com.shohankhan.apexcalculator.navigation.SecondaryScaffold(title = "Formula", onBack = onBack) { padding ->
            com.shohankhan.apexcalculator.core.designsystem.EmptyState(
                icon = ApexIcons.Formula,
                title = "Formula not found",
                message = "This formula is no longer part of the catalogue.",
                modifier = Modifier.padding(padding),
            )
        }
        return
    }
    val favorites by catalogViewModel.favorites.collectAsStateWithLifecycle()
    val isFavorite = CatalogViewModel.isFavorite(favorites, FavoriteKind.FORMULA, formula.id)
    val colors = MaterialTheme.apexColors
    val clipboard = LocalClipboardManager.current

    var values by rememberSaveable { mutableStateOf<Map<String, String>>(emptyMap()) }
    var submitted by rememberSaveable { mutableStateOf(false) }

    val outcome = remember(submitted, values, formula.id) {
        if (!submitted) null else compute(formula, values)
    }

    com.shohankhan.apexcalculator.navigation.SecondaryScaffold(
        title = formula.name,
        subtitle = formula.category.label,
        onBack = onBack,
        actions = {
            IconAction(
                icon = if (isFavorite) ApexIcons.Star else ApexIcons.StarOutline,
                description = if (isFavorite) "Remove from favorites" else "Add to favorites",
                tint = if (isFavorite) colors.warning else colors.textSecondary,
                onClick = { catalogViewModel.toggleFormula(formula) },
            )
            IconAction(
                icon = ApexIcons.Copy,
                description = "Copy result",
                onClick = {
                    val text = outcome?.let { result ->
                        when (result) {
                            is FormulaOutcome.Ok -> result.result.primaryText ?: result.result.primary.toString()
                            is FormulaOutcome.Error -> result.message
                        }
                    }
                    if (!text.isNullOrBlank()) clipboard.setText(AnnotatedString(text))
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
                        Text(
                            text = formula.expression,
                            style = MaterialTheme.typography.titleMedium.copy(
                                fontFamily = FontFamily.Monospace,
                                fontSize = 16.sp,
                            ),
                            color = colors.primary,
                        )
                        Spacer(Modifier.height(ApexSpacing.sm))
                        Text(
                            text = formula.description,
                            style = MaterialTheme.typography.bodyMedium,
                            color = colors.textSecondary,
                        )
                        Spacer(Modifier.height(ApexSpacing.sm))
                        Row(horizontalArrangement = Arrangement.spacedBy(ApexSpacing.xs)) {
                            SmallPill(formula.category.label)
                            SmallPill(formula.output.label, colors.textSecondary)
                        }
                    }
                }
            }

            item {
                ApexCard {
                    Column {
                        Text(
                            text = "Inputs",
                            style = MaterialTheme.typography.titleSmall,
                            color = colors.textPrimary,
                        )
                        Spacer(Modifier.height(ApexSpacing.sm))
                        formula.inputs.forEach { input ->
                            ApexTextField(
                                value = values[input.key].orEmpty(),
                                onValueChange = { newValue ->
                                    values = values + (input.key to newValue)
                                    submitted = false
                                },
                                label = if (input.unit.isNotBlank()) "${input.label} (${input.unit})" else input.label,
                                placeholder = input.hint,
                                modifier = Modifier.padding(bottom = ApexSpacing.sm),
                            )
                        }
                        Spacer(Modifier.height(ApexSpacing.xs))
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
                            Text("Calculate")
                        }
                    }
                }
            }

            when (val result = outcome) {
                null -> Unit
                is FormulaOutcome.Error -> item {
                    InfoBanner(text = result.message, tone = BannerTone.ERROR, title = "Check the inputs")
                }
                is FormulaOutcome.Ok -> item {
                    ApexCard {
                        Column {
                            Text(
                                text = result.result.primaryText ?: formatPrimary(result.result.primary),
                                style = MaterialTheme.typography.headlineSmall,
                                color = colors.primary,
                            )
                            Text(
                                text = formula.output.label + if (formula.output.unit.isNotBlank()) " (${formula.output.unit})" else "",
                                style = MaterialTheme.typography.labelSmall,
                                color = colors.textSecondary,
                            )
                            if (result.result.extras.isNotEmpty()) {
                                Spacer(Modifier.height(ApexSpacing.md))
                                result.result.extras.forEach { (label, value) ->
                                    ValueRow(label = label, value = value)
                                }
                            }
                            if (result.result.steps.isNotEmpty()) {
                                Spacer(Modifier.height(ApexSpacing.md))
                                Text(
                                    text = "Working",
                                    style = MaterialTheme.typography.titleSmall,
                                    color = colors.textPrimary,
                                )
                                Spacer(Modifier.height(ApexSpacing.sm))
                                StepList(result.result.steps)
                            }
                        }
                    }
                }
            }

            if (formula.assumptions != null) {
                item {
                    InfoBanner(
                        text = formula.assumptions,
                        tone = BannerTone.INFO,
                        title = "Assumptions",
                    )
                }
            }

            item {
                Text(
                    text = "Results are computed offline by the APEX math engine.",
                    style = MaterialTheme.typography.bodySmall,
                    color = colors.textTertiary,
                )
            }
        }
    }
}

private sealed interface FormulaOutcome {
    data class Ok(val result: FormulaResult) : FormulaOutcome

    data class Error(val message: String) : FormulaOutcome
}

private fun compute(formula: FormulaDefinition, raw: Map<String, String>): FormulaOutcome {
    val parsed = LinkedHashMap<String, Double>()
    for (input in formula.inputs) {
        val text = raw[input.key]?.trim().orEmpty()
        if (text.isEmpty()) return FormulaOutcome.Error("Enter a value for ${input.label}")
        val value = text.replace(",", "").toDoubleOrNull()
            ?: return FormulaOutcome.Error("${input.label} must be a number")
        if (!value.isFinite()) return FormulaOutcome.Error("${input.label} must be finite")
        parsed[input.key] = value
    }
    return try {
        FormulaOutcome.Ok(formula.compute(parsed))
    } catch (exception: FormulaException) {
        FormulaOutcome.Error(exception.message ?: "The inputs are not valid for this formula")
    } catch (exception: ArithmeticException) {
        FormulaOutcome.Error("The calculation overflowed or divided by zero")
    }
}

private fun formatPrimary(value: Double): String = formatResult(value)
