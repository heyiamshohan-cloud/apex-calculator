package com.shohankhan.apexcalculator.feature.quicksolve

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
import androidx.compose.foundation.lazy.items
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
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.shohankhan.apexcalculator.core.designsystem.ApexIcons
import com.shohankhan.apexcalculator.core.designsystem.ApexSpacing
import com.shohankhan.apexcalculator.core.designsystem.EmptyState
import com.shohankhan.apexcalculator.core.designsystem.SelectableChip
import com.shohankhan.apexcalculator.core.designsystem.apexColors
import com.shohankhan.apexcalculator.core.domain.model.ApexTab
import com.shohankhan.apexcalculator.core.domain.model.FavoriteKind
import com.shohankhan.apexcalculator.core.domain.solver.SolverCategory
import com.shohankhan.apexcalculator.core.domain.solver.SolverSpec
import com.shohankhan.apexcalculator.navigation.TabScaffold

/** Quick Solve hub: every entry exposes a small form and a worked result. */
@Composable
fun QuickSolveScreen(
    solvers: List<SolverSpec>,
    onOpenSolver: (String) -> Unit,
    onNavigate: (ApexTab) -> Unit,
    onOpenHistory: () -> Unit,
    onOpenFavorites: () -> Unit,
    onOpenSettings: () -> Unit,
    onOpenAbout: () -> Unit,
    onOpenPrivacy: () -> Unit,
    favorites: List<com.shohankhan.apexcalculator.core.domain.model.FavoriteItem>,
    subtitle: String,
    emptyMessage: String,
    modifier: Modifier = Modifier,
) {
    var query by rememberSaveable { mutableStateOf("") }
    var categoryName by rememberSaveable { mutableStateOf<String?>(null) }
    val colors = MaterialTheme.apexColors
    val category = categoryName?.let { name -> SolverCategory.entries.firstOrNull { it.name == name } }
    val categories = remember(solvers) { solvers.map { it.category }.distinct() }
    val results = remember(query, category, solvers) {
        val filtered = if (category != null) solvers.filter { it.category == category } else solvers
        if (query.isBlank()) {
            filtered
        } else {
            filtered.filter { spec ->
                spec.title.contains(query, ignoreCase = true) ||
                    spec.description.contains(query, ignoreCase = true) ||
                    spec.category.label.contains(query, ignoreCase = true) ||
                    spec.tags.any { it.contains(query, ignoreCase = true) }
            }
        }
    }

    TabScaffold(
        tab = ApexTab.QUICKSOLVE,
        onNavigate = onNavigate,
        onOpenHistory = onOpenHistory,
        onOpenFavorites = onOpenFavorites,
        onOpenSettings = onOpenSettings,
        onOpenAbout = onOpenAbout,
        onOpenPrivacy = onOpenPrivacy,
        subtitle = subtitle,
    ) { padding ->
        Column(
            modifier = modifier
                .fillMaxSize()
                .padding(padding)
                .padding(horizontal = ApexSpacing.md),
        ) {
            com.shohankhan.apexcalculator.core.designsystem.ApexTextField(
                value = query,
                onValueChange = { query = it },
                label = "Search solvers",
                singleLine = true,
            )
            Spacer(Modifier.height(ApexSpacing.sm))
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(ApexSpacing.sm),
            ) {
                SelectableChip(
                    label = "All (${solvers.size})",
                    selected = category == null,
                    onClick = { categoryName = null },
                )
                categories.forEach { entry ->
                    SelectableChip(
                        label = entry.label,
                        selected = category == entry,
                        onClick = { categoryName = if (category == entry) null else entry.name },
                    )
                }
            }
            Spacer(Modifier.height(ApexSpacing.sm))
            if (results.isEmpty()) {
                EmptyState(
                    icon = ApexIcons.QuickSolve,
                    title = "No solver matches",
                    message = emptyMessage,
                    actionLabel = "Clear filters",
                    onAction = {
                        query = ""
                        categoryName = null
                    },
                )
            } else {
                LazyColumn(
                    contentPadding = PaddingValues(bottom = ApexSpacing.xl),
                    verticalArrangement = Arrangement.spacedBy(ApexSpacing.sm),
                ) {
                    items(results, key = { it.id }) { spec ->
                        com.shohankhan.apexcalculator.core.ui.CatalogRow(
                            title = spec.title,
                            subtitle = spec.description,
                            badge = spec.category.label,
                            icon = ApexIcons.QuickSolve,
                            isFavorite = com.shohankhan.apexcalculator.core.ui.CatalogViewModel.isFavorite(
                                favorites,
                                FavoriteKind.TOOL,
                                spec.id,
                            ),
                            onClick = { onOpenSolver(spec.id) },
                        )
                    }
                    item {
                        Text(
                            text = "Every solver shows the formula, the substitution and the result.",
                            style = MaterialTheme.typography.bodySmall,
                            color = colors.textTertiary,
                            modifier = Modifier.padding(vertical = ApexSpacing.md),
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun QuickSolveCatalogScreen(
    onOpenSolver: (String) -> Unit,
    onNavigate: (ApexTab) -> Unit,
    onOpenHistory: () -> Unit,
    onOpenFavorites: () -> Unit,
    onOpenSettings: () -> Unit,
    onOpenAbout: () -> Unit,
    onOpenPrivacy: () -> Unit,
    catalogViewModel: com.shohankhan.apexcalculator.core.ui.CatalogViewModel = viewModel(),
) {
    val favorites by catalogViewModel.favorites.collectAsStateWithLifecycle()
    val solvers = remember { com.shohankhan.apexcalculator.core.domain.solver.SolverCatalog.quickSolve }
    QuickSolveScreen(
        solvers = solvers,
        onOpenSolver = onOpenSolver,
        onNavigate = onNavigate,
        onOpenHistory = onOpenHistory,
        onOpenFavorites = onOpenFavorites,
        onOpenSettings = onOpenSettings,
        onOpenAbout = onOpenAbout,
        onOpenPrivacy = onOpenPrivacy,
        favorites = favorites,
        subtitle = "${solvers.size} step-by-step solvers",
        emptyMessage = "Try a different word or clear the category filter.",
    )
}
