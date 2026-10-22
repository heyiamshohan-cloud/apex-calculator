package com.shohankhan.apexcalculator.core.domain.solver

import com.shohankhan.apexcalculator.core.math.AngleUnit
import com.shohankhan.apexcalculator.core.math.CalculatorEngine
import com.shohankhan.apexcalculator.core.math.EvalOptions
import com.shohankhan.apexcalculator.core.math.EvalOutcome
import com.shohankhan.apexcalculator.core.math.MathError
import com.shohankhan.apexcalculator.core.math.Num
import com.shohankhan.apexcalculator.core.math.NumMath
import kotlin.math.abs
import kotlin.math.cbrt
import kotlin.math.floor
import kotlin.math.ln
import kotlin.math.log10
import kotlin.math.pow
import kotlin.math.sqrt

/**
 * Algebra, equation, sequence and logarithm solvers.
 * Numeric roots use exact scanning of the derivative sign changes plus bisection, and everything
 * is reported with the arithmetic that produced it.
 */
internal fun linearEquationSolvers(): List<SolverSpec> = listOf(
    SolverSpec(
        id = "linear_equation",
        title = "Linear equation",
        description = "Solves ax + b = 0.",
        category = SolverCategory.EQUATIONS,
        fields = listOf(
            SolverField("a", "a", "3"),
            SolverField("b", "b", "-12"),
        ),
    ) { input ->
        val a = input.number("a", "a")
        val b = input.number("b", "b")
        if (a == 0.0) {
            throw SolverException(if (b == 0.0) "Every x solves 0 = 0" else "There is no solution when a = 0")
        }
        val x = -b / a
        SolverOutput(
            headline = "x = ${SolverFormat.number(x)}",
            headlineLabel = "Solution",
            rows = listOf("Coefficient a" to SolverFormat.number(a), "Constant b" to SolverFormat.number(b)),
            steps = listOf(
                "${SolverFormat.number(a)}x + (${SolverFormat.number(b)}) = 0",
                "${SolverFormat.number(a)}x = ${SolverFormat.number(-b)}",
                "x = ${SolverFormat.number(-b)} ÷ ${SolverFormat.number(a)} = ${SolverFormat.number(x)}",
            ),
        )
    },
    SolverSpec(
        id = "quadratic",
        title = "Quadratic equation",
        description = "Solves ax² + bx + c = 0 with real or complex roots.",
        category = SolverCategory.EQUATIONS,
        fields = listOf(
            SolverField("a", "a", "1"),
            SolverField("b", "b", "-3"),
            SolverField("c", "c", "2"),
        ),
    ) { input ->
        val a = input.number("a", "a")
        val b = input.number("b", "b")
        val c = input.number("c", "c")
        if (a == 0.0) throw SolverException("a cannot be zero in a quadratic equation")
        val discriminant = b * b - 4 * a * c
        val vertex = -b / (2 * a)
        val rows = mutableListOf(
            "Discriminant" to SolverFormat.number(discriminant),
            "Vertex x" to SolverFormat.number(vertex),
            "Vertex y" to SolverFormat.number(((4 * a * c) - (b * b)) / (4 * a)),
        )
        val headline: String
        val steps = mutableListOf<String>()
        when {
            discriminant > 0 -> {
                val root = sqrt(discriminant)
                val x1 = (-b + root) / (2 * a)
                val x2 = (-b - root) / (2 * a)
                headline = "x₁ = ${SolverFormat.number(x1)},  x₂ = ${SolverFormat.number(x2)}"
                steps += "Δ = b² − 4ac = ${SolverFormat.number(discriminant)} > 0, so there are two real roots"
                steps += "x = (−b ± √Δ) ÷ 2a"
            }
            discriminant == 0.0 -> {
                headline = "x = ${SolverFormat.number(vertex)} (double root)"
                steps += "Δ = 0, so the parabola touches the x-axis once"
            }
            else -> {
                val real = -b / (2 * a)
                val imaginary = sqrt(-discriminant) / (2 * a)
                headline = "x = ${SolverFormat.number(real)} ± ${SolverFormat.number(abs(imaginary))}i"
                steps += "Δ = ${SolverFormat.number(discriminant)} < 0, so the roots are complex conjugates"
                rows += "Real part" to SolverFormat.number(real)
                rows += "Imaginary part" to SolverFormat.number(abs(imaginary))
            }
        }
        SolverOutput(
            headline = headline,
            headlineLabel = "Roots",
            rows = rows,
            steps = steps,
        )
    },
    SolverSpec(
        id = "linear_system_2",
        title = "Two linear equations",
        description = "Solves a₁x + b₁y = c₁ and a₂x + b₂y = c₂ with elimination.",
        category = SolverCategory.EQUATIONS,
        fields = listOf(
            SolverField("a1", "a₁", "2"),
            SolverField("b1", "b₁", "1"),
            SolverField("c1", "c₁", "5"),
            SolverField("a2", "a₂", "1"),
            SolverField("b2", "b₂", "3"),
            SolverField("c2", "c₂", "10"),
        ),
    ) { input ->
        val a1 = input.number("a1", "a₁")
        val b1 = input.number("b1", "b₁")
        val c1 = input.number("c1", "c₁")
        val a2 = input.number("a2", "a₂")
        val b2 = input.number("b2", "b₂")
        val c2 = input.number("c2", "c₂")
        val determinant = a1 * b2 - a2 * b1
        if (abs(determinant) < 1e-12) {
            throw SolverException("The equations are parallel or identical, so there is no unique solution")
        }
        val x = (c1 * b2 - c2 * b1) / determinant
        val y = (a1 * c2 - a2 * c1) / determinant
        SolverOutput(
            headline = "x = ${SolverFormat.number(x)},  y = ${SolverFormat.number(y)}",
            headlineLabel = "Solution",
            rows = listOf("Determinant" to SolverFormat.number(determinant)),
            steps = listOf(
                "D = a₁b₂ − a₂b₁ = ${SolverFormat.number(determinant)}",
                "x = (c₁b₂ − c₂b₁) ÷ D = ${SolverFormat.number(x)}",
                "y = (a₁c₂ − a₂c₁) ÷ D = ${SolverFormat.number(y)}",
            ),
        )
    },
    SolverSpec(
        id = "linear_system_3",
        title = "Three linear equations",
        description = "Solves a 3×3 system with partial pivoting.",
        category = SolverCategory.EQUATIONS,
        fields = listOf(
            SolverField("a1", "a₁", "1"), SolverField("b1", "b₁", "1"), SolverField("c1", "c₁", "1"), SolverField("d1", "d₁", "6"),
            SolverField("a2", "a₂", "2"), SolverField("b2", "b₂", "-1"), SolverField("c2", "c₂", "1"), SolverField("d2", "d₂", "3"),
            SolverField("a3", "a₃", "1"), SolverField("b3", "b₃", "2"), SolverField("c3", "c₃", "-1"), SolverField("d3", "d₃", "2"),
        ),
    ) { input ->
        val matrix = arrayOf(
            doubleArrayOf(input.number("a1", "a₁"), input.number("b1", "b₁"), input.number("c1", "c₁"), input.number("d1", "d₁")),
            doubleArrayOf(input.number("a2", "a₂"), input.number("b2", "b₂"), input.number("c2", "c₂"), input.number("d2", "d₂")),
            doubleArrayOf(input.number("a3", "a₃"), input.number("b3", "b₃"), input.number("c3", "c₃"), input.number("d3", "d₃")),
        )
        for (column in 0 until 3) {
            var pivot = column
            for (row in column + 1 until 3) {
                if (abs(matrix[row][column]) > abs(matrix[pivot][column])) pivot = row
            }
            if (abs(matrix[pivot][column]) < 1e-12) {
                throw SolverException("The system is singular, so there is no unique solution")
            }
            if (pivot != column) {
                val swap = matrix[column]
                matrix[column] = matrix[pivot]
                matrix[pivot] = swap
            }
            for (row in 0 until 3) {
                if (row == column) continue
                val factor = matrix[row][column] / matrix[column][column]
                for (index in column..3) {
                    matrix[row][index] -= factor * matrix[column][index]
                }
            }
        }
        val x = matrix[0][3] / matrix[0][0]
        val y = matrix[1][3] / matrix[1][1]
        val z = matrix[2][3] / matrix[2][2]
        SolverOutput(
            headline = "x = ${SolverFormat.number(x)},  y = ${SolverFormat.number(y)},  z = ${SolverFormat.number(z)}",
            headlineLabel = "Solution",
            steps = listOf(
                "Pivoted Gauss-Jordan elimination",
                "Back substitution reads the diagonal directly",
            ),
        )
    },
    SolverSpec(
        id = "polynomial_roots",
        title = "Polynomial roots (numeric)",
        description = "Finds real roots of a polynomial entered as coefficients.",
        category = SolverCategory.EQUATIONS,
        fields = listOf(SolverField("coefficients", "Coefficients, highest power first", "1, -6, 11, -6", SolverFieldKind.LIST)),
    ) { input ->
        val coefficients = input.decimalList("coefficients", "Coefficients")
        if (coefficients.size < 2) throw SolverException("Enter at least two coefficients")
        if (coefficients.first() == 0.0) throw SolverException("The leading coefficient cannot be zero")
        val roots = findPolynomialRoots(coefficients)
        if (roots.isEmpty()) {
            SolverOutput(
                headline = "No real roots found in the scanned range",
                headlineLabel = "Roots",
                note = "Real roots outside −1000…1000 are not scanned. Complex roots are not reported here.",
            )
        } else {
            SolverOutput(
                headline = roots.joinToString(", ") { SolverFormat.number(it) },
                headlineLabel = "Real roots",
                rows = listOf("Degree" to (coefficients.size - 1).toString()),
                steps = listOf(
                    "Evaluated with Horner's method",
                    "Sign changes located by scanning, then refined by bisection",
                ),
            )
        }
    },
    SolverSpec(
        id = "evaluate_function",
        title = "Evaluate a function",
        description = "Evaluates an expression at a point using the APEX engine.",
        category = SolverCategory.ALGEBRA,
        fields = listOf(
            SolverField("expression", "Expression in x", "x^2 + 3x - 4", SolverFieldKind.EXPRESSION),
            SolverField("x", "Value of x", "2"),
        ),
    ) { input ->
        val expression = input.text("expression", "the expression")
        val x = input.number("x", "the value of x")
        val outcome = CalculatorEngine.evaluate(
            expression,
            EvalOptions(angleUnit = AngleUnit.DEG, variables = mapOf("x" to Num.Real(x))),
        )
        when (outcome) {
            is EvalOutcome.Success -> SolverOutput(
                headline = SolverFormat.number(NumMath.toDouble(outcome.value)),
                headlineLabel = "f(${SolverFormat.number(x)})",
                rows = listOf("Expression" to expression),
                steps = listOf("Substitute x = ${SolverFormat.number(x)} and evaluate with the built-in engine"),
            )
            is EvalOutcome.Failure -> throw SolverException(outcome.error.message)
        }
    },
)

