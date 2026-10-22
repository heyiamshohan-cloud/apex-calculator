package com.shohankhan.apexcalculator.core.math

import kotlin.math.abs
import kotlin.math.pow
import kotlin.math.sqrt

/**
 * Descriptive statistics.
 *
 * Conventions (documented so results are reproducible):
 *  - quartiles and percentiles use linear interpolation on the sorted sample (R-7, the same
 *    method as spreadsheet PERCENTILE.INC)
 *  - "population" divides by n, "sample" divides by n − 1
 *  - the mode is only reported when at least one value repeats
 */
data class StatsSummary(
    val values: List<Double>,
    val count: Int,
    val sum: Double,
    val mean: Double,
    val median: Double,
    val modes: List<Double>,
    val minimum: Double,
    val maximum: Double,
    val range: Double,
    val populationVariance: Double,
    val sampleVariance: Double?,
    val populationStandardDeviation: Double,
    val sampleStandardDeviation: Double?,
    val q1: Double,
    val q3: Double,
    val iqr: Double,
    val percentiles: Map<Int, Double>,
)

object Statistics {

    fun summarize(values: List<Double>): StatsSummary {
        if (values.isEmpty()) throw MathEvalException(MathError.Domain("Enter at least one value"))
        if (values.size > MAX_VALUES) {
            throw MathEvalException(MathError.Limit("At most $MAX_VALUES values are supported"))
        }
        values.forEach {
            if (!it.isFinite()) throw MathEvalException(MathError.Domain("Data contains a non-finite value"))
        }
        val sorted = values.sorted()
        val n = values.size
        val sum = values.sum()
        val mean = sum / n
        val median = percentile(sorted, 50.0)
        val populationVariance = values.sumOf { (it - mean).pow(2) } / n
        val sampleVariance = if (n > 1) values.sumOf { (it - mean).pow(2) } / (n - 1) else null
        val frequencies = values.groupingBy { it }.eachCount()
        val highest = frequencies.values.maxOrNull() ?: 0
        val modes = if (highest <= 1) emptyList() else {
            frequencies.filterValues { it == highest }.keys.sorted()
        }
        return StatsSummary(
            values = values,
            count = n,
            sum = sum,
            mean = mean,
            median = median,
            modes = modes,
            minimum = sorted.first(),
            maximum = sorted.last(),
            range = sorted.last() - sorted.first(),
            populationVariance = populationVariance,
            sampleVariance = sampleVariance,
            populationStandardDeviation = sqrt(populationVariance),
            sampleStandardDeviation = sampleVariance?.let { sqrt(it) },
            q1 = percentile(sorted, 25.0),
            q3 = percentile(sorted, 75.0),
            iqr = percentile(sorted, 75.0) - percentile(sorted, 25.0),
            percentiles = listOf(1, 5, 10, 25, 50, 75, 90, 95, 99)
                .associateWith { percentile(sorted, it.toDouble()) },
        )
    }

    fun percentile(sorted: List<Double>, percent: Double): Double {
        require(sorted.isNotEmpty()) { "No data" }
        if (sorted.size == 1) return sorted[0]
        val rank = (percent.coerceIn(0.0, 100.0) / 100.0) * (sorted.size - 1)
        val lowerIndex = kotlin.math.floor(rank).toInt()
        val upperIndex = kotlin.math.ceil(rank).toInt()
        if (lowerIndex == upperIndex) return sorted[lowerIndex]
        val weight = rank - lowerIndex
        return sorted[lowerIndex] * (1 - weight) + sorted[upperIndex] * weight
    }

    fun weightedMean(values: List<Double>, weights: List<Double>): Double {
        if (values.isEmpty() || values.size != weights.size) {
            throw MathEvalException(MathError.Domain("Values and weights must have the same non-zero length"))
        }
        if (weights.any { it < 0 }) throw MathEvalException(MathError.Domain("Weights cannot be negative"))
        val totalWeight = weights.sum()
        if (totalWeight == 0.0) throw MathEvalException(MathError.Domain("The total weight must be greater than zero"))
        return values.indices.sumOf { values[it] * weights[it] } / totalWeight
    }

    fun covariance(xs: List<Double>, ys: List<Double>, sample: Boolean = true): Double {
        if (xs.size != ys.size || xs.size < 2) {
            throw MathEvalException(MathError.Domain("Two paired data sets with at least two values are required"))
        }
        val meanX = xs.average()
        val meanY = ys.average()
        val divisor = if (sample) xs.size - 1 else xs.size
        return xs.indices.sumOf { (xs[it] - meanX) * (ys[it] - meanY) } / divisor
    }

    fun correlation(xs: List<Double>, ys: List<Double>): Double {
        val covariance = covariance(xs, ys)
        val sdX = summarize(xs).sampleStandardDeviation ?: 0.0
        val sdY = summarize(ys).sampleStandardDeviation ?: 0.0
        if (sdX == 0.0 || sdY == 0.0) {
            throw MathEvalException(MathError.Domain("Correlation needs both data sets to vary"))
        }
        return (covariance / (sdX * sdY)).coerceIn(-1.0, 1.0)
    }

    fun zScore(value: Double, mean: Double, standardDeviation: Double): Double {
        if (standardDeviation <= 0.0) {
            throw MathEvalException(MathError.Domain("The standard deviation must be greater than zero"))
        }
        return (value - mean) / standardDeviation
    }

    fun frequencies(values: List<Double>): List<Pair<Double, Int>> {
        val summary = summarize(values)
        return summary.values.groupingBy { it }.eachCount().toList().sortedBy { it.first }
    }

    fun nearlyEqual(a: Double, b: Double): Boolean = abs(a - b) < 1e-9

    const val MAX_VALUES = 1000
}
