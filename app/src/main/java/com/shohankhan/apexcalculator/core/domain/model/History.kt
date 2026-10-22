package com.shohankhan.apexcalculator.core.domain.model

/** Where a calculation came from. Used for grouping and labelling history records. */
enum class CalculationSource(val label: String) {
    STANDARD("Standard"),
    SCIENTIFIC("Scientific"),
    PROGRAMMER("Programmer"),
    QUICK_SOLVE("Quick Solve"),
    FORMULA("Formula Lab"),
    TOOLS("Math Tools"),
}

data class HistoryRecord(
    val id: Long = 0L,
    val expression: String,
    val result: String,
    val source: CalculationSource,
    val createdAt: Long,
    val details: String? = null,
)

enum class FavoriteKind(val label: String) {
    FORMULA("Formula"),
    TOOL("Tool"),
}

data class FavoriteItem(
    val kind: FavoriteKind,
    val refId: String,
    val title: String,
    val subtitle: String,
    val savedAt: Long,
) {
    val key: String get() = "${kind.name}:$refId"
}

/**
 * Memory register shared by the standard and scientific workspaces. The value is stored as
 * plain expression text so that exact values (fractions, large integers) survive a round trip.
 */
data class MemoryState(val text: String? = null) {
    val isSet: Boolean get() = !text.isNullOrBlank()

    companion object {
        val EMPTY = MemoryState()
    }
}
