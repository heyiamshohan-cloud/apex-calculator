package com.shohankhan.apexcalculator.core.math

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test
import java.math.BigDecimal
import java.math.BigInteger

/** Exact fraction arithmetic, mixed numbers and decimal conversion. */
class FractionsTest {

    private fun r(text: String) = Fractions.parse(text)

    @Test
    fun `parses simple fractions and normalises the sign`() {
        val value = r("6/8")
        assertEquals(BigInteger.valueOf(3), value.numerator)
        assertEquals(BigInteger.valueOf(4), value.denominator)

        val negative = r("-6/8")
        assertEquals(BigInteger.valueOf(-3), negative.numerator)
        assertEquals(BigInteger.valueOf(4), negative.denominator)
    }

    @Test
    fun `parses mixed numbers`() {
        val value = r("1 1/2")
        assertEquals(BigInteger.valueOf(3), value.numerator)
        assertEquals(BigInteger.valueOf(2), value.denominator)
    }

    @Test
    fun `parses decimals exactly`() {
        assertEquals(r("3/4"), r("0.75"))
        assertEquals(r("-5/8"), r("-0.625"))
        assertEquals(r("2"), r("2.000"))
    }

    @Test
    fun `adds fractions exactly`() {
        val sum = r("1/3") + r("1/6")
        assertEquals(BigInteger.ONE, sum.numerator)
        assertEquals(BigInteger.valueOf(2), sum.denominator)
    }

    @Test
    fun `basic operations stay exact`() {
        assertEquals(r("5/6"), r("1/2") + r("1/3"))
        assertEquals(r("1/6"), r("1/2") - r("1/3"))
        assertEquals(r("1/6"), r("1/2") * r("1/3"))
        assertEquals(r("3/2"), r("1/2") / r("1/3"))
    }

    @Test
    fun `division by zero is rejected`() {
        try {
            r("1/2") / r("0")
            throw AssertionError("Expected an exception")
        } catch (expected: Exception) {
            // expected
        }
    }

    @Test
    fun `mixed number display`() {
        assertEquals("2 1/2", Fractions.toMixed(r("5/2")).display())
        assertEquals("-2 1/2", Fractions.toMixed(r("-5/2")).display())
        assertEquals("3", Fractions.toMixed(r("3")).display())
    }

    @Test
    fun `decimal to fraction round trips common values`() {
        assertEquals(r("1/8"), Fractions.decimalToFraction(BigDecimal("0.125")))
        assertEquals(r("7/20"), Fractions.decimalToFraction(BigDecimal("0.35")))
    }

    @Test
    fun `terminating decimals are detected`() {
        assertTrue(r("1/8").hasTerminatingDecimal())
        assertTrue(r("3/40").hasTerminatingDecimal())
        assertFalse(r("1/3").hasTerminatingDecimal())
        assertFalse(r("1/7").hasTerminatingDecimal())
    }

    @Test
    fun `ordering and common denominator`() {
        val ordered = Fractions.order(listOf(r("1/2"), r("1/3"), r("1/6")))
        assertEquals(listOf(r("1/6"), r("1/3"), r("1/2")), ordered)
        assertEquals(BigInteger.valueOf(6), Fractions.commonDenominator(listOf(r("1/2"), r("1/3"))))
    }

    @Test
    fun `rejects malformed input`() {
        listOf("", "/", "1/", "/2", "abc", "1/0").forEach { input ->
            try {
                Fractions.parse(input)
                throw AssertionError("Expected rejection for '$input'")
            } catch (expected: Exception) {
                // expected
            }
        }
    }

    @Test
    fun `negative denominators are normalised`() {
        val value = Rational.of(BigInteger.valueOf(3), BigInteger.valueOf(-4))
        assertEquals(BigInteger.valueOf(-3), value.numerator)
        assertEquals(BigInteger.valueOf(4), value.denominator)
    }
}

