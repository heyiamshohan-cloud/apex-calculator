package com.shohankhan.apexcalculator.core.math

import java.math.BigDecimal
import java.math.BigInteger
import java.math.MathContext
import java.math.RoundingMode

/**
 * Exact rational number with a normalised positive denominator.
 *
 * All arithmetic stays exact; conversion to a decimal happens only at the formatting boundary.
 */
data class Rational private constructor(
    val numerator: BigInteger,
    val denominator: BigInteger,
) : Comparable<Rational> {

    val isInteger: Boolean get() = denominator == BigInteger.ONE

    val isZero: Boolean get() = numerator.signum() == 0

    val signum: Int get() = numerator.signum()

    operator fun plus(other: Rational): Rational =
        of(numerator * other.denominator + other.numerator * denominator, denominator * other.denominator)

    operator fun minus(other: Rational): Rational =
        of(numerator * other.denominator - other.numerator * denominator, denominator * other.denominator)

    operator fun times(other: Rational): Rational =
        of(numerator * other.numerator, denominator * other.denominator)

    operator fun div(other: Rational): Rational {
        if (other.isZero) throw MathEvalException(MathError.DivisionByZero)
        return of(numerator * other.denominator, denominator * other.numerator)
    }

    operator fun unaryMinus(): Rational = of(numerator.negate(), denominator)

    fun abs(): Rational = if (numerator.signum() < 0) -this else this

    fun pow(exponent: Int): Rational {
        require(exponent in -MAX_EXPONENT..MAX_EXPONENT) { "Exponent outside supported range" }
        if (exponent == 0) return ONE
        if (exponent > 0) {
            return of(numerator.pow(exponent), denominator.pow(exponent))
        }
        if (numerator.signum() == 0) throw MathEvalException(MathError.DivisionByZero)
        val positive = exponent * -1
        return of(denominator.pow(positive), numerator.pow(positive))
    }

    fun toDouble(): Double = numerator.toDouble() / denominator.toDouble()

    fun toBigDecimal(context: MathContext = MathContext.DECIMAL128): BigDecimal =
        numerator.toBigDecimal().divide(denominator.toBigDecimal(), context)

    /** True when the decimal expansion terminates (denominator only has factors 2 and 5). */
    fun hasTerminatingDecimal(): Boolean {
        var rest = denominator
        while (rest.mod(TWO) == BigInteger.ZERO) rest = rest.divide(TWO)
        while (rest.mod(FIVE) == BigInteger.ZERO) rest = rest.divide(FIVE)
        return rest == BigInteger.ONE
    }

    /** Exact decimal expansion when it terminates, otherwise null. */
    fun exactDecimal(): BigDecimal? {
        if (!hasTerminatingDecimal()) return null
        val scale = scalingExponent()
        return numerator.toBigDecimal().divide(denominator.toBigDecimal(), scale, RoundingMode.UNNECESSARY)
    }

    private fun scalingExponent(): Int {
        var rest = denominator
        var twos = 0
        var fives = 0
        while (rest.mod(TWO) == BigInteger.ZERO) {
            rest = rest.divide(TWO)
            twos++
        }
        while (rest.mod(FIVE) == BigInteger.ZERO) {
            rest = rest.divide(FIVE)
            fives++
        }
        return maxOf(twos, fives)
    }

    fun toMixedString(): String {
        if (isInteger) return numerator.toString()
        val sign = if (numerator.signum() < 0) "-" else ""
        val absNum = numerator.abs()
        val whole = absNum.divide(denominator)
        val remainder = absNum.mod(denominator)
        if (whole.signum() == 0) return "$sign$remainder/$denominator"
        return "$sign$whole $remainder/$denominator"
    }

    override fun compareTo(other: Rational): Int =
        (numerator * other.denominator).compareTo(other.numerator * denominator)

    override fun toString(): String =
        if (isInteger) numerator.toString() else "$numerator/$denominator"

    companion object {
        private val TWO = BigInteger.TWO
        private val FIVE = BigInteger.valueOf(5)
        const val MAX_EXPONENT = 4096

        val ZERO: Rational = Rational(BigInteger.ZERO, BigInteger.ONE)
        val ONE: Rational = Rational(BigInteger.ONE, BigInteger.ONE)

        fun of(numerator: BigInteger, denominator: BigInteger): Rational {
            if (denominator.signum() == 0) throw MathEvalException(MathError.DivisionByZero)
            var num = numerator
            var den = denominator
            if (den.signum() < 0) {
                num = num.negate()
                den = den.negate()
            }
            val gcd = num.gcd(den)
            if (gcd > BigInteger.ONE) {
                num = num.divide(gcd)
                den = den.divide(gcd)
            }
            return Rational(num, den)
        }

        fun of(numerator: BigInteger): Rational = Rational(numerator, BigInteger.ONE)

        fun of(numerator: Long, denominator: Long = 1L): Rational =
            of(BigInteger.valueOf(numerator), BigInteger.valueOf(denominator))

        fun fromBigDecimal(value: BigDecimal): Rational {
            val scale = value.scale()
            return if (scale <= 0) {
                of(value.toBigIntegerExact())
            } else {
                of(value.unscaledValue(), BigInteger.TEN.pow(scale))
            }
        }
    }
}
