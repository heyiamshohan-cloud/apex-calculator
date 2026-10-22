package com.shohankhan.apexcalculator.core.domain.solver

import com.shohankhan.apexcalculator.core.math.DateMath
import com.shohankhan.apexcalculator.core.math.MathEvalException
import com.shohankhan.apexcalculator.core.math.Numerical
import com.shohankhan.apexcalculator.core.math.Probability
import com.shohankhan.apexcalculator.core.math.ResultFormatter
import com.shohankhan.apexcalculator.core.math.Statistics
import java.time.format.DateTimeFormatter
import kotlin.math.abs

internal fun statisticsSolvers(): List<SolverSpec> = listOf(
    SolverSpec(
        id = "dataset_summary",
        title = "Data set summary",
        description = "Mean, median, mode, spread and quartiles for a list of values.",
        category = SolverCategory.STATISTICS,
        fields = listOf(
            SolverField("values", "Values (comma separated)", "2, 4, 6, 8", SolverFieldKind.LIST),
        ),
    ) { input ->
        val values = input.decimalList("values", "Values")
        val summary = Statistics.summarize(values)
        SolverOutput(
            headline = fmt(summary.mean),
            headlineLabel = "Mean",
            rows = listOf(
                "Count" to summary.count.toString(),
                "Sum" to fmt(summary.sum),
                "Median" to fmt(summary.median),
                "Mode" to if (summary.modes.isEmpty()) "No repeated value" else summary.modes.joinToString(", ") { fmt(it) },
                "Minimum" to fmt(summary.minimum),
                "Maximum" to fmt(summary.maximum),
                "Range" to fmt(summary.range),
                "Q1" to fmt(summary.q1),
                "Q3" to fmt(summary.q3),
                "Interquartile range" to fmt(summary.iqr),
                "Population variance" to fmt(summary.populationVariance),
                "Sample variance" to (summary.sampleVariance?.let { fmt(it) } ?: "—"),
                "Population standard deviation" to fmt(summary.populationStandardDeviation),
                "Sample standard deviation" to (summary.sampleStandardDeviation?.let { fmt(it) } ?: "—"),
            ),
            steps = listOf(
                "Mean = sum ÷ count = ${fmt(summary.sum)} ÷ ${summary.count}",
                "Quartiles use linear interpolation on the sorted sample (R-7)",
                "Population statistics divide by n, sample statistics divide by n − 1",
            ),
        )
    },
    SolverSpec(
        id = "weighted_mean_solver",
        title = "Weighted mean",
        description = "Mean of values that carry different weights.",
        category = SolverCategory.STATISTICS,
        fields = listOf(
            SolverField("values", "Values (comma separated)", "80, 90, 70", SolverFieldKind.LIST),
            SolverField("weights", "Weights (comma separated)", "2, 3, 5", SolverFieldKind.LIST),
        ),
    ) { input ->
        val values = input.decimalList("values", "Values")
        val weights = input.decimalList("weights", "Weights")
        if (values.size != weights.size) throw SolverException("Values and weights must have the same count")
        val mean = Statistics.weightedMean(values, weights)
        SolverOutput(
            headline = fmt(mean),
            headlineLabel = "Weighted mean",
            rows = listOf("Total weight" to fmt(weights.sum()), "Unweighted mean" to fmt(values.average())),
            steps = listOf("Σ(value × weight) ÷ Σweight"),
        )
    },
    SolverSpec(
        id = "percentile_solver",
        title = "Percentile",
        description = "Percentile of a data set using linear interpolation (R-7).",
        category = SolverCategory.STATISTICS,
        fields = listOf(
            SolverField("values", "Values (comma separated)", "12, 18, 20, 25, 30", SolverFieldKind.LIST),
            SolverField("p", "Percentile", "75"),
        ),
    ) { input ->
        val values = input.decimalList("values", "Values").sorted()
        val percent = input.number("p", "Percentile")
        if (percent < 0 || percent > 100) throw SolverException("The percentile must be between 0 and 100")
        SolverOutput(
            headline = fmt(Statistics.percentile(values, percent)),
            headlineLabel = "P${fmt(percent)}",
            rows = listOf(
                "Sorted values" to values.joinToString(", ") { fmt(it) },
                "Q1" to fmt(Statistics.percentile(values, 25.0)),
                "Median" to fmt(Statistics.percentile(values, 50.0)),
                "Q3" to fmt(Statistics.percentile(values, 75.0)),
            ),
        )
    },
    SolverSpec(
        id = "z_score_solver",
        title = "Z-score",
        description = "Standard score of a value within a data set.",
        category = SolverCategory.STATISTICS,
        fields = listOf(
            SolverField("values", "Data set (comma separated)", "10, 12, 23, 23, 16, 23, 21, 16", SolverFieldKind.LIST),
            SolverField("x", "Value", "20"),
        ),
    ) { input ->
        val values = input.decimalList("values", "Data set")
        val x = input.number("x", "Value")
        val summary = Statistics.summarize(values)
        val sd = summary.sampleStandardDeviation
            ?: throw SolverException("At least two values are needed")
        if (sd == 0.0) throw SolverException("The standard deviation is zero, so no z-score is defined")
        val z = (x - summary.mean) / sd
        SolverOutput(
            headline = fmt(z),
            headlineLabel = "Z-score",
            rows = listOf(
                "Mean" to fmt(summary.mean),
                "Sample standard deviation" to fmt(sd),
                "Values below z" to fmt(Statistics.percentile(values.sorted(), 50.0)),
            ),
            steps = listOf("z = (x − mean) ÷ standard deviation"),
            note = "Uses the sample standard deviation (n − 1).",
        )
    },
    SolverSpec(
        id = "correlation_solver",
        title = "Covariance and correlation",
        description = "Sample covariance and Pearson correlation for paired data.",
        category = SolverCategory.STATISTICS,
        fields = listOf(
            SolverField("xs", "X values (comma separated)", "1, 2, 3, 4, 5", SolverFieldKind.LIST),
            SolverField("ys", "Y values (comma separated)", "2, 4, 5, 4, 5", SolverFieldKind.LIST),
        ),
    ) { input ->
        val xs = input.decimalList("xs", "X values")
        val ys = input.decimalList("ys", "Y values")
        if (xs.size != ys.size) throw SolverException("Both data sets must have the same count")
        if (xs.size < 2) throw SolverException("At least two pairs are required")
        val covariance = Statistics.covariance(xs, ys)
        val correlation = Statistics.correlation(xs, ys)
        val strength = when {
            abs(correlation) >= 0.9 -> "Very strong"
            abs(correlation) >= 0.7 -> "Strong"
            abs(correlation) >= 0.4 -> "Moderate"
            abs(correlation) >= 0.2 -> "Weak"
            else -> "Very weak"
        }
        SolverOutput(
            headline = fmt(correlation),
            headlineLabel = "Correlation r",
            rows = listOf(
                "Sample covariance" to fmt(covariance),
                "Strength" to strength,
                "Direction" to if (correlation >= 0) "Positive" else "Negative",
                "Pairs" to xs.size.toString(),
            ),
            steps = listOf("Pearson r = covariance ÷ (sx × sy)"),
        )
    },
)

