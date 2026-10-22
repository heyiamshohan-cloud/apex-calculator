package com.shohankhan.apexcalculator.core.domain.solver

import com.shohankhan.apexcalculator.core.math.AngleUnit
import com.shohankhan.apexcalculator.core.math.Fractions
import com.shohankhan.apexcalculator.core.math.Numerical
import com.shohankhan.apexcalculator.core.math.Rational
import com.shohankhan.apexcalculator.core.math.ResultFormatter
import java.math.MathContext

/** Top-level grouping used by Quick Solve and the Math Tools hub. */
enum class SolverCategory(val label: String) {
    FRACTIONS("Fractions"),
    RATIOS("Ratios & proportion"),
    NUMBER_THEORY("Number theory"),
    GEOMETRY("Geometry"),
    ALGEBRA("Algebra"),
    EQUATIONS("Equations"),
    SEQUENCES("Sequences"),
    EXPONENTS("Powers & roots"),
    LOGARITHMS("Logarithms"),
    STATISTICS("Statistics"),
    PROBABILITY("Probability"),
    CALCULUS("Calculus"),
    DATE_TIME("Date & time"),
}

/** Keyboard/format hint for a solver input. */
enum class SolverFieldKind { NUMBER, INTEGER, TEXT, EXPRESSION, LIST }

data class SolverField(
    val key: String,
    val label: String,
    val hint: String? = null,
    val kind: SolverFieldKind = SolverFieldKind.NUMBER,
    val suffix: String? = null,
)

/** Result of a solver: a headline value plus supporting rows and explanation steps. */
data class SolverOutput(
    val headline: String,
    val headlineLabel: String = "Result",
    val rows: List<Pair<String, String>> = emptyList(),
    val steps: List<String> = emptyList(),
    val note: String? = null,
)

/**
 * A single Quick Solve / Math Tools entry. [compute] receives the raw text of every field and
 * either returns an [SolverOutput] or throws [SolverException] with a message meant for the user.
 */
data class SolverSpec(
    val id: String,
    val title: String,
    val description: String,
    val category: SolverCategory,
    val fields: List<SolverField>,
    val tags: List<String> = emptyList(),
    val actionLabel: String = "Calculate",
    val compute: (Map<String, String>) -> SolverOutput,
)

/** Validation failure with a message that is safe to show next to the form. */
class SolverException(message: String) : Exception(message)

/** Shared formatting so every solver renders numbers consistently. */
object SolverFormat {
    fun number(value: Double): String = ResultFormatter.compact(value)

    fun fraction(value: Rational): String =
        if (value.isInteger) value.numerator.toString() else "${value.numerator}/${value.denominator}"

    fun mixed(value: Rational): String = Fractions.toMixed(value).display()
}

internal fun Map<String, String>.text(key: String, label: String): String {
    val value = this[key]?.trim().orEmpty()
    if (value.isEmpty()) throw SolverException("Enter $label")
    return value
}

internal fun Map<String, String>.number(key: String, label: String): Double {
    val raw = text(key, label).replace(",", "").replace(" ", "")
    val cleaned = raw.replace("−", "-")
    val value = cleaned.toDoubleOrNull()
        ?: throw SolverException("$label must be a number")
    if (!value.isFinite()) throw SolverException("$label must be finite")
    return value
}

internal fun Map<String, String>.integer(key: String, label: String): Long {
    val raw = text(key, label).replace(",", "").replace(" ", "").replace("−", "-")
    return raw.toLongOrNull() ?: throw SolverException("$label must be a whole number")
}

internal fun Map<String, String>.fraction(key: String, label: String): Rational = try {
    Fractions.parse(text(key, label))
} catch (exception: Exception) {
    throw SolverException("$label must be a fraction such as 3/4 or a decimal")
}

internal fun Map<String, String>.decimalList(key: String, label: String): List<Double> {
    val raw = text(key, label)
    val parts = raw.split(',', ';', ' ', '\n').map { it.trim() }.filter { it.isNotEmpty() }
    if (parts.isEmpty()) throw SolverException("Enter at least one value for $label")
    return parts.map { part ->
        part.replace("−", "-").toDoubleOrNull()
            ?: throw SolverException("\"$part\" is not a number")
    }
}

internal fun requirePositive(value: Double, label: String): Double {
    if (value <= 0.0) throw SolverException("$label must be greater than zero")
    return value
}

internal fun requireNonNegative(value: Double, label: String): Double {
    if (value < 0.0) throw SolverException("$label cannot be negative")
    return value
}

internal fun requireInteger(value: Double, label: String): Long {
    if (value != Math.floor(value) || Math.abs(value) > 9.0e15) {
        throw SolverException("$label must be a whole number")
    }
    return value.toLong()
}

internal fun fmt(value: Double): String = SolverFormat.number(value)

internal fun rationalText(value: Rational): String = SolverFormat.fraction(value)

internal fun decimalText(value: Rational): String =
    value.toBigDecimal(MathContext(12)).stripTrailingZeros().toPlainString()

internal fun requirePositiveValue(value: Double, label: String): Double = requirePositive(value, label)

internal fun requireNonNegativeValue(value: Double, label: String): Double = requireNonNegative(value, label)

/**
 * Solvers are pure functions, so the angle unit travels with the input map. The UI injects the
 * current setting under the "angle" key when a solver evaluates trigonometry.
 */
internal fun Map<String, String>.angleUnit(): AngleUnit = when (this["angle"]?.trim()?.uppercase()) {
    "RAD", "RADIAN", "RADIANS", "R" -> AngleUnit.RAD
    else -> AngleUnit.DEG
}

internal fun functionOf(expression: String, angleUnit: AngleUnit): (Double) -> Double =
    Numerical.functionOf(expression, angleUnit)

internal fun evaluateAt(expression: String, x: Double, angleUnit: AngleUnit): Double = try {
    functionOf(expression, angleUnit)(x)
} catch (exception: Exception) {
    throw SolverException(exception.message ?: "The expression could not be evaluated")
}

internal fun parsedDecimal(text: String, label: String): java.math.BigDecimal = try {
    java.math.BigDecimal(text.trim().replace(",", ""))
} catch (exception: NumberFormatException) {
    throw SolverException("$label must be a number")
}
