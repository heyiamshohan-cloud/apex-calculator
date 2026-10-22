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
import com.shohankhan.apexcalculator.core.ui.SearchBar
import com.shohankhan.apexcalculator.navigation.TabScaffold

/** Entry point of the Math Tools tab: workspaces first, then the individual solvers. */
@Composable
fun MathToolsScreen(
    onOpenSolver: (String) -> Unit,
    onOpenMatrix: () -> Unit,
    onOpenComplex: () -> Unit,
    onOpenCategory: (String) -> Unit,
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
    val tools = SolverCatalog.mathTools
    val results = remember(query, tools) { SolverCatalog.search(query, tools) }
    val showWorkspaces = query.isBlank()

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
                SearchBar(query = query, onQueryChange = { query = it }, placeholder = "Search tools")
                Spacer(Modifier.height(ApexSpacing.md))
            }
            if (showWorkspaces) {
                item {
                    ApexCard {
                        Column {
                            Text(
                                text = "Workspaces",
                                style = MaterialTheme.typography.titleSmall,
                                color = colors.textPrimary,
                            )
                            Spacer(Modifier.height(ApexSpacing.xs))
                            Text(
                                text = "Interactive tools that keep their own state while you switch tabs.",
                                style = MaterialTheme.typography.bodySmall,
                                color = colors.textSecondary,
                            )
                        }
                    }
                }
                item {
                    CatalogRow(
                        title = "Matrix calculator",
                        subtitle = "Determinant, inverse, RREF, rank, multiply and linear systems up to 8×8.",
                        icon = ApexIcons.Grid,
                        onClick = onOpenMatrix,
                    )
                }
                item {
                    CatalogRow(
                        title = "Complex number workspace",
                        subtitle = "Evaluate expressions, convert to polar form and inspect modulus and argument.",
                        icon = ApexIcons.Scientific,
                        onClick = onOpenComplex,
                    )
                }
                item {
                    CatalogRow(
                        title = "Statistics workspace",
                        subtitle = "Full descriptive report for a dataset: centre, spread, quartiles and outliers.",
                        icon = ApexIcons.Sigma,
                        onClick = { onOpenSolver("dataset_summary") },
                    )
                }
                item {
                    CatalogRow(
                        title = "Date & time tools",
                        subtitle = "Day differences, weekday counts, date shifts and durations.",
                        icon = ApexIcons.Calendar,
                        onClick = { onOpenCategory("DATE_TIME") },
                    )
                }
                item {
                    CatalogRow(
                        title = "Probability tools",
                        subtitle = "Binomial, combinations, permutations and conditional probability.",
                        icon = ApexIcons.Percent,
                        onClick = { onOpenCategory("PROBABILITY") },
                    )
                }
                item {
                    CatalogRow(
                        title = "Sequences & summations",
                        subtitle = "Arithmetic and geometric progressions, sums and term generation.",
                        icon = ApexIcons.Tools,
                        onClick = { onOpenCategory("SEQUENCES") },
                    )
                }
                item {
                    Spacer(Modifier.height(ApexSpacing.sm))
                    Text(
                        text = "All solvers",
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
