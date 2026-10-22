package com.shohankhan.apexcalculator.core.domain.solver

/** Registry of every Quick Solve and Math Tools solver. */
object SolverCatalog {

    val all: List<SolverSpec> = buildList {
        addAll(fractionSolvers())
        addAll(ratioSolvers())
        addAll(numberTheorySolvers())
        addAll(geometrySolvers())
        addAll(algebraSolvers())
        addAll(sequenceSolvers())
        addAll(exponentSolvers())
        addAll(statisticsSolvers())
        addAll(probabilitySolvers())
        addAll(calculusSolvers())
        addAll(dateSolvers())
    }

    private val byId: Map<String, SolverSpec> = all.associateBy { it.id }

    init {
        require(byId.size == all.size) { "Solver identifiers must be unique" }
    }

    private val quickSolveCategories = setOf(
        SolverCategory.FRACTIONS,
        SolverCategory.RATIOS,
        SolverCategory.NUMBER_THEORY,
        SolverCategory.GEOMETRY,
        SolverCategory.ALGEBRA,
        SolverCategory.EQUATIONS,
        SolverCategory.SEQUENCES,
        SolverCategory.EXPONENTS,
        SolverCategory.LOGARITHMS,
        SolverCategory.STATISTICS,
        SolverCategory.PROBABILITY,
    )

    private val toolCategories = setOf(
        SolverCategory.STATISTICS,
        SolverCategory.PROBABILITY,
        SolverCategory.CALCULUS,
        SolverCategory.DATE_TIME,
    )

    val quickSolve: List<SolverSpec> = all.filter { it.category in quickSolveCategories }

    val mathTools: List<SolverSpec> = all.filter { it.category in toolCategories }

    fun find(id: String?): SolverSpec? = id?.let { byId[it] }

    fun search(query: String, source: List<SolverSpec> = all): List<SolverSpec> {
        val term = query.trim()
        if (term.isEmpty()) return source
        return source.filter { spec ->
            spec.title.contains(term, ignoreCase = true) ||
                spec.description.contains(term, ignoreCase = true) ||
                spec.category.label.contains(term, ignoreCase = true) ||
                spec.tags.any { it.contains(term, ignoreCase = true) }
        }
    }

    fun byCategory(category: SolverCategory, source: List<SolverSpec> = all): List<SolverSpec> =
        source.filter { it.category == category }
}
