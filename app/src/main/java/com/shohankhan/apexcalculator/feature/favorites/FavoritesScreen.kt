package com.shohankhan.apexcalculator.feature.favorites

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.shohankhan.apexcalculator.core.designsystem.ApexIcons
import com.shohankhan.apexcalculator.core.designsystem.ApexSpacing
import com.shohankhan.apexcalculator.core.designsystem.EmptyState
import com.shohankhan.apexcalculator.core.domain.model.FavoriteKind
import com.shohankhan.apexcalculator.core.ui.CatalogRow
import com.shohankhan.apexcalculator.core.ui.CatalogViewModel
import com.shohankhan.apexcalculator.navigation.SecondaryScaffold

@Composable
fun FavoritesScreen(
    onOpenFormula: (String) -> Unit,
    onOpenSolver: (String) -> Unit,
    onBack: () -> Unit,
    viewModel: CatalogViewModel = viewModel(),
) {
    val favorites by viewModel.favorites.collectAsStateWithLifecycle()
    val sorted = favorites.sortedByDescending { it.savedAt }
    SecondaryScaffold(
        title = "Favorites",
        subtitle = "${sorted.size} saved",
        onBack = onBack,
    ) { padding ->
        if (sorted.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding),
                contentAlignment = Alignment.TopCenter,
            ) {
                EmptyState(
                    icon = ApexIcons.StarOutline,
                    title = "No favorites yet",
                    message = "Use the star button on a formula or solver to keep it here.",
                    modifier = Modifier.padding(top = ApexSpacing.xl),
                )
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
                    .padding(horizontal = ApexSpacing.md),
                contentPadding = PaddingValues(bottom = ApexSpacing.xxl),
                verticalArrangement = Arrangement.spacedBy(ApexSpacing.sm),
            ) {
                items(sorted, key = { it.key }) { item ->
                    CatalogRow(
                        title = item.title,
                        subtitle = item.subtitle,
                        badge = item.kind.label,
                        isFavorite = true,
                        onClick = {
                            when (item.kind) {
                                FavoriteKind.FORMULA -> onOpenFormula(item.refId)
                                FavoriteKind.TOOL -> onOpenSolver(item.refId)
                            }
                        },
                    )
                }
            }
        }
    }
}
