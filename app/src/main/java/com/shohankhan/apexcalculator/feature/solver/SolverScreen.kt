package com.shohankhan.apexcalculator.feature.solver

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
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
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
import androidx.compose.ui.text.input.KeyboardType
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
import com.shohankhan.apexcalculator.core.designsystem.SmallPill
import com.shohankhan.apexcalculator.core.designsystem.ValueRow
import com.shohankhan.apexcalculator.core.designsystem.apexColors
import com.shohankhan.apexcalculator.core.domain.model.FavoriteKind
import com.shohankhan.apexcalculator.core.domain.solver.SolverCatalog
import com.shohankhan.apexcalculator.core.domain.solver.SolverException
import com.shohankhan.apexcalculator.core.domain.solver.SolverFieldKind
import com.shohankhan.apexcalculator.core.domain.solver.SolverOutput
import com.shohankhan.apexcalculator.core.ui.CatalogViewModel
import com.shohankhan.apexcalculator.core.ui.StepList
import com.shohankhan.apexcalculator.navigation.SecondaryScaffold

private sealed interface SolverState {
    data object Idle : SolverState

    data class Ok(val output: SolverOutput) : SolverState

    data class Error(val message: String) : SolverState
}

@Composable
fun SolverScreen(
    solverId: String,
    onBack: () -> Unit,
    catalogViewModel: CatalogViewModel = viewModel(),
) {
    val spec = remember(solverId) { SolverCatalog.find(solverId) }
    if (spec == null) {
        SolverUnavailable(onBack = onBack)
        return
    }
    val favorites by catalogViewModel.favorites.collectAsStateWithLifecycle()
    val isFavorite = CatalogViewModel.isFavorite(favorites, FavoriteKind.TOOL, spec.id)
    val colors = MaterialTheme.apexColors
    val clipboard = LocalClipboardManager.current

    var values by rememberSaveable { mutableStateOf<Map<String, String>>(emptyMap()) }
    var state by remember(solverId) { mutableStateOf<SolverState>(SolverState.Idle) }

    fun solve() {
        state = try {
            SolverState.Ok(spec.compute(values))
        } catch (exception: SolverException) {
            SolverState.Error(exception.message ?: "Check the inputs")
        } catch (exception: ArithmeticException) {
            SolverState.Error("The calculation overflowed or divided by zero")
        }
    }

    fun clear() {
        values = emptyMap()
        state = SolverState.Idle
    }

    SecondaryScaffold(
        title = spec.title,
        subtitle = spec.category.label,
        onBack = onBack,
        actions = {
            IconAction(
                icon = if (isFavorite) ApexIcons.Star else ApexIcons.StarOutline,
                description = if (isFavorite) "Remove from favorites" else "Add to favorites",
                tint = if (isFavorite) colors.warning else colors.textSecondary,
                onClick = { catalogViewModel.toggleSolver(spec) },
            )
            IconAction(
                icon = ApexIcons.Refresh,
                description = "Clear inputs",
                onClick = { clear() },
            )
            IconAction(
                icon = ApexIcons.Copy,
                description = "Copy result",
                onClick = {
                    val text = when (val current = state) {
                        is SolverState.Ok -> buildString {
                            append(current.output.headlineLabel)
                            append(": ")
                            append(current.output.headline)
                            current.output.rows.forEach { (label, value) ->
                                append("\n")
                                append(label)
                                append(": ")
                                append(value)
                            }
                        }
                        is SolverState.Error -> current.message
                        SolverState.Idle -> ""
                    }
                    if (text.isNotEmpty()) clipboard.setText(AnnotatedString(text))
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
                        Row(horizontalArrangement = Arrangement.spacedBy(ApexSpacing.xs)) {
                            SmallPill(spec.category.label)
                            if (spec.tags.isNotEmpty()) SmallPill(spec.tags.first(), colors.textSecondary)
                        }
                        Spacer(Modifier.height(ApexSpacing.sm))
                        Text(
                            text = spec.description,
                            style = MaterialTheme.typography.bodyMedium,
                            color = colors.textSecondary,
                        )
                    }
                }
            }

            item {
                ApexCard {
                    Column {
                        Text("Inputs", style = MaterialTheme.typography.titleSmall, color = colors.textPrimary)
                        Spacer(Modifier.height(ApexSpacing.sm))
                        spec.fields.forEach { field ->
                            ApexTextField(
                                value = values[field.key].orEmpty(),
                                onValueChange = {
                                    values = values + (field.key to it)
                                    state = SolverState.Idle
                                },
                                label = field.label,
                                placeholder = field.hint,
                                suffix = field.suffix,
                                singleLine = field.kind != SolverFieldKind.LIST
                                    && field.kind != SolverFieldKind.EXPRESSION,
                                minLines = if (field.kind == SolverFieldKind.LIST) 2 else 1,
                                modifier = Modifier.padding(bottom = ApexSpacing.sm),
                            )
                        }
                        Spacer(Modifier.height(ApexSpacing.xs))
                        Button(
                            onClick = { solve() },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(48.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = colors.primary,
                                contentColor = colors.onPrimary,
                            ),
                        ) {
                            Text(spec.actionLabel)
                        }
                    }
                }
            }

            when (val current = state) {
                SolverState.Idle -> Unit
                is SolverState.Error -> item {
                    InfoBanner(
                        text = current.message,
                        tone = BannerTone.ERROR,
                        title = "Check the inputs",
                    )
                }
                is SolverState.Ok -> item {
                    SolverResultCard(output = current.output)
                }
            }

            item {
                Text(
                    text = "Computed offline by the APEX math engine.",
                    style = MaterialTheme.typography.bodySmall,
                    color = colors.textTertiary,
                )
            }
        }
    }
}

@Composable
private fun SolverResultCard(output: SolverOutput) {
    val colors = MaterialTheme.apexColors
    ApexCard {
        Column {
            Text(
                text = output.headline,
                style = MaterialTheme.typography.headlineMedium,
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
                Text("Step by step", style = MaterialTheme.typography.titleSmall, color = colors.textPrimary)
                Spacer(Modifier.height(ApexSpacing.sm))
                StepList(output.steps)
            }
            if (output.note != null) {
                Spacer(Modifier.height(ApexSpacing.md))
                InfoBanner(text = output.note, tone = BannerTone.INFO)
            }
        }
    }
}
