package com.shohankhan.apexcalculator.core.domain.solver

import com.shohankhan.apexcalculator.core.math.CalculatorEngine
import com.shohankhan.apexcalculator.core.math.MathError
import com.shohankhan.apexcalculator.core.math.MathEvalException
import com.shohankhan.apexcalculator.core.math.Matrix
import com.shohankhan.apexcalculator.core.math.MatrixMath
import com.shohankhan.apexcalculator.core.math.MatrixResult
import com.shohankhan.apexcalculator.core.math.Num
import com.shohankhan.apexcalculator.core.math.NumMath
import com.shohankhan.apexcalculator.core.math.ResultFormatter
import com.shohankhan.apexcalculator.core.math.AngleUnit
import com.shohankhan.apexcalculator.core.math.EvalOptions
import kotlin.math.PI
import kotlin.math.abs
import kotlin.math.sqrt

private fun Map<String, String>.angleUnit(): AngleUnit =
    if (this["__angle"] == AngleUnit.RAD.name) AngleUnit.RAD else AngleUnit.DEG

internal fun algebraSolvers(): List<SolverSpec> = listOf(
    SolverSpec(
        id = "linear_equation",
        title = "Linear equation",
        description = "Solves ax + b = c for x.",
        category = SolverCategory.EQUATIONS,
        fields = listOf(
            SolverField("a", "Coefficient a", "2"),
            SolverField("b", "Constant b", "-6"),
            SolverField("c", "Right-hand side c", "10"),
        ),
    ) { input ->
        val a = input.number("a", "Coefficient a")
        val b = input.number("b", "Constant b")
        val c = input.number("c", "Right-hand side c")
        if (a == 0.0) {
            throw SolverException(
                if (b == c) "Every value of x satisfies this equation" else "This equation has no solution",
            )
        }
        val x = (c - b) / a
        SolverOutput(
            headline = "x = ${fmt(x)}",
            headlineLabel = "Solution",
            rows = listOf("Rearranged" to "${fmt(a)}x = ${fmt(c - b)}", "Check" to "${fmt(a)} × ${fmt(x)} + ${fmt(b)} = ${fmt(a * x + b)}"),
            steps = listOf(
                "Start: ${fmt(a)}x + ${fmt(b)} = ${fmt(c)}",
                "Subtract ${fmt(b)} from both sides: ${fmt(a)}x = ${fmt(c - b)}",
                "Divide both sides by ${fmt(a)}: x = ${fmt(x)}",
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
        val vertexX = -b / (2 * a)
        if (discriminant >= 0) {
            val root = sqrt(discriminant)
            val first = (-b + root) / (2 * a)
            val second = (-b - root) / (2 * a)
            SolverOutput(
                headline = "x₁ = ${fmt(first)},  x₂ = ${fmt(second)}",
                headlineLabel = "Roots",
                rows = listOf(
                    "Discriminant" to fmt(discriminant),
                    "Nature" to if (discriminant == 0.0) "One repeated real root" else "Two distinct real roots",
                    "Vertex x" to fmt(vertexX),
                    "Sum of roots" to fmt(-b / a),
                    "Product of roots" to fmt(c / a),
                ),
                steps = listOf(
                    "Discriminant Δ = b² − 4ac = ${fmt(discriminant)}",
                    "x = (−b ± √Δ) ÷ 2a",
                    "x = (${fmt(-b)} ± ${fmt(root)}) ÷ ${fmt(2 * a)}",
                ),
            )
        } else {
            val real = -b / (2 * a)
            val imaginary = sqrt(-discriminant) / (2 * a)
            SolverOutput(
                headline = "x = ${fmt(real)} ± ${fmt(abs(imaginary))}i",
                headlineLabel = "Complex roots",
                rows = listOf(
                    "Discriminant" to fmt(discriminant),
                    "Real part" to fmt(real),
                    "Imaginary part" to fmt(abs(imaginary)),
                    "Vertex x" to fmt(vertexX),
                ),
                steps = listOf(
                    "Discriminant Δ = ${fmt(discriminant)} is negative",
                    "The roots are complex conjugates",
                ),
                note = "Complex roots are reported in the form a ± bi.",
            )
        }
    },
    SolverSpec(
        id = "linear_system_2",
        title = "Two-variable linear system",
        description = "Solves a₁x + b₁y = c₁ and a₂x + b₂y = c₂ using elimination.",
        category = SolverCategory.EQUATIONS,
        fields = listOf(
            SolverField("a1", "a₁", "2"),
            SolverField("b1", "b₁", "3"),
            SolverField("c1", "c₁", "12"),
            SolverField("a2", "a₂", "4"),
            SolverField("b2", "b₂", "-1"),
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
            throw SolverException(
                "The equations are dependent or inconsistent, so there is no unique solution",
            )
        }
        val x = (c1 * b2 - c2 * b1) / determinant
        val y = (a1 * c2 - a2 * c1) / determinant
        SolverOutput(
            headline = "x = ${fmt(x)},  y = ${fmt(y)}",
            headlineLabel = "Solution",
            rows = listOf(
                "Determinant" to fmt(determinant),
                "Check equation 1" to fmt(a1 * x + b1 * y),
                "Check equation 2" to fmt(a2 * x + b2 * y),
            ),
            steps = listOf(
                "D = a₁b₂ − a₂b₁ = ${fmt(determinant)}",
                "x = (c₁b₂ − c₂b₁) ÷ D",
                "y = (a₁c₂ − a₂c₁) ÷ D",
            ),
        )
    },
    SolverSpec(
        id = "linear_system_3",
        title = "Three-variable linear system",
        description = "Solves three equations using Gaussian elimination with partial pivoting.",
        category = SolverCategory.EQUATIONS,
        fields = listOf(
            SolverField("a1", "a₁", "1"), SolverField("b1", "b₁", "1"), SolverField("c1", "c₁", "1"), SolverField("d1", "d₁", "6"),
            SolverField("a2", "a₂", "2"), SolverField("b2", "b₂", "-1"), SolverField("c2", "c₂", "1"), SolverField("d2", "d₂", "3"),
            SolverField("a3", "a₃", "1"), SolverField("b3", "b₃", "2"), SolverField("c3", "c₃", "-1"), SolverField("d3", "d₃", "2"),
        ),
    ) { input ->
        val matrix = Matrix.of(
            listOf(
                listOf(input.number("a1", "a₁"), input.number("b1", "b₁"), input.number("c1", "c₁")),
                listOf(input.number("a2", "a₂"), input.number("b2", "b₂"), input.number("c2", "c₂")),
                listOf(input.number("a3", "a₃"), input.number("b3", "b₃"), input.number("c3", "c₃")),
            ),
        )
        val rhs = Matrix.of(
            listOf(
                listOf(input.number("d1", "d₁")),
                listOf(input.number("d2", "d₂")),
                listOf(input.number("d3", "d₃")),
            ),
        )
        when (val result = MatrixMath.solve(matrix, rhs)) {
            is MatrixResult.MatrixValue -> SolverOutput(
                headline = "x = ${fmt(result.matrix[0, 0])},  y = ${fmt(result.matrix[1, 0])},  z = ${fmt(result.matrix[2, 0])}",
                headlineLabel = "Solution",
                rows = listOf(
                    "Determinant" to ((MatrixMath.determinant(matrix) as? MatrixResult.Scalar)?.value?.let { fmt(it) } ?: "—"),
                    "Check 1" to fmt(
                        matrix[0, 0] * result.matrix[0, 0] + matrix[0, 1] * result.matrix[1, 0] + matrix[0, 2] * result.matrix[2, 0],
                    ),
                ),
                steps = listOf("Row reduction with partial pivoting", "Back substitution"),
            )
            is MatrixResult.Failure -> throw SolverException(result.message)
            is MatrixResult.Scalar -> throw SolverException("Unexpected result type")
        }
    },
    SolverSpec(
        id = "polynomial_roots",
        title = "Polynomial root (numerical)",
        description = "Finds a root of a polynomial inside a bracket using bisection.",
        category = SolverCategory.CALCULUS,
        fields = listOf(
            SolverField("expression", "f(x)", "x^3 - 2x - 5", SolverFieldKind.EXPRESSION),
            SolverField("a", "Lower bound", "2"),
            SolverField("b", "Upper bound", "3"),
        ),
    ) { input ->
        val expression = input.text("expression", "f(x)")
        val lower = input.number("a", "Lower bound")
        val upper = input.number("b", "Upper bound")
        val function = functionOf(expression, input.angleUnit())
        val root = try {
            com.shohankhan.apexcalculator.core.math.Numerical.findRoot(function, lower, upper)
        } catch (exception: MathEvalException) {
            throw SolverException(exception.error.message)
        }
        SolverOutput(
            headline = "x ≈ ${fmt(root.root)}",
            headlineLabel = "Root",
            rows = listOf(
                "f(root)" to fmt(root.residual),
                "Iterations" to root.iterations.toString(),
                "Bracket" to "[${fmt(lower)}, ${fmt(upper)}]",
            ),
            steps = listOf("Bisection with a sign change across the bracket", "Tolerance 1e-12"),
            note = "Numerical result: other roots may exist outside the bracket.",
        )
    },
    SolverSpec(
        id = "evaluate_function",
        title = "Evaluate a function",
        description = "Evaluates an expression of x with the shared math engine.",
        category = SolverCategory.CALCULUS,
        fields = listOf(
            SolverField("expression", "f(x)", "sin(x) + x^2", SolverFieldKind.EXPRESSION),
            SolverField("x", "x", "1.5"),
        ),
    ) { input ->
        val expression = input.text("expression", "f(x)")
        val x = input.number("x", "x")
        val value = evaluateAt(expression, x, input.angleUnit())
        SolverOutput(
            headline = fmt(value),
            headlineLabel = "f(${fmt(x)})",
            rows = listOf("Expression" to expression, "Angle mode" to input.angleUnit().label),
        )
    },
)

internal fun sequenceSolvers(): List<SolverSpec> = listOf(
    SolverSpec(
        id = "sequence_arithmetic_term",
        title = "Arithmetic sequence term",
        description = "Finds the n-th term of an arithmetic sequence.",
        category = SolverCategory.SEQUENCES,
        fields = listOf(
            SolverField("a1", "First term", "3"),
            SolverField("d", "Common difference", "4"),
            SolverField("n", "Term number", "20", SolverFieldKind.INTEGER),
        ),
    ) { input ->
        val first = input.number("a1", "First term")
        val difference = input.number("d", "Common difference")
        val n = input.integer("n", "Term number")
        if (n < 1) throw SolverException("The term number must be at least 1")
        val value = first + (n - 1) * difference
        SolverOutput(
            headline = fmt(value),
            headlineLabel = "a$n",
            rows = listOf(
                "First term" to fmt(first),
                "Common difference" to fmt(difference),
                "Previous term" to fmt(first + (n - 2) * difference),
            ),
            steps = listOf("aₙ = a₁ + (n − 1)d = ${fmt(first)} + ${n - 1} × ${fmt(difference)}"),
        )
    },
    SolverSpec(
        id = "sequence_arithmetic_sum",
        title = "Arithmetic series sum",
        description = "Sums the first n terms of an arithmetic sequence.",
        category = SolverCategory.SEQUENCES,
        fields = listOf(
            SolverField("a1", "First term", "3"),
            SolverField("d", "Common difference", "4"),
            SolverField("n", "Number of terms", "20", SolverFieldKind.INTEGER),
        ),
    ) { input ->
        val first = input.number("a1", "First term")
        val difference = input.number("d", "Common difference")
        val n = input.integer("n", "Number of terms")
        if (n < 1) throw SolverException("The number of terms must be at least 1")
        val last = first + (n - 1) * difference
        SolverOutput(
            headline = fmt(n / 2.0 * (first + last)),
            headlineLabel = "Sum of $n terms",
            rows = listOf("Last term" to fmt(last), "Sum formula" to "n(a₁ + aₙ) ÷ 2"),
            steps = listOf("aₙ = ${fmt(last)}", "S = $n × (${fmt(first)} + ${fmt(last)}) ÷ 2"),
        )
    },
    SolverSpec(
        id = "sequence_geometric_term",
        title = "Geometric sequence term",
        description = "Finds the n-th term of a geometric sequence.",
        category = SolverCategory.SEQUENCES,
        fields = listOf(
            SolverField("a1", "First term", "2"),
            SolverField("r", "Common ratio", "3"),
            SolverField("n", "Term number", "8", SolverFieldKind.INTEGER),
        ),
    ) { input ->
        val first = input.number("a1", "First term")
        val ratio = input.number("r", "Common ratio")
        val n = input.integer("n", "Term number")
        if (n < 1) throw SolverException("The term number must be at least 1")
        val value = first * Math.pow(ratio, (n - 1).toDouble())
        if (!value.isFinite()) throw SolverException("The term is too large to represent")
        SolverOutput(
            headline = fmt(value),
            headlineLabel = "a$n",
            rows = listOf("First term" to fmt(first), "Common ratio" to fmt(ratio)),
            steps = listOf("aₙ = a₁ × r^(n−1)"),
        )
    },
    SolverSpec(
        id = "sequence_geometric_sum",
        title = "Geometric series sum",
        description = "Sums the first n terms of a geometric sequence.",
        category = SolverCategory.SEQUENCES,
        fields = listOf(
            SolverField("a1", "First term", "2"),
            SolverField("r", "Common ratio", "3"),
            SolverField("n", "Number of terms", "8", SolverFieldKind.INTEGER),
        ),
    ) { input ->
        val first = input.number("a1", "First term")
        val ratio = input.number("r", "Common ratio")
        val n = input.integer("n", "Number of terms")
        if (n < 1) throw SolverException("The number of terms must be at least 1")
        val sum = if (ratio == 1.0) first * n else first * (Math.pow(ratio, n.toDouble()) - 1) / (ratio - 1)
        SolverOutput(
            headline = fmt(sum),
            headlineLabel = "Sum",
            rows = listOf("Ratio" to fmt(ratio), "Terms" to n.toString()),
            steps = listOf(if (ratio == 1.0) "r = 1, so the sum is n × a₁" else "Sₙ = a₁(rⁿ − 1) ÷ (r − 1)"),
        )
    },
    SolverSpec(
        id = "sequence_infinite_sum",
        title = "Infinite geometric series",
        description = "Sums an infinite geometric series when it converges.",
        category = SolverCategory.SEQUENCES,
        fields = listOf(
            SolverField("a1", "First term", "4"),
            SolverField("r", "Common ratio", "0.5"),
        ),
    ) { input ->
        val first = input.number("a1", "First term")
        val ratio = input.number("r", "Common ratio")
        if (abs(ratio) >= 1) throw SolverException("The series diverges because |r| ≥ 1")
        SolverOutput(
            headline = fmt(first / (1 - ratio)),
            headlineLabel = "Sum",
            rows = listOf("Condition" to "|r| < 1 is satisfied"),
            steps = listOf("S = a₁ ÷ (1 − r) = ${fmt(first)} ÷ (1 − ${fmt(ratio)})"),
        )
    },
    SolverSpec(
        id = "fibonacci",
        title = "Fibonacci sequence",
        description = "Generates Fibonacci numbers up to a chosen position.",
        category = SolverCategory.SEQUENCES,
        fields = listOf(SolverField("n", "How many terms", "12", SolverFieldKind.INTEGER)),
    ) { input ->
        val n = input.integer("n", "How many terms")
        if (n < 1 || n > 90) throw SolverException("Choose between 1 and 90 terms")
        val values = ArrayList<Long>()
        var a = 0L
        var b = 1L
        for (index in 1..n) {
            values += a
            val next = a + b
            a = b
            b = next
        }
        SolverOutput(
            headline = values.last().toString(),
            headlineLabel = "F($n)",
            rows = listOf(
                "Sequence" to values.joinToString(", "),
                "Golden ratio limit" to fmt(if (n >= 2) values[n - 1].toDouble() / values[n - 2] else 0.0),
            ),
        )
    },
)

internal fun exponentSolvers(): List<SolverSpec> = listOf(
    SolverSpec(
        id = "power",
        title = "Power",
        description = "Raises a number to a power with exact integer results where possible.",
        category = SolverCategory.EXPONENTS,
        fields = listOf(SolverField("base", "Base", "2"), SolverField("exponent", "Exponent", "10")),
    ) { input ->
        val base = input.number("base", "Base")
        val exponent = input.number("exponent", "Exponent")
        val result = Math.pow(base, exponent)
        if (!result.isFinite()) throw SolverException("The result is too large to represent")
        SolverOutput(
            headline = fmt(result),
            headlineLabel = "${fmt(base)} ^ ${fmt(exponent)}",
            rows = listOf("Scientific" to ResultFormatter.compact(result, 10)),
        )
    },
    SolverSpec(
        id = "nth_root",
        title = "n-th root",
        description = "Principal n-th root of a number.",
        category = SolverCategory.EXPONENTS,
        fields = listOf(SolverField("value", "Value", "64"), SolverField("n", "Root degree", "3")),
    ) { input ->
        val value = input.number("value", "Value")
        val degree = input.number("n", "Root degree")
        if (degree == 0.0) throw SolverException("The root degree cannot be zero")
        if (value < 0 && abs(degree % 2.0) > 1e-9) {
            throw SolverException("An even root of a negative number is not real")
        }
        val result = if (value < 0) -Math.pow(-value, 1 / degree) else Math.pow(value, 1 / degree)
        SolverOutput(
            headline = fmt(result),
            headlineLabel = "Root",
            rows = listOf("Check" to fmt(Math.pow(result, degree))),
            steps = listOf("${fmt(value)}^(1 ÷ ${fmt(degree)})"),
        )
    },
    SolverSpec(
        id = "simplify_radical",
        title = "Simplify a square root",
        description = "Writes √n in the form a√b and gives the decimal value.",
        category = SolverCategory.EXPONENTS,
        fields = listOf(SolverField("n", "Number", "72", SolverFieldKind.INTEGER)),
    ) { input ->
        val n = input.integer("n", "Number")
        if (n < 0) throw SolverException("Enter a non-negative integer")
        if (n > 10_000_000) throw SolverException("Radical simplification is limited to 10,000,000")
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
        SolverOutput(
            headline = if (inside == 1L) "$outside" else "$outside√$inside",
            headlineLabel = "Simplified",
            rows = listOf("Decimal" to fmt(sqrt(n.toDouble())), "Perfect square" to if (inside == 1L) "Yes" else "No"),
            steps = listOf("Extract square factors from $n"),
        )
    },
    SolverSpec(
        id = "scientific_notation",
        title = "Scientific notation",
        description = "Converts a number to scientific and engineering notation.",
        category = SolverCategory.EXPONENTS,
        fields = listOf(SolverField("value", "Value", "0.00042")),
    ) { input ->
        val value = input.number("value", "Value")
        if (value == 0.0) throw SolverException("Zero has no scientific notation exponent")
        val exponent = Math.floor(Math.log10(abs(value))).toInt()
        val mantissa = value / Math.pow(10.0, exponent.toDouble())
        val engineeringExponent = Math.floor(exponent / 3.0).toInt() * 3
        val engineeringMantissa = value / Math.pow(10.0, engineeringExponent.toDouble())
        SolverOutput(
            headline = "${fmt(mantissa)} × 10^$exponent",
            headlineLabel = "Scientific",
            rows = listOf("Engineering" to "${fmt(engineeringMantissa)} × 10^$engineeringExponent"),
        )
    },
    SolverSpec(
        id = "logarithm",
        title = "Logarithm",
        description = "Logarithm of a value in any positive base.",
        category = SolverCategory.LOGARITHMS,
        fields = listOf(
            SolverField("value", "Value", "1000"),
            SolverField("base", "Base", "10"),
        ),
    ) { input ->
        val value = input.number("value", "Value")
        val base = input.number("base", "Base")
        if (value <= 0) throw SolverException("The logarithm is only defined for positive values")
        if (base <= 0 || base == 1.0) throw SolverException("The base must be positive and not equal to 1")
        val result = Math.log(value) / Math.log(base)
        SolverOutput(
            headline = fmt(result),
            headlineLabel = "log_${fmt(base)}(${fmt(value)})",
            rows = listOf("Natural log" to fmt(Math.log(value)), "Base 10 log" to fmt(Math.log10(value))),
            steps = listOf("log_b(x) = ln(x) ÷ ln(b)"),
        )
    },
    SolverSpec(
        id = "log_solve",
        title = "Solve an exponential equation",
        description = "Solves b^x = n for x.",
        category = SolverCategory.LOGARITHMS,
        fields = listOf(SolverField("base", "Base b", "2"), SolverField("n", "Result n", "32")),
    ) { input ->
        val base = input.number("base", "Base b")
        val n = input.number("Result n", "n")
        if (base <= 0 || base == 1.0) throw SolverException("The base must be positive and not equal to 1")
        if (n <= 0) throw SolverException("The result must be positive")
        val x = Math.log(n) / Math.log(base)
        SolverOutput(
            headline = "x = ${fmt(x)}",
            headlineLabel = "Solution",
            rows = listOf("Check" to fmt(Math.pow(base, x))),
            steps = listOf("Take the logarithm of both sides", "x = ln(${fmt(n)}) ÷ ln(${fmt(base)})"),
        )
    },
)

internal fun functionOf(expression: String, angleUnit: AngleUnit): (Double) -> Double = try {
    com.shohankhan.apexcalculator.core.math.Numerical.functionOf(expression, angleUnit)
} catch (exception: MathEvalException) {
    throw SolverException(exception.error.message)
}

internal fun evaluateAt(expression: String, x: Double, angleUnit: AngleUnit): Double {
    val options = EvalOptions(angleUnit = angleUnit, variables = mapOf("x" to Num.Real(x)))
    return when (val outcome = CalculatorEngine.evaluate(expression, options)) {
        is com.shohankhan.apexcalculator.core.math.EvalOutcome.Success -> NumMath.toDouble(outcome.value)
        is com.shohankhan.apexcalculator.core.math.EvalOutcome.Failure -> throw SolverException(outcome.error.message)
    }
}

internal fun errorText(error: MathError): String = error.message

internal val PI_CONSTANT: Double = PI