internal fun probabilitySolvers(): List<SolverSpec> = listOf(
    SolverSpec(
        id = "factorial_solver",
        title = "Factorial",
        description = "n! for a non-negative integer up to 1000.",
        category = SolverCategory.PROBABILITY,
        fields = listOf(SolverField("n", "n", "10", SolverFieldKind.INTEGER)),
    ) { input ->
        val n = input.integer("n", "n")
        if (n < 0) throw SolverException("Factorial requires a non-negative integer")
        if (n > 1000) throw SolverException("Factorial is limited to 1000!")
        val value = Probability.factorial(n.toInt())
        SolverOutput(
            headline = value.toString(),
            headlineLabel = "$n!",
            rows = listOf(
                "Digits" to value.toString().length.toString(),
                "Scientific" to ResultFormatter.compact(value.toDouble(), 10),
            ),
        )
    },
    SolverSpec(
        id = "permutation_solver",
        title = "Permutations",
        description = "Ordered arrangements of r items from n.",
        category = SolverCategory.PROBABILITY,
        fields = listOf(
            SolverField("n", "n", "6", SolverFieldKind.INTEGER),
            SolverField("r", "r", "3", SolverFieldKind.INTEGER),
        ),
    ) { input ->
        val n = input.integer("n", "n")
        val r = input.integer("r", "r")
        if (n < 0 || r < 0) throw SolverException("n and r must be non-negative")
        if (r > n) throw SolverException("r cannot be greater than n")
        if (n > 1000) throw SolverException("n is limited to 1000")
        SolverOutput(
            headline = Probability.permutations(n.toInt(), r.toInt()).toString(),
            headlineLabel = "P($n, $r)",
            steps = listOf("P(n, r) = n! ÷ (n − r)!"),
        )
    },
    SolverSpec(
        id = "combination_solver",
        title = "Combinations",
        description = "Unordered selections of r items from n.",
        category = SolverCategory.PROBABILITY,
        fields = listOf(
            SolverField("n", "n", "6", SolverFieldKind.INTEGER),
            SolverField("r", "r", "3", SolverFieldKind.INTEGER),
        ),
    ) { input ->
        val n = input.integer("n", "n")
        val r = input.integer("r", "r")
        if (n < 0 || r < 0) throw SolverException("n and r must be non-negative")
        if (r > n) throw SolverException("r cannot be greater than n")
        if (n > 1000) throw SolverException("n is limited to 1000")
        SolverOutput(
            headline = Probability.combinations(n.toInt(), r.toInt()).toString(),
            headlineLabel = "C($n, $r)",
            steps = listOf("C(n, r) = n! ÷ (r!(n − r)!)"),
        )
    },
    SolverSpec(
        id = "binomial_solver",
        title = "Binomial probability",
        description = "Exactly k successes in n independent trials.",
        category = SolverCategory.PROBABILITY,
        fields = listOf(
            SolverField("n", "Trials n", "10", SolverFieldKind.INTEGER),
            SolverField("k", "Successes k", "3", SolverFieldKind.INTEGER),
            SolverField("p", "Success probability", "0.5"),
        ),
    ) { input ->
        val trials = input.integer("n", "Trials n")
        val successes = input.integer("k", "Successes k")
        val probability = input.number("p", "Success probability")
        if (trials < 0 || trials > 50_000) throw SolverException("Trials must be between 0 and 50,000")
        if (successes < 0 || successes > trials) throw SolverException("Successes must be between 0 and n")
        if (probability < 0 || probability > 1) throw SolverException("The probability must be between 0 and 1")
        val exact = Probability.binomialProbability(trials.toInt(), successes.toInt(), probability)
        SolverOutput(
            headline = fmt(exact),
            headlineLabel = "P(X = $successes)",
            rows = listOf(
                "P(X ≤ $successes)" to fmt(Probability.binomialAtMost(trials.toInt(), successes.toInt(), probability)),
                "P(X ≥ $successes)" to fmt(Probability.binomialAtLeast(trials.toInt(), successes.toInt(), probability)),
                "Expected successes" to fmt(trials * probability),
            ),
            steps = listOf("C(n, k) × p^k × (1 − p)^(n − k)"),
        )
    },
    SolverSpec(
        id = "event_probability_solver",
        title = "Event probability",
        description = "Probability from favourable and total outcomes.",
        category = SolverCategory.PROBABILITY,
        fields = listOf(
            SolverField("favourable", "Favourable outcomes", "3", SolverFieldKind.INTEGER),
            SolverField("total", "Total outcomes", "12", SolverFieldKind.INTEGER),
        ),
    ) { input ->
        val favourable = input.integer("favourable", "Favourable outcomes")
        val total = input.integer("total", "Total outcomes")
        if (total <= 0) throw SolverException("Total outcomes must be greater than zero")
        if (favourable < 0 || favourable > total) throw SolverException("Favourable outcomes must be between 0 and the total")
        val probability = favourable.toDouble() / total
        SolverOutput(
            headline = fmt(probability),
            headlineLabel = "P(E)",
            rows = listOf(
                "Percentage" to "${fmt(probability * 100)}%",
                "Complement P(not E)" to fmt(1 - probability),
                "Odds in favour" to "${favourable}:${total - favourable}",
                "Decimal" to fmt(probability),
            ),
            steps = listOf("P(E) = favourable ÷ total = $favourable ÷ $total"),
        )
    },
    SolverSpec(
        id = "probability_rules_solver",
        title = "Probability rules",
        description = "Complement, independent events and conditional probability.",
        category = SolverCategory.PROBABILITY,
        fields = listOf(
            SolverField("pa", "P(A)", "0.5"),
            SolverField("pb", "P(B)", "0.4"),
            SolverField("joint", "P(A and B)", "0.2"),
        ),
    ) { input ->
        val pa = input.number("pa", "P(A)")
        val pb = input.number("pb", "P(B)")
        val joint = input.number("joint", "P(A and B)")
        if (pa !in 0.0..1.0 || pb !in 0.0..1.0 || joint !in 0.0..1.0) {
            throw SolverException("Probabilities must be between 0 and 1")
        }
        if (joint > minOf(pa, pb)) throw SolverException("P(A and B) cannot exceed P(A) or P(B)")
        val conditional = if (pb > 0) joint / pb else null
        SolverOutput(
            headline = fmt(pa + pb - joint),
            headlineLabel = "P(A or B)",
            rows = listOf(
                "P(not A)" to fmt(1 - pa),
                "P(A|B)" to (conditional?.let { fmt(it) } ?: "P(B) is zero"),
                "Independent?" to if (abs(joint - pa * pb) < 1e-9) "Yes" else "No",
            ),
            steps = listOf("P(A or B) = P(A) + P(B) − P(A and B)", "P(A|B) = P(A and B) ÷ P(B)"),
        )
    },
)