/** Number theory helpers required by Quick Solve. */
class NumberTheoryTest {

    @Test
    fun `gcd and lcm match the mandated vectors`() {
        assertEquals(BigInteger.valueOf(6), NumberTheory.gcd(bi(48), bi(18)))
        assertEquals(BigInteger.valueOf(36), NumberTheory.lcm(bi(12), bi(18)))
    }

    @Test
    fun `gcd handles zero and negatives`() {
        assertEquals(BigInteger.ZERO, NumberTheory.gcd(bi(0), bi(0)))
        assertEquals(BigInteger.valueOf(5), NumberTheory.gcd(bi(-15), bi(25)))
    }

    @Test
    fun `primality`() {
        assertTrue(NumberTheory.isPrime(bi(2)))
        assertTrue(NumberTheory.isPrime(bi(97)))
        assertFalse(NumberTheory.isPrime(bi(1)))
        assertFalse(NumberTheory.isPrime(bi(91)))
        assertFalse(NumberTheory.isPrime(bi(-7)))
    }

    @Test
    fun `prime factorisation is canonical`() {
        val factors = NumberTheory.primeFactorisation(bi(360))
        assertEquals(listOf(bi(2) to 3, bi(3) to 2, bi(5) to 1), factors)
        assertEquals("2^3 × 3^2 × 5", NumberTheory.factorisationText(bi(360)))
    }

    @Test
    fun `divisors are complete and sorted`() {
        assertEquals(
            listOf(bi(1), bi(2), bi(3), bi(4), bi(6), bi(12)),
            NumberTheory.divisors(bi(12)),
        )
    }

    @Test
    fun `multiples are generated in order`() {
        assertEquals(
            listOf(bi(7), bi(14), bi(21)),
            NumberTheory.multiples(bi(7), 3),
        )
    }

    @Test
    fun `perfect squares and cubes`() {
        assertTrue(NumberTheory.isPerfectSquare(bi(144)))
        assertFalse(NumberTheory.isPerfectSquare(bi(145)))
        assertTrue(NumberTheory.isPerfectCube(bi(27)))
        assertTrue(NumberTheory.isPerfectCube(bi(1_000_000)))
        assertFalse(NumberTheory.isPerfectCube(bi(28)))
    }

    @Test
    fun `divisibility rules`() {
        val checks = NumberTheory.divisibilityChecks(bi(132))
        assertEquals(true, checks[2])
        assertEquals(true, checks[3])
        assertEquals(true, checks[4])
        assertEquals(false, checks[5])
        assertEquals(true, checks[6])
        assertEquals(false, checks[9])
        assertEquals(true, checks[11])
    }

    private fun bi(value: Long): BigInteger = BigInteger.valueOf(value)
}

/** Descriptive statistics, including the mandated mean and median vectors. */
class StatisticsTest {

    @Test
    fun `mean and median of the mandated datasets`() {
        assertEquals(5.0, Statistics.summarize(listOf(2.0, 4.0, 6.0, 8.0)).mean, 0.0)
        assertEquals(3.0, Statistics.summarize(listOf(1.0, 3.0, 7.0)).median, 0.0)
    }

    @Test
    fun `quartiles use linear interpolation`() {
        val summary = Statistics.summarize(listOf(1.0, 2.0, 3.0, 4.0, 5.0))
        assertEquals(2.0, summary.q1, 1e-12)
        assertEquals(4.0, summary.q3, 1e-12)
        assertEquals(2.0, summary.iqr, 1e-12)
    }

    @Test
    fun `variance distinguishes population and sample`() {
        val summary = Statistics.summarize(listOf(2.0, 4.0, 4.0, 4.0, 5.0, 5.0, 7.0, 9.0))
        assertEquals(4.0, summary.populationVariance, 1e-12)
        assertEquals(32.0 / 7.0, summary.sampleVariance ?: Double.NaN, 1e-12)
        assertEquals(2.0, summary.populationStandardDeviation, 1e-12)
    }

