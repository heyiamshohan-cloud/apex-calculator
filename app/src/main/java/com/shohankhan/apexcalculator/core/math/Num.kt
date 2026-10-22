package com.shohankhan.apexcalculator.core.math

import java.math.BigDecimal
import java.math.BigInteger
import java.math.MathContext
import kotlin.math.abs

/**
 * Numeric tower used by the engine.
 *
 * IntVal / Rat / Dec are exact, Real is IEEE-754 double precision and Cpx is a complex value.
 * Every operation promotes to the least specific type required, so exact arithmetic is
 * preserved whenever the inputs allow it.
 */
sealed interface Num {

    data class IntVal(val v: BigInteger) : Num

    data class Rat(val r: Rational) : Num

    data class Dec(val d: BigDecimal) : Num

    data class Real(val d: Double) : Num

    data class Cpx(val c: ComplexNumber) : Num

    companion object {
        fun of(value: Int): Num = NumMath.of(value)

        fun of(value: Long): Num = NumMath.of(value)

        fun of(value: Double): Num = NumMath.of(value)

        fun of(value: BigInteger): Num = Num.IntVal(value)

        fun of(value: BigDecimal): Num = NumMath.of(value)

        fun of(value: Rational): Num = NumMath.of(value)
    }
}

object NumMath {

    private const val MAX_RESULT_BITS = 2_000_000

    fun of(value: Long): Num = Num.IntVal(BigInteger.valueOf(value))

    fun of(value: Int): Num = Num.IntVal(BigInteger.valueOf(value.toLong()))

    fun of(value: Double): Num =
        if (value == value.toLong().toDouble() && abs(value) < 1e15) {
            Num.IntVal(BigInteger.valueOf(value.toLong()))
        } else {
            Num.Real(value)
        }

    fun of(value: BigDecimal): Num = Num.Dec(value)

    fun of(value: Rational): Num = rationalResult(value)

    private fun rationalResult(value: Rational): Num =
        if (value.isInteger) Num.IntVal(value.numerator) else Num.Rat(value)

    fun toRational(value: Num): Rational? = when (value) {
        is Num.IntVal -> Rational.of(value.v)
        is Num.Rat -> value.r
        is Num.Dec -> Rational.fromBigDecimal(value.d)
        else -> null
    }

    fun toDecimal(value: Num): BigDecimal? = when (value) {
        is Num.IntVal -> value.v.toBigDecimal()
        is Num.Rat -> value.r.exactDecimal()
        is Num.Dec -> value.d
        is Num.Real -> BigDecimal.valueOf(value.d)
        is Num.Cpx -> null
    }

    fun toDouble(value: Num): Double = when (value) {
        is Num.IntVal -> value.v.toDouble()
        is Num.Rat -> value.r.toDouble()
        is Num.Dec -> value.d.toDouble()
        is Num.Real -> value.d
        is Num.Cpx -> if (value.c.isReal) value.c.re else Double.NaN
    }

    fun toComplex(value: Num): ComplexNumber = when (value) {
        is Num.Cpx -> value.c
        else -> ComplexNumber.ofReal(toDouble(value))
    }

    fun isZero(value: Num): Boolean = when (value) {
        is Num.IntVal -> value.v.signum() == 0
        is Num.Rat -> value.r.isZero
        is Num.Dec -> value.d.signum() == 0
        is Num.Real -> value.d == 0.0
        is Num.Cpx -> value.c.re == 0.0 && value.c.im == 0.0
    }

    fun isNegative(value: Num): Boolean = when (value) {
        is Num.IntVal -> value.v.signum() < 0
        is Num.Rat -> value.r.signum < 0
        is Num.Dec -> value.d.signum() < 0
        is Num.Real -> value.d < 0
        is Num.Cpx -> false
    }

    fun isIntegerValue(value: Num): Boolean = when (value) {
        is Num.IntVal -> true
        is Num.Rat -> value.r.isInteger
        is Num.Dec -> value.d.stripTrailingZeros().scale() <= 0
        is Num.Real -> value.d.isFinite() && value.d == kotlin.math.round(value.d)
        is Num.Cpx -> false
    }

    fun asBigInteger(value: Num): BigInteger? = when (value) {
        is Num.IntVal -> value.v
        is Num.Rat -> if (value.r.isInteger) value.r.numerator else null
        is Num.Dec -> if (value.d.stripTrailingZeros().scale() <= 0) value.d.toBigInteger() else null
        is Num.Real -> if (value.d.isFinite() && value.d == kotlin.math.round(value.d)) {
            BigDecimal.valueOf(value.d).toBigInteger()
        } else {
            null
        }
        is Num.Cpx -> null
    }

