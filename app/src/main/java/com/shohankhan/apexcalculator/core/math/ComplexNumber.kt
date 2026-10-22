package com.shohankhan.apexcalculator.core.math

import kotlin.math.atan2
import kotlin.math.cos
import kotlin.math.exp
import kotlin.math.hypot
import kotlin.math.ln
import kotlin.math.sin

/**
 * Rectangular complex number. Angles are always radians inside the engine; the presentation
 * layer converts to degrees when the user selected DEG mode.
 */
data class ComplexNumber(val re: Double, val im: Double) {

    val isReal: Boolean get() = kotlin.math.abs(im) < EPSILON

    val modulus: Double get() = hypot(re, im)

    val argument: Double get() = atan2(im, re)

    operator fun plus(other: ComplexNumber) = ComplexNumber(re + other.re, im + other.im)

    operator fun minus(other: ComplexNumber) = ComplexNumber(re - other.re, im - other.im)

    operator fun times(other: ComplexNumber) = ComplexNumber(
        re * other.re - im * other.im,
        re * other.im + im * other.re,
    )

    operator fun div(other: ComplexNumber): ComplexNumber {
        val denominator = other.re * other.re + other.im * other.im
        if (denominator == 0.0) throw MathEvalException(MathError.DivisionByZero)
        return ComplexNumber(
            (re * other.re + im * other.im) / denominator,
            (im * other.re - re * other.im) / denominator,
        )
    }

    operator fun unaryMinus() = ComplexNumber(-re, -im)

    fun conjugate() = ComplexNumber(re, -im)

    fun square() = this * this

    fun sqrt(): ComplexNumber {
        val r = modulus
        if (r == 0.0) return ZERO
        val realPart = kotlin.math.sqrt((r + re) / 2.0)
        val imagPart = kotlin.math.sqrt((r - re) / 2.0)
        return ComplexNumber(realPart, if (im < 0) -imagPart else imagPart)
    }

    fun pow(exponent: ComplexNumber): ComplexNumber {
        if (modulus == 0.0) {
            return if (exponent.re > 0 && exponent.im == 0.0) ZERO else throw MathEvalException(
                MathError.Domain("0 raised to a non-positive power is undefined"),
            )
        }
        if (exponent.im == 0.0 && kotlin.math.abs(exponent.re) < 1e15 && exponent.re == kotlin.math.round(exponent.re)) {
            return integerPower(exponent.re.toLong())
        }
        // Principal branch: z^w = exp(w * (ln|z| + i arg z))
        val lnR = ln(modulus)
        val theta = argument
        val realExp = exponent.re * lnR - exponent.im * theta
        val imagExp = exponent.re * theta + exponent.im * lnR
        val magnitude = exp(realExp)
        return ComplexNumber(magnitude * cos(imagExp), magnitude * sin(imagExp))
    }

    private fun integerPower(n: Long): ComplexNumber {
        if (n == 0L) return ONE
        var exponent = n
        var base = this
        if (exponent < 0) {
            base = ONE / base
            exponent = -exponent
        }
        var result = ONE
        var remaining = exponent
        while (remaining > 0) {
            if (remaining and 1L == 1L) result *= base
            base *= base
            remaining = remaining shr 1
        }
        return result
    }

    fun toPolar(angleInDegrees: Boolean): String {
        val r = modulus
        val theta = if (angleInDegrees) Math.toDegrees(argument) else argument
        return "${formatComponent(r)} ∠ ${formatComponent(theta)}${if (angleInDegrees) "°" else " rad"}"
    }

    private fun formatComponent(value: Double): String {
        val rounded = kotlin.math.round(value * 1e10) / 1e10
        return if (rounded == kotlin.math.round(rounded)) rounded.toLong().toString() else rounded.toString()
    }

    companion object {
        const val EPSILON = 1e-13
        val ZERO = ComplexNumber(0.0, 0.0)
        val ONE = ComplexNumber(1.0, 0.0)
        val I = ComplexNumber(0.0, 1.0)

        fun ofReal(value: Double) = ComplexNumber(value, 0.0)
    }
}