    @Test
    fun `modes are reported only when values repeat`() {
        assertTrue(Statistics.summarize(listOf(1.0, 2.0, 3.0)).modes.isEmpty())
        assertEquals(listOf(2.0), Statistics.summarize(listOf(1.0, 2.0, 2.0, 3.0)).modes)
        assertEquals(listOf(1.0, 3.0), Statistics.summarize(listOf(1.0, 1.0, 3.0, 3.0)).modes)
    }

    @Test
    fun `single value has no sample variance`() {
        val summary = Statistics.summarize(listOf(42.0))
        assertEquals(0.0, summary.populationVariance, 0.0)
        assertNull(summary.sampleVariance)
    }

    @Test
    fun `empty and non-finite input is rejected`() {
        try {
            Statistics.summarize(emptyList())
            throw AssertionError("Expected a rejection")
        } catch (expected: MathEvalException) {
            // expected
        }
        try {
            Statistics.summarize(listOf(1.0, Double.NaN))
            throw AssertionError("Expected a rejection")
        } catch (expected: MathEvalException) {
            // expected
        }
    }

    @Test
    fun `percentiles match the R-7 definition`() {
        val values = listOf(1.0, 2.0, 3.0, 4.0, 5.0, 6.0, 7.0, 8.0, 9.0, 10.0)
        assertEquals(1.0, Statistics.percentile(values, 0.0), 1e-12)
        assertEquals(10.0, Statistics.percentile(values, 100.0), 1e-12)
        assertEquals(5.5, Statistics.percentile(values, 50.0), 1e-12)
    }
}

/** Probability helpers are exact for combinatorics and stable for binomials. */
class ProbabilityTest {

    @Test
    fun `factorial is exact`() {
        assertEquals(BigInteger.valueOf(120), Probability.factorial(5))
        assertEquals(BigInteger.ONE, Probability.factorial(0))
        assertEquals(BigInteger("2432902008176640000"), Probability.factorial(20))
    }

    @Test
    fun `combinations and permutations`() {
        assertEquals(BigInteger.valueOf(10), Probability.combinations(5, 2))
        assertEquals(BigInteger.valueOf(20), Probability.permutations(5, 2))
        assertEquals(BigInteger.ONE, Probability.combinations(5, 0))
        assertEquals(BigInteger.ONE, Probability.combinations(5, 5))
        try {
            Probability.combinations(5, 6)
            throw AssertionError("Expected a rejection when r > n")
        } catch (expected: MathEvalException) {
            // expected: r cannot exceed n
        }
    }

    @Test
    fun `binomial distribution probabilities`() {
        val exactlyTwo = Probability.binomialProbability(4, 2, 0.5)
        assertEquals(0.375, exactlyTwo, 1e-12)
        assertEquals(1.0, Probability.binomialAtMost(4, 4, 0.5), 1e-12)
        assertEquals(1.0, Probability.binomialAtLeast(4, 0, 0.5), 1e-12)
        assertEquals(0.5, Probability.binomialAtMost(1, 0, 0.5), 1e-12)
    }

    @Test
    fun `probability rules`() {
        assertEquals(0.25, Probability.independentAnd(0.5, 0.5), 1e-12)
        assertEquals(0.75, Probability.independentOr(0.5, 0.5), 1e-12)
        assertEquals(0.25, Probability.conditional(0.1, 0.4), 1e-12)
        assertEquals(0.2, Probability.eventProbability(3, 15), 1e-12)
        assertEquals(0.7, Probability.complement(0.3), 1e-12)
    }

    @Test
    fun `invalid probability arguments are rejected`() {
        listOf(-0.1, 1.1, Double.NaN).forEach { value ->
            try {
                Probability.requireProbability(value)
                throw AssertionError("Expected rejection for $value")
            } catch (expected: Exception) {
                // expected
            }
        }
    }
}
