package com.shohankhan.apexcalculator.core.domain.formula

import kotlin.math.PI
import kotlin.math.abs
import kotlin.math.hypot
import kotlin.math.pow
import kotlin.math.sqrt

private fun areaOutput(label: String = "Area") = FormulaOutput(label, "square units")

internal fun geometryFormulas(): List<FormulaDefinition> = listOf(
    formula(
        id = "square",
        name = "Square",
        category = FormulaCategory.GEOMETRY,
        expression = "A = a²,  P = 4a,  d = a√2",
        description = "Area, perimeter and diagonal of a square.",
        inputs = listOf(input("a", "Side length")),
        output = areaOutput(),
        tags = listOf("area", "perimeter"),
    ) { values ->
        val side = requirePositive(values.value("a", "Side length"), "Side length")
        FormulaResult(
            side * side,
            listOf("Perimeter" to fmt(4 * side), "Diagonal" to fmt(side * sqrt(2.0))),
        )
    },
    formula(
        id = "rectangle",
        name = "Rectangle",
        category = FormulaCategory.GEOMETRY,
        expression = "A = w × h,  P = 2(w + h),  d = √(w² + h²)",
        description = "Area, perimeter and diagonal of a rectangle.",
        inputs = listOf(input("w", "Width"), input("h", "Height")),
        output = areaOutput(),
    ) { values ->
        val width = requirePositive(values.value("w", "Width"), "Width")
        val height = requirePositive(values.value("h", "Height"), "Height")
        FormulaResult(
            width * height,
            listOf(
                "Perimeter" to fmt(2 * (width + height)),
                "Diagonal" to fmt(hypot(width, height)),
            ),
        )
    },
    formula(
        id = "triangle_base_height",
        name = "Triangle (base and height)",
        category = FormulaCategory.GEOMETRY,
        expression = "A = ½ × b × h",
        description = "Area of a triangle from a base and its perpendicular height.",
        inputs = listOf(input("b", "Base"), input("h", "Height")),
        output = areaOutput(),
    ) { values ->
        val base = requirePositive(values.value("b", "Base"), "Base")
        val height = requirePositive(values.value("h", "Height"), "Height")
        FormulaResult.steps(base * height / 2, listOf("½ × ${fmt(base)} × ${fmt(height)}"))
    },
    formula(
        id = "triangle_heron",
        name = "Triangle (three sides)",
        category = FormulaCategory.GEOMETRY,
        expression = "A = √(s(s−a)(s−b)(s−c)),  s = (a+b+c) ÷ 2",
        description = "Area of a triangle from its three side lengths.",
        inputs = listOf(input("a", "Side a"), input("b", "Side b"), input("c", "Side c")),
        output = areaOutput(),
        assumptions = "The three sides must satisfy the triangle inequality.",
    ) { values ->
        val a = requirePositive(values.value("a", "Side a"), "Side a")
        val b = requirePositive(values.value("b", "Side b"), "Side b")
        val c = requirePositive(values.value("c", "Side c"), "Side c")
        if (a + b <= c || a + c <= b || b + c <= a) {
            throw FormulaException("These side lengths cannot form a triangle")
        }
        val s = (a + b + c) / 2
        FormulaResult.steps(
            sqrt(s * (s - a) * (s - b) * (s - c)),
            listOf("Semi-perimeter s = ${fmt(s)}", "Perimeter = ${fmt(a + b + c)}"),
        )
    },
    formula(
        id = "equilateral_triangle",
        name = "Equilateral triangle",
        category = FormulaCategory.GEOMETRY,
        expression = "A = (√3 ÷ 4)a²,  h = (√3 ÷ 2)a",
        description = "Area, height and perimeter of an equilateral triangle.",
        inputs = listOf(input("a", "Side length")),
        output = areaOutput(),
    ) { values ->
        val side = requirePositive(values.value("a", "Side length"), "Side length")
        FormulaResult(
            sqrt(3.0) / 4 * side * side,
            listOf(
                "Height" to fmt(sqrt(3.0) / 2 * side),
                "Perimeter" to fmt(3 * side),
            ),
        )
    },
    formula(
        id = "parallelogram",
        name = "Parallelogram",
        category = FormulaCategory.GEOMETRY,
        expression = "A = b × h",
        description = "Area of a parallelogram from base and height.",
        inputs = listOf(input("b", "Base"), input("h", "Height")),
        output = areaOutput(),
    ) { values ->
        FormulaResult(requirePositive(values.value("b", "Base"), "Base") * requirePositive(values.value("h", "Height"), "Height"))
    },
    formula(
        id = "trapezoid",
        name = "Trapezoid",
        category = FormulaCategory.GEOMETRY,
        expression = "A = ½(a + b) × h",
        description = "Area of a trapezoid with two parallel sides.",
        inputs = listOf(input("a", "Parallel side a"), input("b", "Parallel side b"), input("h", "Height")),
        output = areaOutput(),
    ) { values ->
        val a = requirePositive(values.value("a", "Parallel side a"), "Side a")
        val b = requirePositive(values.value("b", "Parallel side b"), "Side b")
        val height = requirePositive(values.value("h", "Height"), "Height")
        FormulaResult((a + b) / 2 * height)
    },
    formula(
        id = "circle",
        name = "Circle",
        category = FormulaCategory.GEOMETRY,
        expression = "A = πr²,  C = 2πr",
        description = "Area and circumference of a circle.",
        inputs = listOf(input("r", "Radius")),
        output = areaOutput(),
        tags = listOf("circle", "area", "circumference"),
    ) { values ->
        val radius = requirePositive(values.value("r", "Radius"), "Radius")
        FormulaResult(
            PI * radius * radius,
            listOf(
                "Circumference" to fmt(2 * PI * radius),
                "Diameter" to fmt(2 * radius),
            ),
        )
    },
    formula(
        id = "semicircle",
        name = "Semicircle",
        category = FormulaCategory.GEOMETRY,
        expression = "A = ½πr²,  P = πr + 2r",
        description = "Area and perimeter of a semicircle including the diameter.",
        inputs = listOf(input("r", "Radius")),
        output = areaOutput(),
    ) { values ->
        val radius = requirePositive(values.value("r", "Radius"), "Radius")
        FormulaResult(
            PI * radius * radius / 2,
            listOf("Perimeter" to fmt(PI * radius + 2 * radius), "Arc length" to fmt(PI * radius)),
        )
    },
    formula(
        id = "ellipse",
        name = "Ellipse",
        category = FormulaCategory.GEOMETRY,
        expression = "A = πab,  C ≈ π[3(a+b) − √((3a+b)(a+3b))]",
        description = "Area and approximate circumference of an ellipse.",
        inputs = listOf(input("a", "Semi-major axis a"), input("b", "Semi-minor axis b")),
        output = areaOutput(),
        assumptions = "The circumference uses Ramanujan's approximation.",
    ) { values ->
        val a = requirePositive(values.value("a", "Semi-major axis a"), "a")
        val b = requirePositive(values.value("b", "Semi-minor axis b"), "b")
        val circumference = PI * (3 * (a + b) - sqrt((3 * a + b) * (a + 3 * b)))
        FormulaResult(PI * a * b, listOf("Circumference (approx.)" to fmt(circumference)))
    },
    formula(
        id = "rhombus",
        name = "Rhombus",
        category = FormulaCategory.GEOMETRY,
        expression = "A = ½d₁d₂,  side = √((d₁/2)² + (d₂/2)²)",
        description = "Area and side length of a rhombus from its diagonals.",
        inputs = listOf(input("d1", "Diagonal 1"), input("d2", "Diagonal 2")),
        output = areaOutput(),
    ) { values ->
        val d1 = requirePositive(values.value("d1", "Diagonal 1"), "Diagonal 1")
        val d2 = requirePositive(values.value("d2", "Diagonal 2"), "Diagonal 2")
        FormulaResult(d1 * d2 / 2, listOf("Side" to fmt(hypot(d1 / 2, d2 / 2)), "Perimeter" to fmt(4 * hypot(d1 / 2, d2 / 2))))
    },
    formula(
        id = "kite",
        name = "Kite",
        category = FormulaCategory.GEOMETRY,
        expression = "A = ½d₁d₂",
        description = "Area of a kite from its diagonals.",
        inputs = listOf(input("d1", "Diagonal 1"), input("d2", "Diagonal 2")),
        output = areaOutput(),
    ) { values ->
        FormulaResult(
            requirePositive(values.value("d1", "Diagonal 1"), "Diagonal 1") *
                requirePositive(values.value("d2", "Diagonal 2"), "Diagonal 2") / 2,
        )
    },
    formula(
        id = "cube",
        name = "Cube",
        category = FormulaCategory.GEOMETRY,
        expression = "S = 6a²,  V = a³,  d = a√3",
        description = "Surface area, volume and space diagonal of a cube.",
        inputs = listOf(input("a", "Edge length")),
        output = FormulaOutput("Volume", "cubic units"),
    ) { values ->
        val side = requirePositive(values.value("a", "Edge length"), "Edge length")
        FormulaResult(
            side.pow(3),
            listOf(
                "Surface area" to fmt(6 * side * side),
                "Volume" to fmt(side.pow(3)),
                "Space diagonal" to fmt(side * sqrt(3.0)),
            ),
        )
    },
    formula(
        id = "cuboid",
        name = "Cuboid",
        category = FormulaCategory.GEOMETRY,
        expression = "S = 2(lw + lh + wh),  V = lwh",
        description = "Surface area and volume of a rectangular box.",
        inputs = listOf(input("l", "Length"), input("w", "Width"), input("h", "Height")),
        output = FormulaOutput("Volume", "cubic units"),
    ) { values ->
        val length = requirePositive(values.value("l", "Length"), "Length")
        val width = requirePositive(values.value("w", "Width"), "Width")
        val height = requirePositive(values.value("h", "Height"), "Height")
        FormulaResult(
            length * width * height,
            listOf(
                "Surface area" to fmt(2 * (length * width + length * height + width * height)),
                "Space diagonal" to fmt(sqrt(length * length + width * width + height * height)),
            ),
        )
    },
    formula(
        id = "sphere",
        name = "Sphere",
        category = FormulaCategory.GEOMETRY,
        expression = "S = 4πr²,  V = (4/3)πr³",
        description = "Surface area and volume of a sphere.",
        inputs = listOf(input("r", "Radius")),
        output = FormulaOutput("Volume", "cubic units"),
    ) { values ->
        val radius = requirePositive(values.value("r", "Radius"), "Radius")
        FormulaResult(
            4.0 / 3.0 * PI * radius.pow(3),
            listOf("Surface area" to fmt(4 * PI * radius * radius)),
        )
    },
    formula(
        id = "hemisphere",
        name = "Hemisphere",
        category = FormulaCategory.GEOMETRY,
        expression = "Curved = 2πr²,  Total = 3πr²,  V = (2/3)πr³",
        description = "Surface areas and volume of a solid hemisphere.",
        inputs = listOf(input("r", "Radius")),
        output = FormulaOutput("Volume", "cubic units"),
    ) { values ->
        val radius = requirePositive(values.value("r", "Radius"), "Radius")
        FormulaResult(
            2.0 / 3.0 * PI * radius.pow(3),
            listOf(
                "Curved surface area" to fmt(2 * PI * radius * radius),
                "Total surface area" to fmt(3 * PI * radius * radius),
            ),
        )
    },
    formula(
        id = "cylinder",
        name = "Cylinder",
        category = FormulaCategory.GEOMETRY,
        expression = "Lateral = 2πrh,  Total = 2πr(r+h),  V = πr²h",
        description = "Lateral area, total surface area and volume of a cylinder.",
        inputs = listOf(input("r", "Radius"), input("h", "Height")),
        output = FormulaOutput("Volume", "cubic units"),
    ) { values ->
        val radius = requirePositive(values.value("r", "Radius"), "Radius")
        val height = requirePositive(values.value("h", "Height"), "Height")
        FormulaResult(
            PI * radius * radius * height,
            listOf(
                "Lateral surface" to fmt(2 * PI * radius * height),
                "Total surface" to fmt(2 * PI * radius * (radius + height)),
            ),
        )
    },
    formula(
        id = "cone",
        name = "Cone",
        category = FormulaCategory.GEOMETRY,
        expression = "l = √(r²+h²),  Lateral = πrl,  V = πr²h ÷ 3",
        description = "Slant height, surface areas and volume of a right circular cone.",
        inputs = listOf(input("r", "Radius"), input("h", "Height")),
        output = FormulaOutput("Volume", "cubic units"),
    ) { values ->
        val radius = requirePositive(values.value("r", "Radius"), "Radius")
        val height = requirePositive(values.value("h", "Height"), "Height")
        val slant = hypot(radius, height)
        FormulaResult(
            PI * radius * radius * height / 3,
            listOf(
                "Slant height" to fmt(slant),
                "Lateral surface" to fmt(PI * radius * slant),
                "Total surface" to fmt(PI * radius * (slant + radius)),
            ),
        )
    },
    formula(
        id = "frustum",
        name = "Frustum of a cone",
        category = FormulaCategory.GEOMETRY,
        expression = "l = √((R−r)² + h²),  V = πh(R² + Rr + r²) ÷ 3",
        description = "Slant height, surface areas and volume of a conical frustum.",
        inputs = listOf(input("R", "Bottom radius"), input("r", "Top radius"), input("h", "Height")),
        output = FormulaOutput("Volume", "cubic units"),
        assumptions = "The top radius must not exceed the bottom radius.",
    ) { values ->
        val big = requirePositive(values.value("R", "Bottom radius"), "Bottom radius")
        val small = requireNonNegative(values.value("r", "Top radius"), "Top radius")
        val height = requirePositive(values.value("h", "Height"), "Height")
        if (small > big) throw FormulaException("The top radius cannot exceed the bottom radius")
        val slant = hypot(big - small, height)
        val volume = PI * height * (big * big + big * small + small * small) / 3
        val lateral = PI * (big + small) * slant
        val total = lateral + PI * big * big + PI * small * small
        FormulaResult(
            volume,
            listOf(
                "Slant height" to fmt(slant),
                "Lateral surface" to fmt(lateral),
                "Total surface" to fmt(total),
            ),
        )
    },
    formula(
        id = "prism",
        name = "Prism",
        category = FormulaCategory.GEOMETRY,
        expression = "V = base area × h,  Lateral = perimeter × h",
        description = "Volume and lateral surface area of a right prism.",
        inputs = listOf(
            input("area", "Base area"),
            input("perimeter", "Base perimeter"),
            input("h", "Height"),
        ),
        output = FormulaOutput("Volume", "cubic units"),
    ) { values ->
        val baseArea = requirePositive(values.value("area", "Base area"), "Base area")
        val perimeter = requirePositive(values.value("perimeter", "Base perimeter"), "Base perimeter")
        val height = requirePositive(values.value("h", "Height"), "Height")
        FormulaResult(
            baseArea * height,
            listOf(
                "Lateral surface" to fmt(perimeter * height),
                "Total surface" to fmt(perimeter * height + 2 * baseArea),
            ),
        )
    },
    formula(
        id = "pyramid",
        name = "Pyramid",
        category = FormulaCategory.GEOMETRY,
        expression = "V = (1/3) × base area × h",
        description = "Volume of a pyramid or cone-like solid with a straight-sided base.",
        inputs = listOf(input("area", "Base area"), input("h", "Height")),
        output = FormulaOutput("Volume", "cubic units"),
    ) { values ->
        FormulaResult(
            requirePositive(values.value("area", "Base area"), "Base area") *
                requirePositive(values.value("h", "Height"), "Height") / 3,
        )
    },
    formula(
        id = "right_triangle",
        name = "Right triangle hypotenuse",
        category = FormulaCategory.GEOMETRY,
        expression = "c = √(a² + b²)",
        description = "Pythagorean theorem for the hypotenuse.",
        inputs = listOf(input("a", "Leg a"), input("b", "Leg b")),
        output = FormulaOutput("Hypotenuse"),
    ) { values ->
        val a = requirePositive(values.value("a", "Leg a"), "Leg a")
        val b = requirePositive(values.value("b", "Leg b"), "Leg b")
        FormulaResult.steps(hypot(a, b), listOf("√(${fmt(a)}² + ${fmt(b)}²)"))
    },
    formula(
        id = "missing_dimension",
        name = "Missing rectangle side",
        category = FormulaCategory.GEOMETRY,
        expression = "w = A ÷ h",
        description = "Finds the missing side of a rectangle from its area.",
        inputs = listOf(input("area", "Area"), input("known", "Known side")),
        output = FormulaOutput("Missing side"),
    ) { values ->
        val area = requirePositive(values.value("area", "Area"), "Area")
        val known = requirePositive(values.value("known", "Known side"), "Known side")
        FormulaResult(area / known)
    },
    formula(
        id = "sector",
        name = "Circle sector",
        category = FormulaCategory.GEOMETRY,
        expression = "A = (θ ÷ 360)πr²,  arc = (θ ÷ 360)2πr",
        description = "Area and arc length of a circular sector.",
        inputs = listOf(input("r", "Radius"), input("angle", "Central angle", "°")),
        output = areaOutput(),
    ) { values ->
        val radius = requirePositive(values.value("r", "Radius"), "Radius")
        val angle = values.value("angle", "Central angle")
        if (angle <= 0 || angle > 360) throw FormulaException("The central angle must be between 0 and 360 degrees")
        FormulaResult(
            angle / 360 * PI * radius * radius,
            listOf(
                "Arc length" to fmt(angle / 360 * 2 * PI * radius),
                "Chord" to fmt(2 * radius * kotlin.math.sin(Math.toRadians(angle / 2))),
            ),
        )
    },
    formula(
        id = "similar_triangles",
        name = "Similar triangles scale",
        category = FormulaCategory.GEOMETRY,
        expression = "x = (a ÷ b) × c",
        description = "Finds the unknown side of similar triangles.",
        inputs = listOf(input("a", "Corresponding side a"), input("b", "Corresponding side b"), input("c", "Known side c")),
        output = FormulaOutput("Unknown side"),
    ) { values ->
        val a = requirePositive(values.value("a", "Corresponding side a"), "Side a")
        val b = requirePositive(values.value("b", "Corresponding side b"), "Side b")
        val c = values.value("c", "Known side c")
        FormulaResult(a / b * c, listOf("Scale factor" to fmt(a / b)))
    },
)

