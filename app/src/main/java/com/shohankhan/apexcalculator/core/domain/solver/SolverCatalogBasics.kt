package com.shohankhan.apexcalculator.core.domain.solver

import com.shohankhan.apexcalculator.core.math.Fractions
import com.shohankhan.apexcalculator.core.math.NumberTheory
import com.shohankhan.apexcalculator.core.math.Rational
import java.math.BigInteger
import kotlin.math.abs

internal fun fractionSolvers(): List<SolverSpec> = listOf(
    SolverSpec(
        id = "fraction_simplify",
        title = "Simplify a fraction",
        description = "Reduces a fraction to lowest terms and shows its decimal and mixed form.",
        category = SolverCategory.FRACTIONS,
        fields = listOf(SolverField("fraction", "Fraction", "3/4", SolverFieldKind.TEXT)),
    ) { input ->
        val value = input.fraction("fraction", "Fraction")
        SolverOutput(
            headline = rationalText(value),
            headlineLabel = "Simplified",
            rows = listOf(
                "Improper fraction" to "${value.numerator}/${value.denominator}",
                "Mixed number" to value.toMixedString(),
                "Decimal" to decimalText(value),
            ),
            steps = listOf(
                "Numerator and denominator share a factor of ${value.numerator.gcd(value.denominator)}",
                "Divide both terms by the greatest common divisor",
            ),
        )
    },
    fractionOperation("fraction_add", "Add fractions", "Adds two fractions exactly.", "+") { a, b -> a + b },
    fractionOperation("fraction_subtract", "Subtract fractions", "Subtracts one fraction from another.", "−") { a, b -> a - b },
    fractionOperation("fraction_multiply", "Multiply fractions", "Multiplies two fractions exactly.", "×") { a, b -> a * b },
    fractionOperation("fraction_divide", "Divide fractions", "Divides one fraction by another.", "÷") { a, b ->
        if (b.isZero) throw SolverException("The second fraction cannot be zero")
        a / b
    },
    SolverSpec(
        id = "fraction_compare",
        title = "Compare fractions",
        description = "Compares two fractions with exact rational arithmetic.",
        category = SolverCategory.FRACTIONS,
        fields = listOf(
            SolverField("a", "First fraction", "3/4", SolverFieldKind.TEXT),
            SolverField("b", "Second fraction", "5/7", SolverFieldKind.TEXT),
        ),
    ) { input ->
        val first = input.fraction("a", "First fraction")
        val second = input.fraction("b", "Second fraction")
        val comparison = first.compareTo(second)
        val symbol = when {
            comparison > 0 -> ">"
            comparison < 0 -> "<"
            else -> "="
        }
        SolverOutput(
            headline = "${rationalText(first)} $symbol ${rationalText(second)}",
            headlineLabel = "Comparison",
            rows = listOf(
                "First as decimal" to decimalText(first),
                "Second as decimal" to decimalText(second),
                "Common denominator" to Fractions.commonDenominator(listOf(first, second)).toString(),
            ),
        )
    },
    SolverSpec(
        id = "mixed_to_improper",
        title = "Mixed number to improper fraction",
        description = "Converts a whole number plus fraction into a single fraction.",
        category = SolverCategory.FRACTIONS,
        fields = listOf(
            SolverField("whole", "Whole number", "2", SolverFieldKind.INTEGER),
            SolverField("numerator", "Numerator", "3", SolverFieldKind.INTEGER),
            SolverField("denominator", "Denominator", "4", SolverFieldKind.INTEGER),
        ),
    ) { input ->
        val denominator = input.integer("denominator", "Denominator")
        if (denominator == 0L) throw SolverException("The denominator cannot be zero")
        val value = Fractions.toImproper(
            BigInteger.valueOf(input.integer("whole", "Whole number")),
            BigInteger.valueOf(input.integer("numerator", "Numerator")),
            BigInteger.valueOf(denominator),
        )
        SolverOutput(
            headline = rationalText(value),
            headlineLabel = "Improper fraction",
            rows = listOf("Decimal" to decimalText(value)),
            steps = listOf(
                "Multiply the whole number by the denominator and add the numerator",
                "Keep the same denominator, then simplify",
            ),
        )
    },
    SolverSpec(
        id = "improper_to_mixed",
        title = "Improper fraction to mixed number",
        description = "Rewrites an improper fraction as a whole number and a proper fraction.",
        category = SolverCategory.FRACTIONS,
        fields = listOf(SolverField("fraction", "Fraction", "11/4", SolverFieldKind.TEXT)),
    ) { input ->
        val value = input.fraction("fraction", "Fraction")
        val mixed = Fractions.toMixed(value)
        SolverOutput(
            headline = mixed.display(),
            headlineLabel = "Mixed number",
            rows = listOf(
                "Improper fraction" to rationalText(value),
                "Decimal" to decimalText(value),
                "Whole part" to mixed.whole.toString(),
                "Remainder" to "${mixed.numerator}/${mixed.denominator}",
            ),
            steps = listOf(
                "Divide the numerator by the denominator",
                "The quotient is the whole part, the remainder stays over the denominator",
            ),
        )
    },
    SolverSpec(
        id = "fraction_of_number",
        title = "Fraction of a number",
        description = "Calculates a fraction of any value exactly.",
        category = SolverCategory.FRACTIONS,
        fields = listOf(
            SolverField("fraction", "Fraction", "3/8", SolverFieldKind.TEXT),
            SolverField("number", "Number"),
        ),
    ) { input ->
        val fraction = input.fraction("fraction", "Fraction")
        val number = input.number("number", "Number")
        val result = Fractions.multiply(fraction, Rational.fromBigDecimal(parsedDecimal(number.toString(), "Number")))
        SolverOutput(
            headline = rationalText(result),
            headlineLabel = "Result",
            rows = listOf("Decimal" to decimalText(result)),
            steps = listOf("${rationalText(fraction)} × ${fmt(number)}"),
        )
    },
    SolverSpec(
        id = "fraction_to_decimal",
        title = "Fraction to decimal",
        description = "Shows whether the decimal terminates and gives a rounded value.",
        category = SolverCategory.FRACTIONS,
        fields = listOf(SolverField("fraction", "Fraction", "7/8", SolverFieldKind.TEXT)),
    ) { input ->
        val value = input.fraction("fraction", "Fraction")
        val terminates = value.hasTerminatingDecimal()
        SolverOutput(
            headline = decimalText(value),
            headlineLabel = "Decimal",
            rows = listOf(
                "Exact fraction" to rationalText(value),
                "Expansion" to if (terminates) "Terminating" else "Repeating",
            ),
            steps = listOf(
                if (terminates) {
                    "The denominator only has factors of 2 and 5, so the decimal terminates"
                } else {
                    "The denominator has other factors, so the decimal repeats and is rounded here"
                },
            ),
        )
    },
    SolverSpec(
        id = "decimal_to_fraction",
        title = "Decimal to fraction",
        description = "Converts a terminating decimal to an exact fraction in lowest terms.",
        category = SolverCategory.FRACTIONS,
        fields = listOf(SolverField("decimal", "Decimal", "0.375", SolverFieldKind.TEXT)),
    ) { input ->
        val value = input.fraction("decimal", "Decimal")
        SolverOutput(
            headline = rationalText(value),
            headlineLabel = "Fraction",
            rows = listOf("Mixed number" to value.toMixedString(), "Decimal" to decimalText(value)),
            steps = listOf("Write the decimal over its place value, then divide by the greatest common divisor"),
        )
    },
    SolverSpec(
        id = "fraction_order",
        title = "Order fractions",
        description = "Sorts a list of fractions from smallest to largest.",
        category = SolverCategory.FRACTIONS,
        fields = listOf(SolverField("list", "Fractions (comma separated)", "3/4, 1/2, 5/8", SolverFieldKind.TEXT)),
    ) { input ->
        val raw = input.text("list", "Fractions").split(',', ';')
            .map { it.trim() }
            .filter { it.isNotEmpty() }
        if (raw.size < 2) throw SolverException("Enter at least two fractions")
        val ordered = raw.map { Fractions.parse(it) }.sorted()
        SolverOutput(
            headline = ordered.joinToString(" < ") { rationalText(it) },
            headlineLabel = "Ascending order",
            rows = listOf("Descending" to ordered.reversed().joinToString(" > ") { rationalText(it) }),
        )
    },
)

