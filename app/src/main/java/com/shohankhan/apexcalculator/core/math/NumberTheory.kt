package com.shohankhan.apexcalculator.core.math

import java.math.BigInteger

/**
 * Integer utilities with explicit limits so that a pathological input can never lock the UI.
 * Prime testing and factorisation are bounded by [MAX_FACTOR_INPUT].
 */
object NumberTheory {

    val MAX_FACTOR_INPUT: BigInteger = BigInteger.valueOf(10_000_000_000_000L) // 10^13

    fun gcd(a: BigInteger, b: BigInteger): BigInteger = a.gcd(b)

    /** Integer square root that works on every supported Android level (BigInteger.sqrt is API 33). */
    internal fun BigInteger.integerSquareRoot(): BigInteger {
        if (this < BigInteger.valueOf(2)) return this
        var estimate = BigInteger.ONE.shiftLeft((bitLength() + 1) / 2)
        while (true) {
            val next = estimate.add(this.divide(estimate)).shiftRight(1)
            if (next >= estimate) return estimate
            estimate = next
        }
    }

    fun lcm(a: BigInteger, b: BigInteger): BigInteger =
        if (a.signum() == 0 || b.signum() == 0) BigInteger.ZERO else a.divide(a.gcd(b)).multiply(b).abs()

    fun isPrime(value: BigInteger): Boolean {
        if (value < BigInteger.valueOf(2)) return false
        if (value > MAX_FACTOR_INPUT) {
            throw MathEvalException(MathError.Limit("Prime testing is limited to values up to $MAX_FACTOR_INPUT"))
        }
        if (value == BigInteger.valueOf(2)) return true
        if (value.mod(BigInteger.valueOf(2)) == BigInteger.ZERO) return false
        val limit = value.integerSquareRoot()
        var factor = BigInteger.valueOf(3)
        while (factor <= limit) {
            if (value.mod(factor) == BigInteger.ZERO) return false
            factor = factor.add(BigInteger.valueOf(2))
        }
        return true
    }

    /** Prime factorisation by trial division. Returns a list of (prime, exponent) pairs. */
    fun primeFactorisation(value: BigInteger): List<Pair<BigInteger, Int>> {
        if (value <= BigInteger.ONE) {
            throw MathEvalException(MathError.Domain("Factorisation requires an integer greater than 1"))
        }
        if (value > MAX_FACTOR_INPUT) {
            throw MathEvalException(
                MathError.Limit("Factorisation is limited to values up to ${MAX_FACTOR_INPUT}"),
            )
        }
        var remaining = value
        val factors = ArrayList<Pair<BigInteger, Int>>()
        var divisor = BigInteger.valueOf(2)
        while (divisor.multiply(divisor) <= remaining) {
            if (remaining.mod(divisor) == BigInteger.ZERO) {
                var exponent = 0
                while (remaining.mod(divisor) == BigInteger.ZERO) {
                    remaining = remaining.divide(divisor)
                    exponent++
                }
                factors += divisor to exponent
            }
            divisor = if (divisor == BigInteger.valueOf(2)) BigInteger.valueOf(3) else divisor.add(BigInteger.valueOf(2))
        }
        if (remaining > BigInteger.ONE) factors += remaining to 1
        return factors
    }

    fun factorisationText(value: BigInteger): String =
        primeFactorisation(value).joinToString(" × ") { (prime, exponent) ->
            if (exponent == 1) prime.toString() else "$prime^$exponent"
        }

    fun divisors(value: BigInteger): List<BigInteger> {
        if (value.signum() < 0) throw MathEvalException(MathError.Domain("Divisors require a non-negative integer"))
        if (value == BigInteger.ZERO) throw MathEvalException(MathError.Domain("Zero has infinitely many divisors"))
        if (value > BigInteger.valueOf(1_000_000_000_000L)) {
            throw MathEvalException(MathError.Limit("Divisor listing is limited to values up to 10^12"))
        }
        val factors = primeFactorisation(value)
        var result = listOf(BigInteger.ONE)
        for ((prime, exponent) in factors) {
            val expanded = ArrayList<BigInteger>()
            var power = BigInteger.ONE
            for (index in 0..exponent) {
                if (index > 0) power = power.multiply(prime)
                result.forEach { expanded += it.multiply(power) }
            }
            result = expanded
        }
        return result.sorted()
    }

    fun divisibilityChecks(value: BigInteger): Map<Int, Boolean> = mapOf(
        2 to (value.mod(BigInteger.valueOf(2)) == BigInteger.ZERO),
        3 to (value.mod(BigInteger.valueOf(3)) == BigInteger.ZERO),
        4 to (value.mod(BigInteger.valueOf(4)) == BigInteger.ZERO),
        5 to (value.mod(BigInteger.valueOf(5)) == BigInteger.ZERO),
        6 to (value.mod(BigInteger.valueOf(6)) == BigInteger.ZERO),
        8 to (value.mod(BigInteger.valueOf(8)) == BigInteger.ZERO),
        9 to (value.mod(BigInteger.valueOf(9)) == BigInteger.ZERO),
        10 to (value.mod(BigInteger.valueOf(10)) == BigInteger.ZERO),
        11 to (value.mod(BigInteger.valueOf(11)) == BigInteger.ZERO),
    )

    fun isPerfectSquare(value: BigInteger): Boolean {
        if (value.signum() < 0) return false
        val root = value.integerSquareRoot()
        return root.multiply(root) == value
    }

    fun isPerfectCube(value: BigInteger): Boolean {
        if (value.signum() < 0) return false
        val approx = Math.cbrt(value.toDouble())
        if (!approx.isFinite()) return false
        var root = BigInteger.valueOf(approx.toLong())
        if (root.signum() < 0) root = BigInteger.ZERO
        val cube = root.pow(3)
        return cube == value || root.add(BigInteger.ONE).pow(3) == value
    }

    fun multiples(value: BigInteger, count: Int): List<BigInteger> {
        if (count <= 0) throw MathEvalException(MathError.Domain("Count must be positive"))
        if (count > 200) throw MathEvalException(MathError.Limit("At most 200 multiples can be listed"))
        return (1..count).map { value.multiply(BigInteger.valueOf(it.toLong())) }
    }

    fun commonFactors(a: BigInteger, b: BigInteger): List<BigInteger> {
        val common = a.gcd(b)
        if (common == BigInteger.ZERO) return emptyList()
        return divisors(common)
    }

    fun commonMultiples(a: BigInteger, b: BigInteger, count: Int): List<BigInteger> {
        val step = lcm(a, b)
        if (step == BigInteger.ZERO) return emptyList()
        return multiples(step, count)
    }
}
