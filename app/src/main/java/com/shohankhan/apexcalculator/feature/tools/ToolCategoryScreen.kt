package com.shohankhan.apexcalculator.feature.tools

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.shohankhan.apexcalculator.core.designsystem.ApexSpacing
import com.shohankhan.apexcalculator.core.domain.model.FavoriteKind
import com.shohankhan.apexcalculator.core.domain.solver.SolverCatalog
import com.shohankhan.apexcalculator.core.domain.solver.SolverCategory
import com.shohankhan.apexcalculator.core.ui.CatalogRow
import com.shohankhan.apexcalculator.core.ui.CatalogViewModel
import com.shohankhan.apexcalculator.navigation.SecondaryScaffold

/** Solvers grouped under one category (probability, sequences, date & time...). */
@Composable
fun ToolCategoryScreen(
    categoryName: String,
    onOpenSolver: (String) -> Unit,
    onBack: () -> Unit,
    catalogViewModel: CatalogViewModel = viewModel(),
) {
    val category = remember(categoryName) { SolverCategory.entries.firstOrNull { it.name == categoryName } }
    val favorites by catalogViewModel.favorites.collectAsStateWithLifecycle()
    val solvers = remember(category) { category?.let { SolverCatalog.byCategory(it) }.orEmpty() }

    SecondaryScaffold(
        title = category?.label ?: "Tools",
        subtitle = "${solvers.size} solvers",
        onBack = onBack,
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(horizontal = ApexSpacing.md),
            contentPadding = PaddingValues(bottom = ApexSpacing.xxl),
            verticalArrangement = Arrangement.spacedBy(ApexSpacing.sm),
        ) {
            items(solvers, key = { it.id }) { spec ->
                CatalogRow(
                    title = spec.title,
                    subtitle = spec.description,
                    isFavorite = CatalogViewModel.isFavorite(favorites, FavoriteKind.TOOL, spec.id),
                    onClick = { onOpenSolver(spec.id) },
                )
            }
        }
    }
}
