package com.shohankhan.apexcalculator.core.domain.formula

/** The complete offline formula library. */
object FormulaCatalog {

    val all: List<FormulaDefinition> = buildList {
        addAll(percentageFormulas())
        addAll(financeFormulas())
        addAll(timeSpeedFormulas())
        addAll(geometryFormulas())
        addAll(algebraFormulas())
        addAll(physicsFormulas())
        addAll(statisticsFormulas())
    }

    private val byId: Map<String, FormulaDefinition> = all.associateBy { it.id }

    init {
        require(byId.size == all.size) { "Formula identifiers must be unique" }
    }

    fun find(id: String?): FormulaDefinition? = id?.let { byId[it] }

    fun byCategory(category: FormulaCategory): List<FormulaDefinition> = all.filter { it.category == category }

    fun countByCategory(category: FormulaCategory): Int = byCategory(category).size

    fun search(query: String): List<FormulaDefinition> {
        val term = query.trim()
        if (term.isEmpty()) return all
        return all.filter { formula ->
            formula.name.contains(term, ignoreCase = true) ||
                formula.description.contains(term, ignoreCase = true) ||
                formula.expression.contains(term, ignoreCase = true) ||
                formula.category.label.contains(term, ignoreCase = true) ||
                formula.tags.any { it.contains(term, ignoreCase = true) }
        }
    }
}