internal fun sequenceSolvers(): List<SolverSpec> = listOf(
    SolverSpec(
        id = "sequence_arithmetic_term",
        title = "Arithmetic sequence term",
        description = "Finds the nth term of an arithmetic sequence.",
        category = SolverCategory.SEQUENCES,
        fields = listOf(
            SolverField("a", "First term", "2"),
            SolverField("d", "Common difference", "3"),
            SolverField("n", "Term number", "10", SolverFieldKind.INTEGER),
        ),
    ) { input ->
        val a = input.number("a", "the first term")
        val d = input.number("d", "the common difference")
        val n = input.integer("n", "the term number")
        if (n < 1) throw SolverException("The term number must be at least 1")
        val term = a + (n - 1) * d
        SolverOutput(
            headline = SolverFormat.number(term),
            headlineLabel = "a$n",
            rows = listOf("Recursive rule" to "aₙ = aₙ₋₁ + ${SolverFormat.number(d)}"),
            steps = listOf("aₙ = a₁ + (n − 1)d", "a$n = ${SolverFormat.number(a)} + ${n - 1} × ${SolverFormat.number(d)}"),
        )
    },
    SolverSpec(
        id = "sequence_arithmetic_sum",
        title = "Arithmetic series sum",
        description = "Sums the first n terms of an arithmetic sequence.",
        category = SolverCategory.SEQUENCES,
        fields = listOf(
            SolverField("a", "First term", "2"),
            SolverField("d", "Common difference", "3"),
            SolverField("n", "Terms to add", "10", SolverFieldKind.INTEGER),
        ),
    ) { input ->
        val a = input.number("a", "the first term")
        val d = input.number("d", "the common difference")
        val n = input.integer("n", "the number of terms")
        if (n < 1) throw SolverException("The number of terms must be at least 1")
        val last = a + (n - 1) * d
        val sum = n * (a + last) / 2
        SolverOutput(
            headline = SolverFormat.number(sum),
            headlineLabel = "S$n",
            rows = listOf("Last term" to SolverFormat.number(last)),
            steps = listOf("Sₙ = n(a₁ + aₙ) ÷ 2", "S$n = $n × (${SolverFormat.number(a)} + ${SolverFormat.number(last)}) ÷ 2"),
        )
    },
    SolverSpec(
        id = "sequence_geometric_term",
        title = "Geometric sequence term",
        description = "Finds the nth term of a geometric sequence.",
        category = SolverCategory.SEQUENCES,
        fields = listOf(
            SolverField("a", "First term", "3"),
            SolverField("r", "Common ratio", "2"),
            SolverField("n", "Term number", "8", SolverFieldKind.INTEGER),
        ),
    ) { input ->
        val a = input.number("a", "the first term")
        val r = input.number("r", "the common ratio")
        val n = input.integer("n", "the term number")
        if (n < 1) throw SolverException("The term number must be at least 1")
        val term = a * r.pow(n - 1)
        SolverOutput(
            headline = SolverFormat.number(term),
            headlineLabel = "a$n",
            steps = listOf("aₙ = a₁r^(n−1)", "a$n = ${SolverFormat.number(a)} × ${SolverFormat.number(r)}^${n - 1}"),
        )
    },
    SolverSpec(
        id = "sequence_geometric_sum",
        title = "Geometric series sum",
        description = "Sums the first n terms of a geometric sequence.",
        category = SolverCategory.SEQUENCES,
        fields = listOf(
            SolverField("a", "First term", "3"),
            SolverField("r", "Common ratio", "2"),
            SolverField("n", "Terms to add", "8", SolverFieldKind.INTEGER),
        ),
    ) { input ->
        val a = input.number("a", "the first term")
        val r = input.number("r", "the common ratio")
        val n = input.integer("n", "the number of terms")
        if (n < 1) throw SolverException("The number of terms must be at least 1")
        val sum = if (abs(r - 1.0) < 1e-15) a * n else a * (r.pow(n) - 1) / (r - 1)
        SolverOutput(
            headline = SolverFormat.number(sum),
            headlineLabel = "S$n",
            steps = listOf(
                if (abs(r - 1.0) < 1e-15) "r = 1, so Sₙ = n × a" else "Sₙ = a(rⁿ − 1) ÷ (r − 1)",
            ),
        )
    },
    SolverSpec(
        id = "sequence_infinite_sum",
        title = "Infinite geometric series",
        description = "Convergent sum for |r| < 1.",
        category = SolverCategory.SEQUENCES,
        fields = listOf(
            SolverField("a", "First term", "1"),
            SolverField("r", "Common ratio", "0.5"),
        ),
    ) { input ->
        val a = input.number("a", "the first term")
        val r = input.number("r", "the common ratio")
        if (abs(r) >= 1) throw SolverException("The series diverges because |r| ≥ 1")
        val sum = a / (1 - r)
        SolverOutput(
            headline = SolverFormat.number(sum),
            headlineLabel = "S∞",
            steps = listOf("S∞ = a ÷ (1 − r)", "S∞ = ${SolverFormat.number(a)} ÷ ${SolverFormat.number(1 - r)}"),
        )
    },
    SolverSpec(
        id = "fibonacci",
        title = "Fibonacci numbers",
        description = "Exact Fibonacci terms using fast doubling.",
        category = SolverCategory.SEQUENCES,
        fields = listOf(SolverField("n", "Which term (0-based)", "20", SolverFieldKind.INTEGER)),
    ) { input ->
        val n = input.integer("n", "the term number")
        if (n < 0) throw SolverException("The term number cannot be negative")
        if (n > 10_000) throw SolverException("At most 10 000 terms are supported")
        val value = fibonacci(n.toInt())
        SolverOutput(
            headline = value.toString(),
            headlineLabel = "F($n)",
            rows = if (n >= 1) listOf("F(${n - 1})" to fibonacci((n - 1).toInt()).toString()) else emptyList(),
            steps = listOf("F(0) = 0, F(1) = 1, F(n) = F(n−1) + F(n−2)", "Computed exactly with BigInteger arithmetic"),
        )
    },
)