internal fun algebraFormulas(): List<FormulaDefinition> = listOf(
    formula(
        id = "linear_equation",
        name = "Linear equation ax + b = c",
        category = FormulaCategory.ALGEBRA,
        expression = "x = (c − b) ÷ a",
        description = "Solves a one-variable linear equation.",
        inputs = listOf(input("a", "Coefficient a"), input("b", "Constant b"), input("c", "Right-hand side c")),
        output = FormulaOutput("x"),
    ) { values ->
        val a = values.value("a", "Coefficient a")
        if (a == 0.0) throw FormulaException("If a is zero the equation is not linear and may have no unique solution")
        val b = values.value("b", "Constant b")
        val c = values.value("c", "Right-hand side c")
        FormulaResult.steps((c - b) / a, listOf("${fmt(a)}x + ${fmt(b)} = ${fmt(c)}", "x = (${fmt(c)} − ${fmt(b)}) ÷ ${fmt(a)}"))
    },
    formula(
        id = "discriminant",
        name = "Discriminant",
        category = FormulaCategory.ALGEBRA,
        expression = "Δ = b² − 4ac",
        description = "Determines the nature of the roots of a quadratic.",
        inputs = listOf(input("a", "a"), input("b", "b"), input("c", "c")),
        output = FormulaOutput("Discriminant"),
    ) { values ->
        val a = values.value("a", "a")
        val b = values.value("b", "b")
        val c = values.value("c", "c")
        val discriminant = b * b - 4 * a * c
        val nature = when {
            a == 0.0 -> "not a quadratic"
            discriminant > 0 -> "two distinct real roots"
            discriminant == 0.0 -> "one repeated real root"
            else -> "two complex conjugate roots"
        }
        FormulaResult(discriminant, listOf("Nature" to nature))
    },
    formula(
        id = "quadratic_roots",
        name = "Quadratic roots",
        category = FormulaCategory.ALGEBRA,
        expression = "x = (−b ± √(b² − 4ac)) ÷ 2a",
        description = "Real and complex roots of ax² + bx + c = 0.",
        inputs = listOf(input("a", "a"), input("b", "b"), input("c", "c")),
        output = FormulaOutput("Roots"),
    ) { values ->
        val a = values.value("a", "a")
        val b = values.value("b", "b")
        val c = values.value("c", "c")
        if (a == 0.0) throw FormulaException("a cannot be zero in a quadratic equation")
        val discriminant = b * b - 4 * a * c
        if (discriminant >= 0) {
            val root = sqrt(discriminant)
            val first = (-b + root) / (2 * a)
            val second = (-b - root) / (2 * a)
            FormulaResult(
                first,
                listOf("Second root" to fmt(second), "Discriminant" to fmt(discriminant)),
                listOf("Δ = ${fmt(discriminant)}", "x = (${fmt(-b)} ± ${fmt(root)}) ÷ ${fmt(2 * a)}"),
            )
        } else {
            val real = -b / (2 * a)
            val imaginary = sqrt(-discriminant) / (2 * a)
            val text = "x = ${fmt(real)} ± ${fmt(abs(imaginary))}i"
            FormulaResult(
                real,
                listOf(
                    "First root" to "${fmt(real)} + ${fmt(abs(imaginary))}i",
                    "Second root" to "${fmt(real)} − ${fmt(abs(imaginary))}i",
                    "Discriminant" to fmt(discriminant),
                ),
                listOf("Δ is negative, so the roots are complex conjugates"),
                primaryText = text,
            )
        }
    },
    formula(
        id = "arithmetic_term",
        name = "Arithmetic progression term",
        category = FormulaCategory.ALGEBRA,
        expression = "aₙ = a₁ + (n − 1)d",
        description = "n-th term of an arithmetic sequence.",
        inputs = listOf(
            input("a1", "First term"),
            input("d", "Common difference"),
            input("n", "Term number", kind = InputKind.INTEGER),
        ),
        output = FormulaOutput("n-th term"),
    ) { values ->
        val first = values.value("a1", "First term")
        val difference = values.value("d", "Common difference")
        val n = requireInteger(values.value("n", "Term number"), "Term number")
        if (n < 1) throw FormulaException("The term number must be at least 1")
        FormulaResult(first + (n - 1) * difference, steps = listOf("aₙ = ${fmt(first)} + ($n − 1) × ${fmt(difference)}"))
    },
    formula(
        id = "arithmetic_sum",
        name = "Arithmetic series sum",
        category = FormulaCategory.ALGEBRA,
        expression = "Sₙ = n ÷ 2 × (2a₁ + (n − 1)d)",
        description = "Sum of the first n terms of an arithmetic sequence.",
        inputs = listOf(
            input("a1", "First term"),
            input("d", "Common difference"),
            input("n", "Number of terms", kind = InputKind.INTEGER),
        ),
        output = FormulaOutput("Sum"),
    ) { values ->
        val first = values.value("a1", "First term")
        val difference = values.value("d", "Common difference")
        val n = requireInteger(values.value("n", "Number of terms"), "Number of terms")
        if (n < 1) throw FormulaException("The number of terms must be at least 1")
        FormulaResult(n / 2.0 * (2 * first + (n - 1) * difference))
    },
    formula(
        id = "geometric_term",
        name = "Geometric progression term",
        category = FormulaCategory.ALGEBRA,
        expression = "aₙ = a₁ × r^(n−1)",
        description = "n-th term of a geometric sequence.",
        inputs = listOf(
            input("a1", "First term"),
            input("r", "Common ratio"),
            input("n", "Term number", kind = InputKind.INTEGER),
        ),
        output = FormulaOutput("n-th term"),
    ) { values ->
        val first = values.value("a1", "First term")
        val ratio = values.value("r", "Common ratio")
        val n = requireInteger(values.value("n", "Term number"), "Term number")
        if (n < 1) throw FormulaException("The term number must be at least 1")
        val result = first * ratio.pow((n - 1).toDouble())
        if (!result.isFinite()) throw FormulaException("The term is too large to represent")
        FormulaResult(result)
    },
    formula(
        id = "geometric_sum",
        name = "Geometric series sum",
        category = FormulaCategory.ALGEBRA,
        expression = "Sₙ = a₁(rⁿ − 1) ÷ (r − 1)",
        description = "Sum of the first n terms of a geometric sequence.",
        inputs = listOf(
            input("a1", "First term"),
            input("r", "Common ratio"),
            input("n", "Number of terms", kind = InputKind.INTEGER),
        ),
        output = FormulaOutput("Sum"),
    ) { values ->
        val first = values.value("a1", "First term")
        val ratio = values.value("r", "Common ratio")
        val n = requireInteger(values.value("n", "Number of terms"), "Number of terms")
        if (n < 1) throw FormulaException("The number of terms must be at least 1")
        val sum = if (ratio == 1.0) first * n else first * (ratio.pow(n.toDouble()) - 1) / (ratio - 1)
        FormulaResult(sum)
    },
    formula(
        id = "infinite_geometric_sum",
        name = "Infinite geometric series",
        category = FormulaCategory.ALGEBRA,
        expression = "S = a₁ ÷ (1 − r),  |r| < 1",
        description = "Sum of an infinite geometric series when it converges.",
        inputs = listOf(input("a1", "First term"), input("r", "Common ratio")),
        output = FormulaOutput("Sum"),
        assumptions = "Converges only when the absolute value of the ratio is less than 1.",
    ) { values ->
        val first = values.value("a1", "First term")
        val ratio = values.value("r", "Common ratio")
        if (abs(ratio) >= 1) throw FormulaException("The series does not converge because |r| ≥ 1")
        FormulaResult(first / (1 - ratio))
    },
    formula(
        id = "polynomial_value",
        name = "Polynomial evaluation",
        category = FormulaCategory.ALGEBRA,
        expression = "P(x) = ax³ + bx² + cx + d",
        description = "Evaluates a cubic polynomial at a point.",
        inputs = listOf(
            input("a", "Coefficient a"),
            input("b", "Coefficient b"),
            input("c", "Coefficient c"),
            input("d", "Constant d"),
            input("x", "x"),
        ),
        output = FormulaOutput("P(x)"),
    ) { values ->
        val x = values.value("x", "x")
        val result = values.value("a", "Coefficient a") * x.pow(3) +
            values.value("b", "Coefficient b") * x.pow(2) +
            values.value("c", "Coefficient c") * x +
            values.value("d", "Constant d")
        FormulaResult(result)
    },
)
