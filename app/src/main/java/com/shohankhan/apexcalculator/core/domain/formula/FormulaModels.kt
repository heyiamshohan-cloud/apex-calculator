package com.shohankhan.apexcalculator.core.domain.formula

/** Top level grouping used by the Formula Lab browser. */
enum class FormulaCategory(val label: String, val description: String) {
    PERCENTAGE("Percentages", "Percentage change, discounts, tax and proportions"),
    FINANCE("Interest & finance", "Simple and compound interest, loans, savings"),
    TIME_SPEED("Time & speed", "Distance, speed, work and duration"),
    GEOMETRY("Geometry", "Areas, surface areas and volumes"),
    ALGEBRA("Algebra & sequences", "Equations, progressions and polynomials"),
    PHYSICS("Physics", "Mechanics, waves and electricity"),
    STATISTICS("Statistics", "Averages, spread and probability"),
}

enum class InputKind { DECIMAL, INTEGER, PERCENT }

data class FormulaInput(
    val key: String,
    val label: String,
    val unit: String = "",
    val hint: String? = null,
    val kind: InputKind = InputKind.DECIMAL,
)

data class FormulaOutput(val label: String, val unit: String = "")

data class FormulaResult(
    val primary: Double,
    val extras: List<Pair<String, String>> = emptyList(),
    val steps: List<String> = emptyList(),
    /** Optional headline text used when the answer is not a single number (for example complex roots). */
    val primaryText: String? = null,
) {
    companion object {
        fun of(value: Double, vararg extras: Pair<String, String>) = FormulaResult(value, extras.toList())

        fun steps(value: Double, steps: List<String>, vararg extras: Pair<String, String>) =
            FormulaResult(value, extras.toList(), steps)
    }
}

/** Raised for invalid user input inside a formula; the message is shown inline. */
class FormulaException(message: String) : Exception(message)

data class FormulaDefinition(
    val id: String,
    val name: String,
    val category: FormulaCategory,
    val expression: String,
    val description: String,
    val inputs: List<FormulaInput>,
    val output: FormulaOutput,
    val assumptions: String? = null,
    val tags: List<String> = emptyList(),
    val compute: (Map<String, Double>) -> FormulaResult,
)

/** Small DSL used to declare formulas compactly and consistently. */
internal fun input(
    key: String,
    label: String,
    unit: String = "",
    hint: String? = null,
    kind: InputKind = InputKind.DECIMAL,
): FormulaInput = FormulaInput(key, label, unit, hint, kind)

internal fun formula(
    id: String,
    name: String,
    category: FormulaCategory,
    expression: String,
    description: String,
    inputs: List<FormulaInput>,
    output: FormulaOutput,
    assumptions: String? = null,
    tags: List<String> = emptyList(),
    compute: (Map<String, Double>) -> FormulaResult,
): FormulaDefinition = FormulaDefinition(
    id = id,
    name = name,
    category = category,
    expression = expression,
    description = description,
    inputs = inputs,
    output = output,
    assumptions = assumptions,
    tags = tags,
    compute = compute,
)

internal fun values(vararg pairs: Pair<String, Double>): Map<String, Double> = pairs.toMap()

internal fun Map<String, Double>.value(key: String, label: String): Double =
    this[key] ?: throw FormulaException("Enter a value for $label")

internal fun requirePositive(value: Double, label: String): Double {
    if (!value.isFinite()) throw FormulaException("$label must be a finite number")
    if (value <= 0.0) throw FormulaException("$label must be greater than zero")
    return value
}

internal fun requireNonNegative(value: Double, label: String): Double {
    if (!value.isFinite()) throw FormulaException("$label must be a finite number")
    if (value < 0.0) throw FormulaException("$label cannot be negative")
    return value
}

internal fun requireInteger(value: Double, label: String): Long {
    if (!value.isFinite()) throw FormulaException("$label must be a finite number")
    if (value != Math.floor(value)) throw FormulaException("$label must be a whole number")
    return value.toLong()
}

/** Compact, locale independent number rendering used inside formula explanations. */
internal fun fmt(value: Double): String {
    if (!value.isFinite()) return "undefined"
    if (value == Math.floor(value) && kotlin.math.abs(value) < 1e15) return value.toLong().toString()
    val rounded = (Math.round(value * 1e6) / 1e6)
    return java.math.BigDecimal.valueOf(rounded).stripTrailingZeros().toPlainString()
}

/** Public formatter used by the formula screens (same rules as quick solve output). */
fun formatResult(value: Double): String = fmt(value)