private fun fractionOperation(
    id: String,
    title: String,
    description: String,
    symbol: String,
    operation: (Rational, Rational) -> Rational,
): SolverSpec = SolverSpec(
    id = id,
    title = title,
    description = description,
    category = SolverCategory.FRACTIONS,
    fields = listOf(
        SolverField("a", "First fraction", "1/2", SolverFieldKind.TEXT),
        SolverField("b", "Second fraction", "1/3", SolverFieldKind.TEXT),
    ),
) { input ->
    val first = input.fraction("a", "First fraction")
    val second = input.fraction("b", "Second fraction")
    val result = operation(first, second)
    SolverOutput(
        headline = rationalText(result),
        headlineLabel = "Result",
        rows = listOf(
            "Decimal" to decimalText(result),
            "Mixed number" to result.toMixedString(),
        ),
        steps = listOf("${rationalText(first)} $symbol ${rationalText(second)} = ${rationalText(result)}"),
    )
}

internal fun ratioSolvers(): List<SolverSpec> = listOf(
    SolverSpec(
        id = "ratio_simplify",
        title = "Simplify a ratio",
        description = "Reduces a two-part ratio and converts it to percentages.",
        category = SolverCategory.RATIOS,
        fields = listOf(
            SolverField("a", "First term", "24", SolverFieldKind.NUMBER),
            SolverField("b", "Second term", "36", SolverFieldKind.NUMBER),
        ),
    ) { input ->
        val a = input.number("a", "First term")
        val b = input.number("b", "Second term")
        if (a == 0.0 && b == 0.0) throw SolverException("A ratio cannot have both terms equal to zero")
        if (b == 0.0) throw SolverException("The second term cannot be zero")
        if (a < 0 || b < 0) throw SolverException("Ratio terms cannot be negative")
        val ratio = ratioOf(a, b)
        val total = a + b
        SolverOutput(
            headline = "${ratio.first}:${ratio.second}",
            headlineLabel = "Simplified ratio",
            rows = listOf(
                "First term" to "${fmt(a / total * 100)}% of the whole",
                "Second term" to "${fmt(b / total * 100)}% of the whole",
                "Decimal form" to fmt(a / b),
            ),
            steps = listOf("Divide both terms by their greatest common divisor"),
        )
    },
    SolverSpec(
        id = "ratio_divide",
        title = "Divide a quantity in a ratio",
        description = "Splits an amount into parts that follow a ratio.",
        category = SolverCategory.RATIOS,
        fields = listOf(
            SolverField("total", "Total quantity", "600"),
            SolverField("a", "First part", "2", SolverFieldKind.NUMBER),
            SolverField("b", "Second part", "3", SolverFieldKind.NUMBER),
        ),
    ) { input ->
        val total = input.number("total", "Total quantity")
        val a = requireNonNegativeValue(input.number("a", "First part"), "First part")
        val b = requireNonNegativeValue(input.number("b", "Second part"), "Second part")
        if (a + b == 0.0) throw SolverException("The ratio cannot be zero to zero")
        SolverOutput(
            headline = fmt(total * a / (a + b)),
            headlineLabel = "First part",
            rows = listOf(
                "Second part" to fmt(total * b / (a + b)),
                "Total parts" to fmt(a + b),
                "Value per part" to fmt(total / (a + b)),
            ),
            steps = listOf("Each part is total ÷ (a + b), then multiplied by the ratio term"),
        )
    },
    SolverSpec(
        id = "ratio_proportion",
        title = "Find a missing proportion",
        description = "Solves a : b = c : x for x.",
        category = SolverCategory.RATIOS,
        fields = listOf(
            SolverField("a", "a", "3"),
            SolverField("b", "b", "4"),
            SolverField("c", "c", "15"),
        ),
    ) { input ->
        val a = input.number("a", "a")
        val b = input.number("b", "b")
        val c = input.number("c", "c")
        if (a == 0.0) throw SolverException("a cannot be zero")
        SolverOutput(
            headline = fmt(b * c / a),
            headlineLabel = "x",
            rows = listOf("Proportion" to "${fmt(a)} : ${fmt(b)} = ${fmt(c)} : x"),
            steps = listOf("Cross multiply: x = b × c ÷ a"),
        )
    },
    SolverSpec(
        id = "ratio_percent",
        title = "Ratio to percentages",
        description = "Converts a two-part ratio into percentage shares.",
        category = SolverCategory.RATIOS,
        fields = listOf(
            SolverField("a", "First term", "2"),
            SolverField("b", "Second term", "5"),
        ),
    ) { input ->
        val a = requireNonNegativeValue(input.number("a", "First term"), "First term")
        val b = requireNonNegativeValue(input.number("b", "Second term"), "Second term")
        if (a + b == 0.0) throw SolverException("The ratio cannot be zero to zero")
        val first = a / (a + b) * 100
        SolverOutput(
            headline = "${fmt(first)}%",
            headlineLabel = "First share",
            rows = listOf("Second share" to "${fmt(100 - first)}%"),
        )
    },
    SolverSpec(
        id = "ratio_scale",
        title = "Scale a ratio",
        description = "Multiplies both terms of a ratio by a factor.",
        category = SolverCategory.RATIOS,
        fields = listOf(
            SolverField("a", "First term", "3"),
            SolverField("b", "Second term", "5"),
            SolverField("factor", "Scale factor", "4"),
        ),
    ) { input ->
        val factor = requirePositiveValue(input.number("factor", "Scale factor"), "Scale factor")
        val a = input.number("a", "First term")
        val b = input.number("b", "Second term")
        SolverOutput(
            headline = "${fmt(a * factor)}:${fmt(b * factor)}",
            headlineLabel = "Scaled ratio",
            rows = listOf("Original ratio" to "${fmt(a)}:${fmt(b)}"),
        )
    },
    SolverSpec(
        id = "ratio_three_part",
        title = "Three-part ratio split",
        description = "Divides a quantity using a three-part ratio.",
        category = SolverCategory.RATIOS,
        fields = listOf(
            SolverField("total", "Total", "720"),
            SolverField("a", "Part a", "2"),
            SolverField("b", "Part b", "3"),
            SolverField("c", "Part c", "4"),
        ),
    ) { input ->
        val total = input.number("total", "Total")
        val a = requireNonNegativeValue(input.number("a", "Part a"), "Part a")
        val b = requireNonNegativeValue(input.number("b", "Part b"), "Part b")
        val c = requireNonNegativeValue(input.number("c", "Part c"), "Part c")
        val sum = a + b + c
        if (sum == 0.0) throw SolverException("The ratio cannot be all zeros")
        SolverOutput(
            headline = fmt(total * a / sum),
            headlineLabel = "Part a",
            rows = listOf(
                "Part b" to fmt(total * b / sum),
                "Part c" to fmt(total * c / sum),
                "Total ratio parts" to fmt(sum),
            ),
            steps = listOf("Each part = total × (term ÷ sum of terms)"),
        )
    },
)