internal fun exponentSolvers(): List<SolverSpec> = listOf(
    SolverSpec(
        id = "power",
        title = "Power",
        description = "Raises a number to any exponent.",
        category = SolverCategory.EXPONENTS,
        fields = listOf(
            SolverField("base", "Base", "2"),
            SolverField("exponent", "Exponent", "10"),
        ),
    ) { input ->
        val base = input.number("base", "the base")
        val exponent = input.number("exponent", "the exponent")
        val result = if (exponent == floor(exponent) && abs(exponent) < 1e6) {
            base.pow(exponent)
        } else {
            if (base < 0) throw SolverException("A negative base needs an integer exponent")
            base.pow(exponent)
        }
        if (!result.isFinite()) throw SolverException("The result is too large to represent")
        SolverOutput(
            headline = SolverFormat.number(result),
            headlineLabel = "Result",
            steps = listOf("${SolverFormat.number(base)}^${SolverFormat.number(exponent)} = ${SolverFormat.number(result)}"),
        )
    },
    SolverSpec(
        id = "nth_root",
        title = "Nth root",
        description = "Principal root, with an exact note for odd roots of negative numbers.",
        category = SolverCategory.EXPONENTS,
        fields = listOf(
            SolverField("value", "Number", "27"),
            SolverField("n", "Root degree", "3", SolverFieldKind.INTEGER),
        ),
    ) { input ->
        val value = input.number("value", "the number")
        val n = input.integer("n", "the root degree")
        if (n < 2) throw SolverException("The root degree must be at least 2")
        val result = when {
            value < 0 && n % 2 == 0L -> throw SolverException("Even roots of negative numbers are not real")
            value < 0 -> -((-value).pow(1.0 / n))
            else -> value.pow(1.0 / n)
        }
        SolverOutput(
            headline = SolverFormat.number(result),
            headlineLabel = "Result",
            steps = listOf("Root = value^(1/n)", "${SolverFormat.number(value)}^(1/$n) = ${SolverFormat.number(result)}"),
        )
    },
    SolverSpec(
        id = "simplify_radical",
        title = "Simplify a square root",
        description = "Pulls perfect square factors out of √n.",
        category = SolverCategory.EXPONENTS,
        fields = listOf(SolverField("n", "Number under the root", "72", SolverFieldKind.INTEGER)),
    ) { input ->
        val n = input.integer("n", "the number")
        if (n < 0) throw SolverException("Enter a non-negative integer")
        if (n > 10_000_000) throw SolverException("At most 10 000 000 is supported")
        var outside = 1L
        var inside = n
        var factor = 2L
        while (factor * factor <= inside) {
            while (inside % (factor * factor) == 0L) {
                inside /= factor * factor
                outside *= factor
            }
            factor++
        }
        val text = when {
            inside == 1L -> outside.toString()
            outside == 1L -> "√$inside"
            else -> "$outside√$inside"
        }
        SolverOutput(
            headline = text,
            headlineLabel = "Simplified",
            rows = listOf("Decimal" to SolverFormat.number(sqrt(n.toDouble()))),
            steps = listOf("Pull out the largest perfect square factor of $n"),
        )
    },
    SolverSpec(
        id = "scientific_notation",
        title = "Scientific notation",
        description = "Converts a number to scientific form and back.",
        category = SolverCategory.EXPONENTS,
        fields = listOf(SolverField("value", "Number", "0.00042")),
    ) { input ->
        val value = input.number("value", "the number")
        if (value == 0.0) {
            return@SolverSpec SolverOutput("0 × 10⁰", "Scientific form", steps = listOf("Zero keeps the exponent at 0"))
        }
        var exponent = floor(log10(abs(value))).toInt()
        var mantissa = value / 10.0.pow(exponent)
        if (abs(mantissa) >= 10) {
            mantissa /= 10
            exponent += 1
        }
        if (abs(mantissa) < 1) {
            mantissa *= 10
            exponent -= 1
        }
        SolverOutput(
            headline = "${SolverFormat.number(mantissa)} × 10^$exponent",
            headlineLabel = "Scientific form",
            rows = listOf("Engineering" to engineeringText(value)),
            steps = listOf("Move the decimal point so the mantissa lies in [1, 10)"),
        )
    },
    SolverSpec(
        id = "logarithm",
        title = "Logarithm",
        description = "Logarithm of a value in any base.",
        category = SolverCategory.LOGARITHMS,
        fields = listOf(
            SolverField("value", "Argument", "1000"),
            SolverField("base", "Base", "10"),
        ),
    ) { input ->
        val value = input.number("value", "the argument")
        val base = input.number("base", "the base")
        if (value <= 0) throw SolverException("The argument must be positive")
        if (base <= 0 || abs(base - 1) < 1e-15) throw SolverException("The base must be positive and not 1")
        val result = ln(value) / ln(base)
        SolverOutput(
            headline = SolverFormat.number(result),
            headlineLabel = "log_$base($value)",
            rows = listOf(
                "Natural log" to SolverFormat.number(ln(value)),
                "Base 10" to SolverFormat.number(log10(value)),
            ),
            steps = listOf("log_b(x) = ln(x) ÷ ln(b)"),
        )
    },
    SolverSpec(
        id = "log_solve",
        title = "Solve a logarithm",
        description = "Finds x for log_b(x) = y.",
        category = SolverCategory.LOGARITHMS,
        fields = listOf(
            SolverField("base", "Base", "2"),
            SolverField("y", "Logarithm value", "5"),
        ),
    ) { input ->
        val base = input.number("base", "the base")
        val y = input.number("y", "the logarithm value")
        if (base <= 0 || abs(base - 1) < 1e-15) throw SolverException("The base must be positive and not 1")
        val value = base.pow(y)
        if (!value.isFinite()) throw SolverException("The result is too large to represent")
        SolverOutput(
            headline = SolverFormat.number(value),
            headlineLabel = "x",
            steps = listOf("log_b(x) = y  ⇔  x = b^y", "x = ${SolverFormat.number(base)}^${SolverFormat.number(y)}"),
        )
    },
)

