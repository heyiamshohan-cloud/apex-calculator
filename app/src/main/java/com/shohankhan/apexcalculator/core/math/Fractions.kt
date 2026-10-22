package com.shohankhan.apexcalculator.core.math

import java.math.BigDecimal
import java.math.BigInteger

data class MixedNumber(val whole: BigInteger, val numerator: BigInteger, val denominator: BigInteger) {
    fun toImproper(): Rational {
        val sign = if (whole.signum() < 0) BigInteger.valueOf(-1) else BigInteger.ONE
        val magnitude = whole.abs().multiply(denominator).add(numerator)
        return Rational.of(magnitude.multiply(sign), denominator)
    }

    fun display(): String = when {
        numerator.signum() == 0 -> whole.toString()
        whole.signum() == 0 -> "${numerator}/${denominator}"
        else -> "$whole ${numerator}/${denominator}"
    }
}

/** Exact fraction utilities used by the Quick Solve fraction workspace. */
object Fractions {

    /** Accepts "3/4", "1 1/2", "-2 3/8", "0.75" and plain integers. */
    fun parse(input: String): Rational {
        val text = input.trim().replace("−", "-").replace(",", "")
        if (text.isEmpty()) throw MathEvalException(MathError.Incomplete("Enter a fraction"))
        return try {
            when {
                text.contains('/') && text.contains(' ') -> {
                    val (wholePart, fractionPart) = text.split(" ", limit = 2)
                    val whole = BigInteger(wholePart)
                    val (num, den) = fractionPart.split("/")
                    val magnitude = whole.abs().multiply(BigInteger(den)).add(BigInteger(num))
                    val sign = if (whole.signum() < 0 || text.startsWith("-")) BigInteger.valueOf(-1) else BigInteger.ONE
                    Rational.of(magnitude.multiply(sign), BigInteger(den))
                }
                text.contains('/') -> {
                    val parts = text.split("/")
                    if (parts.size != 2) throw MathEvalException(MathError.Syntax(0, "Use the form a/b"))
                    Rational.of(BigInteger(parts[0].trim()), BigInteger(parts[1].trim()))
                }
                else -> Rational.fromBigDecimal(BigDecimal(text))
            }
        } catch (exception: NumberFormatException) {
            throw MathEvalException(MathError.Syntax(0, "Not a valid fraction: \"$input\""))
        }
    }

    fun simplify(numerator: BigInteger, denominator: BigInteger): Rational = Rational.of(numerator, denominator)

    fun add(a: Rational, b: Rational): Rational = a + b

    fun subtract(a: Rational, b: Rational): Rational = a - b

    fun multiply(a: Rational, b: Rational): Rational = a * b

    fun divide(a: Rational, b: Rational): Rational = a / b

    fun toMixed(value: Rational): MixedNumber {
        val sign = if (value.numerator.signum() < 0) BigInteger.valueOf(-1) else BigInteger.ONE
        val magnitude = value.numerator.abs()
        val whole = magnitude.divide(value.denominator).multiply(sign)
        val remainder = magnitude.mod(value.denominator)
        return MixedNumber(whole, remainder, value.denominator)
    }

    fun toImproper(whole: BigInteger, numerator: BigInteger, denominator: BigInteger): Rational =
        MixedNumber(whole, numerator, denominator).toImproper()

    fun fractionOf(value: Rational, number: Rational): Rational = value * number

    /** Converts a terminating decimal into an exact fraction in lowest terms. */
    fun decimalToFraction(decimal: BigDecimal, maxScale: Int = 12): Rational {
        val stripped = decimal.stripTrailingZeros()
        if (stripped.scale() > maxScale) {
            throw MathEvalException(
                MathError.Unsupported("Decimals with more than $maxScale places are not converted automatically"),
            )
        }
        return Rational.fromBigDecimal(stripped)
    }

    fun order(values: List<Rational>): List<Rational> = values.sorted()

    fun commonDenominator(values: List<Rational>): BigInteger =
        values.fold(BigInteger.ONE) { acc, value -> lcm(acc, value.denominator) }

    fun lcm(a: BigInteger, b: BigInteger): BigInteger =
        if (a.signum() == 0 || b.signum() == 0) BigInteger.ZERO else a.divide(a.gcd(b)).multiply(b).abs()
}