internal fun calculusSolvers(): List<SolverSpec> = listOf(
    SolverSpec(
        id = "derivative_point",
        title = "Numerical derivative",
        description = "Slope of a function at a point using a five-point central difference.",
        category = SolverCategory.CALCULUS,
        fields = listOf(
            SolverField("expression", "f(x)", "x^2 + 2x", SolverFieldKind.EXPRESSION),
            SolverField("x", "Point x", "3"),
        ),
    ) { input ->
        val expression = input.text("expression", "f(x)")
        val x = input.number("x", "Point x")
        val function = functionOf(expression, input.angleUnit())
        val derivative = Numerical.derivative(function, x)
        SolverOutput(
            headline = fmt(derivative),
            headlineLabel = "f′(${fmt(x)})",
            rows = listOf(
                "f(x)" to fmt(function(x)),
                "Method" to "Five-point central difference",
                "Step h" to fmt(1e-5 * maxOf(1.0, abs(x))),
            ),
            note = "Numerical approximation; verify against the analytic derivative when available.",
        )
    },
    SolverSpec(
        id = "definite_integral",
        title = "Definite integral",
        description = "Numeric area under a curve using adaptive Simpson quadrature.",
        category = SolverCategory.CALCULUS,
        fields = listOf(
            SolverField("expression", "f(x)", "x^2", SolverFieldKind.EXPRESSION),
            SolverField("a", "Lower limit", "0"),
            SolverField("b", "Upper limit", "3"),
        ),
    ) { input ->
        val expression = input.text("expression", "f(x)")
        val a = input.number("a", "Lower limit")
        val b = input.number("b", "Upper limit")
        val function = functionOf(expression, input.angleUnit())
        val integral = try {
            Numerical.integrate(function, a, b)
        } catch (exception: MathEvalException) {
            throw SolverException(exception.error.message)
        }
        SolverOutput(
            headline = fmt(integral),
            headlineLabel = "∫ f(x) dx",
            rows = listOf(
                "Interval" to "[${fmt(minOf(a, b))}, ${fmt(maxOf(a, b))}]",
                "Method" to "Adaptive Simpson",
                "Tolerance" to "1e-9",
            ),
            note = "Numerical approximation; discontinuous or singular functions may need a different method.",
        )
    },
    SolverSpec(
        id = "root_finding",
        title = "Numerical root finding",
        description = "Finds a root inside a bracket where the function changes sign.",
        category = SolverCategory.CALCULUS,
        fields = listOf(
            SolverField("expression", "f(x)", "cos(x) - x", SolverFieldKind.EXPRESSION),
            SolverField("a", "Lower bound", "0"),
            SolverField("b", "Upper bound", "1"),
        ),
    ) { input ->
        val expression = input.text("expression", "f(x)")
        val a = input.number("a", "Lower bound")
        val b = input.number("b", "Upper bound")
        val function = functionOf(expression, input.angleUnit())
        val result = try {
            Numerical.findRoot(function, a, b)
        } catch (exception: MathEvalException) {
            throw SolverException(exception.error.message)
        }
        SolverOutput(
            headline = "x ≈ ${fmt(result.root)}",
            headlineLabel = "Root",
            rows = listOf("f(x)" to fmt(result.residual), "Iterations" to result.iterations.toString()),
            steps = listOf("Bisection on a sign-changing bracket", "Tolerance 1e-12"),
        )
    },
    SolverSpec(
        id = "function_table",
        title = "Function table",
        description = "Generates a table of values for a function.",
        category = SolverCategory.CALCULUS,
        fields = listOf(
            SolverField("expression", "f(x)", "x^2", SolverFieldKind.EXPRESSION),
            SolverField("start", "Start x", "0"),
            SolverField("end", "End x", "5"),
            SolverField("step", "Step", "1"),
        ),
    ) { input ->
        val expression = input.text("expression", "f(x)")
        val start = input.number("start", "Start x")
        val end = input.number("end", "End x")
        val step = input.number("step", "Step")
        val function = functionOf(expression, input.angleUnit())
        val rows = try {
            Numerical.table(function, start, end, step)
        } catch (exception: MathEvalException) {
            throw SolverException(exception.error.message)
        }
        SolverOutput(
            headline = "${rows.size} rows",
            headlineLabel = "Function table",
            rows = rows.take(40).map { fmt(it.x) to fmt(it.y) },
            note = if (rows.size > 40) "Showing the first 40 rows of ${rows.size}" else null,
        )
    },
)