    fun negate(value: Num): Num = when (value) {
        is Num.IntVal -> Num.IntVal(value.v.negate())
        is Num.Rat -> Num.Rat(-value.r)
        is Num.Dec -> Num.Dec(value.d.negate())
        is Num.Real -> Num.Real(-value.d)
        is Num.Cpx -> Num.Cpx(-value.c)
    }

    fun add(a: Num, b: Num): Num = when {
        a is Num.Cpx || b is Num.Cpx -> Num.Cpx(toComplex(a) + toComplex(b))
        a is Num.Real || b is Num.Real -> Num.Real(toDouble(a) + toDouble(b))
        a is Num.Dec || b is Num.Dec -> Num.Dec(toDecimal(a)!!.add(toDecimal(b)!!))
        a is Num.Rat || b is Num.Rat -> rationalResult(toRational(a)!! + toRational(b)!!)
        else -> Num.IntVal((a as Num.IntVal).v + (b as Num.IntVal).v)
    }

    fun subtract(a: Num, b: Num): Num = add(a, negate(b))

    fun multiply(a: Num, b: Num): Num = when {
        a is Num.Cpx || b is Num.Cpx -> Num.Cpx(toComplex(a) * toComplex(b))
        a is Num.Real || b is Num.Real -> Num.Real(toDouble(a) * toDouble(b))
        a is Num.Dec || b is Num.Dec -> Num.Dec(toDecimal(a)!!.multiply(toDecimal(b)!!))
        a is Num.Rat || b is Num.Rat -> rationalResult(toRational(a)!! * toRational(b)!!)
        else -> Num.IntVal((a as Num.IntVal).v * (b as Num.IntVal).v)
    }

    fun divide(a: Num, b: Num): Num {
        if (isZero(b)) throw MathEvalException(MathError.DivisionByZero())
        return when {
            a is Num.Cpx || b is Num.Cpx -> Num.Cpx(toComplex(a) / toComplex(b))
            a is Num.Real || b is Num.Real -> {
                val result = toDouble(a) / toDouble(b)
                if (!result.isFinite()) throw MathEvalException(MathError.Overflow("The result is too large to represent"))
                Num.Real(result)
            }
            else -> rationalResult(toRational(a)!! / toRational(b)!!)
        }
    }

    fun pow(base: Num, exponent: Num, allowComplex: Boolean): Num {
        if (base is Num.Cpx || exponent is Num.Cpx) {
            return Num.Cpx(toComplex(base).pow(toComplex(exponent)))
        }
        val integerExponent = (exponent as? Num.IntVal)
            ?.v
            ?.takeIf { it.bitLength() <= 31 }
            ?.toInt()

        if (integerExponent != null && abs(integerExponent) <= Rational.MAX_EXPONENT) {
            return when (base) {
                is Num.IntVal -> {
                    if (integerExponent >= 0) {
                        val estimatedBits = base.v.bitLength().toLong() * integerExponent
                        if (estimatedBits > MAX_RESULT_BITS) {
                            throw MathEvalException(MathError.Limit("The result is too large to compute exactly"))
                        }
                        Num.IntVal(base.v.pow(integerExponent))
                    } else {
                        rationalResult(Rational.of(base.v).pow(integerExponent))
                    }
                }
                is Num.Rat -> rationalResult(base.r.pow(integerExponent))
                is Num.Dec -> Num.Dec(base.d.pow(integerExponent, MathContext.DECIMAL128))
                is Num.Real -> Num.Real(Math.pow(base.d, integerExponent.toDouble()))
                is Num.Cpx -> Num.Cpx(base.c.pow(ComplexNumber.ofReal(integerExponent.toDouble())))
            }
        }

        val baseValue = toDouble(base)
        val exponentValue = toDouble(exponent)
        if (isZero(base) && exponentValue < 0) {
            throw MathEvalException(MathError.DivisionByZero())
        }
        if (baseValue < 0 && !isIntegerValue(exponent)) {
            if (!allowComplex) {
                throw MathEvalException(
                    MathError.Domain("A negative base with a fractional exponent has no real result. Enable complex mode."),
                )
            }
            return Num.Cpx(toComplex(base).pow(toComplex(exponent)))
        }
        val result = Math.pow(baseValue, exponentValue)
        if (result.isNaN()) {
            throw MathEvalException(MathError.Domain("The power is undefined for these values"))
        }
        if (result.isInfinite()) {
            throw MathEvalException(MathError.Overflow("The result is too large to represent"))
        }
        return Num.Real(result)
    }

