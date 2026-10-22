package com.shohankhan.apexcalculator.core.domain.solver

import com.shohankhan.apexcalculator.core.math.Fractions
import com.shohankhan.apexcalculator.core.math.Rational
import com.shohankhan.apexcalculator.core.math.ResultFormatter
import java.math.BigDecimal

/** Grouping used by Quick Solve and Math Tools. */
enum class SolverCategory(val label: String) {
    FRACTIONS("Fractions"),
    RATIOS("Ratios & proportion"),
    NUMBER_THEORY("Number theory"),
    ALGEBRA("Algebra"),
    EQUATIONS("Equations"),
    GEOMETRY("Geometry"),
    SEQUENCES("Sequences"),
    EXPONENTS("Exponents & roots"),
    LOGARITHMS("Logarithms"),
    STATISTICS("Statistics"),
    PROBABILITY("Probability"),
    CALCULUS("Calculus"),
    COMPLEX("Complex numbers"),
    DATE_TIME("Date & time"),
    CONVERSIONS("Conversions"),
}

enum class SolverFieldKind { NUMBER, INTEGER, TEXT, EXPRESSION, LIST }

data class SolverField(
    val key: String,
    val label: String,
    val hint: String? = null,
    val kind: SolverFieldKind = SolverFieldKind.NUMBER,
    val suffix: String? = null,
)

data class SolverOutput(
    val headline: String,
    val headlineLabel: String = "Result",
    val rows: List<Pair<String, String>> = emptyList(),
    val steps: List<String> = emptyList(),
    val note: String? = null,
)

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

/** Raised for invalid user input; the message is displayed inline next to the form. */
class SolverException(message: String) : Exception(message)

internal fun fmt(value: Double): String = ResultFormatter.compact(value)

internal fun fmt(value: Rational): String =
    if (value.isInteger) value.numerator.toString() else "${value.numerator}/${value.denominator}"

internal fun Map<String, String>.text(key: String, label: String): String {
    val value = this[key]?.trim().orEmpty()
    if (value.isEmpty()) throw SolverException("Enter $label")
    return value
}

internal fun Map<String, String>.number(key: String, label: String): Double {
    val raw = text(key, label).replace(",", "")
    val parsed = raw.toDoubleOrNull()
        ?: throw SolverException("$label must be a number")
    if (!parsed.isFinite()) throw SolverException("$label must be finite")
    return parsed
}

internal fun Map<String, String>.integer(key: String, label: String): Long {
    val raw = text(key, label).replace(",", "").replace(" ", "")
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
    if (parts.size > 1000) throw SolverException("At most 1000 values are supported")
    return parts.map { part ->
        part.toDoubleOrNull() ?: throw SolverException("\"$part\" in $label is not a number")
    }
}

internal fun Map<String, String>.integerList(key: String, label: String): List<Long> {
    val raw = text(key, label)
    val parts = raw.split(',', ';', ' ', '\n').map { it.trim() }.filter { it.isNotEmpty() }
    if (parts.isEmpty()) throw SolverException("Enter at least one value for $label")
    return parts.map { part ->
        part.toLongOrNull() ?: throw SolverException("\"$part\" in $label is not a whole number")
    }
}

internal fun requirePositiveValue(value: Double, label: String): Double {
    if (value <= 0.0) throw SolverException("$label must be greater than zero")
    return value
}

internal fun requireNonNegativeValue(value: Double, label: String): Double {
    if (value < 0.0) throw SolverException("$label cannot be negative")
    return value
}

internal fun rationalText(value: Rational): String =
    if (value.isInteger) value.numerator.toString() else "${value.numerator}/${value.denominator}"

internal fun decimalText(value: Rational): String =
    value.toBigDecimal(java.math.MathContext(12)).stripTrailingZeros().toPlainString()

internal fun parsedDecimal(text: String, label: String): BigDecimal = try {
    BigDecimal(text.trim())
} catch (exception: NumberFormatException) {
    throw SolverException("$label must be a number")
}
