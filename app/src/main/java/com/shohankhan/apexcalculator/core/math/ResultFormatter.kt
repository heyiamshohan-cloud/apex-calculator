package com.shohankhan.apexcalculator.core.math

import com.shohankhan.apexcalculator.core.domain.model.NumberFormatMode
import java.math.BigDecimal
import java.math.MathContext
import java.math.RoundingMode
import java.math.BigInteger
import kotlin.math.abs
import kotlin.math.floor

/**
 * Presentation settings. Formatting never feeds back into the numeric value; the engine always
 * keeps the unrounded result.
 */
data class FormatSettings(
    val decimalPlaces: Int = 10,
    val numberFormat: NumberFormatMode = NumberFormatMode.AUTO,
    val thousandsSeparator: Boolean = true,
    val fractionDisplay: Boolean = false,
    val scientificThreshold: Int = 12,
    val significantFigures: Int = 10,
)

/**
 * Renders [Num] values for the UI. Formatting is locale independent (dot decimal separator,
 * comma grouping) so that results can be copied back into the expression field.
 */
class ResultFormatter(private val settings: FormatSettings) {

    fun format(value: Num): String = when (value) {
        is Num.IntVal -> formatInteger(value.v)
        is Num.Rat -> formatRational(value.r)
        is Num.Dec -> formatDecimal(value.d)
        is Num.Real -> formatReal(value.d)
        is Num.Cpx -> formatComplex(value.c)
    }

    /** Optional exact representation shown under an approximate result, for example "= 5/2". */
    fun secondary(value: Num): String? = when (value) {
        is Num.Rat -> when {
            value.r.isInteger -> null
            value.r.numerator.abs() > value.r.denominator -> "exact: ${value.r.numerator}/${value.r.denominator} = ${value.r.toMixedString()}"
            else -> "exact: ${value.r.numerator}/${value.r.denominator}"
        }
        is Num.Dec -> if (value.d.scale() > 6) {
            val rational = Rational.fromBigDecimal(value.d)
            if (!rational.isInteger && rational.denominator < BigInteger.valueOf(1_000_000)) {
                "exact: ${rational.numerator}/${rational.denominator}"
            } else {
                null
            }
        } else {
            null
        }
        else -> null
    }

    /**
     * Serialises a value so it can be inserted back into the expression field and parsed again
     * by the same engine. Complex values have no plain expression syntax, so they return null.
     */
    fun toInput(value: Num): String? = when (value) {
        is Num.IntVal -> value.v.toString()
        is Num.Rat -> if (value.r.isInteger) {
            value.r.numerator.toString()
        } else {
            "(${value.r.numerator}/${value.r.denominator})"
        }
        is Num.Dec -> value.d.toPlainString()
        is Num.Real -> when {
            !value.d.isFinite() -> null
            value.d == 0.0 -> "0"
            abs(value.d) >= 1e15 || abs(value.d) < 1e-9 ->
                BigDecimal.valueOf(value.d).round(MathContext(15)).toString().uppercase()
            else -> BigDecimal.valueOf(value.d).stripTrailingZeros().toPlainString()
        }
        is Num.Cpx -> null
    }

    fun formatComplex(value: ComplexNumber): String {
        val real = roundComponent(value.re)
        val imaginary = roundComponent(value.im)
        val realText = componentText(real)
        val imaginaryMagnitude = componentText(abs(imaginary))
        val imaginaryText = if (abs(imaginary) == 1.0) "i" else "${imaginaryMagnitude}i"
        return when {
            imaginary == 0.0 -> realText
            real == 0.0 -> if (imaginary < 0) "-$imaginaryText" else imaginaryText
            imaginary < 0 -> "$realText - $imaginaryText"
            else -> "$realText + $imaginaryText"
        }
    }

    private fun roundComponent(value: Double): Double {
        if (!value.isFinite()) return value
        val rounded = BigDecimal.valueOf(value)
            .round(MathContext(settings.decimalPlaces.coerceAtLeast(1), RoundingMode.HALF_UP))
            .toDouble()
        return if (rounded == -0.0) 0.0 else rounded
    }

    private fun componentText(value: Double): String = when {
        value.isNaN() -> "Not a number"
        value.isInfinite() -> if (value > 0) "∞" else "-∞"
        value == value.toLong().toDouble() && abs(value) < 1e15 -> value.toLong().toString()
        else -> BigDecimal.valueOf(value).stripTrailingZeros().toPlainString()
    }

    private fun formatInteger(value: BigInteger): String {
        val digits = value.abs().toString()
        val sign = if (value.signum() < 0) "-" else ""
        return when {
            settings.numberFormat == NumberFormatMode.SCIENTIFIC ||
                settings.numberFormat == NumberFormatMode.ENGINEERING ->
                formatBigDecimal(BigDecimal(value))
            settings.numberFormat == NumberFormatMode.FIXED ->
                formatBigDecimal(BigDecimal(value))
            digits.length > settings.scientificThreshold -> formatBigDecimal(BigDecimal(value))
            else -> sign + group(digits)
        }
    }

