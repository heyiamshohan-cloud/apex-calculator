package com.shohankhan.apexcalculator.core.ui

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.shohankhan.apexcalculator.ApexApplication
import com.shohankhan.apexcalculator.core.domain.formula.FormulaDefinition
import com.shohankhan.apexcalculator.core.domain.model.FavoriteItem
import com.shohankhan.apexcalculator.core.domain.model.FavoriteKind
import com.shohankhan.apexcalculator.core.domain.solver.SolverSpec
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

/** Shared favourites state for the formula catalogue, solvers and tools. */
class CatalogViewModel(application: Application) : AndroidViewModel(application) {

    private val container = (application as ApexApplication).container

    val favorites: StateFlow<List<FavoriteItem>> = container.favoritesRepository.observe()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    fun toggleFormula(formula: FormulaDefinition) {
        toggle(
            FavoriteItem(
                kind = FavoriteKind.FORMULA,
                refId = formula.id,
                title = formula.name,
                subtitle = formula.category.label,
                savedAt = System.currentTimeMillis(),
            ),
        )
    }

    fun toggleSolver(spec: SolverSpec) {
        toggle(
            FavoriteItem(
                kind = FavoriteKind.TOOL,
                refId = spec.id,
                title = spec.title,
                subtitle = spec.category.label,
                savedAt = System.currentTimeMillis(),
            ),
        )
    }

    fun toggleTool(id: String, title: String, subtitle: String) {
        toggle(
            FavoriteItem(
                kind = FavoriteKind.TOOL,
                refId = id,
                title = title,
                subtitle = subtitle,
                savedAt = System.currentTimeMillis(),
            ),
        )
    }

    fun remove(item: FavoriteItem) {
        viewModelScope.launch { runCatching { container.favoritesRepository.remove(item) } }
    }

    private fun toggle(item: FavoriteItem) {
        viewModelScope.launch { runCatching { container.favoritesRepository.toggle(item) } }
    }

    companion object {
        fun isFavorite(favorites: List<FavoriteItem>, kind: FavoriteKind, refId: String): Boolean =
            favorites.any { it.kind == kind && it.refId == refId }
    }
}
