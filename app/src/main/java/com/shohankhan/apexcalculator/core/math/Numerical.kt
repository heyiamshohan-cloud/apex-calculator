package com.shohankhan.apexcalculator.core.math

import kotlin.math.abs

/**
 * Numerical calculus utilities.
 *
 * These methods are approximations and the UI must always label them as numerical results:
 *  - derivative: five-point central difference with h = 1e-5·max(1, |x|)
 *  - integration: adaptive Simpson quadrature with a tolerance of 1e-9 and a depth limit of 24
 *  - root finding: bisection on a bracket with a sign change, 200 iterations, tolerance 1e-12
 *
 * No method here guarantees convergence for arbitrary functions; failures are reported.
 */
object Numerical {

    data class TableRow(val x: Double, val y: Double)

    data class RootResult(val root: Double, val iterations: Int, val residual: Double)

    const val MAX_TABLE_ROWS = 500

    fun functionOf(expression: String, angleUnit: AngleUnit): (Double) -> Double {
        if (expression.isBlank()) throw MathEvalException(MathError.Incomplete("Enter a function of x"))
        // Validate once so the UI can show a syntax error before running a table.
        CalculatorEngine.parse(expression)
        return { x ->
            val options = EvalOptions(
                angleUnit = angleUnit,
                variables = mapOf("x" to Num.Real(x)),
            )
            val result = CalculatorEngine.evaluateOrNull(expression, options)
                ?: throw MathEvalException(MathError.Domain("The function is undefined at x = ${ResultFormatter.compact(x)}"))
            val value = NumMath.toDouble(result)
            if (!value.isFinite()) {
                throw MathEvalException(
                    MathError.Domain("The function is not finite at x = ${ResultFormatter.compact(x)}"),
                )
            }
            value
        }
    }

    fun derivative(f: (Double) -> Double, x: Double): Double {
        if (!x.isFinite()) throw MathEvalException(MathError.Domain("The point must be finite"))
        val h = 1e-5 * maxOf(1.0, abs(x))
        val numerator = f(x - 2 * h) - 8 * f(x - h) + 8 * f(x + h) - f(x + 2 * h)
        val result = numerator / (12 * h)
        if (!result.isFinite()) throw MathEvalException(MathError.Domain("The derivative is not finite at this point"))
        return result
    }

    fun integrate(f: (Double) -> Double, a: Double, b: Double, tolerance: Double = 1e-9): Double {
        if (!a.isFinite() || !b.isFinite()) {
            throw MathEvalException(MathError.Domain("Integration limits must be finite"))
        }
        if (a == b) return 0.0
        val (lower, upper) = if (a < b) a to b else b to a
        val sign = if (a < b) 1.0 else -1.0
        val fa = f(lower)
        val fb = f(upper)
        val middle = (lower + upper) / 2
        val fm = f(middle)
        val whole = simpson(lower, upper, fa, fm, fb)
        val result = adaptive(f, lower, upper, fa, fm, fb, whole, tolerance, 24)
        if (!result.isFinite()) {
            throw MathEvalException(MathError.Domain("The integral could not be evaluated over this interval"))
        }
        return sign * result
    }

    private fun simpson(a: Double, b: Double, fa: Double, fm: Double, fb: Double): Double =
        (b - a) / 6.0 * (fa + 4 * fm + fb)

    private fun adaptive(
        f: (Double) -> Double,
        a: Double,
        b: Double,
        fa: Double,
        fm: Double,
        fb: Double,
        whole: Double,
        tolerance: Double,
        depth: Int,
    ): Double {
        val middle = (a + b) / 2
        val leftMiddle = (a + middle) / 2
        val rightMiddle = (middle + b) / 2
        val flm = f(leftMiddle)
        val frm = f(rightMiddle)
        val left = simpson(a, middle, fa, flm, fm)
        val right = simpson(middle, b, fm, frm, fb)
        val delta = left + right - whole
        if (depth <= 0 || abs(delta) <= 15 * tolerance) {
            return left + right + delta / 15
        }
        return adaptive(f, a, middle, fa, flm, fm, left, tolerance / 2, depth - 1) +
            adaptive(f, middle, b, fm, frm, fb, right, tolerance / 2, depth - 1)
    }

    fun findRoot(f: (Double) -> Double, a: Double, b: Double): RootResult {
        if (!a.isFinite() || !b.isFinite()) {
            throw MathEvalException(MathError.Domain("The bracket endpoints must be finite"))
        }
        if (a == b) throw MathEvalException(MathError.Domain("The bracket endpoints must be different"))
        var lower = minOf(a, b)
        var upper = maxOf(a, b)
        var fLower = f(lower)
        var fUpper = f(upper)
        if (fLower == 0.0) return RootResult(lower, 0, 0.0)
        if (fUpper == 0.0) return RootResult(upper, 0, 0.0)
        if (fLower.sign() == fUpper.sign()) {
            throw MathEvalException(
                MathError.Domain("The function has the same sign at both endpoints, so no bracket is guaranteed"),
            )
        }
        var iteration = 0
        var middle = lower
        var fMiddle = fLower
        while (iteration < 200) {
            middle = (lower + upper) / 2
            fMiddle = f(middle)
            if (abs(fMiddle) < 1e-12 || (upper - lower) / 2 < 1e-12) {
                return RootResult(middle, iteration, fMiddle)
            }
            if (fLower.sign() == fMiddle.sign()) {
                lower = middle
                fLower = fMiddle
            } else {
                upper = middle
                fUpper = fMiddle
            }
            iteration++
        }
        return RootResult(middle, iteration, fMiddle)
    }

    fun table(f: (Double) -> Double, start: Double, end: Double, step: Double): List<TableRow> {
        if (!start.isFinite() || !end.isFinite() || !step.isFinite()) {
            throw MathEvalException(MathError.Domain("Table inputs must be finite"))
        }
        if (step == 0.0) throw MathEvalException(MathError.Domain("The step cannot be zero"))
        if (start == end) throw MathEvalException(MathError.Domain("The start and end values must differ"))
        if ((end - start) / step < 0) {
            throw MathEvalException(MathError.Domain("The step direction does not reach the end value"))
        }
        val rows = ArrayList<TableRow>()
        var x = start
        var guard = 0
        while (guard < MAX_TABLE_ROWS) {
            if ((step > 0 && x > end + abs(step) * 1e-9) || (step < 0 && x < end - abs(step) * 1e-9)) break
            val y = f(x)
            rows += TableRow(x, y)
            x += step
            guard++
        }
        if (rows.isEmpty()) throw MathEvalException(MathError.Domain("The requested range produces no rows"))
        if (guard >= MAX_TABLE_ROWS) {
            throw MathEvalException(MathError.Limit("Tables are limited to $MAX_TABLE_ROWS rows"))
        }
        return rows
    }

    private fun Double.sign(): Int = when {
        this > 0 -> 1
        this < 0 -> -1
        else -> 0
    }
}