internal fun numberTheorySolvers(): List<SolverSpec> = listOf(
    SolverSpec(
        id = "gcd_lcm",
        title = "GCD and LCM",
        description = "Greatest common divisor and least common multiple of two integers.",
        category = SolverCategory.NUMBER_THEORY,
        fields = listOf(
            SolverField("a", "First integer", "48", SolverFieldKind.INTEGER),
            SolverField("b", "Second integer", "18", SolverFieldKind.INTEGER),
        ),
    ) { input ->
        val a = input.integer("a", "First integer")
        val b = input.integer("b", "Second integer")
        val left = BigInteger.valueOf(a)
        val right = BigInteger.valueOf(b)
        SolverOutput(
            headline = NumberTheory.gcd(left, right).toString(),
            headlineLabel = "GCD",
            rows = listOf(
                "LCM" to NumberTheory.lcm(left, right).toString(),
                "Product" to left.multiply(right).toString(),
            ),
            steps = listOf(
                "GCD × LCM = a × b",
                "Euclidean algorithm used for the GCD",
            ),
        )
    },
    SolverSpec(
        id = "prime_check",
        title = "Prime check",
        description = "Tests whether an integer is prime and lists its factors.",
        category = SolverCategory.NUMBER_THEORY,
        fields = listOf(SolverField("n", "Integer", "97", SolverFieldKind.INTEGER)),
    ) { input ->
        val n = input.integer("n", "Integer")
        val value = BigInteger.valueOf(n)
        if (n < 0) throw SolverException("Enter a non-negative integer")
        val prime = n >= 2 && NumberTheory.isPrime(value)
        val factors = if (n in 2..100_000) {
            NumberTheory.primeFactorisation(value).joinToString(" × ") { (p, e) ->
                if (e == 1) p.toString() else "$p^$e"
            }
        } else {
            "Not calculated for values above 100,000"
        }
        SolverOutput(
            headline = if (prime) "Prime" else "Not prime",
            headlineLabel = "$n is",
            rows = listOf(
                "Prime factors" to factors,
                "Digit sum" to n.toString().sumOf { it - '0' }.toString(),
            ),
            steps = listOf("Divisibility checked up to the square root of $n"),
        )
    },
    SolverSpec(
        id = "prime_factorization",
        title = "Prime factorisation",
        description = "Expresses an integer as a product of primes.",
        category = SolverCategory.NUMBER_THEORY,
        fields = listOf(SolverField("n", "Integer", "360", SolverFieldKind.INTEGER)),
    ) { input ->
        val n = input.integer("n", "Integer")
        if (n < 2) throw SolverException("Enter an integer of 2 or more")
        if (n > NumberTheory.MAX_FACTOR_INPUT.toLong()) throw SolverException("Factorisation is limited to 10^13")
        val factors = NumberTheory.primeFactorisation(BigInteger.valueOf(n))
        SolverOutput(
            headline = factors.joinToString(" × ") { (p, e) -> if (e == 1) p.toString() else "$p^$e" },
            headlineLabel = "Prime factorisation",
            rows = listOf(
                "Number of prime factors" to factors.sumOf { it.second }.toString(),
                "Distinct primes" to factors.size.toString(),
            ),
            steps = factors.map { (p, e) -> "$p appears $e time(s)" },
        )
    },
    SolverSpec(
        id = "divisors",
        title = "Divisors and factors",
        description = "Lists every positive divisor of an integer.",
        category = SolverCategory.NUMBER_THEORY,
        fields = listOf(SolverField("n", "Integer", "36", SolverFieldKind.INTEGER)),
    ) { input ->
        val n = input.integer("n", "Integer")
        if (n <= 0) throw SolverException("Enter a positive integer")
        val divisors = NumberTheory.divisors(BigInteger.valueOf(n))
        SolverOutput(
            headline = divisors.size.toString(),
            headlineLabel = "Number of divisors",
            rows = listOf(
                "Divisors" to divisors.joinToString(", "),
                "Sum of divisors" to divisors.fold(BigInteger.ZERO) { acc, value -> acc.add(value) }.toString(),
            ),
        )
    },
    SolverSpec(
        id = "divisibility",
        title = "Divisibility tests",
        description = "Checks divisibility by 2, 3, 4, 5, 6, 8, 9, 10 and 11.",
        category = SolverCategory.NUMBER_THEORY,
        fields = listOf(SolverField("n", "Integer", "1386", SolverFieldKind.INTEGER)),
    ) { input ->
        val n = input.integer("n", "Integer")
        val checks = NumberTheory.divisibilityChecks(BigInteger.valueOf(n))
        SolverOutput(
            headline = checks.filterValues { it }.keys.joinToString(", ").ifEmpty { "none" },
            headlineLabel = "Divisible by",
            rows = checks.map { (divisor, divisible) -> "Divisible by $divisor" to if (divisible) "Yes" else "No" },
        )
    },
    SolverSpec(
        id = "multiples",
        title = "Multiples",
        description = "Lists the first multiples of an integer.",
        category = SolverCategory.NUMBER_THEORY,
        fields = listOf(
            SolverField("n", "Integer", "7", SolverFieldKind.INTEGER),
            SolverField("count", "How many", "10", SolverFieldKind.INTEGER),
        ),
    ) { input ->
        val n = input.integer("n", "Integer")
        val count = input.integer("count", "How many").toInt()
        if (count !in 1..200) throw SolverException("Choose between 1 and 200 multiples")
        val multiples = NumberTheory.multiples(BigInteger.valueOf(n), count)
        SolverOutput(
            headline = multiples.joinToString(", "),
            headlineLabel = "First $count multiples",
            rows = listOf("Step" to n.toString()),
        )
    },
)

private fun ratioOf(a: Double, b: Double): Pair<String, String> {
    val left = Rational.fromBigDecimal(parsedDecimal(a.toString(), "a"))
    val right = Rational.fromBigDecimal(parsedDecimal(b.toString(), "b"))
    val scale = Fractions.commonDenominator(listOf(left, right))
    val intLeft = (left * Rational.of(scale)).numerator
    val intRight = (right * Rational.of(scale)).numerator
    val divisor = intLeft.gcd(intRight)
    return (intLeft.divide(divisor)).toString() to (intRight.divide(divisor)).toString()
}
