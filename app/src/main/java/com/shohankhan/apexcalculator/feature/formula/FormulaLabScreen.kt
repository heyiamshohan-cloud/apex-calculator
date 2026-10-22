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
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.horizontalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.shohankhan.apexcalculator.core.designsystem.ApexIcons
import com.shohankhan.apexcalculator.core.designsystem.ApexSpacing
import com.shohankhan.apexcalculator.core.designsystem.EmptyState
import com.shohankhan.apexcalculator.core.designsystem.SelectableChip
import com.shohankhan.apexcalculator.core.designsystem.apexColors
import com.shohankhan.apexcalculator.core.domain.formula.FormulaCatalog
import com.shohankhan.apexcalculator.core.domain.formula.FormulaCategory
import com.shohankhan.apexcalculator.core.domain.model.ApexTab
import com.shohankhan.apexcalculator.core.domain.model.FavoriteKind
import com.shohankhan.apexcalculator.core.ui.CatalogRow
import com.shohankhan.apexcalculator.core.ui.CatalogViewModel
import com.shohankhan.apexcalculator.core.ui.SearchBar
import com.shohankhan.apexcalculator.navigation.TabScaffold

@Composable
fun FormulaLabScreen(
    onOpenFormula: (String) -> Unit,
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
    val category = categoryName?.let { name -> FormulaCategory.entries.firstOrNull { it.name == name } }
    val results = remember(query, category) {
        val base = if (category != null) FormulaCatalog.byCategory(category) else FormulaCatalog.all
        if (query.isBlank()) base else base.filter { formula ->
            formula.name.contains(query, ignoreCase = true) ||
                formula.description.contains(query, ignoreCase = true) ||
                formula.expression.contains(query, ignoreCase = true) ||
                formula.tags.any { it.contains(query, ignoreCase = true) }
        }
    }

    TabScaffold(
        tab = ApexTab.FORMULA,
        onNavigate = onNavigate,
        onOpenHistory = onOpenHistory,
        onOpenFavorites = onOpenFavorites,
        onOpenSettings = onOpenSettings,
        onOpenAbout = onOpenAbout,
        onOpenPrivacy = onOpenPrivacy,
        subtitle = "${FormulaCatalog.all.size} offline formulas",
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(horizontal = ApexSpacing.md),
        ) {
            SearchBar(
                query = query,
                onQueryChange = { query = it },
                placeholder = "Search formulas",
            )
            Spacer(Modifier.height(ApexSpacing.sm))
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(ApexSpacing.sm),
            ) {
                SelectableChip(
                    label = "All (${FormulaCatalog.all.size})",
                    selected = category == null,
                    onClick = { categoryName = null },
                )
                FormulaCategory.entries.forEach { entry ->
                    SelectableChip(
                        label = "${entry.label} (${FormulaCatalog.countByCategory(entry)})",
                        selected = category == entry,
                        onClick = { categoryName = if (category == entry) null else entry.name },
                    )
                }
            }
            Spacer(Modifier.height(ApexSpacing.sm))
            if (results.isEmpty()) {
                EmptyState(
                    icon = ApexIcons.Formula,
                    title = "No formulas match",
                    message = "Try a different word or clear the category filter.",
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
                    items(results, key = { it.id }) { formula ->
                        CatalogRow(
                            title = formula.name,
                            subtitle = formula.description,
                            badge = formula.category.label,
                            isFavorite = CatalogViewModel.isFavorite(favorites, FavoriteKind.FORMULA, formula.id),
                            onClick = { onOpenFormula(formula.id) },
                        )
                    }
                    item {
                        Text(
                            text = "Every formula is evaluated locally by the APEX math engine.",
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