    fun factorial(value: Num): Num {
        val n = asBigInteger(value)
            ?: throw MathEvalException(MathError.Domain("Factorial is defined for non-negative integers"))
        if (n.signum() < 0) {
            throw MathEvalException(MathError.Domain("Factorial is not defined for negative numbers"))
        }
        if (n > BigInteger.valueOf(MAX_FACTORIAL.toLong())) {
            throw MathEvalException(MathError.Limit("Factorial is limited to $MAX_FACTORIAL!"))
        }
        return Num.IntVal(factorialBig(n.toInt()))
    }

    const val MAX_FACTORIAL = 1000

    private val factorialCache = ArrayList<BigInteger>(MAX_FACTORIAL + 1).apply { add(BigInteger.ONE) }

    fun factorialBig(n: Int): BigInteger {
        require(n in 0..MAX_FACTORIAL) { "n outside factorial limit" }
        synchronized(factorialCache) {
            while (factorialCache.size <= n) {
                val next = factorialCache.last().multiply(BigInteger.valueOf(factorialCache.size.toLong()))
                factorialCache.add(next)
            }
            return factorialCache[n]
        }
    }

    fun abs(value: Num): Num = when (value) {
        is Num.IntVal -> Num.IntVal(value.v.abs())
        is Num.Rat -> Num.Rat(value.r.abs())
        is Num.Dec -> Num.Dec(value.d.abs())
        is Num.Real -> Num.Real(abs(value.d))
        is Num.Cpx -> Num.Real(value.c.modulus)
    }

    fun signum(value: Num): Num = when (value) {
        is Num.IntVal -> Num.IntVal(BigInteger.valueOf(value.v.signum().toLong()))
        is Num.Rat -> Num.IntVal(BigInteger.valueOf(value.r.signum.toLong()))
        is Num.Dec -> Num.IntVal(BigInteger.valueOf(value.d.signum().toLong()))
        is Num.Real -> Num.IntVal(BigInteger.valueOf(when { value.d > 0 -> 1L; value.d < 0 -> -1L; else -> 0L }))
        is Num.Cpx -> Num.IntVal(BigInteger.valueOf(if (value.c.modulus == 0.0) 0L else 1L))
    }

    fun compare(a: Num, b: Num): Int {
        val exactA = toRational(a)
        val exactB = toRational(b)
        return if (exactA != null && exactB != null) {
            exactA.compareTo(exactB)
        } else {
            toDouble(a).compareTo(toDouble(b))
        }
    }

    fun floor(value: Num): Num {
        asBigIntegerExact(value)?.let { return Num.IntVal(it) }
        return Num.IntVal(BigDecimal.valueOf(toDouble(value)).setScale(0, java.math.RoundingMode.FLOOR).toBigInteger())
    }

    fun ceil(value: Num): Num {
        asBigIntegerExact(value)?.let { return Num.IntVal(it) }
        return Num.IntVal(BigDecimal.valueOf(toDouble(value)).setScale(0, java.math.RoundingMode.CEILING).toBigInteger())
    }

    fun round(value: Num): Num {
        asBigIntegerExact(value)?.let { return Num.IntVal(it) }
        return Num.IntVal(BigDecimal.valueOf(toDouble(value)).setScale(0, java.math.RoundingMode.HALF_UP).toBigInteger())
    }

    fun truncate(value: Num): Num {
        asBigIntegerExact(value)?.let { return Num.IntVal(it) }
        return Num.IntVal(BigDecimal.valueOf(toDouble(value)).setScale(0, java.math.RoundingMode.DOWN).toBigInteger())
    }

    private fun asBigIntegerExact(value: Num): BigInteger? = when (value) {
        is Num.IntVal -> value.v
        is Num.Rat -> if (value.r.isInteger) value.r.numerator else null
        is Num.Dec -> if (value.d.stripTrailingZeros().scale() <= 0) value.d.toBigInteger() else null
        else -> null
    }

    fun requiresComplex(value: Num): Boolean = value is Num.Cpx && !value.c.isReal
}