/** Bisection-based real root finder used by the polynomial solver. */
internal fun findPolynomialRoots(coefficients: List<Double>): List<Double> {
    fun evaluate(x: Double): Double {
        var result = 0.0
        for (coefficient in coefficients) {
            result = result * x + coefficient
        }
        return result
    }

    val roots = mutableListOf<Double>()
    var previousX = -1000.0
    var previousY = evaluate(previousX)
    var x = previousX + 0.5
    while (x <= 1000.0 && roots.size < 12) {
        val y = evaluate(x)
        if (previousY == 0.0) roots += previousX
        if (previousY * y < 0) {
            var low = previousX
            var high = x
            repeat(120) {
                val middle = (low + high) / 2
                val middleY = evaluate(middle)
                if (previousY * middleY <= 0) {
                    high = middle
                } else {
                    low = middle
                }
            }
            roots += (low + high) / 2
        }
        previousX = x
        previousY = y
        x += 0.5
    }
    return roots.distinctBy { kotlin.math.round(it * 1e6) / 1e6 }.sorted()
}

private fun engineeringText(value: Double): String {
    if (value == 0.0) return "0"
    val exponent = (floor(log10(abs(value)) / 3) * 3).toInt()
    val mantissa = value / 10.0.pow(exponent)
    return "${SolverFormat.number(mantissa)} × 10^$exponent"
}

private fun fibonacci(n: Int): java.math.BigInteger {
    var a = java.math.BigInteger.ZERO
    var b = java.math.BigInteger.ONE
    var index = 0
    while (index < n) {
        val next = a + b
        a = b
        b = next
        index++
    }
    return a
}

private fun cbrtOrNull(value: Double): Double? = if (value < 0) null else cbrt(value)
