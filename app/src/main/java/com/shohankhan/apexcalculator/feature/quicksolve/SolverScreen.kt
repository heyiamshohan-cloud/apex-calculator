package com.shohankhan.apexcalculator.feature.quicksolve

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
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
import com.shohankhan.apexcalculator.core.designsystem.BannerTone
import com.shohankhan.apexcalculator.core.designsystem.EmptyState
import com.shohankhan.apexcalculator.core.designsystem.IconAction
import com.shohankhan.apexcalculator.core.designsystem.InfoBanner
import com.shohankhan.apexcalculator.core.designsystem.SmallPill
import com.shohankhan.apexcalculator.core.designsystem.apexColors
import com.shohankhan.apexcalculator.core.domain.model.CalculationSource
import com.shohankhan.apexcalculator.core.domain.model.FavoriteKind
import com.shohankhan.apexcalculator.core.domain.solver.SolverCatalog
import com.shohankhan.apexcalculator.core.domain.solver.SolverException
import com.shohankhan.apexcalculator.core.domain.solver.SolverOutput
import com.shohankhan.apexcalculator.core.domain.solver.SolverSpec
import com.shohankhan.apexcalculator.core.ui.CatalogViewModel
import com.shohankhan.apexcalculator.core.ui.SessionViewModel
import com.shohankhan.apexcalculator.navigation.SecondaryScaffold

private sealed interface SolverState {
    data object Idle : SolverState

    data class Done(val output: SolverOutput) : SolverState

    data class Failed(val message: String) : SolverState
}

/**
 * Generic solver screen: renders the declared fields, runs the pure compute function and shows
 * the worked result. Failed validation is reported next to the form, never as a fake result.
 */
@Composable
fun SolverScreen(
    solverId: String,
    source: CalculationSource,
    onBack: () -> Unit,
    sessionViewModel: SessionViewModel = viewModel(),
    catalogViewModel: CatalogViewModel = viewModel(),
    settingsViewModel: com.shohankhan.apexcalculator.core.ui.SettingsStateViewModel = viewModel(),
) {
    val settings by settingsViewModel.settings.collectAsStateWithLifecycle()
    val angleUnit = settings.angleMode.name
    val spec = remember(solverId) { SolverCatalog.find(solverId) }
    if (spec == null) {
        SecondaryScaffold(title = "Solver", onBack = onBack) { padding ->
            EmptyState(
                icon = ApexIcons.Warning,
                title = "Solver not found",
                message = "This solver is no longer part of the catalogue.",
                modifier = Modifier.padding(padding),
            )
        }
        return
    }
    val favorites by catalogViewModel.favorites.collectAsStateWithLifecycle()
    val isFavorite = CatalogViewModel.isFavorite(favorites, FavoriteKind.TOOL, spec.id)
    val colors = MaterialTheme.apexColors
    val clipboard = LocalClipboardManager.current

    var values by rememberSaveable(spec.id) { mutableStateOf<Map<String, String>>(emptyMap()) }
    var state by remember(spec.id) { mutableStateOf<SolverState>(SolverState.Idle) }

    fun calculate() {
        val inputs = if (spec.fields.any { it.label.contains("f(x)", ignoreCase = true) || it.key == "expression" }) {
            values + ("angle" to angleUnit)
        } else {
            values
        }
        state = try {
            val output = spec.compute(inputs)
            sessionViewModel.record(
                source = source,
                expression = "${spec.title}: " + spec.fields.joinToString(", ") { field ->
                    "${field.label}=${values[field.key].orEmpty()}"
                },
                result = output.headline,
            )
            SolverState.Done(output)
        } catch (exception: SolverException) {
            SolverState.Failed(exception.message ?: "Check the inputs")
        } catch (exception: ArithmeticException) {
            SolverState.Failed("The calculation overflowed or divided by zero")
        }
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
                icon = ApexIcons.Copy,
                description = "Copy result",
                onClick = {
                    val text = when (val current = state) {
                        is SolverState.Done -> buildString {
                            append(current.output.headlineLabel).append(": ").append(current.output.headline)
                            current.output.rows.forEach { (label, value) -> append("\n").append(label).append(": ").append(value) }
                        }
                        is SolverState.Failed -> current.message
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
                    androidx.compose.foundation.layout.Column {
                        SmallPill(spec.category.label)
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
                SolverFormCard(
                    fields = spec.fields,
                    values = values,
                    onValueChange = { key, value ->
                        values = values + (key to value)
                        state = SolverState.Idle
                    },
                    actionLabel = spec.actionLabel,
                    onCalculate = ::calculate,
                )
            }
            when (val current = state) {
                SolverState.Idle -> Unit
                is SolverState.Failed -> item {
                    InfoBanner(text = current.message, tone = BannerTone.ERROR, title = "Check the inputs")
                }
                is SolverState.Done -> item {
                    SolverResultCard(current.output)
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
