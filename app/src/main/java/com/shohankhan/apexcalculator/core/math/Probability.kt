package com.shohankhan.apexcalculator.core.math

import java.math.BigInteger

/** Combinatorics and probability helpers with range validation built in. */
object Probability {

    const val MAX_FACTORIAL_INPUT = 1000

    fun factorial(n: Int): BigInteger {
        if (n < 0) throw MathEvalException(MathError.Domain("Factorial is defined for non-negative integers"))
        if (n > MAX_FACTORIAL_INPUT) {
            throw MathEvalException(MathError.Limit("Factorial is limited to $MAX_FACTORIAL_INPUT!"))
        }
        return NumMath.factorialBig(n)
    }

    fun permutations(n: Int, r: Int): BigInteger {
        if (n < 0 || r < 0) throw MathEvalException(MathError.Domain("n and r must be non-negative"))
        if (r > n) throw MathEvalException(MathError.Domain("r cannot be greater than n"))
        return NumMath.factorialBig(n).divide(NumMath.factorialBig(n - r))
    }

    fun combinations(n: Int, r: Int): BigInteger {
        if (n < 0 || r < 0) throw MathEvalException(MathError.Domain("n and r must be non-negative"))
        if (r > n) throw MathEvalException(MathError.Domain("r cannot be greater than n"))
        var result = BigInteger.ONE
        val k = minOf(r, n - r)
        for (index in 0 until k) {
            result = result.multiply(BigInteger.valueOf((n - index).toLong()))
                .divide(BigInteger.valueOf((index + 1).toLong()))
        }
        return result
    }

    fun requireProbability(value: Double, label: String = "Probability"): Double {
        if (value < 0.0 || value > 1.0) {
            throw MathEvalException(MathError.Domain("$label must be between 0 and 1"))
        }
        return value
    }

    fun binomialProbability(trials: Int, successes: Int, probability: Double): Double {
        if (trials < 0) throw MathEvalException(MathError.Domain("The number of trials cannot be negative"))
        if (successes < 0 || successes > trials) {
            throw MathEvalException(MathError.Domain("Successes must be between 0 and the number of trials"))
        }
        if (trials > 50_000) throw MathEvalException(MathError.Limit("At most 50,000 trials are supported"))
        requireProbability(probability)
        val coefficient = combinations(trials, successes).toDouble()
        val term = probability.pow(successes) * (1 - probability).pow(trials - successes)
        return (coefficient * term).coerceIn(0.0, 1.0)
    }

    fun binomialAtMost(trials: Int, successes: Int, probability: Double): Double {
        if (successes < 0) throw MathEvalException(MathError.Domain("Successes cannot be negative"))
        var total = 0.0
        for (k in 0..minOf(successes, trials)) {
            total += binomialProbability(trials, k, probability)
        }
        return total.coerceIn(0.0, 1.0)
    }

    fun binomialAtLeast(trials: Int, successes: Int, probability: Double): Double {
        if (successes < 0) return 1.0
        var total = 0.0
        for (k in successes..trials) {
            total += binomialProbability(trials, k, probability)
        }
        return total.coerceIn(0.0, 1.0)
    }

    fun complement(probability: Double): Double = 1.0 - requireProbability(probability)

    fun independentAnd(first: Double, second: Double): Double =
        requireProbability(first) * requireProbability(second)

    fun independentOr(first: Double, second: Double): Double {
        val a = requireProbability(first)
        val b = requireProbability(second)
        return (a + b - a * b).coerceIn(0.0, 1.0)
    }

    fun conditional(probabilityAAndB: Double, probabilityB: Double): Double {
        requireProbability(probabilityAAndB, "P(A and B)")
        requireProbability(probabilityB, "P(B)")
        if (probabilityB == 0.0) throw MathEvalException(MathError.DivisionByZero("P(B) must be greater than zero"))
        return (probabilityAAndB / probabilityB).coerceIn(0.0, 1.0)
    }

    fun eventProbability(favourableOutcomes: Int, totalOutcomes: Int): Double {
        if (totalOutcomes <= 0) throw MathEvalException(MathError.Domain("Total outcomes must be greater than zero"))
        if (favourableOutcomes < 0 || favourableOutcomes > totalOutcomes) {
            throw MathEvalException(MathError.Domain("Favourable outcomes must be between 0 and the total"))
        }
        return favourableOutcomes.toDouble() / totalOutcomes
    }

    private fun Double.pow(exponent: Int): Double = Math.pow(this, exponent.toDouble())
}
