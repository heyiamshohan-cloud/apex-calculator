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
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.horizontalScroll
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
import com.shohankhan.apexcalculator.core.domain.solver.SolverCatalog
import com.shohankhan.apexcalculator.core.domain.solver.SolverCategory
import com.shohankhan.apexcalculator.core.ui.CatalogRow
import com.shohankhan.apexcalculator.core.ui.CatalogViewModel
import com.shohankhan.apexcalculator.core.ui.SearchBar
import com.shohankhan.apexcalculator.navigation.TabScaffold

@Composable
fun QuickSolveScreen(
    onOpenSolver: (String) -> Unit,
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
    var categoryName by rememberSaveable { mutableStateOf<String?>(null) }
    val colors = MaterialTheme.apexColors
    val source = SolverCatalog.quickSolve
    val category = categoryName?.let { name -> SolverCategory.entries.firstOrNull { it.name == name } }
    val categories = remember(source) { source.map { it.category }.distinct() }
    val results = remember(query, category, source) {
        val filtered = if (category != null) source.filter { it.category == category } else source
        SolverCatalog.search(query, filtered)
    }

    TabScaffold(
        tab = ApexTab.QUICKSOLVE,
        onNavigate = onNavigate,
        onOpenHistory = onOpenHistory,
        onOpenFavorites = onOpenFavorites,
        onOpenSettings = onOpenSettings,
        onOpenAbout = onOpenAbout,
        onOpenPrivacy = onOpenPrivacy,
        subtitle = "${source.size} step-by-step solvers",
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(horizontal = ApexSpacing.md),
        ) {
            SearchBar(query = query, onQueryChange = { query = it }, placeholder = "Search solvers")
            Spacer(Modifier.height(ApexSpacing.sm))
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(ApexSpacing.sm),
            ) {
                SelectableChip(
                    label = "All",
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
                    message = "Try a different search or clear the filter.",
                    actionLabel = "Clear filters",
                    onAction = {
                        query = ""
                        categoryName = null
                    },
                )
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(bottom = ApexSpacing.xl),
                    verticalArrangement = Arrangement.spacedBy(ApexSpacing.sm),
                ) {
                    items(results, key = { it.id }) { spec ->
                        CatalogRow(
                            title = spec.title,
                            subtitle = spec.description,
                            badge = spec.category.label,
                            icon = ApexIcons.QuickSolve,
                            isFavorite = CatalogViewModel.isFavorite(favorites, FavoriteKind.TOOL, spec.id),
                            onClick = { onOpenSolver(spec.id) },
                        )
                    }
                    item {
                        Text(
                            text = "Each solver shows the formula, the substitution and the result.",
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
fun SolverUnavailable(
    onBack: () -> Unit,
) {
    com.shohankhan.apexcalculator.navigation.SecondaryScaffold(title = "Solver", onBack = onBack) { padding ->
        EmptyState(
            icon = ApexIcons.Warning,
            title = "Solver not found",
            message = "This solver is no longer available.",
            modifier = Modifier.padding(padding),
        )
    }
}