internal fun dateSolvers(): List<SolverSpec> = listOf(
    SolverSpec(
        id = "days_between",
        title = "Days between dates",
        description = "Difference between two calendar dates, including weekdays.",
        category = SolverCategory.DATE_TIME,
        fields = listOf(
            SolverField("start", "Start date (yyyy-MM-dd)", "2026-01-15", SolverFieldKind.TEXT),
            SolverField("end", "End date (yyyy-MM-dd)", "2026-03-01", SolverFieldKind.TEXT),
        ),
    ) { input ->
        val start = parseDate(input.text("start", "Start date"))
        val end = parseDate(input.text("end", "End date"))
        val difference = DateMath.difference(start, end)
        SolverOutput(
            headline = "${difference.totalDays} days",
            headlineLabel = "Difference",
            rows = listOf(
                "Calendar difference" to "${difference.yearsComponent} y ${difference.monthsComponent} m ${difference.daysComponent} d",
                "Weeks" to "${difference.weeks} weeks and ${difference.totalDays % 7} days",
                "Months (approx.)" to difference.months.toString(),
                "Weekdays" to difference.weekdays.toString(),
                "Start" to start.format(DateTimeFormatter.ISO_DATE),
                "End" to end.format(DateTimeFormatter.ISO_DATE),
            ),
            steps = listOf("Dates are counted as whole calendar days", "Weekdays count Monday to Friday"),
        )
    },
    SolverSpec(
        id = "add_days",
        title = "Add or subtract days",
        description = "Moves a date forward or backward by a number of days.",
        category = SolverCategory.DATE_TIME,
        fields = listOf(
            SolverField("date", "Date (yyyy-MM-dd)", "2026-01-31", SolverFieldKind.TEXT),
            SolverField("days", "Days (negative to subtract)", "45", SolverFieldKind.INTEGER),
        ),
    ) { input ->
        val date = parseDate(input.text("date", "Date"))
        val days = input.integer("days", "Days")
        val result = DateMath.addDays(date, days)
        SolverOutput(
            headline = result.format(DateTimeFormatter.ISO_DATE),
            headlineLabel = "Result date",
            rows = listOf(
                "Original" to date.format(DateTimeFormatter.ISO_DATE),
                "Weekday" to result.dayOfWeek.name.lowercase().replaceFirstChar { it.uppercase() },
                "Days in result month" to result.lengthOfMonth().toString(),
            ),
        )
    },
    SolverSpec(
        id = "add_months",
        title = "Add or subtract months",
        description = "Month arithmetic that clamps to the last valid day.",
        category = SolverCategory.DATE_TIME,
        fields = listOf(
            SolverField("date", "Date (yyyy-MM-dd)", "2026-01-31", SolverFieldKind.TEXT),
            SolverField("months", "Months (negative to subtract)", "1", SolverFieldKind.INTEGER),
        ),
    ) { input ->
        val date = parseDate(input.text("date", "Date"))
        val months = input.integer("months", "Months")
        val result = DateMath.addMonths(date, months)
        SolverOutput(
            headline = result.format(DateTimeFormatter.ISO_DATE),
            headlineLabel = "Result date",
            rows = listOf("Original" to date.format(DateTimeFormatter.ISO_DATE)),
            note = "If the target day does not exist, the last day of that month is used.",
        )
    },
    SolverSpec(
        id = "time_difference",
        title = "Time difference",
        description = "Duration between two times of day.",
        category = SolverCategory.DATE_TIME,
        fields = listOf(
            SolverField("start", "Start time (HH:mm)", "09:30", SolverFieldKind.TEXT),
            SolverField("end", "End time (HH:mm)", "17:45", SolverFieldKind.TEXT),
            SolverField("next_day", "Ends next day? (1 = yes, 0 = no)", "0", SolverFieldKind.INTEGER),
        ),
    ) { input ->
        val start = parseTime(input.text("start", "Start time"))
        val end = parseTime(input.text("end", "End time"))
        val nextDay = input.integer("next_day", "Next day flag") == 1L
        val parts = try {
            DateMath.durationBetween(start, end, nextDay)
        } catch (exception: MathEvalException) {
            throw SolverException(exception.error.message)
        }
        val totalSeconds = parts.days * 86400 + parts.hours * 3600 + parts.minutes * 60 + parts.seconds
        SolverOutput(
            headline = parts.display(),
            headlineLabel = "Duration",
            rows = listOf(
                "Total minutes" to fmt(totalSeconds / 60.0),
                "Total hours" to fmt(totalSeconds / 3600.0),
                "Total seconds" to totalSeconds.toString(),
            ),
        )
    },
    SolverSpec(
        id = "duration_breakdown",
        title = "Duration breakdown",
        description = "Converts a number of seconds into days, hours, minutes and seconds.",
        category = SolverCategory.DATE_TIME,
        fields = listOf(SolverField("seconds", "Total seconds", "93784", SolverFieldKind.INTEGER)),
    ) { input ->
        val seconds = input.integer("seconds", "Total seconds")
        if (seconds < 0) throw SolverException("Total seconds cannot be negative")
        val parts = DateMath.fromSeconds(seconds)
        SolverOutput(
            headline = parts.display(),
            headlineLabel = "Duration",
            rows = listOf(
                "Days" to parts.days.toString(),
                "Hours" to parts.hours.toString(),
                "Minutes" to parts.minutes.toString(),
                "Seconds" to parts.seconds.toString(),
            ),
            steps = listOf("Divide by 86400 for days, then 3600, 60 and 1"),
        )
    },
)

private fun parseDate(text: String): java.time.LocalDate = try {
    DateMath.parseDate(text)
} catch (exception: MathEvalException) {
    throw SolverException(exception.error.message)
}

private fun parseTime(text: String): java.time.LocalTime = try {
    DateMath.parseTime(text)
} catch (exception: MathEvalException) {
    throw SolverException(exception.error.message)
}