    private fun formatRational(value: Rational): String {
        if (value.isInteger) return formatInteger(value.numerator)
        if (settings.fractionDisplay) {
            val sign = if (value.numerator.signum() < 0) "-" else ""
            return "$sign${value.numerator.abs()}/${value.denominator}"
        }
        val exact = value.exactDecimal()
        val candidate = exact ?: value.toBigDecimal(MathContext(settings.decimalPlaces + 6))
        return formatBigDecimal(candidate)
    }

    private fun formatDecimal(value: BigDecimal): String = formatBigDecimal(value)

    private fun formatReal(value: Double): String {
        if (value.isNaN()) return "Not a number"
        if (value.isInfinite()) return if (value > 0) "∞" else "-∞"
        if (value == 0.0) return "0"
        return formatBigDecimal(BigDecimal.valueOf(value))
    }

    private fun formatBigDecimal(raw: BigDecimal): String {
        if (raw.signum() == 0) return "0"
        val mode = settings.numberFormat
        val value = when (mode) {
            NumberFormatMode.FIXED ->
                raw.setScale(settings.decimalPlaces, RoundingMode.HALF_UP)
            NumberFormatMode.SIGNIFICANT ->
                raw.round(MathContext(settings.significantFigures.coerceAtLeast(1), RoundingMode.HALF_UP))
            else -> raw
        }
        val stripped = value.stripTrailingZeros()
        val exponent = stripped.precision() - stripped.scale() - 1
        val useScientific = when (mode) {
            NumberFormatMode.SCIENTIFIC -> true
            NumberFormatMode.ENGINEERING -> true
            NumberFormatMode.FIXED -> exponent > 18
            NumberFormatMode.SIGNIFICANT -> exponent >= settings.scientificThreshold || exponent <= -7
            NumberFormatMode.AUTO ->
                exponent >= settings.scientificThreshold || exponent <= -5 ||
                    stripped.precision() > settings.decimalPlaces + 6
        }
        return when {
            mode == NumberFormatMode.ENGINEERING -> engineering(stripped, exponent)
            useScientific -> scientific(stripped, exponent)
            else -> plain(stripped)
        }
    }

    private fun plain(value: BigDecimal): String {
        val rounded = if (value.scale() > settings.decimalPlaces) {
            value.setScale(settings.decimalPlaces, RoundingMode.HALF_UP).stripTrailingZeros()
        } else {
            value
        }
        if (rounded.signum() == 0) return "0"
        val text = rounded.toPlainString()
        val negative = text.startsWith("-")
        val unsigned = if (negative) text.substring(1) else text
        val dot = unsigned.indexOf('.')
        val integerPart = if (dot >= 0) unsigned.substring(0, dot) else unsigned
        val fractionPart = if (dot >= 0) unsigned.substring(dot) else ""
        val grouped = group(integerPart) + fractionPart
        return if (negative) "-$grouped" else grouped
    }

    private fun scientific(value: BigDecimal, exponent: Int): String {
        val mantissa = value.movePointLeft(exponent)
            .round(MathContext(settings.significantFigures.coerceAtLeast(1), RoundingMode.HALF_UP))
            .stripTrailingZeros()
        return "${mantissa.toPlainString()}E${exponent}"
    }

    private fun engineering(value: BigDecimal, exponent: Int): String {
        val engineeringExponent = floor(exponent / 3.0).toInt() * 3
        val mantissa = value.movePointLeft(engineeringExponent)
            .round(MathContext(settings.significantFigures.coerceAtLeast(1), RoundingMode.HALF_UP))
            .stripTrailingZeros()
        return "${mantissa.toPlainString()}E${engineeringExponent}"
    }

    private fun group(digits: String): String {
        if (!settings.thousandsSeparator || digits.length <= 3) return digits
        val builder = StringBuilder()
        for (index in digits.indices) {
            if (index > 0 && (digits.length - index) % 3 == 0) builder.append(',')
            builder.append(digits[index])
        }
        return builder.toString()
    }

    companion object {
        /** Compact fixed formatting for tables and supporting text. */
        fun compact(value: Double, decimals: Int = 6): String {
            if (value.isNaN()) return "Not a number"
            if (value.isInfinite()) return if (value > 0) "∞" else "-∞"
            if (value == 0.0) return "0"
            val rounded = BigDecimal.valueOf(value)
                .setScale(decimals, RoundingMode.HALF_UP)
                .stripTrailingZeros()
            return if (abs(value) < 1e-6 || abs(value) >= 1e12) {
                rounded.toString().uppercase()
            } else {
                rounded.toPlainString()
            }
        }
    }
}
