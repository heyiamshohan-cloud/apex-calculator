package com.shohankhan.apexcalculator.feature.tools

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.shohankhan.apexcalculator.core.designsystem.ApexCard
import com.shohankhan.apexcalculator.core.designsystem.ApexIcons
import com.shohankhan.apexcalculator.core.designsystem.ApexSpacing
import com.shohankhan.apexcalculator.core.designsystem.apexColors
import com.shohankhan.apexcalculator.core.domain.model.ApexTab
import com.shohankhan.apexcalculator.core.domain.model.FavoriteKind
import com.shohankhan.apexcalculator.core.domain.solver.SolverCatalog
import com.shohankhan.apexcalculator.core.ui.CatalogRow
import com.shohankhan.apexcalculator.core.ui.CatalogViewModel
import com.shohankhan.apexcalculator.navigation.TabScaffold

/** Math Tools hub: dedicated workspaces plus every solver that is not part of Quick Solve. */
@Composable
fun ToolsHubScreen(
    onOpenSolver: (String) -> Unit,
    onOpenMatrix: () -> Unit,
    onOpenComplex: () -> Unit,
    onNavigate: (ApexTab) -> Unit,
    onOpenHistory: () -> Unit,
    onOpenFavorites: () -> Unit,
    onOpenSettings: () -> Unit,
    onOpenAbout: () -> Unit,
    onOpenPrivacy: () -> Unit,
    catalogViewModel: CatalogViewModel = viewModel(),
) {
    val favorites by catalogViewModel.favorites.collectAsStateWithLifecycle()
    var query by rememberSaveable { mutableStateOf("") }
    val colors = MaterialTheme.apexColors
    val tools = remember { SolverCatalog.mathTools }
    val results = remember(query, tools) { SolverCatalog.search(query, tools) }

    TabScaffold(
        tab = ApexTab.TOOLS,
        onNavigate = onNavigate,
        onOpenHistory = onOpenHistory,
        onOpenFavorites = onOpenFavorites,
        onOpenSettings = onOpenSettings,
        onOpenAbout = onOpenAbout,
        onOpenPrivacy = onOpenPrivacy,
        subtitle = "Workspaces and offline tools",
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(horizontal = ApexSpacing.md),
            contentPadding = PaddingValues(bottom = ApexSpacing.xl),
            verticalArrangement = Arrangement.spacedBy(ApexSpacing.sm),
        ) {
            item {
                com.shohankhan.apexcalculator.core.designsystem.ApexTextField(
                    value = query,
                    onValueChange = { query = it },
                    label = "Search tools",
                    singleLine = true,
                )
                Spacer(Modifier.height(ApexSpacing.md))
            }
            if (query.isBlank()) {
                item {
                    ApexCard {
                        Column {
                            Text("Workspaces", style = MaterialTheme.typography.titleSmall, color = colors.textPrimary)
                            Spacer(Modifier.height(ApexSpacing.xs))
                            Text(
                                text = "Interactive tools with their own state: matrices, complex arithmetic and the full solver set.",
                                style = MaterialTheme.typography.bodySmall,
                                color = colors.textSecondary,
                            )
                        }
                    }
                }
                item {
                    CatalogRow(
                        title = "Matrix calculator",
                        subtitle = "Determinant, inverse, RREF, rank, transpose and linear systems up to 8×8.",
                        icon = ApexIcons.Grid,
                        onClick = onOpenMatrix,
                    )
                }
                item {
                    CatalogRow(
                        title = "Complex numbers",
                        subtitle = "Add, subtract, multiply, divide and raise complex numbers; see modulus, argument and polar form.",
                        icon = ApexIcons.Scientific,
                        onClick = onOpenComplex,
                    )
                }
                item {
                    CatalogRow(
                        title = "Statistics workspace",
                        subtitle = "Mean, median, mode, spread, quartiles and outliers for any data set.",
                        icon = ApexIcons.Sigma,
                        onClick = { onOpenSolver("dataset_summary") },
                    )
                }
                item {
                    CatalogRow(
                        title = "Probability",
                        subtitle = "Permutations, combinations, binomial distribution and event algebra.",
                        icon = ApexIcons.Percent,
                        onClick = { onOpenSolver("binomial_solver") },
                    )
                }
                item {
                    CatalogRow(
                        title = "Sequences & summations",
                        subtitle = "Arithmetic and geometric progressions, infinite sums and Fibonacci numbers.",
                        icon = ApexIcons.Tools,
                        onClick = { onOpenSolver("sequence_arithmetic_sum") },
                    )
                }
                item {
                    CatalogRow(
                        title = "Date & time",
                        subtitle = "Differences, weekday counts, date shifting and durations — fully offline.",
                        icon = ApexIcons.Calendar,
                        onClick = { onOpenSolver("days_between") },
                    )
                }
                item {
                    CatalogRow(
                        title = "Numerical calculus",
                        subtitle = "Derivatives, definite integrals, root finding and function tables.",
                        icon = ApexIcons.QuickSolve,
                        onClick = { onOpenSolver("derivative_point") },
                    )
                }
                item {
                    Spacer(Modifier.height(ApexSpacing.sm))
                    Text(
                        text = "All tools (${tools.size})",
                        style = MaterialTheme.typography.titleSmall,
                        color = colors.textPrimary,
                        modifier = Modifier.padding(vertical = ApexSpacing.xs),
                    )
                }
            }
            items(results, key = { it.id }) { spec ->
                CatalogRow(
                    title = spec.title,
                    subtitle = spec.description,
                    badge = spec.category.label,
                    isFavorite = CatalogViewModel.isFavorite(favorites, FavoriteKind.TOOL, spec.id),
                    onClick = { onOpenSolver(spec.id) },
                )
            }
        }
    }
}
